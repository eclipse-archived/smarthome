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
package org.eclipse.smarthome.binding.onewire.internal.device;

import java.util.HashMap;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OwDeviceParameter} stores bridge specific implementation details of a device
 *
 * @author Jan N. Klug - Initial contribution
 */
public class OwDeviceParameter {

    private final HashMap<ThingTypeUID, Object> map = new HashMap<>();

    /**
     * sets (or replaces) implementation details for a given bridge
     *
     * @param thingTypeUID the bridges thing type UID
     * @param obj the information for this bridge
     */
    public void set(ThingTypeUID thingTypeUID, Object obj) {
        if (map.containsKey(thingTypeUID)) {
            map.replace(thingTypeUID, obj);
        } else {
            map.put(thingTypeUID, obj);
        }
    }

    /**
     * gets implementation details for a given bridge
     *
     * @param thingTypeUID the bridges thing type UID
     * @return the information for this bridge
     */
    public Object get(ThingTypeUID thingTypeUID) {
        return map.get(thingTypeUID);
    }
}
