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

        StateOption noSignalOption = signalStrengthChannelTypeOptions.stream().filter(it -> "0".equals(it.getValue()))
                .findFirst().get();
        assertNotNull(noSignalOption);
        assertEquals("Kein Signal", noSignalOption.getLabel());
        StateOption weakOption = signalStrengthChannelTypeOptions.stream().filter(it -> "1".equals(it.getValue()))
                .findFirst().get();
        assertNotNull(weakOption);
        assertEquals("Schwach", weakOption.getLabel());
        StateOption averageOption = signalStrengthChannelTypeOptions.stream().filter(it -> "2".equals(it.getValue()))
                .findFirst().get();
        assertNotNull(averageOption);
        assertEquals("Durchschnittlich", averageOption.getLabel());
        StateOption goodOption = signalStrengthChannelTypeOptions.stream().filter(it -> "3".equals(it.getValue()))
                .findFirst().get();
        assertNotNull(goodOption);
        assertEquals("Gut", goodOption.getLabel());
        StateOption excellentOption = signalStrengthChannelTypeOptions.stream().filter(it -> "4".equals(it.getValue()))
                .findFirst().get();
        assertNotNull(excellentOption);
        assertEquals("Ausgezeichnet", excellentOption.getLabel());

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

        ChannelType powerChannelType = channelTypeRegistry.getChannelType(POWER_CHANNEL_TYPE_UID, Locale.GERMAN);
        assertNotNull(powerChannelType);
        assertEquals("Betrieb", powerChannelType.getLabel());
        assertEquals(
                "Ermöglicht die Steuerung der Betriebsbereitschaft. Das Gerät ist betriebsbereit, wenn \"Betrieb\" den Status ON hat.",
                powerChannelType.getDescription());

        ChannelType locationChannelType = channelTypeRegistry.getChannelType(LOCATION_CHANNEL_TYPE_UID, Locale.GERMAN);
        assertNotNull(locationChannelType);
        assertEquals("Ort", locationChannelType.getLabel());
        assertEquals("Ort in geographischen Koordinaten (Breitengrad/Längengrad/Höhe).",
                locationChannelType.getDescription());

        ChannelType motionChannelType = channelTypeRegistry.getChannelType(MOTION_CHANNEL_TYPE_UID, Locale.GERMAN);
        assertNotNull(motionChannelType);
        assertEquals("Bewegung", motionChannelType.getLabel());
        assertEquals("Zeigt eine erkannte Bewegung an.", motionChannelType.getDescription());

        ChannelType brightnessChannelType = channelTypeRegistry.getChannelType(BRIGHTNESS_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(brightnessChannelType);
        assertEquals("Helligkeit", brightnessChannelType.getLabel());
        assertNull(brightnessChannelType.getDescription());

        ChannelType colorChannelType = channelTypeRegistry.getChannelType(COLOR_CHANNEL_TYPE_UID, Locale.GERMAN);
        assertNotNull(colorChannelType);
        assertEquals("Farbe", colorChannelType.getLabel());
        assertNull(colorChannelType.getDescription());

        ChannelType colorTemperatureChannelType = channelTypeRegistry.getChannelType(COLOR_TEMPERATURE_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(colorTemperatureChannelType);
        assertEquals("Farbtemperatur", colorTemperatureChannelType.getLabel());
        assertNull(colorTemperatureChannelType.getDescription());

        ChannelType volumeChannelType = channelTypeRegistry.getChannelType(VOLUME_CHANNEL_TYPE_UID, Locale.GERMAN);
        assertNotNull(volumeChannelType);
        assertEquals("Lautstärke", volumeChannelType.getLabel());
        assertEquals("Ermöglicht die Steuerung der Lautstärke.", volumeChannelType.getDescription());

        ChannelType muteChannelType = channelTypeRegistry.getChannelType(MUTE_CHANNEL_TYPE_UID, Locale.GERMAN);
        assertNotNull(muteChannelType);
        assertEquals("Stumm schalten", muteChannelType.getLabel());
        assertEquals("Ermöglicht die Lautstärke auf stumm zu schalten.", muteChannelType.getDescription());

        ChannelType mediaControlChannelType = channelTypeRegistry.getChannelType(MEDIA_CONTROL_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(mediaControlChannelType);
        assertEquals("Fernbedienung", mediaControlChannelType.getLabel());
        assertNull(mediaControlChannelType.getDescription());

        ChannelType mediaTitleChannelType = channelTypeRegistry.getChannelType(MEDIA_TITLE_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(mediaTitleChannelType);
        assertEquals("Titel", mediaTitleChannelType.getLabel());
        assertEquals("Zeigt den Titel der (aktuell abgespielten) Video- oder Audiodatei an.",
                mediaTitleChannelType.getDescription());

        ChannelType mediaArtistChannelType = channelTypeRegistry.getChannelType(MEDIA_ARTIST_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(mediaArtistChannelType);
        assertEquals("Künstler", mediaArtistChannelType.getLabel());
        assertEquals("Zeigt den Künstler der (aktuell abgespielten) Video- oder Audiodatei an.",
                mediaArtistChannelType.getDescription());

        ChannelType windDirectionChannelType = channelTypeRegistry.getChannelType(WIND_DIRECTION_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(windDirectionChannelType);
        assertEquals("Windrichtung", windDirectionChannelType.getLabel());
        assertNull(windDirectionChannelType.getDescription());

        ChannelType windSpeedChannelType = channelTypeRegistry.getChannelType(WIND_SPEED_CHANNEL_TYPE_UID,
                Locale.GERMAN);
        assertNotNull(windSpeedChannelType);
        assertEquals("Windgeschwindigkeit", windSpeedChannelType.getLabel());
        assertNull(windSpeedChannelType.getDescription());

        ChannelType outdoorTemperatureChannelType = channelTypeRegistry
                .getChannelType(OUTDOOR_TEMPERATURE_CHANNEL_TYPE_UID, Locale.GERMAN);
        assertNotNull(outdoorTemperatureChannelType);
        assertEquals("Außentemperatur", outdoorTemperatureChannelType.getLabel());
        assertNull(outdoorTemperatureChannelType.getDescription());

        ChannelType atmosphericHumidityChannelType = channelTypeRegistry
                .getChannelType(ATMOSPHERIC_HUMIDITY_CHANNEL_TYPE_UID, Locale.GERMAN);
        assertNotNull(atmosphericHumidityChannelType);
        assertEquals("Luftfeuchtigkeit", atmosphericHumidityChannelType.getLabel());
        assertNull(atmosphericHumidityChannelType.getDescription());

        ChannelType barometricPressureChannelType = channelTypeRegistry
                .getChannelType(BAROMETRIC_PRESSURE_CHANNEL_TYPE_UID, Locale.GERMAN);
        assertNotNull(barometricPressureChannelType);
        assertEquals("Luftdruck", barometricPressureChannelType.getLabel());
        assertNull(barometricPressureChannelType.getDescription());
    }
}
