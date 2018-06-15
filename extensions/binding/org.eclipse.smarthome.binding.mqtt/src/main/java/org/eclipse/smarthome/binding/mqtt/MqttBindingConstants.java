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
package org.eclipse.smarthome.binding.mqtt;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MqttBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttBindingConstants {
    private static final String BINDING_ID = "mqtt";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_SYSTEMBROKER = new ThingTypeUID(BINDING_ID, "systemBroker");
    public static final ThingTypeUID BRIDGE_TYPE_BROKER = new ThingTypeUID(BINDING_ID, "broker");
}
