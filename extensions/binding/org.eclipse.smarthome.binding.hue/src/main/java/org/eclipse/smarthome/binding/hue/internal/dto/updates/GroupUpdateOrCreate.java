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
package org.eclipse.smarthome.binding.hue.internal.dto.updates;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.smarthome.binding.hue.internal.dto.Light;
import org.eclipse.smarthome.binding.hue.internal.utils.Util;

/**
 * Contains all fields that can be updated on the /groups/{id} rest endpoint for creating a group on the /groups rest
 * endpoint.
 *
 * @author David Graeff - Initial contribution
 */
public class GroupUpdateOrCreate {
    public String name;
    public List<String> lights;

    public GroupUpdateOrCreate(String name) {
        if (name != null && Util.stringSize(name) > 32) {
            throw new IllegalArgumentException("Name can be at most 32 characters long");
        }
        this.name = name;
    }

    public GroupUpdateOrCreate(String name, List<Light> lights) {
        if (name != null && Util.stringSize(name) > 32) {
            throw new IllegalArgumentException("Name can be at most 32 characters long");
        } else if (lights != null && (lights.size() == 0 || lights.size() > 16)) {
            throw new IllegalArgumentException("Group cannot be empty and cannot have more than 16 lights");
        }

        this.name = name;
        if (lights != null) {
            this.lights = lights.stream().map(e -> e.id).collect(Collectors.toList());
        }
    }
}
