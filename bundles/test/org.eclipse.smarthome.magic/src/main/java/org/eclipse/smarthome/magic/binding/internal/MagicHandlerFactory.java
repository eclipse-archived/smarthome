/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.magic.binding.internal;

import static org.eclipse.smarthome.magic.binding.MagicBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.magic.binding.handler.MagicColorLightHandler;
import org.eclipse.smarthome.magic.binding.handler.MagicConfigurableThingHandler;
import org.eclipse.smarthome.magic.binding.handler.MagicContactHandler;
import org.eclipse.smarthome.magic.binding.handler.MagicDimmableLightHandler;
import org.eclipse.smarthome.magic.binding.handler.MagicExtensibleThingHandler;
import org.eclipse.smarthome.magic.binding.handler.MagicOnOffLightHandler;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.Sets;

/**
 * The {@link MagicHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Henning Treu - Initial contribution
 */
@Component(immediate = true, service = ThingHandlerFactory.class, configurationPid = "binding.magic")
public class MagicHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_EXTENSIBLE_THING,
            THING_TYPE_ON_OFF_LIGHT, THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_COLOR_LIGHT, THING_TYPE_CONTACT_SENSOR,
            THING_TYPE_CONFIG_THING);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_EXTENSIBLE_THING)) {
            return new MagicExtensibleThingHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_ON_OFF_LIGHT)) {
            return new MagicOnOffLightHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_DIMMABLE_LIGHT)) {
            return new MagicDimmableLightHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_COLOR_LIGHT)) {
            return new MagicColorLightHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_CONTACT_SENSOR)) {
            return new MagicContactHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_CONFIG_THING)) {
            return new MagicConfigurableThingHandler(thing);
        }

        return null;
    }
}
