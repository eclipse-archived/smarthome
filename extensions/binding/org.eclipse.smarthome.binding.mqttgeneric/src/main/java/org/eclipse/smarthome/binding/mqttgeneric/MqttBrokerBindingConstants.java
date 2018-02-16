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
package org.eclipse.smarthome.binding.mqttgeneric;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MqttBrokerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttBrokerBindingConstants {

    public static final String BINDING_ID = "mqttgeneric";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_CONNECTION = new ThingTypeUID(BINDING_ID, "brokerconnection");
    public static final ThingTypeUID THING_TYPE = new ThingTypeUID(BINDING_ID, "topic");

    // Bridge parameters
    public static final String PARAM_BRIDGE_name = "brokername";

    // Channels
    public static final String TEXT_CHANNEL = "text";
    public static final String NUMBER_CHANNEL = "number";
    public static final String PERCENTAGE_CHANNEL = "percentage";
    public static final String ONOFF_CHANNEL = "onoff";

}
