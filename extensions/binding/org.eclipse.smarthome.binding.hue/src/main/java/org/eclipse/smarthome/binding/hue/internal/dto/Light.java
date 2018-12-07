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

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

/**
 * Light information
 *
 * REST API endpoint: /api/{username}/lights/{id}
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Thomas Höfer - added unique id and changed range check for brightness and saturation
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class Light {
    public static final Type GSON_TYPE = new TypeToken<Map<String, Light>>() {
    }.getType();

    public transient String id;
    public String name;
    public LightState state;
    public String type;
    public String modelid;
    public String swversion;
    public String uniqueid;

    public Light withId(String id) {
        this.id = id;
        return this;
    }
}
