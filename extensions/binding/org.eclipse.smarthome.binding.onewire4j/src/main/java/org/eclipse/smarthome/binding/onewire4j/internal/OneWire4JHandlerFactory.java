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
package org.eclipse.smarthome.binding.onewire4j.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.onewire4j.OneWire4JBindingConstants;
import org.eclipse.smarthome.binding.onewire4j.handler.SpswBridgeHandler;
import org.eclipse.smarthome.binding.onewire4j.handler.TemperatureHandler;
import org.eclipse.smarthome.binding.onewire4j.handler.UnknownDeviceHandler;
import org.eclipse.smarthome.binding.onewire4j.internal.discovery.OneWire4JDiscoveryService;
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

import com.google.common.collect.ImmutableSet;

import de.ibapl.spsw.api.SerialPortSocketFactory;

/**
 * The {@link OneWire4JHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author aploese@gmx.de - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.onewire4j")
public class OneWire4JHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(
            OneWire4JBindingConstants.THING_TYPE_ONEWIRE_TEMPERATURE,
            OneWire4JBindingConstants.BRIDGE_TYPE_ONEWIRE_RS232);

    // @Reference
    private final SerialPortSocketFactory serialPortSocketFactory = de.ibapl.spsw.provider.SerialPortSocketFactoryImpl
            .singleton();

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(OneWire4JBindingConstants.THING_TYPE_ONEWIRE_TEMPERATURE)) {
            return new TemperatureHandler(thing);
        } else if (thingTypeUID.equals(OneWire4JBindingConstants.BRIDGE_TYPE_ONEWIRE_RS232)) {
            final SpswBridgeHandler spswBridgeHandler = new SpswBridgeHandler((Bridge) thing, serialPortSocketFactory);
            registerDiscoveryService(spswBridgeHandler);
            return spswBridgeHandler;
        } else if (thingTypeUID.equals(OneWire4JBindingConstants.THING_TYPE_ONEWIRE_UNKNOWN)) {
            return new UnknownDeviceHandler(thing);
        } else {
            return null;
        }
    }

    private synchronized void registerDiscoveryService(@NonNull SpswBridgeHandler spswBridgeHandler) {
        OneWire4JDiscoveryService discoveryService = new OneWire4JDiscoveryService(spswBridgeHandler);
        this.discoveryServiceRegs.put(spswBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SpswBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                OneWire4JDiscoveryService service = (OneWire4JDiscoveryService) bundleContext
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
