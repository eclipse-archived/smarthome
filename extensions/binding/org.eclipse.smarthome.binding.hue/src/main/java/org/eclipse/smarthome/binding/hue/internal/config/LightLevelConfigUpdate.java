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
package org.eclipse.smarthome.binding.hue.internal.config;

import org.eclipse.smarthome.binding.hue.internal.Command;
import org.eclipse.smarthome.binding.hue.internal.ConfigUpdate;

/**
 * Updates the configuration of a light level sensor
 *
 * @author Samuel Leisering
 *
 */
public class LightLevelConfigUpdate extends ConfigUpdate {

    /**
     * The lightlevel threshold for the "dark" property
     *
     * @param threshold
     */
    public void setThresholdDark(int threshold) {
        commands.add(new Command("tholddark", threshold));
    }

    /**
     * The lightlevel threshold for the "daylight" property,
     * given as an relative offset to the dark threshold
     *
     * @param lightOffset
     */
    public void setThresholdOffset(int lightOffset) {
        commands.add(new Command("tholdoffset", lightOffset));
    }
}
