/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.triggertest;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link triggerTestBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Moritz Kammerer - Initial contribution
 */
public class TriggerTestBindingConstants {

    public static final String BINDING_ID = "triggertest";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    public final static String CHANNEL_1 = "channel_1";
    public final static String CHANNEL_2 = "channel_2";
    public final static String TRIGGER_1 = "trigger_1";
    public final static String TRIGGER_2 = "trigger_2";
}
