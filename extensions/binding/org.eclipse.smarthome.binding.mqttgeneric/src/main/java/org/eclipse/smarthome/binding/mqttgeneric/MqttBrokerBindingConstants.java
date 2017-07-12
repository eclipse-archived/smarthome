/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    public static final String THING_EMBEDDED_ID = "embedded";

    // Bridge parameters

    public static final String PARAM_BRIDGE_name = "brokername";
    public static final String PROPERTY_internal_status = "internal_status";

    // Channels

    public static final String TEXT_CHANNEL = "text";
    public static final String NUMBER_CHANNEL = "number";
    public static final String PERCENTAGE_CHANNEL = "percentage";
    public static final String ONOFF_CHANNEL = "onoff";

}
