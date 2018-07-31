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
 * Updates the configuration of a presence sensor
 *
 * @author Samuel Leisering - Sensor Support
 *
 */
public class PresenceConfigUpdate extends ConfigUpdate {
    /**
     * enable/disable the sensor
     *
     * @param on
     */
    public void setOn(boolean on) {
        commands.add(new Command("on", on));
    }

    /**
     * The sensitivity of the presence sensor,
     * should not be larger than the maximum sensitivity
     *
     * @param sensitivity
     */
    public void setSensitivity(int sensitivity) {
        commands.add(new Command("sensitivity", sensitivity));
    }
}
