/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

/**
 * Basic light information.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class Light {
    public final static Type gsonType = new TypeToken<Map<String, Light>>() {
    }.getType();

    private String id;
    private String name;

    Light() {
    }

    void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the id of the light.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the light.
     *
     * @return name
     */
    public String getName() {
        return name;
    }
}
