/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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

public class BridgeImpl extends ThingImpl implements Bridge {

    private List<Thing> things = new CopyOnWriteArrayList<>();

    public BridgeImpl(ThingTypeUID thingTypeUID, String bridgeId) {
        super(thingTypeUID, bridgeId);
    }

    public void addThing(Thing thing) {
        things.add(thing);
        if (thing.getBridge() == null || !thing.getBridge().getUID().equals(this.getUID())) {
            thing.setBridge(this);
        }
    }

    public void removeThing(Thing thing) {
        things.remove(thing);
        if (thing.getBridge() != null) {
            thing.setBridge(null);
        }
    }

    @Override
    public List<Thing> getThings() {
        return things;
    }

    @Override
    public void setStatus(ThingStatus status) {
        super.setStatus(status);
        for (Thing thing : this.things) {
            thing.setStatus(status);
        }
    }

}
