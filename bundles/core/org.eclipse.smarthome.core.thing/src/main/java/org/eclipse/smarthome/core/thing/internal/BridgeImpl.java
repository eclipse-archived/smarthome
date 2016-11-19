/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class BridgeImpl extends ThingImpl implements Bridge {

    private transient List<Thing> things = new CopyOnWriteArrayList<>();
    private transient Logger logger = LoggerFactory.getLogger(BridgeImpl.class);

    /**
     * Package protected default constructor to allow reflective instantiation.
     */
    BridgeImpl() {
    }

    public BridgeImpl(ThingTypeUID thingTypeUID, String bridgeId) {
        super(thingTypeUID, bridgeId);
    }

    /**
     * @param thingUID
     * @throws IllegalArgumentException
     * @deprecated use {@link #BridgeImpl(ThingTypeUID, ThingUID)} instead.
     */
    @Deprecated
    public BridgeImpl(ThingUID thingUID) throws IllegalArgumentException {
        super(thingUID);
    }

    /**
     * @param thingTypeUID
     * @param thingUID
     * @throws IllegalArgumentException
     */
    public BridgeImpl(ThingTypeUID thingTypeUID, ThingUID thingUID) throws IllegalArgumentException {
        super(thingTypeUID, thingUID);
    }

    public void addThing(Thing thing) {
        things.add(thing);
    }

    public void removeThing(Thing thing) {
        things.remove(thing);
    }

    @Override
    public List<Thing> getThings() {
        return ImmutableList.copyOf(things);
    }

    @Override
    public BridgeHandler getHandler() {
        BridgeHandler bridgeHandler = null;
        ThingHandler thingHandler = super.getHandler();
        if (thingHandler instanceof BridgeHandler) {
            bridgeHandler = (BridgeHandler) thingHandler;
        } else if (thingHandler != null) {
            logger.warn("Handler of bridge '{}' must implement BridgeHandler interface.", getUID());
        }
        return bridgeHandler;
    }

}
