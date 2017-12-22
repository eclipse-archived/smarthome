/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.onewire4j;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OneWire4JBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author aploese@gmx.de - Initial contribution
 */
@NonNullByDefault
public class OneWire4JBindingConstants {

    public static final String BINDING_ID = "onewire4j";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ONEWIRE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    public static final ThingTypeUID THING_TYPE_ONEWIRE_UNKNOWN = new ThingTypeUID(BINDING_ID, "unknown");

    // List of all Bridge Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_ONEWIRE_RS232 = new ThingTypeUID(BINDING_ID, "rs232-bridge");

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_MIN_TEMPERATURE = "minTemperature";
    public static final String CHANNEL_MAX_TEMPERATURE = "maxTemperature";

}
