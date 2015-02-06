/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.List;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * {@link BaseBridgeHandler} adds some convenience methods for bridges to the {@link BaseThingHandler}.
 *
 * @author Dennis Nobel - Initial contribution
 */
public abstract class BaseBridgeHandler extends BaseThingHandler {

    /**
     * @see BaseThingHandler
     */
    public BaseBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Finds and returns a child thing for a given UID of this bridge.
     *
     * @param uid
     *            uid of the child thing
     * @return child thing with the given uid or null if thing was not found
     */
    public Thing getThingByUID(ThingUID uid) {

        Bridge bridge = getThing();

        List<Thing> things = bridge.getThings();

        for (Thing thing : things) {
            if (thing.getUID().equals(uid)) {
                return thing;
            }
        }

        return null;
    }

    @Override
    public Bridge getThing() {
        return (Bridge) super.getThing();
    }
}