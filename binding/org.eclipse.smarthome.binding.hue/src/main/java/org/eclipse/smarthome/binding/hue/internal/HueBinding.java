/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Lists;


public class HueBinding {

    public static final String BINDING_ID = "hue";

    public final static ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "bridge");

    public final static ThingTypeUID LIGHT_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "light");

    public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(
            LIGHT_THING_TYPE_UID, BRIDGE_THING_TYPE_UID);


}
