/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.handler.MoonHandler;
import org.eclipse.smarthome.binding.astro.handler.SunHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link AstroHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true)
public class AstroHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .concat(SunHandler.SUPPORTED_THING_TYPES.stream(), MoonHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());
    private static final Map<String, AstroThingHandler> astroThingHandlers = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        AstroThingHandler thingHandler = null;

        if (thingTypeUID.equals(THING_TYPE_SUN)) {
            thingHandler = new SunHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_MOON)) {
            thingHandler = new MoonHandler(thing);
        }
        if (thingHandler != null) {
            astroThingHandlers.put(thing.getUID().toString(), thingHandler);
        }
        return thingHandler;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        astroThingHandlers.remove(thing.getUID().toString());
    }

    public static AstroThingHandler getHandler(String thingUid) {
        return astroThingHandlers.get(thingUid);
    }
}
