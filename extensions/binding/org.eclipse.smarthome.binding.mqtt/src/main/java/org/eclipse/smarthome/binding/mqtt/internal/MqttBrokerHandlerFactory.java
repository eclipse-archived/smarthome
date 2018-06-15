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
package org.eclipse.smarthome.binding.mqtt.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.binding.mqtt.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.handler.BrokerHandler;
import org.eclipse.smarthome.binding.mqtt.handler.SystemBrokerHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MqttBrokerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = ThingHandlerFactory.class, configurationPid = "MqttBrokerHandlerFactory")
public class MqttBrokerHandlerFactory extends BaseThingHandlerFactory {
    private MqttService mqttService;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER, MqttBindingConstants.BRIDGE_TYPE_BROKER)
            .collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Reference
    public void setMqttService(MqttService service) {
        mqttService = service;
    }

    public void unsetMqttService(MqttService service) {
        mqttService = null;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (mqttService == null) {
            throw new IllegalStateException("MqttService must be bound, before ThingHandlers can be created");
        }
        if (!(thing instanceof Bridge)) {
            throw new IllegalStateException("A bridge type is expected");
        }
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER)) {
            return new SystemBrokerHandler((Bridge) thing, mqttService);
        } else if (thingTypeUID.equals(MqttBindingConstants.BRIDGE_TYPE_BROKER)) {
            return new BrokerHandler((Bridge) thing);
        }

        throw new IllegalStateException("Not supported " + thingTypeUID.toString());

        // return null;
    }
}
