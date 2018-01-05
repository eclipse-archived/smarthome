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
package org.eclipse.smarthome.core.internal.common;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps the ScheduledThreadPoolExecutor to implement the {@link #afterExecute(Runnable, Throwable)} method
 * and log the exception in case the scheduled runnable threw an exception. The error will otherwise go unnoticed
 * because an exception thrown in the runnable will simply end with no logging unless the user handles it. This
 * wrapper removes the burden for the user to always catch errors in scheduled runnables for logging, and it also
 * catches unchecked exceptions that can be the cause of very hard to catch bugs because no error is ever shown if the
 * user doesn't catch the error in the runnable itself.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class WrappedScheduledExecutorService extends ScheduledThreadPoolExecutor {

    final static Logger logger = LoggerFactory.getLogger(WrappedScheduledExecutorService.class);

    public WrappedScheduledExecutorService(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Throwable actualThrowable = t;
        if (actualThrowable == null && r instanceof Future<?>) {
            try {
                ((Future<?>) r).get();
            } catch (CancellationException ce) {
                actualThrowable = ce;
            } catch (ExecutionException ee) {
                actualThrowable = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (actualThrowable != null) {
            logger.warn("Scheduled runnable ended with an exception", actualThrowable);
        }
    }
}
