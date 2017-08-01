/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extended version of {@link ThreadPoolManager}, which can also handle expressions for scheduling tasks.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public class ExpressionThreadPoolManager extends ThreadPoolManager {

    /**
     * Returns an instance of an expression-driven scheduled thread pool service. If it is the first request for the
     * given pool name, the instance is newly created.
     *
     * @param poolName a short name used to identify the pool, e.g. "discovery"
     * @return an instance to use
     */
    public static ExpressionThreadPoolExecutor getExpressionScheduledPool(String poolName) {
        ExecutorService pool = pools.get(poolName);
        if (pool == null) {
            synchronized (pools) {
                // do a double check if it is still null or if another thread might have created it meanwhile
                pool = pools.get(poolName);
                if (pool == null) {
                    Integer cfg = getConfig(poolName);
                    pool = new ExpressionThreadPoolExecutor(poolName, cfg);
                    ((ThreadPoolExecutor) pool).setKeepAliveTime(THREAD_TIMEOUT, TimeUnit.SECONDS);
                    ((ThreadPoolExecutor) pool).allowCoreThreadTimeOut(true);
                    pools.put(poolName, pool);
                    LoggerFactory.getLogger(ExpressionThreadPoolManager.class)
                            .debug("Created an expression-drive scheduled thread pool '{}' of size {}", poolName, cfg);
                }
            }
        }
        if (pool instanceof ExpressionThreadPoolExecutor) {
            return (ExpressionThreadPoolExecutor) pool;
        } else {
            throw new IllegalArgumentException("Pool " + poolName + " is not an expression-driven scheduled pool!");
        }
    }

    public static class ExpressionThreadPoolExecutor extends ScheduledThreadPoolExecutor {

        private final Logger logger = LoggerFactory.getLogger(ExpressionThreadPoolExecutor.class);

        private Map<Expression, RunnableWrapper> scheduled = new ConcurrentHashMap<>();
        private Map<RunnableWrapper, List<ScheduledFuture<?>>> futures = Collections.synchronizedMap(new HashMap<>());
        private final Lock futuresLock = new ReentrantLock();
        private final Map<Future<?>, Date> timestamps = Collections.synchronizedMap(new HashMap<Future<?>, Date>());
        private volatile Thread monitor;
        private NamedThreadFactory monitorThreadFactory;
        private final Lock monitoringLock = new ReentrantLock();
        private final Condition newExpressionCondition = monitoringLock.newCondition();

        public ExpressionThreadPoolExecutor(final String poolName, int corePoolSize) {
            this(poolName, corePoolSize, new NamedThreadFactory(poolName), new ThreadPoolExecutor.DiscardPolicy() {

                private final Logger logger = LoggerFactory.getLogger(ThreadPoolExecutor.DiscardPolicy.class);

                // The pool is bounded and rejections will happen during shutdown
                @Override
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                    // Log and discard
                    logger.debug("Thread pool '{}' rejected execution of {}", poolName, runnable.getClass());
                    super.rejectedExecution(runnable, threadPoolExecutor);
                }
            });
        }

        public ExpressionThreadPoolExecutor(String threadPool, int corePoolSize, NamedThreadFactory threadFactory,
                RejectedExecutionHandler rejectedHandler) {
            super(corePoolSize, threadFactory, rejectedHandler);
            this.monitorThreadFactory = new NamedThreadFactory(threadFactory.getName() + "-" + "Monitor");
        }

        @Override
        protected void afterExecute(Runnable runnable, Throwable throwable) {
            logger.trace("Cleaning up after the execution of '{}'", runnable);
            super.afterExecute(runnable, throwable);

            if (runnable instanceof Future) {
                Future<?> future = (Future<?>) runnable;
                try {
                    futuresLock.lock();
                    for (Runnable aRunnable : futures.keySet()) {
                        futures.get(aRunnable).removeIf(entry -> entry == future);
                    }
                } finally {
                    futuresLock.unlock();
                }
                timestamps.remove(future);
            } else {
                List<ScheduledFuture<?>> obsoleteFutures = new ArrayList<ScheduledFuture<?>>();
                try {
                    futuresLock.lock();
                    List<ScheduledFuture<?>> taskFutures = futures.get(runnable);

                    if (taskFutures != null) {
                        logger.trace("Runnable '{}' has {} Futures scheduled", runnable, taskFutures.size());

                        for (ScheduledFuture<?> future : taskFutures) {
                            if (future.isDone()) {
                                obsoleteFutures.add(future);
                            }
                        }

                        logger.trace("Runnable '{}' has {} Futures that will be removed", runnable,
                                obsoleteFutures.size());
                        for (Future<?> future : obsoleteFutures) {
                            taskFutures.remove(future);
                            timestamps.remove(future);
                        }
                    } else {
                        logger.debug("Runnable '{}' has no Futures scheduled", runnable);
                    }
                } finally {
                    futuresLock.unlock();
                }
            }

            if (throwable != null) {
                Throwable cause = throwable.getCause();
                if (cause instanceof InterruptedException) {
                    // Ignore this, might happen when we shutdownNow() the executor. We can't
                    // log at this point as the logging system might be stopped already.
                    return;
                }
            }
        }

        Runnable monitorTask = new Runnable() {

            @Override
            public void run() {
                logger.debug("Starting the monitor thread '{}'", Thread.currentThread().getName());
                while (true) {
                    try {
                        Date earliestExecution = null;
                        final Date now = new Date();

                        List<Expression> finishedExpressions = new ArrayList<Expression>();

                        if (logger.isTraceEnabled()) {
                            logger.trace("There are {} scheduled expressions", scheduled.keySet().size());
                            for (Entry<Expression, RunnableWrapper> entry : scheduled.entrySet()) {
                                logger.trace("  Runnable {} with {}", entry.getValue(), entry.getValue());
                            }
                        }
                        for (Expression e : scheduled.keySet()) {
                            Date time = e.getTimeAfter(now);

                            if (time != null) {
                                logger.trace("Expression's '{}' next execution time is {}", e, time);

                                final RunnableWrapper task = scheduled.get(e);

                                if (task != null) {
                                    try {
                                        futuresLock.lock();
                                        List<ScheduledFuture<?>> taskFutures = futures.get(task);

                                        if (taskFutures == null) {
                                            taskFutures = new ArrayList<ScheduledFuture<?>>();
                                            futures.put(task, taskFutures);
                                        }

                                        boolean schedule = false;

                                        long delay = time.getTime() - now.getTime();
                                        if (taskFutures.size() == 0) {
                                            // if no futures are currently scheduled, we definitely have to schedule the
                                            // task
                                            schedule = true;
                                        } else {
                                            // check the time stamp of the last scheduled task if an additional task
                                            // needs
                                            // to be scheduled
                                            Date timestamp = timestamps.get(taskFutures.get(taskFutures.size() - 1));

                                            if (time.after(timestamp)) {
                                                schedule = true;
                                            } else {
                                                logger.trace("The task '{}' is already scheduled to execute in {} ms",
                                                        task, delay);
                                            }
                                        }

                                        if (schedule) {
                                            logger.debug("Scheduling the task '{}' to execute in {} ms", task, delay);
                                            ScheduledFuture<?> newFuture = ExpressionThreadPoolExecutor.this
                                                    .schedule(task, delay, TimeUnit.MILLISECONDS);
                                            taskFutures.add(newFuture);
                                            logger.trace("Task '{}' has now {} Futures", task, taskFutures.size());
                                            timestamps.put(newFuture, time);
                                        }
                                        if (logger.isTraceEnabled()) {
                                            for (ScheduledFuture<?> future : taskFutures) {
                                                logger.trace("Task {} ({}) will run in {}", task,
                                                        System.identityHashCode(task),
                                                        future.getDelay(TimeUnit.MILLISECONDS));
                                            }
                                        }
                                    } finally {
                                        futuresLock.unlock();
                                    }
                                } else {
                                    logger.trace("Expressions without tasks are not valid");
                                }

                                if (earliestExecution == null) {
                                    earliestExecution = time;
                                } else {
                                    if (time.before(earliestExecution)) {
                                        earliestExecution = time;
                                    }
                                }

                            } else {
                                logger.debug("Expression '{}' has no future executions anymore", e);
                                finishedExpressions.add(e);
                            }
                        }

                        for (Expression e : finishedExpressions) {
                            scheduled.remove(e);
                            logger.trace("Cleaning up finished expression '{}'", e);
                        }

                        if (earliestExecution != null) {
                            boolean newExpression = false;
                            while (!newExpression && new Date().before(earliestExecution)) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("Putting the monitor thread '{}' to sleep for {} ms",
                                            Thread.currentThread().getName(),
                                            earliestExecution.getTime() - new Date().getTime());
                                }
                                try {
                                    monitoringLock.lock();
                                    newExpression = newExpressionCondition.await(
                                            earliestExecution.getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);
                                    logger.trace("Monitor thread woke again with {}", newExpression);
                                } finally {
                                    monitoringLock.unlock();
                                }
                            }

                        } else {
                            logger.trace("Putting the monitor thread '{}' to sleep for {} ms",
                                    Thread.currentThread().getName(), THREAD_MONITOR_SLEEP);
                            try {
                                monitoringLock.lock();
                                newExpressionCondition.await(THREAD_MONITOR_SLEEP, TimeUnit.MILLISECONDS);
                            } finally {
                                monitoringLock.unlock();
                            }
                        }
                    } catch (RejectedExecutionException ex) {
                        logger.error("The executor has already been shut down : '{}'", ex.getMessage());
                    } catch (CancellationException ex) {
                        logger.error("Non executed tasks are cancelled : '{}'", ex.getMessage());
                    } catch (InterruptedException ex) {
                        logger.trace("The monitor thread was interrupted : '{}'", ex.getMessage());
                    }
                }
            }
        };

        public void schedule(final Runnable task, final Expression expression) {
            if (task == null || expression == null) {
                throw new IllegalArgumentException("Task cannot be scheduled as task or expression is null.");
            }
            RunnableWrapper wrapper = new RunnableWrapper(task);
            synchronized (this) {
                if (monitor == null) {
                    monitor = monitorThreadFactory.newThread(monitorTask);
                    monitor.start();
                }
            }
            if (logger.isDebugEnabled()) {
                if (scheduled.containsValue(wrapper)) {
                    logger.debug("Task {} is already scheduled (potentially with a different expression).", wrapper);
                }
            }
            scheduled.put(expression, wrapper);
            logger.debug("Scheduled task '{}' using expression '{}'", wrapper, expression);
            try {
                monitoringLock.lock();
                newExpressionCondition.signalAll();
            } finally {
                monitoringLock.unlock();
            }
        }

        public boolean remove(Expression expression) {
            logger.debug("Removing the expression '{}' from the scheduler", expression);
            RunnableWrapper task = scheduled.remove(expression);

            if (task != null) {
                return doRemoveFutures(task);
            } else {
                return false;
            }
        }

        @Override
        public boolean remove(Runnable task) {
            RunnableWrapper wrapper = new RunnableWrapper(task);
            Expression theExpression = null;
            for (Expression anExpression : scheduled.keySet()) {
                if (wrapper.equals(scheduled.get(anExpression))) {
                    theExpression = anExpression;
                    break;
                }
            }

            if (theExpression != null) {
                return remove(theExpression);
            } else {
                return super.remove(task);
            }

        }

        public boolean removeFutures(Runnable task) {
            RunnableWrapper wrapper = new RunnableWrapper(task);
            return doRemoveFutures(wrapper);
        }

        private boolean doRemoveFutures(RunnableWrapper task) {
            logger.trace("Removing Runnable '{}' from the scheduler", task);

            List<Future<?>> obsoleteFutures = new ArrayList<Future<?>>();
            try {
                futuresLock.lock();
                List<ScheduledFuture<?>> taskFutures = futures.get(task);
                if (taskFutures != null) {
                    if (taskFutures.size() != 0) {
                        logger.trace("Runnable '{}' has {} Futures to be removed", task, taskFutures.size());
                        for (Future<?> future : taskFutures) {
                            future.cancel(false);
                            timestamps.remove(future);
                            obsoleteFutures.add(future);
                        }
                    }

                    for (Future<?> future : obsoleteFutures) {
                        taskFutures.remove(future);
                    }

                    super.purge();

                    if (taskFutures.size() == 0) {
                        futures.remove(task);
                        return true;
                    }
                }
                return false;
            } finally {
                futuresLock.unlock();
            }
        }
    }
}
