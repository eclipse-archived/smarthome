/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.test.java;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * {@link JavaTest} is an abstract base class for tests which are not necessarily based on OSGi.
 *
 * @author Simon Kaufmann - factored out of JavaOSGiTest
 */
public class JavaTest {

    protected static final int DFL_TIMEOUT = 10000;
    protected static final int DFL_SLEEP_TIME = 50;

    /**
     * Wait until the condition is fulfilled or the timeout is reached.
     *
     * <p>
     * This method uses the default timing parameters.
     *
     * @param condition the condition to check
     * @return true on success, false on timeout
     */
    protected boolean waitFor(BooleanSupplier condition) {
        return waitFor(condition, DFL_TIMEOUT, DFL_SLEEP_TIME);
    }

    /**
     * Wait until the condition is fulfilled or the timeout is reached.
     *
     * @param condition the condition to check
     * @param timeout timeout
     * @param sleepTime interval for checking the condition
     * @return true on success, false on timeout
     */
    protected boolean waitFor(BooleanSupplier condition, int timeout, int sleepTime) {
        int waitingTime = 0;
        boolean rv;
        while (!(rv = condition.getAsBoolean()) && waitingTime < timeout) {
            waitingTime += sleepTime;
            internalSleep(sleepTime);
        }
        return rv;
    }

    /**
     * Wait until the assertion is fulfilled or the timeout is reached.
     *
     * <p>
     * This method uses the default timing parameters.
     *
     * @param assertion closure that must not have an argument
     */
    protected void waitForAssert(Runnable assertion) {
        waitForAssert(assertion, null, DFL_TIMEOUT, DFL_SLEEP_TIME);
    }

    /**
     * Wait until the assertion is fulfilled or the timeout is reached.
     *
     * @param assertion the logic to execute
     * @param timeout timeout
     * @param sleepTime interval for checking the condition
     */
    protected void waitForAssert(Runnable assertion, int timeout, int sleepTime) {
        waitForAssert(assertion, null, timeout, sleepTime);
    }

    /**
     * Wait until the assertion is fulfilled or the timeout is reached.
     *
     * <p>
     * This method uses the default timing parameters.
     *
     * @param assertion the logic to execute
     * @return the return value of the supplied assertion object's function on success
     */
    protected <T> T waitForAssert(Supplier<T> assertion) {
        return waitForAssert(assertion, null, DFL_TIMEOUT, DFL_SLEEP_TIME);
    }

    /**
     * Wait until the assertion is fulfilled or the timeout is reached.
     *
     * @param assertion the logic to execute
     * @param timeout timeout
     * @param sleepTime interval for checking the condition
     * @return the return value of the supplied assertion object's function on success
     */
    protected <T> T waitForAssert(Supplier<T> assertion, int timeout, int sleepTime) {
        return waitForAssert(assertion, null, timeout, sleepTime);
    }

    /**
     * Wait until the assertion is fulfilled or the timeout is reached.
     *
     * @param assertion the logic to execute
     * @param beforeLastCall logic to execute in front of the last call to ${code assertion}
     * @param sleepTime interval for checking the condition
     */
    protected void waitForAssert(Runnable assertion, Runnable beforeLastCall, int timeout, int sleepTime) {
        waitForAssert(assertion, beforeLastCall, null, timeout, sleepTime);
    }

    /**
     * Wait until the assertion is fulfilled or the timeout is reached.
     *
     * @param assertion the logic to execute
     * @param beforeLastCall logic to execute in front of the last call to ${code assertion}
     * @param afterLastCall logic to execute after the last call to ${code assertion}
     * @param sleepTime interval for checking the condition
     */
    protected void waitForAssert(Runnable assertion, Runnable beforeLastCall, Runnable afterLastCall, int timeout,
            int sleepTime) {
        int waitingTime = 0;
        while (waitingTime < timeout) {
            try {
                assertion.run();

                if (afterLastCall != null) {
                    afterLastCall.run();
                }
                return;
            } catch (final Error | NullPointerException error) {
                waitingTime += sleepTime;
                internalSleep(sleepTime);
            }
        }
        if (beforeLastCall != null) {
            beforeLastCall.run();
        }

        try {
            assertion.run();
        } finally {
            if (afterLastCall != null) {
                afterLastCall.run();
            }
        }
    }

    /**
     * Wait until the assertion is fulfilled or the timeout is reached.
     *
     * @param assertion the logic to execute
     * @param beforeLastCall logic to execute in front of the last call to ${code assertion}
     * @param sleepTime interval for checking the condition
     * @return the return value of the supplied assertion object's function on success
     */
    private <T> T waitForAssert(Supplier<T> assertion, Runnable beforeLastCall, long timeout, int sleepTime) {
        final long timeoutNs = TimeUnit.MILLISECONDS.toNanos(timeout);
        final long startingTime = System.nanoTime();
        while (System.nanoTime() - startingTime < timeoutNs) {
            try {
                return assertion.get();
            } catch (final Error | NullPointerException error) {
                internalSleep(sleepTime);
            }
        }
        if (beforeLastCall != null) {
            beforeLastCall.run();
        }
        return assertion.get();
    }

    private void internalSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new Error("We shouldn't be interrupted while testing");
        }
    }

}
