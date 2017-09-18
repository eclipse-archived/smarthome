/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.scheduler2;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.scheduler2.CronJob;
import org.eclipse.smarthome.core.scheduler2.Scheduler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author Peter Kriens - initial contribution and API
 *
 */
@Component(servicefactory = true)
public class DelegatedScheduler implements Scheduler {

    private InternalSchedulerImpl delegate;
    private final Set<Closeable> closeables = new HashSet<>();

    @Deactivate
    void close() {
        while (true) {
            Closeable c;
            synchronized (closeables) {
                if (closeables.isEmpty()) {
                    return;
                }
                Iterator<Closeable> iterator = closeables.iterator();
                c = iterator.next();
                iterator.remove();
            }

            try {
                c.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    @Override
    public CompletableFuture<Instant> after(Duration delay) {
        return delegate.after(delay);
    }

    @Override
    public <T> CompletableFuture<T> after(Callable<T> callable, Duration delay) {
        return add(delegate.after(callable, delay));
    }

    @Override
    public <T> CompletableFuture<T> before(CompletableFuture<T> promise, Duration timeout) {
        return add(delegate.before(promise, timeout));
    }

    @Override
    public Closeable schedule(RunnableWithException r, Duration first, Duration... delays) {
        return add(delegate.schedule(r, first, delays));
    }

    @Override
    public Closeable schedule(RunnableWithException r, String cronExpression) {
        return add(delegate.schedule(r, cronExpression));
    }

    @Override
    public <T> Closeable schedule(CronJob job, Map<String, Object> data, String cronExpression) {
        return add(delegate.schedule(job, data, cronExpression));
    }

    @Override
    public CompletableFuture<Instant> at(Instant instant) {
        return add(delegate.at(instant));
    }

    @Override
    public <T> CompletableFuture<T> at(Callable<T> callable, Instant instant) {
        return add(delegate.at(callable, instant));
    }

    private Closeable add(Closeable t) {
        synchronized (closeables) {
            closeables.add(t);
        }
        return () -> {
            synchronized (closeables) {
                if (!closeables.remove(t)) {
                    return;
                }
            }
            t.close();
        };
    }

    private <T> CompletableFuture<T> add(CompletableFuture<T> p) {
        Closeable closable = add(() -> {
            p.cancel(true);
        });
        p.thenRun(() -> closeables.remove(closable));
        return p;
    }

    @Reference
    void setDelegate(InternalSchedulerImpl delegate) {
        this.delegate = delegate;
    }

    void unsetDelegate(InternalSchedulerImpl delegate) {
        this.delegate = null;
    }
}
