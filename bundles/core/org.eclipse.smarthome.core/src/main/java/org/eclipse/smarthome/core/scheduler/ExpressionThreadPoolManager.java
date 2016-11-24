/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private final static Logger logger = LoggerFactory.getLogger(ExpressionThreadPoolManager.class);

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Returns an instance of an expression-driven scheduled thread pool service. If it is the first request for the
     * given pool name, the instance is newly created.
     *
     * @param poolName a short name used to identify the pool, e.g. "discovery"
     * @return an instance to use
     */
    static public ExpressionThreadPoolExecutor getExpressionScheduledPool(String poolName) {
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
                    logger.debug("Created an expression-drive scheduled thread pool '{}' of size {}",
                            new Object[] { poolName, cfg });
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

        private Map<Expression, Runnable> scheduled = new ConcurrentHashMap<>();
        private Map<Runnable, ArrayList<Future<?>>> futures = Collections
                .synchronizedMap(new HashMap<Runnable, ArrayList<Future<?>>>());
        private Map<Future<?>, Date> timestamps = Collections.synchronizedMap(new HashMap<Future<?>, Date>());
        private Thread monitor;
        private NamedThreadFactory monitorThreadFactory;

        public ExpressionThreadPoolExecutor(final String poolName, int corePoolSize) {
            this(poolName, corePoolSize, new NamedThreadFactory(poolName), new ThreadPoolExecutor.DiscardPolicy() {
                // The pool is bounded and rejections will happen during shutdown
                @Override
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                    // Log and discard
                    logger.warn("Thread pool '{}' rejected execution of {}",
                            new Object[] { poolName, runnable.getClass() });
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
            logger.trace("Cleaning up after the execution of '{}'", runnable.toString());
            super.afterExecute(runnable, throwable);

            if (runnable instanceof Future) {
                for (Runnable aRunnable : futures.keySet()) {
                    Future<?> toDelete = null;
                    synchronized (futures) {
                        for (Future<?> future : futures.get(aRunnable)) {
                            if (future == runnable) {
                                toDelete = future;
                                break;
                            }
                        }
                        if (toDelete != null) {
                            logger.trace("Removing Future '{}' (out of {}) for Runnable '{}'", new Object[] {
                                    toDelete.toString(), futures.get(aRunnable).size(), aRunnable.toString() });
                            futures.get(aRunnable).remove(toDelete);
                        }
                    }
                }

                timestamps.remove(runnable);

            } else {
                ArrayList<Future<?>> obsoleteFutures = new ArrayList<Future<?>>();
                synchronized (futures) {
                    ArrayList<Future<?>> taskFutures = futures.get(runnable);

                    if (taskFutures != null) {
                        logger.trace("Runnable '{}' has {} Futures scheduled", taskFutures.size());

                        for (Future<?> future : taskFutures) {
                            if (future.isDone()) {
                                obsoleteFutures.add(future);
                            }
                        }

                        logger.trace("Runnable '{}' has {} Futures that will be removed", obsoleteFutures.size());
                        for (Future<?> future : obsoleteFutures) {
                            taskFutures.remove(future);
                            timestamps.remove(future);
                        }
                    } else {
                        logger.trace("Runnable '{}' has no Futures scheduled");
                    }
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
                logger.trace("Starting the monitor thread '{}'", Thread.currentThread().getName());
                while (true) {
                    try {
                        Date firstExecution = null;
                        Date now = new Date();

                        List<Expression> finishedExpressions = new ArrayList<Expression>();

                        logger.trace("There are {} scheduled expressions", scheduled.keySet().size());

                        for (Expression e : scheduled.keySet()) {
                            Date time = e.getTimeAfter(now);

                            if (time != null) {
                                logger.trace("Expression's '{}' next execution time is {}", e.toString(),
                                        sdf.format(time));

                                Runnable task = scheduled.get(e);

                                if (task != null) {
                                    synchronized (futures) {
                                        ArrayList<Future<?>> taskFutures = futures.get(task);

                                        if (taskFutures == null) {
                                            taskFutures = new ArrayList<Future<?>>();
                                            futures.put(task, taskFutures);
                                        }

                                        boolean schedule = false;

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
                                                        task.toString(), time.getTime() - now.getTime());
                                            }
                                        }

                                        if (schedule) {
                                            logger.trace("Scheduling the task '{}' to execute in {} ms",
                                                    task.toString(), time.getTime() - now.getTime());
                                            Future<?> newFuture = ExpressionThreadPoolExecutor.this.schedule(task,
                                                    time.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
                                            taskFutures.add(newFuture);
                                            logger.trace("Task '{}' has now {} Futures", task.toString(),
                                                    taskFutures.size());
                                            timestamps.put(newFuture, time);
                                        }
                                    }
                                } else {
                                    logger.trace("Expressions without tasks are not valid");
                                }

                                if (firstExecution == null) {
                                    firstExecution = time;
                                } else {
                                    if (time.before(firstExecution)) {
                                        firstExecution = time;
                                    }
                                }

                            } else {
                                logger.info("Expression '{}' has no future executions anymore", e.toString());
                                finishedExpressions.add(e);
                            }
                        }

                        for (Expression e : finishedExpressions) {
                            scheduled.remove(e);
                        }

                        if (firstExecution != null) {
                            while (now.before(firstExecution)) {
                                logger.trace("Putting the monitor thread '{}' to sleep for {} ms",
                                        Thread.currentThread().getName(), firstExecution.getTime() - now.getTime());
                                Thread.sleep(firstExecution.getTime() - now.getTime());
                                now = new Date();
                            }

                        } else {
                            logger.trace("Putting the monitor thread '{}' to sleep for {} ms",
                                    Thread.currentThread().getName(), THREAD_MONITOR_SLEEP);
                            Thread.sleep(THREAD_MONITOR_SLEEP);
                        }
                    } catch (RejectedExecutionException ex) {
                        logger.error("The executor has already shutdown : '{}'", ex.getMessage());
                    } catch (CancellationException ex) {
                        logger.error("Non executed tasks are cancelled : '{}'", ex.getMessage());
                    } catch (InterruptedException ex) {
                        logger.trace("The monitor thread as interrupted : '{}'", ex.getMessage());
                    }
                }
            }
        };

        public void schedule(final Runnable task, final Expression expression) {
            if (task == null || expression == null) {
                throw new NullPointerException();
            }

            if (monitor == null) {
                monitor = monitorThreadFactory.newThread(monitorTask);
                monitor.start();
            }

            scheduled.put(expression, task);
            logger.trace("Scheduled task '{}' using expression '{}'", task.toString(), expression.toString());
            monitor.interrupt();
        }

        public boolean remove(Expression expression) {
            logger.trace("Removing the expression '{}' from the scheduler", expression.toString());
            Runnable task = scheduled.remove(expression);

            if (task != null) {
                return removeFutures(task);
            } else {
                return false;
            }
        }

        @Override
        public boolean remove(Runnable task) {
            Expression theExpression = null;
            for (Expression anExpression : scheduled.keySet()) {
                if (task.equals(scheduled.get(anExpression))) {
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
            logger.trace("Removing Runnable '{}' from the scheduler", task.toString());

            ArrayList<Future<?>> obsoleteFutures = new ArrayList<Future<?>>();
            synchronized (futures) {
                ArrayList<Future<?>> taskFutures = futures.get(task);
                if (taskFutures.size() != 0) {
                    logger.trace("Runnable '{}' has {} Futures to be removed", task.toString(), taskFutures.size());
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
                } else {
                    return false;
                }
            }
        }

    }
}
