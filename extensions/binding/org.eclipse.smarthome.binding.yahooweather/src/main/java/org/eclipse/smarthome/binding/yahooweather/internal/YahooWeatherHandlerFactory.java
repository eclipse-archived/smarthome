/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.yahooweather.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.binding.yahooweather.YahooWeatherBindingConstants;
import org.eclipse.smarthome.binding.yahooweather.handler.YahooWeatherHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link YahooWeatherHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class YahooWeatherHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(YahooWeatherBindingConstants.THING_TYPE_WEATHER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(YahooWeatherBindingConstants.THING_TYPE_WEATHER)) {
            return new YahooWeatherHandler(thing);
        }

        return null;
    }
}
