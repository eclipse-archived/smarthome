/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.tradfri.internal;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.binding.tradfri.handler.TradfriControllerHandler;
import org.eclipse.smarthome.binding.tradfri.handler.TradfriGatewayHandler;
import org.eclipse.smarthome.binding.tradfri.handler.TradfriLightHandler;
import org.eclipse.smarthome.binding.tradfri.handler.TradfriPlugHandler;
import org.eclipse.smarthome.binding.tradfri.handler.TradfriSensorHandler;
import org.eclipse.smarthome.binding.tradfri.internal.discovery.TradfriDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link TradfriHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Christoph Weitkamp - Added support for remote controller and motion sensor devices (read-only battery level)
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tradfri")
public class TradfriHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(Stream.of(GATEWAY_TYPE_UID), SUPPORTED_LIGHT_TYPES_UIDS.stream(),
                    SUPPORTED_CONTROLLER_TYPES_UIDS.stream(), SUPPORTED_PLUG_TYPES_UIDS.stream())
            .reduce(Stream::concat).orElseGet(Stream::empty).collect(Collectors.toSet());

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (GATEWAY_TYPE_UID.equals(thingTypeUID)) {
            TradfriGatewayHandler handler = new TradfriGatewayHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (THING_TYPE_DIMMER.equals(thingTypeUID) || THING_TYPE_REMOTE_CONTROL.equals(thingTypeUID)) {
            return new TradfriControllerHandler(thing);
        } else if (THING_TYPE_MOTION_SENSOR.equals(thingTypeUID)) {
            return new TradfriSensorHandler(thing);
        } else if (SUPPORTED_LIGHT_TYPES_UIDS.contains(thingTypeUID)) {
            return new TradfriLightHandler(thing);
        } else if (SUPPORTED_PLUG_TYPES_UIDS.contains(thingTypeUID)) {
            return new TradfriPlugHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof TradfriGatewayHandler) {
            unregisterDiscoveryService((TradfriGatewayHandler) thingHandler);
        }
    }

    private synchronized void registerDiscoveryService(TradfriGatewayHandler bridgeHandler) {
        TradfriDiscoveryService discoveryService = new TradfriDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), getBundleContext()
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void unregisterDiscoveryService(TradfriGatewayHandler bridgeHandler) {
        ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(bridgeHandler.getThing().getUID());
        if (serviceReg != null) {
            TradfriDiscoveryService service = (TradfriDiscoveryService) getBundleContext()
                    .getService(serviceReg.getReference());
            serviceReg.unregister();
            if (service != null) {
                service.deactivate();
            }
        }
    }
}
