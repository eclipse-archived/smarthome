/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.SUPPORTED_THING_TYPES;

import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.service.component.ComponentContext;

/**
 * The {@link LifxHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Karel Goderis - Remove dependency on external libraries
 */
public class LifxHandlerFactory extends BaseThingHandlerFactory {

    private LifxChannelFactory channelFactory;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (supportsThingType(thing.getThingTypeUID())) {
            return new LifxLightHandler(thing, channelFactory);
        }

        return null;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    protected void setChannelFactory(LifxChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    protected void unsetChannelFactory(LifxChannelFactory channelFactory) {
        this.channelFactory = null;
    }

}
