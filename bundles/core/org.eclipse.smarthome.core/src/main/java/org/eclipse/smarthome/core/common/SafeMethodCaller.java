/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SafeMethodCaller} is a helper class, which can be used to call a method safely. Safely means that a separate
 * thread is opened, so that a method call can not block the execution of the system. It also catches Errors and wraps
 * them into a {@link ExecutionException}, so that the caller does not have to catch {@link Throwable}. This helper
 * class is useful when calling third party code like bindings.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class SafeMethodCaller {

    private static final String SAFE_CALL_POOL_NAME = "safeCall";

    /**
     * Executable Action. See {@link SafeMethodCaller#call(Action)}
     *
     * @param <V> return type
     */
    public interface Action<V> extends Callable<V> {

    }

    /**
     * Executable Action with exception. See {@link SafeMethodCaller#call(ActionWithException)}
     *
     * @param <V> return type
     */
    public interface ActionWithException<V> extends Callable<V> {

    }

    /**
     * Default timeout for actions in milliseconds.
     */
    public static int DEFAULT_TIMEOUT = 5000 /* milliseconds */;

    /**
     * Executes the action in a new thread with a default timeout (see {@link SafeMethodCaller#DEFAULT_TIMEOUT}). If an
     * exception occurs while calling the action or the action does not terminate within the timeout this method
     * rethrows the exception.
     *
     * @param action action to be called
     * @return result
     * @throws TimeoutException if the action does not terminate within the timeout
     * @throws ExecutionException if the action throws an Exception or an Error
     */
    public static <V> V call(ActionWithException<V> action) throws TimeoutException, ExecutionException {
        return call(action, DEFAULT_TIMEOUT);
    }

    /**
     * Executes the action in a new thread with a given timeout. If an exception occurs while calling the action or the
     * action does not terminate within the timeout this method rethrows the exception.
     *
     * @param action action to be called
     * @param timeout timeout of the action in milliseconds. If the action takes longer than the defined timeout a
     *            {@link TimeoutException} is thrown
     * @return result
     * @throws TimeoutException if the action does not terminate within the timeout
     * @throws ExecutionException if the action throws an Exception or an Error
     */
    public static <V> V call(ActionWithException<V> action, int timeout) throws TimeoutException, ExecutionException {
        try {
            return callAsynchronous(action, timeout);
        } catch (InterruptedException ex) {
            throw new IllegalStateException("Thread was interrupted.", ex);
        }
    }

    /**
     * Executes the action in a new thread with a default timeout (see {@link SafeMethodCaller#DEFAULT_TIMEOUT}). If
     * an exception occurs while calling the action or the action does not terminate within the timeout this method just
     * logs the exception, but does not rethrow it. In case an exception occurred or the action timeout the result will
     * always be null.
     *
     * @param action action to be called
     * @return result or null if an exception occurred or the timeout was reached
     */
    public static <V> V call(Action<V> action) {
        return call(action, DEFAULT_TIMEOUT);
    }

    /**
     * Executes the action in a new thread with a given timeout. If an exception occurs while calling the action or the
     * action does not terminate within the timeout this method just logs the exception, but does not rethrow it. In
     * case an exception occurred or the action timeout the result will always be null.
     *
     * @param action action to be called
     * @param timeout timeout of the action in milliseconds. If the action takes longer than the defined timeout an
     *            exception is logged and this method returns null
     * @return result or null if an exception occurred or the timeout was reached
     */
    public static <V> V call(Action<V> action, int timeout) {
        try {
            return callAsynchronous(action, timeout);
        } catch (ExecutionException ex) {
            StackTraceElement stackTraceElement = findCalledMethod(ex, action.getClass());
            if (stackTraceElement != null) {

                String className = stackTraceElement.getClassName();
                String methodName = stackTraceElement.getMethodName();

                getLogger().error("Exception occurred while calling '{}' at '{}'", methodName, className, ex);
            } else {
                getLogger().error("Exception occurred while calling action", ex);
            }
            return null;
        } catch (TimeoutException ex) {
            getLogger().error(
                    "Timeout occurred while calling method. Execution took longer than {} milliseconds.", timeout, 
                    ex);
            return null;
        } catch (Throwable throwable) {
            getLogger().error("Unkown Exception or Error occurred while calling action", throwable);
            return null;
        }
    }

    /**
     * This method tries to find the method which was called within the action.
     *
     * @param eex ExecutionException
     * @param actionClass action class
     * @return stack trace element for the called method or null
     */
    private static StackTraceElement findCalledMethod(ExecutionException eex, Class<?> actionClass) {
        if (eex.getCause() == null) {
            return null;
        }

        StackTraceElement[] stackTrace = eex.getCause().getStackTrace();
        if (stackTrace == null) {
            return null;
        }

        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            if (stackTraceElement.getClassName().equals(actionClass.getName())) {
                return stackTrace[i - 1];
            }
        }

        return null;
    }

    private static class CallableWrapper<V> implements Callable<V> {

        private final Callable<V> callable;
        private Thread thread;

        public CallableWrapper(final Callable<V> callable) {
            this.callable = callable;
        }

        public Thread getThread() {
            return thread;
        }

        @Override
        public V call() throws Exception {
            thread = Thread.currentThread();
            return callable.call();
        }
    }

    private static <V> V callAsynchronous(final Callable<V> callable, int timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (Thread.currentThread().getName().startsWith(SAFE_CALL_POOL_NAME + "-")) {
            getLogger().trace("Already in a SafeMethodCaller context, executing {} directly.", callable);
            return executeDirectly(callable);
        }
        CallableWrapper<V> wrapper = new CallableWrapper<>(callable);
        try {
            Future<V> future = ThreadPoolManager.getPool(SAFE_CALL_POOL_NAME).submit(wrapper);
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            if (wrapper.getThread() != null) {
                final Thread thread = wrapper.getThread();
                StackTraceElement element = AccessController.doPrivileged(new PrivilegedAction<StackTraceElement>() {

                    @Override
                    public StackTraceElement run() {
                        return thread.getStackTrace()[0];
                    }
                });
                getLogger().debug("Timeout of {}ms exceeded, thread {} ({}) in state {} is at {}.{}({}:{}).", timeout,
                        thread.getName(), thread.getId(), thread.getState().toString(), element.getClassName(),
                        element.getMethodName(), element.getFileName(), element.getLineNumber());
                throw e;
            } else {
                getLogger().debug("Timeout of {}ms exceeded but the task was still queued.", timeout);
            }
            return null;
        }
    }

    private static <V> V executeDirectly(final Callable<V> callable) throws ExecutionException {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(SafeMethodCaller.class);
    }

}
