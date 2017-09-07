/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LIRCBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCBindingConstants {

    public final static String BINDING_ID = "lirc";
    public final static int DISCOVERY_TIMOUT = 5;

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_REMOTE = new ThingTypeUID(BINDING_ID, "remote");

    // List of all channel ids
    public final static String CHANNEL_EVENT = "event";
    public final static String CHANNEL_TRANSMIT = "transmit";

    // List of all supported thing types
    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_TYPES = Collections.singleton(THING_TYPE_REMOTE);
    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_REMOTE, THING_TYPE_BRIDGE)
            .collect(Collectors.toSet());

    // List of all properties
    public final static String PROPERTY_REMOTE = "remote";

}
