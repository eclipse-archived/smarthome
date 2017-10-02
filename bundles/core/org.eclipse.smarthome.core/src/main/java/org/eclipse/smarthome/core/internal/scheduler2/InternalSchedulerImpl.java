/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.scheduler2;

import java.io.Closeable;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.core.scheduler2.CronJob;
import org.eclipse.smarthome.core.scheduler2.Scheduler;
import org.eclipse.smarthome.core.scheduler2.TimeoutException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Kriens - initial contribution and API
 * @author Simon Kaufmann - ported to CompletableFuture
 */
@Component(name = "org.eclipse.smarthome.scheduler", service = InternalSchedulerImpl.class, immediate = true)
public class InternalSchedulerImpl implements Scheduler {

    private static final int THREAD_POOL_SIZE = 10;
    private static final long THREAD_TIMEOUT = 65L;

    private final List<Cron> crons = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(InternalSchedulerImpl.class);

    private final Clock clock = Clock.systemDefaultZone();
    private ScheduledExecutorService executor;

    @Activate
    void activate() {
        executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        ((ThreadPoolExecutor) executor).setKeepAliveTime(THREAD_TIMEOUT, TimeUnit.SECONDS);
        ((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);
    }

    @Deactivate
    void deactivate() {
        List<Runnable> shutdownNow = executor.shutdownNow();
        if (shutdownNow != null && shutdownNow.size() > 0) {
            logger.warn("Shutdown executables {}", shutdownNow);
        }
    }

    @Override
    public CompletableFuture<Instant> after(Duration duration) {
        CompletableFuture<Instant> deferred = new CompletableFuture<>();
        Instant start = Instant.now();
        ScheduledFuture<?> schedule = executor.schedule(() -> {
            deferred.complete(start);
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
        deferred.exceptionally((Throwable e) -> {
            if (e instanceof CancellationException) {
                schedule.cancel(true);
            }
            return null;
        });
        return deferred;
    }

    @Override
    public <T> CompletableFuture<T> after(Callable<T> callable, Duration duration) {
        CompletableFuture<T> deferred = new CompletableFuture<>();
        ScheduledFuture<?> schedule = executor.schedule(() -> {
            try {
                deferred.complete(callable.call());
            } catch (Throwable e) {
                deferred.completeExceptionally(e);
            }
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
        deferred.exceptionally((Throwable e) -> {
            if (e instanceof CancellationException) {
                schedule.cancel(true);
            }
            return null;
        });
        return deferred;
    }

    static class Unique {
        AtomicBoolean done = new AtomicBoolean();

        interface RunnableException {
            public void run();
        }

        boolean once(RunnableException o) {
            if (done.getAndSet(true) == false) {
                o.run();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public <T> CompletableFuture<T> before(CompletableFuture<T> promise, Duration timeout) {
        CompletableFuture<T> d = new CompletableFuture<T>();
        Unique only = new Unique();

        after(timeout).thenAccept((p) -> {
            only.once(() -> d.completeExceptionally(TimeoutException.SINGLETON));
        });

        promise.thenAccept((p) -> {
            only.once(() -> d.complete(p));
        }).exceptionally((Throwable ex) -> {
            only.once(() -> d.completeExceptionally(ex));
            return null;
        });
        return d;
    }

    @Override
    public Closeable schedule(RunnableWithException r, Duration first, Duration... delays) {
        PeriodicAdjuster cronAdjuster = new PeriodicAdjuster(first, delays);
        Schedule s = new Schedule(cronAdjuster, r);
        schedule(s, s.next(ZonedDateTime.now(clock)));
        return () -> {
            s.canceled = true;
            s.promise.cancel(true);
        };
    }

    private void schedule(Schedule s, Instant instant) {
        s.promise = at(() -> {
            try {
                s.doIt();
            } catch (Throwable t) {
                if (s.exception != null) {
                    logger.warn("Schedule failed {}", s, t);
                }
                s.exception = t;
            }

            schedule(s, s.next(ZonedDateTime.now(clock)));
            return null;
        }, instant);
        if (s.canceled) {
            s.promise.cancel(true);
        }
    }

    @Override
    public Closeable schedule(RunnableWithException r, String cronExpression) {
        Schedule s = new Schedule(new CronAdjuster(cronExpression), r);
        schedule(s, s.next(ZonedDateTime.now(clock)));
        return () -> {
            s.canceled = true;
            s.promise.cancel(true);
        };
    }

    @Override
    public <T> Closeable schedule(CronJob job, Map<String, Object> config, String cronExpression) {
        CronAdjuster cronAdjuster = new CronAdjuster(cronExpression);
        Schedule s = new Schedule(cronAdjuster, job, config);
        schedule(s, cronAdjuster.isReboot() ? Instant.ofEpochMilli(1) : s.next(ZonedDateTime.now(clock)));
        return () -> {
            s.canceled = true;
            s.promise.cancel(true);
        };
    }

    @Override
    public CompletableFuture<Instant> at(Instant instant) {
        long delay = instant.toEpochMilli() - System.currentTimeMillis();
        return after(Duration.ofMillis(delay));
    }

    @Override
    public <T> CompletableFuture<T> at(Callable<T> callable, Instant instant) {
        long delay = instant.toEpochMilli() - System.currentTimeMillis();
        return after(callable, Duration.ofMillis(delay));
    }

    private static class Cron {

        private final Closeable schedule;
        private final CronJob target;

        private Cron(CronJob target, Closeable schedule) {
            this.target = target;
            this.schedule = schedule;
        }
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
    <T> void addSchedule(CronJob s, Map<String, Object> map) {
        Object scheduleConfig = map.get(CronJob.CRON);
        String[] schedules = null;
        if (scheduleConfig instanceof String[]) {
            schedules = (String[]) scheduleConfig;
        } else if (scheduleConfig instanceof String) {
            schedules = new String[] { (String) scheduleConfig };
        }
        if (schedules == null || schedules.length == 0) {
            return;
        }

        synchronized (crons) {
            for (String schedule : schedules) {
                try {
                    Cron cron = new Cron(s, schedule(s, map, schedule));
                    crons.add(cron);
                } catch (Exception e) {
                    logger.error("Invalid  cron expression {} from {}", schedule, map, e);
                }
            }
        }
    }

    void removeSchedule(CronJob s) {
        synchronized (crons) {
            for (Iterator<Cron> cron = crons.iterator(); cron.hasNext();) {
                try {
                    Cron c = cron.next();
                    if (c.target == s) {
                        cron.remove();
                        c.schedule.close();
                    }
                } catch (IOException e) {
                    // we're closing, so ignore any errors
                }
            }
        }
    }

    private static class Schedule {

        private final TemporalAdjuster adjuster;
        private final RunnableWithException runnable;
        private final CronJob job;
        private final Map<String, Object> env;

        volatile CompletableFuture<?> promise;
        volatile boolean canceled;
        Throwable exception;

        public Schedule(TemporalAdjuster adjuster, CronJob job, Map<String, Object> env) {
            this.adjuster = adjuster;
            this.job = job;
            this.env = env;
            this.runnable = null;
        }

        public Schedule(TemporalAdjuster adjuster, RunnableWithException runnable) {
            this.adjuster = adjuster;
            this.job = null;
            this.env = null;
            this.runnable = runnable;
        }

        Instant next(ZonedDateTime now) {
            return now.with(adjuster).toInstant();
        }

        void doIt() throws Exception {
            if (runnable != null) {
                runnable.run();
            } else {
                job.run(env);
            }
        }

    }

}
