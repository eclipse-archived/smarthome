/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.hue.internal;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.LIGHT_ID;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.handler.HueLightHandler;
import org.eclipse.smarthome.binding.hue.internal.discovery.HueLightDiscoveryService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.ServiceRegistration;

/**
 * {@link HueThingHandlerFactory} is a factory for {@link HueBridgeHandler}s.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Kai Kreuzer - added supportsThingType method
 * @author Andre Fuechsel - implemented to use one discovery service per bridge
 *
 */
@NonNullByDefault
public class HueThingHandlerFactory extends BaseThingHandlerFactory {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .concat(HueBridgeHandler.SUPPORTED_THING_TYPES.stream(), HueLightHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (HueBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        }
        if (HueLightHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID hueLightUID = getLightUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, hueLightUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the hue binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    private ThingUID getLightUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID, Configuration configuration,
            @Nullable ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        } else {
            String lightId = (String) configuration.get(LIGHT_ID);
            if (bridgeUID != null) {
                return new ThingUID(thingTypeUID, lightId, bridgeUID.getId());
            } else {
                return new ThingUID(thingTypeUID, lightId, (String[]) null);
            }
        }
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (HueBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            HueBridgeHandler handler = new HueBridgeHandler((Bridge) thing);
            registerLightDiscoveryService(handler);
            return handler;
        } else if (HueLightHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new HueLightHandler(thing);
        } else {
            return null;
        }
    }

    private synchronized void registerLightDiscoveryService(HueBridgeHandler bridgeHandler) {
        HueLightDiscoveryService discoveryService = new HueLightDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HueBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                HueLightDiscoveryService service = (HueLightDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
