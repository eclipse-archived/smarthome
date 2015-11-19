/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.enocean.internal;

import static org.eclipse.smarthome.binding.enocean.EnOceanBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.binding.enocean.discovery.EnOceanDiscoveryService;
import org.eclipse.smarthome.binding.enocean.handler.EnOceanHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.service.enocean.EnOceanDevice;

import com.google.common.collect.Sets;

/**
 * The {@link EnOceanHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 *
 */
public class EnOceanHandlerFactory extends BaseThingHandlerFactory {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_ELTAKO_SMOKE_DETECTOR,
            THING_TYPE_ON_OFF_PLUG);

    private EnOceanDiscoveryService enoceanDiscoveryService;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ELTAKO_SMOKE_DETECTOR)) {
            EnOceanHandler handler = new EnOceanHandler(thing);
            EnOceanDevice device = enoceanDiscoveryService.getEnOceanDevice(thing.getUID());
            handler.setDevice(device);
            return handler;
        }

        return null;
    }

    public void setEnoceanDiscoveryService(EnOceanDiscoveryService enoceanDiscoveryService) {
        this.enoceanDiscoveryService = enoceanDiscoveryService;
    }

    public void unsetEnoceanDiscoveryService(EnOceanDiscoveryService enoceanDiscoveryService) {
        this.enoceanDiscoveryService = null;
    }
}
