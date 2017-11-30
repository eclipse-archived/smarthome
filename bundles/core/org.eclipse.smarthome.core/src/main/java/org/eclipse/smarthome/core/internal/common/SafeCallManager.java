/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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

import java.util.concurrent.ExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public interface SafeCallManager {

    /**
     * Track that the call to the target method starts.
     *
     * @param invocation
     * @param wrapper
     */
    void recordCallStart(Invocation invocation, TrackingCallable wrapper);

    /**
     * Track that the call to the target method finished.
     *
     * @param invocation
     * @param wrapper
     */
    void recordCallEnd(Invocation invocation, TrackingCallable wrapper);

    /**
     * Queue the given invocation for asynchronous execution.
     *
     * @param call
     */
    void enqueue(Invocation call);

    /**
     * Get the safe-caller's executor service instance
     *
     * @return
     */
    ExecutorService getScheduler();

    /**
     * Determine if the current thread is one of the safe-caller's thread pool.
     *
     * @return
     */
    boolean isSafeContext();

}
