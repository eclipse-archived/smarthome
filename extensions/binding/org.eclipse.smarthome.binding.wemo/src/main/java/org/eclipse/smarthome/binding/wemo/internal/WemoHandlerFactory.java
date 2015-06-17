/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.internal;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.UDN;

import java.util.Set;

import org.eclipse.smarthome.binding.wemo.handler.WemoHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link WemoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Kai Kreuzer - some refactoring for performance and simplification
 */
public class WemoHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(WemoHandlerFactory.class);


    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(WemoHandler.SUPPORTED_THING_TYPES);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (WemoHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            logger.debug("Creating a WemoHandler for thing '{}' with UDN '{}'", thing.getUID(), thing
                    .getConfiguration().get(UDN));
            return new WemoHandler(thing);
        }

        return null;
    }


}
