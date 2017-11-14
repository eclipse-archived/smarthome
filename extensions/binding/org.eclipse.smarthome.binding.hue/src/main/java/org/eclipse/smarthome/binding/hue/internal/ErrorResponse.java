/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public final static Type gsonType = new TypeToken<List<ErrorResponse>>() {
    }.getType();

    public class Error {
        private Integer type;
        private String address;
        private String description;
    }

    private Error error;

    public Integer getType() {
        return error.type;
    }

    public String getAddress() {
        return error.address;
    }

    public String getDescription() {
        return error.description;
    }
}
