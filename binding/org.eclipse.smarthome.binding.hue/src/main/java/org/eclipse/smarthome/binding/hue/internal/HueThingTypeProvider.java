/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeChangeListener;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;

import com.google.common.collect.Lists;

/**
 * 
 * {@link HueThingTypeProvider} provides a list of supported thing types by this
 * binding. In the future the supported thing types can specified via xml.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public class HueThingTypeProvider implements ThingTypeProvider {

    private static final String MANUFACTURER = "Philips";

    public final static ChannelType COLOR_CHANNEL_TYPE = new ChannelType(new ChannelTypeUID(
            "hue:color"), "Color", "Color", "Color Channel", "?");

    public final static ChannelDefinition COLOR_CHANNEL_DEFINITION = new ChannelDefinition("color",
            COLOR_CHANNEL_TYPE);

	public final static ChannelType COLOR_TEMPERATURE_CHANNEL_TYPE = new ChannelType(
			new ChannelTypeUID("hue:color_temperature"), "Dimmer",
			"Color Temperature", "Color Temperature Channel", "?");

	public final static ChannelDefinition COLOR_TEMPERATURE_CHANNEL_DEFINITION = new ChannelDefinition(
			"color_temperature", COLOR_TEMPERATURE_CHANNEL_TYPE);

	public final static BridgeType BRIDGE_THING_TYPE = new BridgeType(
			HueBindingInfo.BINDING_ID, "bridge", "hue Bridge",
			"The hue Bridge", MANUFACTURER);

	public final static ThingType LIGHT_THING_TYPE = new ThingType(
			new ThingTypeUID(HueBindingInfo.BINDING_ID, "light"),
			Lists.newArrayList("hue:bridge"), "hue Light", "The hue Light",
			MANUFACTURER, Lists.newArrayList(COLOR_CHANNEL_DEFINITION,
					COLOR_TEMPERATURE_CHANNEL_DEFINITION), "?");

    public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(
            BRIDGE_THING_TYPE.getUID(), LIGHT_THING_TYPE.getUID());

    @Override
    public Collection<ThingType> getThingTypes() {
        return Lists.newArrayList(BRIDGE_THING_TYPE, LIGHT_THING_TYPE);
    }

    @Override
    public void addThingTypeChangeListener(ThingTypeChangeListener listener) {
        // collection of thing types is static
    }

    @Override
    public void removeThingTypeChangeListener(ThingTypeChangeListener listener) {
        // collection of thing types is static
    }

}
