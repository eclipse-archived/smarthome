/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.yeelightblue.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.binding.ble.yeelightblue.YeeLightBlueBindingConstants;
import org.eclipse.smarthome.binding.ble.yeelightblue.handler.YeeLightBlueHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link Ble.YeeLightBlueHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 */
public class YeeLightBlueHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(YeeLightBlueBindingConstants.THING_TYPE_BLUE2);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean x = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(YeeLightBlueBindingConstants.THING_TYPE_BLUE2)) {
            return new YeeLightBlueHandler(thing);
        }

        return null;
    }
}
