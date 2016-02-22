/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import java.util.ArrayList;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.internal.BridgeImpl;

public class BridgeBuilder extends GenericBridgeBuilder<BridgeBuilder> {

    private BridgeBuilder(BridgeImpl thing) {
        super(thing);
    }

    public static BridgeBuilder create(ThingTypeUID thingTypeUID, String bridgeId) {
        BridgeImpl bridge = new BridgeImpl(thingTypeUID, bridgeId);
        bridge.setChannels(new ArrayList<Channel>());
        return new BridgeBuilder(bridge);
    }

    @Deprecated
    public static BridgeBuilder create(ThingUID thingUID) {
        BridgeImpl bridge = new BridgeImpl(thingUID);
        return new BridgeBuilder(bridge);
    }

    public static BridgeBuilder create(ThingTypeUID thingTypeUID, ThingUID thingUID) {
        BridgeImpl bridge = new BridgeImpl(thingTypeUID, thingUID);
        return new BridgeBuilder(bridge);
    }

}
