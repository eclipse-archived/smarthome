/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.internal;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.UDN;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants;
import org.eclipse.smarthome.binding.wemo.discovery.WemoLinkDiscoveryService;
import org.eclipse.smarthome.binding.wemo.handler.WemoBridgeHandler;
import org.eclipse.smarthome.binding.wemo.handler.WemoHandler;
import org.eclipse.smarthome.binding.wemo.handler.WemoLightHandler;
import org.eclipse.smarthome.binding.wemo.handler.WemoMakerHandler;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link WemoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Kai Kreuzer - some refactoring for performance and simplification
 */
public class WemoHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(WemoHandlerFactory.class);

    private UpnpIOService upnpIOService;

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(WemoBindingConstants.SUPPORTED_THING_TYPES);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID != null) {

            logger.debug("Trying to create a handler for ThingType '{}", thingTypeUID);

            if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_BRIDGE)) {
                logger.debug("Creating a WemoBridgeHandler for thing '{}' with UDN '{}'", thing.getUID(),
                        thing.getConfiguration().get(UDN));
                WemoBridgeHandler handler = new WemoBridgeHandler((Bridge) thing);
                registerDeviceDiscoveryService(handler);
                return handler;
            } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_MAKER)) {
                logger.debug("Creating a WemoMakerHandler for thing '{}' with UDN '{}'", thing.getUID(),
                        thing.getConfiguration().get(UDN));
                return new WemoMakerHandler(thing, upnpIOService);
            } else if (WemoBindingConstants.SUPPORTED_DEVICE_THING_TYPES.contains(thing.getThingTypeUID())) {
                logger.debug("Creating a WemoHandler for thing '{}' with UDN '{}'", thing.getUID(),
                        thing.getConfiguration().get(UDN));
                return new WemoHandler(thing, upnpIOService);
            } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_MZ100)) {
                return new WemoLightHandler(thing, upnpIOService);
            } else {
                logger.warn("ThingHandler not found for {}", thingTypeUID);
                return null;
            }
        }
        return null;
    }

    protected void setUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = upnpIOService;
    }

    protected void unsetUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof WemoBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    private void registerDeviceDiscoveryService(WemoBridgeHandler wemoBridgeHandler) {
        WemoLinkDiscoveryService discoveryService = new WemoLinkDiscoveryService(wemoBridgeHandler, upnpIOService);
        this.discoveryServiceRegs.put(wemoBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

}
