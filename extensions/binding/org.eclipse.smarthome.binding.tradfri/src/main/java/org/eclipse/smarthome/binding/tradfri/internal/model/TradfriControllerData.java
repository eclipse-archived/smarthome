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
package org.eclipse.smarthome.binding.tradfri.internal.model;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link TradfriControllerData} class is a Java wrapper for the raw JSON data about the controller state.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class TradfriControllerData extends TradfriWirelessDeviceData {

    private final Logger logger = LoggerFactory.getLogger(TradfriControllerData.class);

    public TradfriControllerData(JsonElement json) {
        super(SWITCH, json);
    }
}
