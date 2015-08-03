/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.test.hue;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Kaufmann - Initial contribution and API
 */
public class DumbThingTypeProvider implements ThingTypeProvider {

    private final Logger logger = LoggerFactory.getLogger(DumbThingTypeProvider.class);
    private static final Map<ThingTypeUID, ThingType> thingTypes = new HashMap<ThingTypeUID, ThingType>();

    public DumbThingTypeProvider() {
        logger.debug("DumbThingTypeProvider created");
        try {
            thingTypes.put(DumbThingHandlerFactory.THING_TYPE_TEST,
                    new ThingType(DumbThingHandlerFactory.THING_TYPE_TEST, null, "DUMB", "Funky Thing", null, null,
                            null, new URI("dumb", "DUMB", null)));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingTypeProvider#getThingTypes
     * (java.util.Locale)
     */
    @Override
    public Collection<ThingType> getThingTypes(Locale locale) {
        return thingTypes.values();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingTypeProvider#getThingType
     * (org.eclipse.smarthome.core.thing.ThingTypeUID, java.util.Locale)
     */
    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        return thingTypes.get(thingTypeUID);
    }

}
