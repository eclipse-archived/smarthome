package org.eclipse.smarthome.core.common;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 * @param <T>
 */
public interface SafeCallerBuilder<T> {

    public T build();

    public SafeCallerBuilder<T> withTimeout(int timeout);

    public SafeCallerBuilder<T> withIdentifier(Object identifier);

    public SafeCallerBuilder<T> onException(Consumer<Throwable> exceptionHandler);

    public SafeCallerBuilder<T> onTimeout(Consumer<TimeoutException> timeoutHandler);

    public SafeCallerBuilder<T> withAsync();

}
