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

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Wraps a {@link Callable} and tracks the executing thread.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
class TrackingCallable implements Callable<Object> {

    private final Invocation invocation;

    @Nullable
    private Thread thread;

    TrackingCallable(Invocation invocation) {
        this.invocation = invocation;
    }

    @Nullable
    Thread getThread() {
        return thread;
    }

    @Override
    public Object call() throws Exception {
        thread = Thread.currentThread();
        return invocation.getInvocationHandler().invokeDirect(invocation, this);
    }

    Method getMethod() {
        return invocation.getMethod();
    }

    long getTimeout() {
        return invocation.getTimeout();
    }

}
