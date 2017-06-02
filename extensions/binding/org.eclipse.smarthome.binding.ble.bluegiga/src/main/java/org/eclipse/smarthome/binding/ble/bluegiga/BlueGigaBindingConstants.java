/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.bluegiga;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BlueGigaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class BlueGigaBindingConstants {

    private static final String BINDING_ID = "ble";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BLUEGIGA = new ThingTypeUID(BINDING_ID, "bluegiga");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

    public static final String CONFIGURATION_PORT = "bluegiga_port";

}
