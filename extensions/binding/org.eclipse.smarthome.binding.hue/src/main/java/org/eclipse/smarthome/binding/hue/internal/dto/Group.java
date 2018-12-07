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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;

/**
 * Hue Group object
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class Group {
    public static final Type GSON_TYPE = new TypeToken<Map<String, Group>>() {
    }.getType();

    public transient String id;
    public String name;
    public LightState action;
    public List<String> lights;

    public Group() {
        this.id = "0";
        this.name = "Lightset 0";
    }

    public Group(String name) {
        this.name = name;
    }

    /**
     * Returns a list of the lights in the group.
     */
    public List<Light> getLights() {
        return lights.stream().map(id -> new Light().withId(id)).collect(Collectors.toList());
    }

    /**
     * Returns if the group can be modified.
     * Currently only returns false for the all-lights pseudo group.
     */
    public boolean isModifiable() {
        return !id.equals("0");
    }

    public Group withId(String id) {
        this.id = id;
        return this;
    }
}
