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
package org.eclipse.smarthome.core.common;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder to create a safe-call wrapper for another object.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 * @param <T>
 */
@NonNullByDefault
public interface SafeCallerBuilder<T> {

    public T build();

    /**
     * Sets the timeout
     *
     * @param timeout the timeout in milliseconds.
     * @return the SafeCallerBuilder itself
     */
    public SafeCallerBuilder<T> withTimeout(int timeout);

    /**
     * Specifies the identifier for the context in which only one thread may be occupied at the same time.
     *
     * @param identifier the identifier much must have a proper hashcode()/equals() implementation in order to
     *            distinguish different contexts.
     * @return the SafeCallerBuilder itself
     */
    public SafeCallerBuilder<T> withIdentifier(Object identifier);

    /**
     * Specifies a callback in case of execution errors.
     *
     * @param exceptionHandler
     * @return the SafeCallerBuilder itself
     */
    public SafeCallerBuilder<T> onException(Consumer<Throwable> exceptionHandler);

    /**
     * Specifies a callback in case of timeouts.
     *
     * @param timeoutHandler
     * @return the SafeCallerBuilder itself
     */
    public SafeCallerBuilder<T> onTimeout(Consumer<TimeoutException> timeoutHandler);

    /**
     * Denotes that the calls should be executed asynchronously, i.e. that they should return immediately and not even
     * block until they reached the timeout.
     * <p>
     * By default, calls will be executed synchronously (i.e. blocking) until the timeout is reached.
     *
     * @return the SafeCallerBuilder itself
     */
    public SafeCallerBuilder<T> withAsync();

}
