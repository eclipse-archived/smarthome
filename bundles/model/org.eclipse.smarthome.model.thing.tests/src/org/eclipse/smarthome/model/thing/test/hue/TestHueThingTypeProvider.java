/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
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
            thingTypes.put(TestHueThingHandlerFactory.THING_TYPE_BRIDGE, new BridgeType(
                    TestHueThingHandlerFactory.THING_TYPE_BRIDGE, null, "HueBridge", "HueBridge", null, null, null));
            ChannelType ctColor = new ChannelType(new ChannelTypeUID("hue:LCT001:color"), false, "Color", "colorLabel",
                    "description", null, null, null, new URI("hue", "LCT001:color", null));
            ChannelDefinition color = new ChannelDefinition("color", ctColor);
            ChannelType ctColorTemperature = new ChannelType(new ChannelTypeUID("hue:LCT001:color_temperature"), false,
                    "Dimmer", "colorTemperatureLabel", "description", null, null, null, new URI("hue",
                            "LCT001:color_temperature", null));
            ChannelDefinition colorTemp = new ChannelDefinition("color_temperature", ctColorTemperature);
            thingTypes.put(
                    TestHueThingHandlerFactory.THING_TYPE_LCT001,
                    new ThingType(TestHueThingHandlerFactory.THING_TYPE_LCT001, Lists
                            .newArrayList(TestHueThingHandlerFactory.THING_TYPE_BRIDGE.toString()), "LCT001",
                            "Hue LAMP", Lists.newArrayList(color, colorTemp), null, new URI("hue", "LCT001", null)));
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
