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
package org.eclipse.smarthome.binding.hue.internal;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
class ErrorResponse {
    public static final Type GSON_TYPE = new TypeToken<List<ErrorResponse>>() {
    }.getType();

    public class Error {
        private Integer type;
        private String address;
        private String description;
    }

    private Error error;

    public Integer getType() {
        if (error == null) {
            return null;
        }
        return error.type;
    }

    public String getAddress() {
        if (error == null) {
            return null;
        }
        return error.address;
    }

    public String getDescription() {
        if (error == null) {
            return null;
        }
        return error.description;
    }
}
