/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler2;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * A Scheduler service provides timed semantics to CompletableFutures. A Scheduler can
 * delay a CompletableFutures, it can resolve a CompletableFutures at a certain time, or it can
 * provide a timeout to a CompletableFutures.
 * <p>
 * This scheduler has a millisecond resolution.
 *
 * @author Peter Kriens - initial contribution and API
 * @author Simon Kaufmann - adapted to CompletableFutures
 */
public interface Scheduler {

    /**
     * Convenience interface that is similar as Runnable but allows exceptions
     */
    @FunctionalInterface
    interface RunnableWithException {
        void run() throws Exception;
    }

    /**
     * Return a CompletableFutures that resolves after delaying with the result of the
     * call that is executed after the delay.
     *
     * @param call
     *            provides the result
     * @param delay
     *            The duration to wait
     * @return A CompletableFutures
     */
    <T> CompletableFuture<T> after(Callable<T> call, Duration delay);

    /**
     * Return a CompletableFutures that resolves at the given epochTime
     *
     * @param instant
     *            The Java (System.currentMillis) time
     * @return A CompletableFutures
     */
    CompletableFuture<Instant> at(Instant instant);

    /**
     * Return a CompletableFutures that resolves at the given epochTime with the result of
     * the call.
     *
     * @param callable
     *            provides the result
     * @param instant
     *            The Java (System.currentMillis) time
     * @return A cancellable Promise
     */
    <T> CompletableFuture<T> at(Callable<T> callable, Instant instant);

    /**
     * Schedule a runnable to be executed for the give cron expression (See
     * {@link CronJob}). Every time when the cronExpression matches the current
     * time, the runnable will be run. The method returns a closeable that can
     * be used to stop scheduling. This variation does not take an environment
     * object.
     *
     * @param r
     *            The Runnable to run
     * @param cronExpression
     *            A Cron Expression
     * @return A closeable to terminate the schedule
     */
    Closeable schedule(RunnableWithException r, String cronExpression);

    /**
     * Schedule a runnable to be executed for the give cron expression (See
     * {@link CronJob}). Every time when the cronExpression matches the current
     * time, the runnable will be run. The method returns a closeable that can
     * be used to stop scheduling. The run method of r takes an environment
     * object. An environment object is a custom interface where the names of
     * the methods are the keys in the properties (see {@link DTOs}).
     *
     * @param type
     *            The data type of the parameter for the cron job
     * @param r
     *            The Runnable to run
     * @param cronExpression
     *            A Cron Expression
     * @return A closeable to terminate the schedule
     */
    <T> Closeable schedule(CronJob r, Map<String, Object> config, String cronExpression);

    /**
     * Return a CompletableFutures that fails with a {@link TimeoutException}
     * when the given CompletableFutures is not resolved before the given timeout. If the
     * given CompletableFutures fails or is resolved before the timeout then the returned
     * CompletableFutures will be treated accordingly. The cancellation does not influence
     * the final result of the given CompletableFutures since a CompletableFutures can only be failed
     * or resolved by its creator.
     * <p>
     * If the timeout is in the past then the CompletableFutures will be resolved
     * immediately
     *
     * @param promise
     *            The CompletableFutures to base the returned CompletableFutures on
     * @param timeout
     *            The number of milliseconds to wait.
     * @return A CompletableFutures
     */
    <T> CompletableFuture<T> before(CompletableFuture<T> promise, Duration timeout);

    /**
     * Convenience method to use an instant and a RunnableWithException. See
     * {@link #at(RunnableWithException, long)}.
     *
     * @param r
     *            the runnable with exception to call when instant has been
     *            reached
     * @param instant
     *            the time to run r
     * @return A CompletableFutures
     */
    default CompletableFuture<Void> at(RunnableWithException r, Instant instant) {
        return at(() -> {
            r.run();
            return null;
        }, instant);
    }

    /**
     * Return a CompletableFutures that will resolve after the given number of
     * milliseconds. This CompletableFutures can be canceled.
     *
     * @param delay
     *            the delay to wait
     * @return A CompletableFutures
     */
    CompletableFuture<Instant> after(Duration delay);

    /**
     * Convenience method use a duration and a RunnableWithException. See
     * {@link #after(Callable, Duration)}.
     *
     * @param r
     *            the runnable with exception to call when instant has been
     *            reached
     * @param ms
     *            the time to wait in milliseconds
     * @return A cancellable promise
     */
    default <T> CompletableFuture<T> after(RunnableWithException r, Duration delay) {
        return after(() -> {
            r.run();
            return null;
        }, delay);
    }

    /**
     * Schedule a runnable to be executed in a loop. At the first time, first is
     * used as delay, later the delays are used sequentially. If no more values
     * are present, the last value is re-used. The method returns a {@link Closeable}
     * that can be used to stop scheduling. This is a fixed rate scheduler. That
     * is, a base time is established when this method is called and subsequent
     * firings are always calculated relative to this start time.
     *
     * @param r
     *            the runnable to run after each duration
     * @param first
     *            the first delay
     * @param delays
     *            subsequent delays
     * @return A CompletableFutures
     */
    Closeable schedule(RunnableWithException r, Duration first, Duration... delays);

}
