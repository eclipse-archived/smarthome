/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.common;

import static java.util.concurrent.Executors.defaultThreadFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A builder for {@link ThreadFactory} instances. This builder is intended to be use for creating thread factories to be
 * used, e.g., when creating {@link Executor}s via the {@link Executors} utility methods.
 * <p>
 * The built {@link ThreadFactory} uses a wrapped {@link ThreadFactory} to create threads (defaulting to
 * {@link Executors#defaultThreadFactory()}, and then overwrites thread properties as indicated in the build process.
 *
 * @author Henning Sudbrock - initial contribution
 */
@NonNullByDefault
public class ThreadFactoryBuilder {

    @Nullable
    private ThreadFactory wrappedThreadFactory;
    @Nullable
    private String namePrefix;
    private boolean daemonThreads;
    @Nullable
    private UncaughtExceptionHandler uncaughtExceptionHandler;
    @Nullable
    private Integer priority;

    /**
     * Creates a fresh {@link ThreadFactoryBuilder}.
     *
     * @return A {@link ThreadFactoryBuilder}
     */
    public static ThreadFactoryBuilder create() {
        return new ThreadFactoryBuilder();
    }

    private ThreadFactoryBuilder() {
        // use static factory method to create ThreadFactoryBuilder
    }

    /**
     * Sets the wrapped thread factory used to create threads. If set to null, {@link Executors#defaultThreadFactory()}
     * is used. Defaults to null.
     *
     * @param wrappedThreadFactory the wrapped thread factory to be used
     * @return this {@link ThreadFactoryBuilder} instance
     */
    public ThreadFactoryBuilder withWrappedThreadFactory(@Nullable ThreadFactory wrappedThreadFactory) {
        this.wrappedThreadFactory = wrappedThreadFactory;
        return this;
    }

    /**
     * Sets the name prefix to be used by the {@link ThreadFactory}.
     * <p>
     * The threads created by the {@link ThreadFactory} will be named 'ESH-namePrefix-i', where i is an integer
     * incremented with each new thread, initialized to 1.
     * <p>
     * If the namePrefix is null, the naming strategy from the wrapped {@link ThreadFactory} is used. Defaults to null.
     *
     * @param namePrefix The name prefix (can be null)
     * @return this {@link ThreadFactoryBuilder} instance
     */
    public ThreadFactoryBuilder withNamePrefix(@Nullable String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    /**
     * Sets whether the {@link ThreadFactory} will create daemon threads. Defaults to false.
     *
     * @param daemonThreads indicates whether daemon threads shall be created
     * @return this {@link ThreadFactoryBuilder} instance
     */
    public ThreadFactoryBuilder withDaemonThreads(boolean daemonThreads) {
        this.daemonThreads = daemonThreads;
        return this;
    }

    /**
     * Sets the {@link Thread.UncaughtExceptionHandler} to be used for created threads. If set to null, the built
     * {@link ThreadFactory} will not set a handler. Defaults to null.
     *
     * @param uncaughtExceptionHandler The {@link Thread.UncaughtExceptionHandler} to be use for created threads.
     * @return this {@link ThreadFactoryBuilder} instance
     */
    public ThreadFactoryBuilder withUncaughtExceptionHandler(
            @Nullable UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    /**
     * Sets the priority to be set for created threads. Must be a valid thread priority, as indicated by
     * {@link Thread#MIN_PRIORITY} and {@link Thread#MAX_PRIORITY}. If set to null, the built {@link ThreadFactory} will
     * not set a priority. Defaults to null.
     *
     * @param priority The priority to be used for created threads.
     * @return this {@link ThreadFactoryBuilder} instance
     */
    public ThreadFactoryBuilder withPriority(@Nullable Integer priority) {
        if (priority != null && priority.intValue() < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException(String.format(
                    "The provided priority %d is below the minimal thread priority %d", priority, Thread.MIN_PRIORITY));
        }

        if (priority != null && priority.intValue() > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(String.format(
                    "The provided priority %d is above the maximal thread priority %d", priority, Thread.MAX_PRIORITY));
        }

        this.priority = priority;
        return this;
    }

    /**
     * Builds the {@link ThreadFactory}, configuring it as specified during the build.
     *
     * @return the {@link ThreadFactory}
     */
    public ThreadFactory build() {
        ThreadFactory wrappedThreadFactory = this.wrappedThreadFactory;
        if (wrappedThreadFactory == null) {
            wrappedThreadFactory = defaultThreadFactory();
        }

        return ThreadFactoryBuilder.build(wrappedThreadFactory, namePrefix, daemonThreads, uncaughtExceptionHandler,
                priority);
    }

    private static ThreadFactory build(ThreadFactory wrappedThreadFactory, @Nullable String namePrefix,
            boolean daemonThreads, @Nullable UncaughtExceptionHandler uncaughtExceptionHandler,
            @Nullable Integer priority) {

        return new ThreadFactory() {
            AtomicInteger threadCounter = new AtomicInteger(1);

            @Override
            public Thread newThread(@Nullable Runnable runnable) {
                Thread thread = wrappedThreadFactory.newThread(runnable);

                if (namePrefix != null) {
                    thread.setName(String.format("ESH-%s-%d", namePrefix, threadCounter.getAndIncrement()));
                }

                thread.setDaemon(daemonThreads);

                if (uncaughtExceptionHandler != null) {
                    thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                }

                if (priority != null) {
                    thread.setPriority(priority.intValue());
                }

                return thread;
            }
        };
    }

}
