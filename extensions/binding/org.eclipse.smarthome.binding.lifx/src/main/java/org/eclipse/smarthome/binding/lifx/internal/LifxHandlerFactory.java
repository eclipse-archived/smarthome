/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.*;

import java.util.Collection;

import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.service.component.ComponentContext;

import com.google.common.collect.Lists;

/**
 * The {@link LifxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Karel Goderis - Remove dependency on external libraries
 */
public class LifxHandlerFactory extends BaseThingHandlerFactory {

    public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(THING_TYPE_COLORLIGHT,
            THING_TYPE_WHITELIGHT);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_COLORLIGHT) || thingTypeUID.equals(THING_TYPE_WHITELIGHT)) {
            return new LifxLightHandler(thing);
        }

        return null;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }
}
