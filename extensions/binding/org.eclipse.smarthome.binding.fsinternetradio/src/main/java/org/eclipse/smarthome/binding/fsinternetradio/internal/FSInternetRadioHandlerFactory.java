/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.fsinternetradio.internal;

import static org.eclipse.smarthome.binding.fsinternetradio.FSInternetRadioBindingConstants.THING_TYPE_RADIO;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.binding.fsinternetradio.handler.FSInternetRadioHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link FSInternetRadioHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrick Koenemann - Initial contribution
 */
public class FSInternetRadioHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_RADIO);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_RADIO)) {
            return new FSInternetRadioHandler(thing);
        }

        return null;
    }
}
