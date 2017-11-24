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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * OSGi service to obtain a {@link SafeCallerBuilder}.
 *
 * Safe-calls are used within the framework in order to protect it from hanging/blocking binding code and log meaningful
 * messages to detect and identify such hanging code.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 * @param <T>
 */
@NonNullByDefault
public interface SafeCaller {

    /**
     * Default timeout for actions in milliseconds.
     */
    public final int DEFAULT_TIMEOUT = 5000 /* milliseconds */;

    public <T> SafeCallerBuilder<T> create(T target, Class<T> interfaceType);

    public <T> SafeCallerBuilder<T> create(T target);

}
