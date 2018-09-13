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
package org.eclipse.smarthome.binding.iota;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link IotaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Theo Giovanna - Initial contribution
 */
@NonNullByDefault
public class IotaBindingConstants {

    public static final String BINDING_ID = "iota";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_IOTA_TOPIC = new ThingTypeUID(BINDING_ID, "topic");
    public static final ThingTypeUID THING_TYPE_IOTA_WALLET = new ThingTypeUID(BINDING_ID, "wallet");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_IOTA_TOPIC, THING_TYPE_IOTA_WALLET));

    // Bridge config properties
    public static final String PROTOCOL = "protocol";
    public static final String HOST = "host";
    public static final String PORT = "port";

    // Channels
    public static final String CHANNEL_BALANCE = "balance";
    public static final String TEXT_CHANNEL = "text";
    public static final String NUMBER_CHANNEL = "number";
    public static final String ONOFF_CHANNEL = "onoff";

}
