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
package org.eclipse.smarthome.binding.mqtt.generic.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.MqttChannelStateDescriptionProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.MqttChannelTypeProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.TransformationServiceProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.GenericThingHandler;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.HomeAssistantThingHandler;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.HomieThingHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MqttThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = ThingHandlerFactory.class)
@NonNullByDefault
public class MqttThingHandlerFactory extends BaseThingHandlerFactory implements TransformationServiceProvider {
    private @NonNullByDefault({}) MqttChannelTypeProvider typeProvider;
    private @NonNullByDefault({}) MqttChannelStateDescriptionProvider stateDescriptionProvider;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(MqttBindingConstants.GENERIC_MQTT_THING, MqttBindingConstants.HOMIE300_MQTT_THING,
                    MqttBindingConstants.HOMEASSISTANT_MQTT_THING)
            .collect(Collectors.toSet());

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

    @Reference
    protected void setStateDescriptionProvider(MqttChannelStateDescriptionProvider stateDescription) {
        this.stateDescriptionProvider = stateDescription;
    }

    protected void unsetStateDescriptionProvider(MqttChannelStateDescriptionProvider stateDescription) {
        this.stateDescriptionProvider = null;
    }

    @Reference
    protected void setChannelProvider(MqttChannelTypeProvider provider) {
        this.typeProvider = provider;
    }

    protected void unsetChannelProvider(MqttChannelTypeProvider provider) {
        this.typeProvider = null;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MqttBindingConstants.GENERIC_MQTT_THING)) {
            return new GenericThingHandler(thing, stateDescriptionProvider, this, 1500);
        } else if (thingTypeUID.equals(MqttBindingConstants.HOMIE300_MQTT_THING)) {
            return new HomieThingHandler(thing, typeProvider, 1500, 200);
        } else if (thingTypeUID.equals(MqttBindingConstants.HOMEASSISTANT_MQTT_THING)) {
            return new HomeAssistantThingHandler(thing, typeProvider, 1500, 200);
        }
        return null;
    }

    @Override
    public @Nullable TransformationService getTransformationService(String type) {
        return TransformationHelper.getTransformationService(bundleContext, type);
    }

}
