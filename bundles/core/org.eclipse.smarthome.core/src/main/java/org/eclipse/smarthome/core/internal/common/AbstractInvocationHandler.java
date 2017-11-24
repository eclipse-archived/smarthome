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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for synchronous and ansynchronous invocation handlers.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 * @param <T>
 */
@NonNullByDefault
abstract class AbstractInvocationHandler<T> {

    private static final String MSG_TIMEOUT_R = "Timeout of {}ms exceeded while calling method '{}' on '{}'. Thread '{}' ({}) is in state '{}'\n{}";
    private static final String MSG_TIMEOUT_Q = "Timeout of {}ms exceeded while calling method '{}' on '{}'. The task was still queued.";
    private static final String MSG_DUPLICATE = "Thread occupied while calling method '{}' on '{}' because of another blocking call.\n\tThe other call was to '{}'.\n\tIt's thread '{}' ({}) is in state '{}'\n{}";
    private static final String MSG_ERROR = "An error occurred while calling method '{}' on '{}': {}";

    private final Logger logger = LoggerFactory.getLogger(AbstractInvocationHandler.class);

    private final SafeCallManager manager;
    private final T target;
    private final Object identifier;
    private final long timeout;
    @Nullable
    private final Consumer<Throwable> exceptionHandler;
    @Nullable
    private final Consumer<TimeoutException> timeoutHandler;

    AbstractInvocationHandler(SafeCallManager manager, T target, Object identifier, long timeout,
            @Nullable Consumer<Throwable> exceptionHandler, @Nullable Consumer<TimeoutException> timeoutHandler) {
        super();
        this.manager = manager;
        this.target = target;
        this.identifier = identifier;
        this.timeout = timeout;
        this.exceptionHandler = exceptionHandler;
        this.timeoutHandler = timeoutHandler;
    }

    SafeCallManager getManager() {
        return manager;
    }

    T getTarget() {
        return target;
    }

    Object getIdentifier() {
        return identifier;
    }

    long getTimeout() {
        return timeout;
    }

    @Nullable
    Consumer<Throwable> getExceptionHandler() {
        return exceptionHandler;
    }

    @Nullable
    Consumer<TimeoutException> getTimeoutHandler() {
        return timeoutHandler;
    }

    void handleExecutionException(Method method, ExecutionException e) {
        if (e.getCause() instanceof DuplicateExecutionException) {
            handleDuplicate(method, (DuplicateExecutionException) e.getCause());
        } else if (e.getCause() instanceof InvocationTargetException) {
            handleException(method, (InvocationTargetException) e.getCause());
        }
    }

    void handleException(Method method, InvocationTargetException e) {
        logger.error(MSG_ERROR, toString(method), target, e.getCause().getMessage(), e.getCause());
        if (exceptionHandler != null) {
            exceptionHandler.accept(e.getCause());
        }
    }

    void handleDuplicate(Method method, DuplicateExecutionException e) {
        Thread thread = e.getCallable().getThread();
        logger.warn(MSG_DUPLICATE, toString(method), target, toString(e.getCallable().getMethod()), thread.getName(),
                thread.getId(), thread.getState().toString(), getStacktrace(thread));
    }

    void handleTimeout(Method method, TrackingCallable wrapper, TimeoutException e) {
        if (wrapper.getThread() != null) {
            final Thread thread = wrapper.getThread();
            logger.warn(MSG_TIMEOUT_R, timeout, toString(method), target, thread.getName(), thread.getId(),
                    thread.getState().toString(), getStacktrace(thread));
        } else {
            logger.warn(MSG_TIMEOUT_Q, timeout, toString(method), target);
        }
        if (timeoutHandler != null) {
            timeoutHandler.accept(e);
        }
    }

    private String getStacktrace(final Thread thread) {
        StackTraceElement[] elements = AccessController.doPrivileged(new PrivilegedAction<StackTraceElement[]>() {
            @Override
            public StackTraceElement[] run() {
                return thread.getStackTrace();
            }
        });
        StringBuilder sb = new StringBuilder();
        String previous = "";
        for (int i = 0; i < elements.length; i++) {
            String current = elements[i].toString();
            sb.append("\tat " + current + "\n");
            if (previous.startsWith("org.eclipse.smarthome.") && !current.startsWith("org.eclipse.smarthome.")) {
                sb.append("\t...");
                break;
            }
            previous = current;
        }
        return sb.toString();
    }

    String toString(Method method) {
        return method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
    }

    @Nullable
    Object invokeDirect(Invocation invocation, TrackingCallable wrapper)
            throws IllegalAccessException, IllegalArgumentException {
        try {
            manager.recordCallStart(invocation, wrapper);
        } catch (DuplicateExecutionException e) {
            return null;
        }
        try {
            return invocation.getMethod().invoke(target, invocation.getArgs());
        } catch (InvocationTargetException e) {
            handleException(invocation.getMethod(), e);
            return null;
        } finally {
            manager.recordCallEnd(invocation, wrapper);
        }
    }

}
