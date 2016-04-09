/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.raspbee.internal;

import static org.eclipse.smarthome.binding.raspbee.RaspBeeBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.raspbee.handler.RaspBeeBridgeHandler;
import org.eclipse.smarthome.binding.raspbee.handler.RaspBeeLightHandler;
import org.eclipse.smarthome.binding.raspbee.internal.discovery.RaspBeeLightDiscoveryService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Sets;

/**
 * {@link RaspBeeThingHandlerFactory} is a factory for {@link HueBridgeHandler}s.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Kai Kreuzer - added supportsThingType method
 * @author Andre Fuechsel - implemented to use one discovery service per bridge
 * @author Fatih Boy - modified for raspbee
 *
 */
public class RaspBeeThingHandlerFactory extends BaseThingHandlerFactory {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(RaspBeeBridgeHandler.SUPPORTED_THING_TYPES,
            RaspBeeLightHandler.SUPPORTED_THING_TYPES);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (RaspBeeBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID hueBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, hueBridgeUID, null);
        }
        if (RaspBeeLightHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID hueLightUID = getLightUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, hueLightUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the hue binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String serialNumber = (String) configuration.get(SERIAL_NUMBER);
            thingUID = new ThingUID(thingTypeUID, serialNumber);
        }
        return thingUID;
    }

    private ThingUID getLightUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        String lightId = (String) configuration.get(LIGHT_ID);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, lightId, bridgeUID.getId());
        }
        return thingUID;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (RaspBeeBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            RaspBeeBridgeHandler handler = new RaspBeeBridgeHandler((Bridge) thing);
            registerLightDiscoveryService(handler);
            return handler;
        } else if (RaspBeeLightHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new RaspBeeLightHandler(thing);
        } else {
            return null;
        }
    }

    private synchronized void registerLightDiscoveryService(RaspBeeBridgeHandler bridgeHandler) {
        RaspBeeLightDiscoveryService discoveryService = new RaspBeeLightDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof RaspBeeBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                RaspBeeLightDiscoveryService service = (RaspBeeLightDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                service.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
