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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a call to the dynamic proxy.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
class Invocation {

    private final Method method;
    private final @Nullable Object @Nullable [] args;
    private final AbstractInvocationHandler<?> invocationHandler;

    Invocation(AbstractInvocationHandler<?> invocationHandler, Method method, @Nullable Object @Nullable [] args) {
        this.method = method;
        this.args = args;
        this.invocationHandler = invocationHandler;
    }

    @Nullable
    Object @Nullable [] getArgs() {
        return args;
    }

    Object getIdentifier() {
        return invocationHandler.getIdentifier();
    }

    Method getMethod() {
        return method;
    }

    long getTimeout() {
        return invocationHandler.getTimeout();
    }

    void handleTimeout(TrackingCallable wrapper) {
        invocationHandler.handleTimeout(method, wrapper);
    }

    public AbstractInvocationHandler<?> getInvocationHandler() {
        return invocationHandler;
    }

    @Override
    public String toString() {
        return "invocation of '" + method.getName() + "()' on '" + invocationHandler.getTarget() + "'";
    }

}
