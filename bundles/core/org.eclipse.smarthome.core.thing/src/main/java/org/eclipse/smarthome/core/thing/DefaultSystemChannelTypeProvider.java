/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.EventOption;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * Implementation providing default system wide channel types
 *
 * @author Ivan Iliev - Initial Contribution
 * @author Chris Jackson - Added battery level
 * @author Dennis Nobel - Changed to {@link ChannelTypeProvider}
 * @author Markus Rathgeb - Make battery-low indication read-only
 * @author Moritz Kammerer - Added system trigger types
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
            new ChannelTypeUID("system:low-battery"), false, "Switch", "Low Battery", null, "Battery", null,
            new StateDescription(null, null, null, null, true, null), null);

    /**
     * Battery level default system wide {@link ChannelType}. Represents the battery level as a percentage.
     */
    public static final ChannelType SYSTEM_CHANNEL_BATTERY_LEVEL = new ChannelType(
            new ChannelTypeUID("system:battery-level"), false, "Number", "Battery Level", null, "Battery", null, null,
            null);

    /**
     * System wide trigger {@link ChannelType} without event options.
     */
    public static final ChannelType SYSTEM_TRIGGER = new ChannelType(new ChannelTypeUID("system:trigger"), false, null,
            ChannelKind.TRIGGER, "Trigger", null, null, null, null, null, null);

    /**
     * System wide trigger {@link ChannelType} which triggers "PRESSED" and "RELEASED" events.
     */
    public static final ChannelType SYSTEM_RAWBUTTON = new ChannelType(new ChannelTypeUID("system:rawbutton"), false,
            null, ChannelKind.TRIGGER, "Raw button", null, null, null, null,
            new EventDescription(Arrays.asList(new EventOption(CommonTriggerEvents.PRESSED, null),
                    new EventOption(CommonTriggerEvents.RELEASED, null))),
            null);

    /**
     * System wide trigger {@link ChannelType} which triggers "SHORT_PRESSED", "DOUBLE_PRESSED" and "LONG_PRESSED"
     * events.
     */
    public static final ChannelType SYSTEM_BUTTON = new ChannelType(new ChannelTypeUID("system:button"), false, null,
            ChannelKind.TRIGGER, "Button", null, null, null, null,
            new EventDescription(Arrays.asList(new EventOption(CommonTriggerEvents.SHORT_PRESSED, null),
                    new EventOption(CommonTriggerEvents.DOUBLE_PRESSED, null),
                    new EventOption(CommonTriggerEvents.LONG_PRESSED, null))),
            null);

    private final Collection<ChannelType> channelTypes;

    public DefaultSystemChannelTypeProvider() {
        this.channelTypes = Collections.unmodifiableCollection(
                Arrays.asList(new ChannelType[] { SYSTEM_CHANNEL_SIGNAL_STRENGTH, SYSTEM_CHANNEL_LOW_BATTERY,
                        SYSTEM_CHANNEL_BATTERY_LEVEL, SYSTEM_TRIGGER, SYSTEM_RAWBUTTON, SYSTEM_BUTTON }));

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
        } else if (channelTypeUID.equals(SYSTEM_TRIGGER.getUID())) {
            return SYSTEM_TRIGGER;
        } else if (channelTypeUID.equals(SYSTEM_RAWBUTTON.getUID())) {
            return SYSTEM_RAWBUTTON;
        } else if (channelTypeUID.equals(SYSTEM_BUTTON.getUID())) {
            return SYSTEM_BUTTON;
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
