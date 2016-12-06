/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.internal;

import static org.eclipse.smarthome.binding.dmx.DmxBindingConstants.*;

import org.eclipse.smarthome.binding.dmx.DmxBindingConstants;
import org.eclipse.smarthome.binding.dmx.handler.ArtnetBridgeHandler;
import org.eclipse.smarthome.binding.dmx.handler.ChaserThingHandler;
import org.eclipse.smarthome.binding.dmx.handler.DimmerThingHandler;
import org.eclipse.smarthome.binding.dmx.handler.Lib485BridgeHandler;
import org.eclipse.smarthome.binding.dmx.handler.SacnBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * The {@link DmxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, name = "binding.dmx", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class DmxHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return DmxBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_ARTNET_BRIDGE)) {
            ArtnetBridgeHandler handler = new ArtnetBridgeHandler((Bridge) thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_LIB485_BRIDGE)) {
            Lib485BridgeHandler handler = new Lib485BridgeHandler((Bridge) thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_SACN_BRIDGE)) {
            SacnBridgeHandler handler = new SacnBridgeHandler((Bridge) thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_DIMMER)) {
            DimmerThingHandler handler = new DimmerThingHandler(thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_CHASER)) {
            ChaserThingHandler handler = new ChaserThingHandler(thing);
            return handler;
        }
        return null;
    }
}
