/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

import com.google.common.collect.ImmutableList;

public class BridgeImpl extends ThingImpl implements Bridge {

    private transient List<Thing> things = new CopyOnWriteArrayList<>();

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
     */
    public BridgeImpl(ThingUID thingUID) throws IllegalArgumentException {
        super(thingUID);
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
    public void setStatus(ThingStatus status) {
        super.setStatus(status);
        for (Thing thing : this.things) {
            thing.setStatus(status);
        }
    }

}
