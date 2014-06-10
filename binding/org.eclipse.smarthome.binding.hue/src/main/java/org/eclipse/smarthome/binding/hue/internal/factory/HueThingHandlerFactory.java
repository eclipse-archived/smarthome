/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.factory;

import org.eclipse.smarthome.binding.hue.config.HueBridgeConfiguration;
import org.eclipse.smarthome.binding.hue.config.HueLightConfiguration;
import org.eclipse.smarthome.binding.hue.internal.HueThingTypeProvider;
import org.eclipse.smarthome.binding.hue.internal.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.internal.handler.HueLightHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * {@link HueThingHandlerFactory} is a factory for {@link HueBridgeHandler}s.
 * 
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Kai Kreuzer - added supportsThing method
 */
public class HueThingHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public Thing createThing(ThingType thingType, Configuration configuration, ThingUID thingUID,
            Bridge bridge) {
        if (HueThingTypeProvider.BRIDGE_THING_TYPE.equals(thingType)) {
            ThingUID bridgeUID = getBridgeThingUID(thingType, thingUID, configuration);
            return ThingFactory.createThing(thingType, bridgeUID, configuration, bridge);
        }
        if (HueThingTypeProvider.LIGHT_THING_TYPE.equals(thingType)) {
            ThingUID lightUID = getLightUID(thingType, thingUID, configuration, bridge);
            return ThingFactory.createThing(thingType, lightUID, configuration, bridge);
        }
        throw new IllegalArgumentException("The thing type " + thingType
                + " is not supported by the hue binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return HueThingTypeProvider.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingType thingType, ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String serialNumber = (String) configuration
                    .get(HueBridgeConfiguration.BRIDGE_SERIAL_NUMBER);
            thingUID = new ThingUID(thingType.getUID(), serialNumber);
        }
        return thingUID;
    }

    private ThingUID getLightUID(ThingType thingType, ThingUID thingUID,
            Configuration configuration, Bridge bridge) {
        String lightId = (String) configuration.get(HueLightConfiguration.LIGHT_ID);

        if (thingUID == null) {
            thingUID = new ThingUID(thingType.getUID(), bridge.getUID() + "Light" + lightId);
        }
        return thingUID;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID())) {
            return new HueBridgeHandler((Bridge) thing);
        } else if (thing.getThingTypeUID().equals(HueThingTypeProvider.LIGHT_THING_TYPE.getUID())) {
            return new HueLightHandler(thing);
        } else {
            return null;
        }
    }

}
