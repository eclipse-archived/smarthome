/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.hue.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.utils.Util;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@NonNullByDefault
public class CreateUserRequest {
    public final @Nullable String username;
    public final String devicetype;

    public CreateUserRequest(@Nullable String username, String devicetype) {
        if (Util.stringSize(devicetype) > 40) {
            throw new IllegalArgumentException("Device type can be at most 40 characters long");
        }

        if (username != null && (Util.stringSize(username) < 10 || Util.stringSize(username) > 40)) {
            throw new IllegalArgumentException("Username must be between 10 and 40 characters long");
        }

        this.username = username;
        this.devicetype = devicetype;
    }
}
