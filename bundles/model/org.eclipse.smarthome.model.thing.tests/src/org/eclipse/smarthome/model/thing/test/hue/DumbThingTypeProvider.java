/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.test.hue;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
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
            ChannelDefinition channel1 = new ChannelDefinition("channel1",
                    new ChannelTypeUID(DumbThingHandlerFactory.BINDING_ID, "channel1"));
            List<ChannelDefinition> channelDefinitions = Collections.singletonList(channel1);

            thingTypes.put(DumbThingHandlerFactory.THING_TYPE_TEST,
                    ThingTypeBuilder.instance(DumbThingHandlerFactory.THING_TYPE_TEST, "DUMB").withDescription("Funky Thing")
                            .isListed(false).withChannelDefinitions(channelDefinitions)
                            .withConfigDescriptionURI(new URI("dumb:DUMB")).build());
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
        }
    }

    @Override
    public Collection<ThingType> getThingTypes(Locale locale) {
        return thingTypes.values();
    }

    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        return thingTypes.get(thingTypeUID);
    }

}
