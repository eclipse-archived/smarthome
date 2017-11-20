/**
 * Copyright (c) 2016 Microchip Technology Inc. and its subsidiaries and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.binding.coapconnectedlighting.internal;

import static org.eclipse.smarthome.binding.coapconnectedlighting.CoAPConnectedLightingBindingConstants.THING_TYPE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.binding.coapconnectedlighting.handler.CoAPConnectedLightingHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link CoAPConnectedLightingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Microchip Technology - Initial contribution
 */
public class CoAPConnectedLightingHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE)) {
            return new CoAPConnectedLightingHandler(thing);
        }

        return null;
    }
}
