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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronous invocation handler implementation.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 * @param <T>
 */
public class InvocationHandlerSync<T> extends AbstractInvocationHandler<T> implements InvocationHandler {

    private static final String MSG_CONTEXT = "Already in a safe-call context, executing '{}' directly on '{}'.";

    private final Logger logger = LoggerFactory.getLogger(InvocationHandlerSync.class);

    public InvocationHandlerSync(SafeCallManager manager, T target, Object identifier, long timeout,
            @Nullable Consumer<Throwable> exceptionHandler, @Nullable Runnable timeoutHandler) {
        super(manager, target, identifier, timeout, exceptionHandler, timeoutHandler);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TrackingCallable wrapper = new TrackingCallable(new Invocation(this, method, args));
        if (getManager().isSafeContext()) {
            logger.debug(MSG_CONTEXT, toString(method), getTarget());
            return invokeDirect(new Invocation(this, method, args), wrapper);
        }
        try {
            Future<Object> future = getManager().getScheduler().submit(wrapper);
            return future.get(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            handleTimeout(method, wrapper);
        } catch (ExecutionException e) {
            handleExecutionException(method, e);
        }
        return null;
    }

}
