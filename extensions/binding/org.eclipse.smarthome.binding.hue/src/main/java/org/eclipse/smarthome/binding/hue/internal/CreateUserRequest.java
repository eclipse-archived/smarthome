/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@SuppressWarnings("unused")
class CreateUserRequest {
    private String username;
    private String devicetype;

    public CreateUserRequest(String username, String devicetype) {
        if (Util.stringSize(devicetype) > 40) {
            throw new IllegalArgumentException("Device type can be at most 40 characters long");
        }

        if (Util.stringSize(username) < 10 || Util.stringSize(username) > 40) {
            throw new IllegalArgumentException("Username must be between 10 and 40 characters long");
        }

        this.username = username;
        this.devicetype = devicetype;
    }

    public CreateUserRequest(String devicetype) {
        if (Util.stringSize(devicetype) > 40) {
            throw new IllegalArgumentException("Device type can be at most 40 characters long");
        }

        this.devicetype = devicetype;
    }
}
