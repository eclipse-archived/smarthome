/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.internal;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.binding.ble.BleBindingConstants;
import org.eclipse.smarthome.binding.ble.handler.BleGenericHandler;
import org.eclipse.smarthome.binding.ble.handler.ParrotFlowerPowerThingHandler;
import org.eclipse.smarthome.binding.ble.handler.WiTEnergyThingHandler;
import org.eclipse.smarthome.binding.ble.handler.YeelightBlueThingHandler;
import org.eclipse.smarthome.binding.ble.internal.discovery.BleDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BleHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial Contribution
 */
public class BleHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(BleHandlerFactory.class);

    Map<ThingTypeUID, Class<? extends BaseThingHandler>> thingHandlers = new HashMap<ThingTypeUID, Class<? extends BaseThingHandler>>();

    private BleDiscoveryService adapterDiscoveryService;
    private ServiceRegistration<?> serviceReg;

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        thingHandlers.put(BleBindingConstants.THING_TYPE_GENERIC, BleGenericHandler.class);
        thingHandlers.put(BleBindingConstants.THING_TYPE_PARROT_FLOWERPOWER, ParrotFlowerPowerThingHandler.class);
        thingHandlers.put(BleBindingConstants.THING_TYPE_WIT_ENERGY, WiTEnergyThingHandler.class);
        thingHandlers.put(BleBindingConstants.THING_TYPE_YEELIGHT_BLUE, YeelightBlueThingHandler.class);

        // Start the discovery service
        adapterDiscoveryService = new BleDiscoveryService();
        // adapterDiscoveryService.activate();

        // And register it as an OSGi service
        serviceReg = bundleContext.registerService(DiscoveryService.class.getName(), adapterDiscoveryService,
                new Hashtable<String, Object>());
    }

    @Override
    public void deactivate(ComponentContext componentContext) {
        super.activate(componentContext);

        // Remove the discovery service
        // adapterDiscoveryService.();
        serviceReg.unregister();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingHandlers.containsKey(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        Class<? extends BaseThingHandler> handlerClass;
        handlerClass = thingHandlers.get(thingTypeUID);
        if (handlerClass != null) {
            Constructor<? extends BaseThingHandler> constructor;
            try {
                constructor = handlerClass.getConstructor(Thing.class);
                return constructor.newInstance(thing);
            } catch (Exception e) {
                logger.error("Command processor error: {}", e);
            }
        }

        return null;
    }
}
