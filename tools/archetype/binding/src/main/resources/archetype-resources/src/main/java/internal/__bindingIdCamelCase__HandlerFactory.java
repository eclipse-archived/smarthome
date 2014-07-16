/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ${package}.internal;

import static ${package}.${bindingIdCamelCase}BindingConstants.*;

import java.util.Collection;
import ${package}.handler.${bindingIdCamelCase}Handler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import com.google.common.collect.Lists;

/**
 * The {@link ${bindingIdCamelCase}HandlerFactory} is responsible for creating things and thing 
 * handlers.
 * 
 * @author ${author} - Initial contribution
 */
public class ${bindingIdCamelCase}HandlerFactory extends BaseThingHandlerFactory {
    
    private final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(THING_TYPE_SAMPLE);
    
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SAMPLE)) {
            return new ${bindingIdCamelCase}Handler(thing);
        }

        return null;
    }
}

