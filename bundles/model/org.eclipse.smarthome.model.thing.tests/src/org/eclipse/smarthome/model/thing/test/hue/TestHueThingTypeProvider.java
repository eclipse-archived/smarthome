/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
 *
 */
package org.eclipse.smarthome.model.thing.test.hue;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author Benedikt Niehues - Fix ESH Bug 450236
 *         https://bugs.eclipse.org/bugs/show_bug.cgi?id=450236 - Considering
 *         ThingType Description
 *
 */
public class TestHueThingTypeProvider implements ThingTypeProvider {

    private final Logger logger = LoggerFactory.getLogger(TestHueThingTypeProvider.class);
    private static final Map<ThingTypeUID, ThingType> thingTypes = new HashMap<ThingTypeUID, ThingType>();

    public TestHueThingTypeProvider() {
        logger.debug("TestHueThingTypeProvider created");
        try {
            thingTypes.put(TestHueThingHandlerFactory.THING_TYPE_BRIDGE,
                    ThingTypeBuilder.instance(TestHueThingHandlerFactory.THING_TYPE_BRIDGE, "HueBridge")
                            .withDescription("HueBridge").isListed(false).buildBridge());

            ChannelDefinition color = new ChannelDefinition("color", TestHueChannelTypeProvider.COLOR_CHANNEL_TYPE_UID);

            ChannelDefinition colorTemp = new ChannelDefinition("color_temperature",
                    TestHueChannelTypeProvider.COLOR_TEMP_CHANNEL_TYPE_UID);
            thingTypes.put(TestHueThingHandlerFactory.THING_TYPE_LCT001,
                    ThingTypeBuilder.instance(TestHueThingHandlerFactory.THING_TYPE_LCT001, "LCT001")
                            .withSupportedBridgeTypeUIDs(
                                    Lists.newArrayList(TestHueThingHandlerFactory.THING_TYPE_BRIDGE.toString()))
                            .withDescription("Hue LAMP").isListed(false)
                            .withChannelDefinitions(Lists.newArrayList(color, colorTemp))
                            .withConfigDescriptionURI(new URI("hue", "LCT001", null)).build());

            thingTypes.put(TestHueThingHandlerFactoryX.THING_TYPE_BRIDGE,
                    ThingTypeBuilder.instance(TestHueThingHandlerFactoryX.THING_TYPE_BRIDGE, "HueBridge")
                            .withDescription("HueBridge").isListed(false).buildBridge());

            ChannelDefinition colorX = new ChannelDefinition("Xcolor",
                    TestHueChannelTypeProvider.COLORX_CHANNEL_TYPE_UID);

            ChannelDefinition colorTempX = new ChannelDefinition("Xcolor_temperature",
                    TestHueChannelTypeProvider.COLORX_TEMP_CHANNEL_TYPE_UID);
            thingTypes.put(TestHueThingHandlerFactoryX.THING_TYPE_LCT001,
                    ThingTypeBuilder.instance(TestHueThingHandlerFactoryX.THING_TYPE_LCT001, "XLCT001")
                            .withSupportedBridgeTypeUIDs(
                                    Lists.newArrayList(TestHueThingHandlerFactoryX.THING_TYPE_BRIDGE.toString()))
                            .withDescription("Hue LAMP").isListed(false)
                            .withChannelDefinitions(Lists.newArrayList(colorX, colorTempX))
                            .withConfigDescriptionURI(new URI("Xhue", "XLCT001", null)).build());

            ChannelGroupDefinition groupDefinition = new ChannelGroupDefinition("group",
                    TestHueChannelTypeProvider.GROUP_CHANNEL_GROUP_TYPE_UID);
            thingTypes.put(TestHueThingHandlerFactory.THING_TYPE_GROUPED,
                    ThingTypeBuilder.instance(TestHueThingHandlerFactory.THING_TYPE_GROUPED, "grouped")
                            .withSupportedBridgeTypeUIDs(
                                    Lists.newArrayList(TestHueThingHandlerFactory.THING_TYPE_BRIDGE.toString()))
                            .withDescription("Grouped Lamp")
                            .withChannelGroupDefinitions(Lists.newArrayList(groupDefinition))
                            .withConfigDescriptionURI(new URI("hue", "grouped", null)).build());

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
