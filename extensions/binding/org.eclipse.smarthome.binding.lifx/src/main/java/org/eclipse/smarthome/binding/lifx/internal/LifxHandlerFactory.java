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
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.SUPPORTED_THING_TYPES;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link LifxHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Karel Goderis - Remove dependency on external libraries
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.lifx")
public class LifxHandlerFactory extends BaseThingHandlerFactory {

    private @NonNullByDefault({}) LifxChannelFactory channelFactory;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (supportsThingType(thing.getThingTypeUID())) {
            return new LifxLightHandler(thing, channelFactory);
        }

        return null;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Reference
    protected void setChannelFactory(LifxChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    protected void unsetChannelFactory(LifxChannelFactory channelFactory) {
        this.channelFactory = null;
    }

}
