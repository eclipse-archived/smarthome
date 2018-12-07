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

import org.eclipse.smarthome.binding.hue.internal.dto.LightState;

/**
 * Builder for the current state of a hue light.
 *
 * @author Dominic Lerbs - Initial contribution
 * @author Markus Mazurczak - Added possibility to set modelId to "PAR16 50 TW" to test osram workaround
 * @author Markus Rathgeb - migrated to plain Java test
 * @author Christoph Weitkamp - Added support for bulbs using CIE XY colormode only
 */
public class HueLightStateExtended extends LightState {
    public String model = "LCT001";

    public HueLightStateExtended() {
    }

    public HueLightStateExtended withModel(String model) {
        this.model = model;
        return this;
    }
}
