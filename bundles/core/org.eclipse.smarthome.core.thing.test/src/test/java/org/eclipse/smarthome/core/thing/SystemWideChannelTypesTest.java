/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.thing;

import static org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider.BINDING_ID;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the {@link DefaultSystemChannelTypeProvider} class.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class SystemWideChannelTypesTest extends JavaOSGiTest {

    private static final ChannelTypeUID SIGNAL_STRENGTH_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID,
            "signal-strength");
    private static final ChannelTypeUID LOW_BATTERY_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "low-battery");
    private static final ChannelTypeUID BATTERY_LEVEL_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID,
            "battery-level");
    private static final ChannelTypeUID TRIGGER_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "trigger");
    private static final ChannelTypeUID RAWBUTTON_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "rawbutton");
    private static final ChannelTypeUID BUTTON_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "button");
    private static final ChannelTypeUID RAWROCKER_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "rawrocker");
    private static final ChannelTypeUID POWER_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "power");
    private static final ChannelTypeUID LOCATION_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "location");
    private static final ChannelTypeUID MOTION_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "motion");
    private static final ChannelTypeUID BRIGHTNESS_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "brightness");
    private static final ChannelTypeUID COLOR_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "color");
    private static final ChannelTypeUID COLOR_TEMPERATURE_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID,
            "color-temperature");
    private static final ChannelTypeUID VOLUME_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "volume");
    private static final ChannelTypeUID MUTE_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "mute");
    private static final ChannelTypeUID MEDIA_CONTROL_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID,
            "media-control");
    private static final ChannelTypeUID MEDIA_TITLE_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "media-title");
    private static final ChannelTypeUID MEDIA_ARTIST_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "media-artist");
    private static final ChannelTypeUID WIND_DIRECTION_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID,
            "wind-direction");
    private static final ChannelTypeUID WIND_SPEED_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, "wind-speed");
    private static final ChannelTypeUID OUTDOOR_TEMPERATURE_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID,
            "outdoor-temperature");
    private static final ChannelTypeUID ATMOSPHERIC_HUMIDITY_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID,
            "atmospheric-humidity");
    private static final ChannelTypeUID BAROMETRIC_PRESSURE_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID,
            "barometric-pressure");

    private ChannelTypeRegistry channelTypeRegistry;

    @Before
    public void setUp() {
        channelTypeRegistry = getService(ChannelTypeRegistry.class);
        assertNotNull(channelTypeRegistry);
    }

    @Test
    public void systemChannelTypesShouldBeAvailable() {
        List<ChannelType> sytemChannelTypes = channelTypeRegistry.getChannelTypes();
        assertEquals(23, sytemChannelTypes.size());

        assertNotNull(channelTypeRegistry.getChannelType(SIGNAL_STRENGTH_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(LOW_BATTERY_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(BATTERY_LEVEL_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(TRIGGER_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(RAWBUTTON_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(BUTTON_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(RAWROCKER_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(POWER_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(LOCATION_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(MOTION_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(BRIGHTNESS_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(COLOR_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(COLOR_TEMPERATURE_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(VOLUME_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(MUTE_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(MEDIA_CONTROL_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(MEDIA_TITLE_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(MEDIA_ARTIST_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(WIND_DIRECTION_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(WIND_SPEED_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(OUTDOOR_TEMPERATURE_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(ATMOSPHERIC_HUMIDITY_CHANNEL_TYPE_UID));
        assertNotNull(channelTypeRegistry.getChannelType(BAROMETRIC_PRESSURE_CHANNEL_TYPE_UID));
    }

    @Test
    public void systemChannelTypesShouldBeTranslatedProperly() {
        List<ChannelType> localizedChannelTypes = channelTypeRegistry.getChannelTypes(Locale.GERMAN);
        assertEquals(23, localizedChannelTypes.size());

        ChannelType signalStrengthChannelType = channelTypeRegistry.getChannelType(SIGNAL_STRENGTH_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(signalStrengthChannelType);
        assertEquals("Signalstärke", signalStrengthChannelType.getLabel());
        assertNull(signalStrengthChannelType.getDescription());

        List<StateOption> signalStrengthChannelTypeOptions = signalStrengthChannelType.getState().getOptions();
        assertEquals(5, signalStrengthChannelTypeOptions.size());

        ChannelType lowBatteryChannelType = channelTypeRegistry.getChannelType(LOW_BATTERY_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(lowBatteryChannelType);
        assertEquals("Niedriger Batteriestatus", lowBatteryChannelType.getLabel());
        assertNull(lowBatteryChannelType.getDescription());

        ChannelType batteryLevelChannelType = channelTypeRegistry.getChannelType(BATTERY_LEVEL_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(batteryLevelChannelType);
        assertEquals("Batterieladung", batteryLevelChannelType.getLabel());
        assertNull(batteryLevelChannelType.getDescription());
    }
}
