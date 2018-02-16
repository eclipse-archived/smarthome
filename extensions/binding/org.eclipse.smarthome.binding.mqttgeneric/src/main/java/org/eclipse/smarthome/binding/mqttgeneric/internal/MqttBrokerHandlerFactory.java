/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.mqttgeneric.internal;

import java.util.Set;

import org.eclipse.smarthome.binding.mqttgeneric.MqttBrokerBindingConstants;
import org.eclipse.smarthome.binding.mqttgeneric.handler.MqttBrokerConnectionHandler;
import org.eclipse.smarthome.binding.mqttgeneric.handler.MqttThingHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import com.google.common.collect.Sets;

/**
 * The {@link MqttBrokerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, name = "MqttBrokerHandlerFactory")
public class MqttBrokerHandlerFactory extends BaseThingHandlerFactory {
    private MqttService mqttService;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .newHashSet(MqttBrokerBindingConstants.BRIDGE_TYPE_CONNECTION, MqttBrokerBindingConstants.THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Activate
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Deactivate
    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
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
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MqttBrokerBindingConstants.BRIDGE_TYPE_CONNECTION)) {
            return new MqttBrokerConnectionHandler((Bridge) thing, mqttService);
        } else {
            return new MqttThingHandler(thing);
        }
    }
}
