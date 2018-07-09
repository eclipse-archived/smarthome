/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lwm2m;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Lwm2mBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
public class Lwm2mBindingConstants {
    public static final String BINDING_ID = "lwm2m";
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "client");
}
