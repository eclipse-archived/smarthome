/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.yeelight.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.yeelight.YeelightBindingConstants;
import org.eclipse.smarthome.binding.yeelight.handler.YeelightThingHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YeelightHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial Contribution
 */
public class YeelightHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(YeelightHandlerFactory.class);

    Map<ThingTypeUID, Class<? extends BaseThingHandler>> thingHandlers = new HashMap<ThingTypeUID, Class<? extends BaseThingHandler>>();

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        thingHandlers.put(YeelightBindingConstants.THING_TYPE_YEELIGHT_BLUE, YeelightThingHandler.class);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingHandlers.containsKey(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (YeelightBindingConstants.THING_TYPE_YEELIGHT_BLUE.equals(thing.getThingTypeUID())) {
            return new YeelightThingHandler(thing);
        }

        return null;
    }
}
