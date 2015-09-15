/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * Implementation providing default system wide channel types
 *
 * @author Ivan Iliev - Initial Contribution
 * @author Chris Jackson - Aded battery level
 * @author Dennis Nobel - Changed to {@link ChannelTypeProvider}
 *
 */
public class DefaultSystemChannelTypeProvider implements ChannelTypeProvider {

    /**
     * Signal strength default system wide {@link ChannelType}. Represents signal strength of a device as a number
     * with values 0, 1, 2, 3 or 4, 0 being worst strength and 4 being best strength.
     */
    public static final ChannelType SYSTEM_CHANNEL_SIGNAL_STRENGTH = new ChannelType(
            new ChannelTypeUID("system:signal-strength"), false, "Number", "Signal Strength", null, "QualityOfService",
            null, null, null);

    /**
     * Low battery default system wide {@link ChannelType}. Represents a low battery warning with possible values
     * on/off.
     */
    public static final ChannelType SYSTEM_CHANNEL_LOW_BATTERY = new ChannelType(
            new ChannelTypeUID("system:low-battery"), false, "Switch", "Low Battery", null, "Battery", null, null,
            null);

    /**
     * Battery level default system wide {@link ChannelType}. Represents the battery level as a percentage.
     */
    public static final ChannelType SYSTEM_CHANNEL_BATTERY_LEVEL = new ChannelType(
            new ChannelTypeUID("system:battery-level"), false, "Number", "Battery Level", null, "Battery", null, null,
            null);

    private final Collection<ChannelType> channelTypes;

    public DefaultSystemChannelTypeProvider() {
        this.channelTypes = Collections.unmodifiableCollection(Arrays.asList(new ChannelType[] {
                SYSTEM_CHANNEL_SIGNAL_STRENGTH, SYSTEM_CHANNEL_LOW_BATTERY, SYSTEM_CHANNEL_BATTERY_LEVEL }));

    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return this.channelTypes;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        if (channelTypeUID.equals(SYSTEM_CHANNEL_SIGNAL_STRENGTH.getUID())) {
            return SYSTEM_CHANNEL_SIGNAL_STRENGTH;
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_LOW_BATTERY.getUID())) {
            return SYSTEM_CHANNEL_LOW_BATTERY;
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_BATTERY_LEVEL.getUID())) {
            return SYSTEM_CHANNEL_BATTERY_LEVEL;
        }
        return null;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return Collections.emptyList();
    }
}
