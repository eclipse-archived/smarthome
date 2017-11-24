package org.eclipse.smarthome.core.common;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 * @param <T>
 */
public interface SafeCaller {

    /**
     * Default timeout for actions in milliseconds.
     */
    public final int DEFAULT_TIMEOUT = 5000 /* milliseconds */;

    public <T> SafeCallerBuilder<T> create(T target, Class<T> interfaceType);

    public <T> SafeCallerBuilder<T> create(T target);

}
