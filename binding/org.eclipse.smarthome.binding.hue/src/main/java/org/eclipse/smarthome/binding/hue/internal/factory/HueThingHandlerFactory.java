/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.factory;

import java.util.Collection;
import java.util.Hashtable;

import org.eclipse.smarthome.binding.hue.HueBindingConstants;
import org.eclipse.smarthome.binding.hue.config.HueBridgeConfiguration;
import org.eclipse.smarthome.binding.hue.config.HueLightConfiguration;
import org.eclipse.smarthome.binding.hue.internal.discovery.HueLightDiscoveryService;
import org.eclipse.smarthome.binding.hue.internal.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.internal.handler.HueLightHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Lists;

/**
 * {@link HueThingHandlerFactory} is a factory for {@link HueBridgeHandler}s.
 * 
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Kai Kreuzer - added supportsThing method
 */
public class HueThingHandlerFactory extends BaseThingHandlerFactory {

    public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(
            HueBindingConstants.THING_TYPE_LCT001, HueBindingConstants.THING_TYPE_BRIDGE);

    private ServiceRegistration<?> discoveryServiceReg;
    
    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            ThingUID thingUID, ThingUID bridgeUID) {
        if (HueBindingConstants.THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            ThingUID hueBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, hueBridgeUID, null);
        }
        if (HueBindingConstants.THING_TYPE_LCT001.equals(thingTypeUID)) {
            ThingUID hueLightUID = getLightUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, hueLightUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID
                + " is not supported by the hue binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String serialNumber = (String) configuration.get(HueBridgeConfiguration.SERIAL_NUMBER);
            thingUID = new ThingUID(thingTypeUID, serialNumber);
        }
        return thingUID;
    }

    private ThingUID getLightUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
            Configuration configuration, ThingUID bridgeUID) {
        String lightId = (String) configuration.get(HueLightConfiguration.LIGHT_ID);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, "Light" + lightId, bridgeUID.getId());
        }
        return thingUID;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(HueBindingConstants.THING_TYPE_BRIDGE)) {
            HueBridgeHandler handler = new HueBridgeHandler((Bridge) thing);
            registerLightDiscoveryService(handler);
            return handler;
        } else if (thing.getThingTypeUID().equals(HueBindingConstants.THING_TYPE_LCT001)) {
            return new HueLightHandler(thing);
        } else {
            return null;
        }
    }
    
    private void registerLightDiscoveryService(HueBridgeHandler bridgeHandler) {
    	HueLightDiscoveryService discoveryService = new HueLightDiscoveryService(bridgeHandler);
    	discoveryService.activate();
    	this.discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>());
    }
    
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
    	if(this.discoveryServiceReg!=null) {
    		HueLightDiscoveryService service = (HueLightDiscoveryService) bundleContext.getService(discoveryServiceReg.getReference());
    		service.deactivate();
    		discoveryServiceReg.unregister();
    		discoveryServiceReg = null;
    	}
    }
}
