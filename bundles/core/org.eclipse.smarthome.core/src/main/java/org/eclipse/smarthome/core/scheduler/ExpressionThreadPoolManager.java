/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import java.util.concurrent.CancellationException;
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
                    int[] cfg = getConfig(poolName);
                    pool = new ExpressionThreadPoolExecutor(poolName, cfg[0]);
                    ((ThreadPoolExecutor) pool).setKeepAliveTime(THREAD_TIMEOUT, TimeUnit.SECONDS);
                    ((ThreadPoolExecutor) pool).allowCoreThreadTimeOut(true);
                    pools.put(poolName, pool);
                    logger.debug("Created an expression-drive scheduled thread pool '{}' of size {}",
                            new Object[] { poolName, cfg[0] });
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

        private List<Runnable> running = Collections.synchronizedList(new ArrayList<Runnable>());
        private Map<Expression, Runnable> scheduled = Collections.synchronizedMap(new HashMap<Expression, Runnable>());
        private Map<Runnable, Future<?>> futures = Collections.synchronizedMap(new HashMap<Runnable, Future<?>>());
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
        protected void beforeExecute(Thread thread, Runnable runnable) {
            super.beforeExecute(thread, runnable);
            running.add(runnable);
        }

        @Override
        protected void afterExecute(Runnable runnable, Throwable throwable) {
            super.afterExecute(runnable, throwable);
            running.remove(runnable);
            Future<?> future = futures.remove(runnable);
            timestamps.remove(future);
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

                        for (Expression e : scheduled.keySet()) {
                            Date time = e.getTimeAfter(now);

                            if (time != null) {
                                logger.trace("Expression's '{}' next execution time is {}", e.toString(),
                                        time.toString());

                                Runnable task = scheduled.get(e);

                                if (task != null) {
                                    Future<?> future = futures.get(task);

                                    boolean schedule = false;

                                    if (future == null) {
                                        schedule = true;
                                    } else {
                                        Date timestamp = timestamps.get(future);
                                        if (time.after(timestamp)) {
                                            schedule = true;
                                        } else {
                                            logger.trace("The task '{}' is already scheduled to execute in {} ms",
                                                    task.toString(), time.getTime() - now.getTime());
                                        }
                                    }

                                    if (schedule) {
                                        logger.trace("Scheduling the task '{}' to execute in {} ms", task.toString(),
                                                time.getTime() - now.getTime());
                                        futures.put(task, ExpressionThreadPoolExecutor.this.schedule(task,
                                                time.getTime() - now.getTime(), TimeUnit.MILLISECONDS));
                                        timestamps.put(futures.get(task), time);
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
            monitor.interrupt();
        }

        @Override
        public boolean remove(Runnable task) {
            if (futures.get(task) != null && futures.get(task).cancel(false)) {
                running.remove(task);
            }
            futures.remove(task);
            return super.remove(task);
        }

        public boolean remove(Expression expression) {
            Runnable task = scheduled.remove(expression);

            if (task != null) {
                return remove(task);
            } else {
                return false;
            }
        }
    }
}
