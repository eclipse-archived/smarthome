/**
 *
 *  Copyright (c) 2016 by the respective copyright holders.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  @author Pascal Larin
 *  https://github.com/chaton78
 *
*/

package org.eclipse.smarthome.binding.sinope.internal;

import java.util.Set;

import org.eclipse.smarthome.binding.sinope.SinopeBindingConstants;
import org.eclipse.smarthome.binding.sinope.handler.SinopeGatewayHandler;
import org.eclipse.smarthome.binding.sinope.handler.SinopeThermostatHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * {@link HueHandlerFactory} is a factory for {@link SinopeThermostatHandler}s and {@link SinopeGatewayHandler}s
 *
 * @author Pascal Larin - Initial contribution
 */

public class SinopeHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = SinopeBindingConstants.SUPPORTED_THING_TYPES_UIDS;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SinopeBindingConstants.THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            return new SinopeGatewayHandler(thing);
        } else if (SinopeBindingConstants.THING_TYPE_THERMO.equals(thingTypeUID)) {
            return new SinopeThermostatHandler(thing);
        }

        return null;
    }

}
