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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The hue datastore, available under the REST endpoint: /api/{user-name}.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@NonNullByDefault
public class Datastore {
    public Map<String, Light> lights = Collections.emptyMap();
    public Map<String, Group> groups = Collections.emptyMap();
    public Map<String, Schedule> schedules = Collections.emptyMap();
    public HueConfig config = new HueConfig();

    public List<Light> getLights() {
        return lights.entrySet().stream().map(e -> e.getValue().withId(e.getKey())).collect(Collectors.toList());
    }

    public List<Group> getGroups() {
        return groups.entrySet().stream().map(e -> e.getValue().withId(e.getKey())).collect(Collectors.toList());
    }

    public List<Schedule> getSchedules() {
        return schedules.entrySet().stream().map(e -> e.getValue().withId(e.getKey())).collect(Collectors.toList());
    }
}
