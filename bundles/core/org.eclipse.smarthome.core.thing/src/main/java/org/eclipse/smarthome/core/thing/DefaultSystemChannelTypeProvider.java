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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.i18n.ThingTypeI18nUtil;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.EventOption;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.util.BundleResolver;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation providing default system wide channel types
 *
 * @author Ivan Iliev - Initial Contribution
 * @author Chris Jackson - Added battery level
 * @author Dennis Nobel - Changed to {@link ChannelTypeProvider}
 * @author Markus Rathgeb - Make battery-low indication read-only
 * @author Moritz Kammerer - Added system trigger types
 * @author Christoph Weitkamp - Added support for translation
 * @author Stefan Triller - Added more system channels
 *
 */
@Component(immediate = true)
public class DefaultSystemChannelTypeProvider implements ChannelTypeProvider {

    private static final String BINDING_ID = "system";

    /**
     * Signal strength default system wide {@link ChannelType}. Represents signal strength of a device as a number
     * with values 0, 1, 2, 3 or 4, 0 being worst strength and 4 being best strength.
     */
    public static final ChannelType SYSTEM_CHANNEL_SIGNAL_STRENGTH = new ChannelTypeBuilder(BINDING_ID,
            "signal-strength", "Signal Strength")
                    .withItemType("Number").withCategory("QualityOfService")
                    .withStateDescription(
                            new StateDescription(BigDecimal.ZERO, new BigDecimal(4), BigDecimal.ONE, null, true,
                                    Arrays.asList(new StateOption("0", "no signal"), new StateOption("1", "weak"),
                                            new StateOption("2", "average"), new StateOption("3", "good"),
                                            new StateOption("4", "excellent"))))
                    .build();

    /**
     * Low battery default system wide {@link ChannelType}. Represents a low battery warning with possible values
     * on/off.
     */
    public static final ChannelType SYSTEM_CHANNEL_LOW_BATTERY = new ChannelTypeBuilder(BINDING_ID, "low-battery",
            "Low Battery")
                    .withItemType("Switch").withCategory("Battery")
                    .withStateDescription(
                            StateDescriptionFragmentBuilder.create().withReadOnly(true).build().toStateDescription())
                    .build();

    /**
     * Battery level default system wide {@link ChannelType}. Represents the battery level as a percentage.
     */
    public static final ChannelType SYSTEM_CHANNEL_BATTERY_LEVEL = new ChannelTypeBuilder(BINDING_ID, "battery-level",
            "Battery Level").withItemType("Number").withCategory("Battery")
                    .withStateDescription(new StateDescription(BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ONE,
                            "%.0f %%", true, null))
                    .build();

    /**
     * System wide trigger {@link ChannelType} without event options.
     */
    public static final ChannelType SYSTEM_TRIGGER = new ChannelTypeBuilder(BINDING_ID, "trigger", "Trigger")
            .withKind(ChannelKind.TRIGGER).build();

    /**
     * System wide trigger {@link ChannelType} which triggers "PRESSED" and "RELEASED" events.
     */
    public static final ChannelType SYSTEM_RAWBUTTON = new ChannelTypeBuilder(BINDING_ID, "rawbutton", "Raw button")
            .withKind(ChannelKind.TRIGGER)
            .withEventDescription(new EventDescription(Arrays.asList(new EventOption(CommonTriggerEvents.PRESSED, null),
                    new EventOption(CommonTriggerEvents.RELEASED, null))))
            .build();

    /**
     * System wide trigger {@link ChannelType} which triggers "SHORT_PRESSED", "DOUBLE_PRESSED" and "LONG_PRESSED"
     * events.
     */
    public static final ChannelType SYSTEM_BUTTON = new ChannelTypeBuilder(BINDING_ID, "button", "Button")
            .withKind(ChannelKind.TRIGGER)
            .withEventDescription(
                    new EventDescription(Arrays.asList(new EventOption(CommonTriggerEvents.SHORT_PRESSED, null),
                            new EventOption(CommonTriggerEvents.DOUBLE_PRESSED, null),
                            new EventOption(CommonTriggerEvents.LONG_PRESSED, null))))
            .build();

    /**
     * System wide trigger {@link ChannelType} which triggers "DIR1_PRESSED", "DIR1_RELEASED", "DIR2_PRESSED" and
     * "DIR2_RELEASED" events.
     */
    public static final ChannelType SYSTEM_RAWROCKER = new ChannelTypeBuilder(BINDING_ID, "rawrocker",
            "Raw rocker button")
                    .withKind(ChannelKind.TRIGGER)
                    .withEventDescription(
                            new EventDescription(Arrays.asList(new EventOption(CommonTriggerEvents.DIR1_PRESSED, null),
                                    new EventOption(CommonTriggerEvents.DIR1_RELEASED, null),
                                    new EventOption(CommonTriggerEvents.DIR2_PRESSED, null),
                                    new EventOption(CommonTriggerEvents.DIR2_RELEASED, null))))
                    .build();

    /**
     * Power: default system wide {@link ChannelType} which allows turning off (potentially on) a device
     */
    public static final ChannelType SYSTEM_POWER = new ChannelTypeBuilder(BINDING_ID, "power", "Power")
            .withItemType("Switch").build();

    /**
     * Location: default system wide {@link ChannelType} which displays a location
     */
    public static final ChannelType SYSTEM_LOCATION = new ChannelTypeBuilder(BINDING_ID, "location", "Location")
            .withItemType("Location").withDescription("Location in lat./lon./height coordinates").withStateDescription(
                    StateDescriptionFragmentBuilder.create().withReadOnly(true).build().toStateDescription())
            .build();
    /**
     * Motion default system wide {@link ChannelType} which indications whether motion was detected (state. ON)
     */
    public static final ChannelType SYSTEM_MOTION = new ChannelTypeBuilder(BINDING_ID, "motion", "Motion")
            .withItemType("Switch").withDescription("Motion detected by the device").withCategory("Motion")
            .withStateDescription(
                    StateDescriptionFragmentBuilder.create().withReadOnly(true).build().toStateDescription())
            .build();

    /**
     * Brightness: default system wide {@link ChannelType} which allows changing the brightness from 0-100%
     */
    public static final ChannelType SYSTEM_BRIGHTNESS = new ChannelTypeBuilder(BINDING_ID, "brightness", "Brightness")
            .withItemType("Dimmer")
            .withStateDescription(
                    new StateDescription(BigDecimal.ZERO, new BigDecimal(100), null, "%d %%", false, null))
            .withCategory("Light").build();

    /**
     * Color: default system wide {@link ChannelType} which allows changing the color
     */
    public static final ChannelType SYSTEM_COLOR = new ChannelTypeBuilder(BINDING_ID, "color", "Color")
            .withItemType("Color").withCategory("ColorLight").build();

    /**
     * Color-temperature: default system wide {@link ChannelType} which allows changing the color temperature
     */
    public static final ChannelType SYSTEM_COLOR_TEMPERATURE = new ChannelTypeBuilder(BINDING_ID, "color-temperature",
            "Color Temperature")
                    .withItemType("Dimmer")
                    .withStateDescription(
                            new StateDescription(BigDecimal.ZERO, new BigDecimal(100), null, "%d", false, null))
                    .withCategory("ColorLight").build();

    // media channels

    /**
     * Volume: default system wide {@link ChannelType} which allows changing the audio volume from 0-100%
     */
    public static final ChannelType SYSTEM_VOLUME = new ChannelTypeBuilder(BINDING_ID, "volume", "Volume")
            .withItemType("Dimmer").withDescription("Change the sound volume of a device")
            .withStateDescription(
                    new StateDescription(BigDecimal.ZERO, new BigDecimal(100), null, "%d %%", false, null))
            .withCategory("SoundVolume").build();

    /**
     * Mute: default system wide {@link ChannelType} which allows muting and un-muting audio
     */
    public static final ChannelType SYSTEM_MUTE = new ChannelTypeBuilder(BINDING_ID, "mute", "Mute")
            .withItemType("Switch").withDescription("Mute audio of the device").withCategory("SoundVolume").build();

    /**
     * Media-control: system wide {@link ChannelType} which controls a media player
     */
    public static final ChannelType SYSTEM_MEDIA_CONTROL = new ChannelTypeBuilder(BINDING_ID, "media-control",
            "Media Control").withItemType("Player").withCategory("MediaControl").build();

    /**
     * Media-title: default system wide {@link ChannelType} which displays the title of a (played) song
     */
    public static final ChannelType SYSTEM_MEDIA_TITLE = new ChannelTypeBuilder(BINDING_ID, "media-title",
            "Media Title")
                    .withItemType("String").withDescription("Title of a (played) media file")
                    .withStateDescription(
                            StateDescriptionFragmentBuilder.create().withReadOnly(true).build().toStateDescription())
                    .build();

    /**
     * Media-artist: default system wide {@link ChannelType} which displays the artist of a (played) song
     */
    public static final ChannelType SYSTEM_MEDIA_ARTIST = new ChannelTypeBuilder(BINDING_ID, "media-artist",
            "Media Artist")
                    .withItemType("String").withDescription("Artist of a (played) media file")
                    .withStateDescription(
                            StateDescriptionFragmentBuilder.create().withReadOnly(true).build().toStateDescription())
                    .build();

    // weather channels

    /**
     * Wind-direction: system wide {@link ChannelType} which shows the wind direction in degrees 0-360
     */
    public static final ChannelType SYSTEM_WIND_DIRECTION = new ChannelTypeBuilder(BINDING_ID, "wind-direction",
            "Wind Direction")
                    .withItemType("Number:Angle").withCategory("Wind")
                    .withStateDescription(
                            new StateDescription(BigDecimal.ZERO, new BigDecimal(360), null, "%.0f %unit%", true, null))
                    .build();

    /**
     * Wind-speed: system wide {@link ChannelType} which shows the wind speed
     */
    public static final ChannelType SYSTEM_WIND_SPEED = new ChannelTypeBuilder(BINDING_ID, "wind-speed", "Wind Speed")
            .withItemType("Number:Speed").withCategory("Wind").withStateDescription(StateDescriptionFragmentBuilder
                    .create().withReadOnly(true).withPattern("%.1f %unit%").build().toStateDescription())
            .build();

    /**
     * Outdoor-temperature: system wide {@link ChannelType} which shows the outdoor temperature
     */
    public static final ChannelType SYSTEM_OUTDOOR_TEMPERATURE = new ChannelTypeBuilder(BINDING_ID,
            "outdoor-temperature", "Current Outdoor Temperatur").withItemType("Number:Temperature")
                    .withCategory("Temperature").withStateDescription(StateDescriptionFragmentBuilder.create()
                            .withReadOnly(true).withPattern("%.1f %unit%").build().toStateDescription())
                    .build();

    /**
     * Atmospheric-humidity: system wide {@link ChannelType} which shows the atmospheric humidity
     */
    public static final ChannelType SYSTEM_ATMOSPHERIC_HUMIDITY = new ChannelTypeBuilder(BINDING_ID,
            "atmospheric-humidity", "Atmospheric Humidity").withItemType("Number:Dimensionless")
                    .withCategory("Humidity").withStateDescription(StateDescriptionFragmentBuilder.create()
                            .withReadOnly(true).withPattern("%.0f %%").build().toStateDescription())
                    .build();

    /**
     * Barometric-pressure: system wide {@link ChannelType} which shows the barometric pressure
     */
    public static final ChannelType SYSTEM_BAROMETRIC_PRESSURE = new ChannelTypeBuilder(BINDING_ID,
            "barometric-pressure", "Barometric Pressure").withItemType("Number:Pressure").withCategory("Pressure")
                    .withStateDescription(StateDescriptionFragmentBuilder.create().withReadOnly(true)
                            .withPattern("%.3f %unit%").build().toStateDescription())
                    .build();

    private static class LocalizedChannelTypeKey {
        public final String locale;
        public final UID uid;

        public LocalizedChannelTypeKey(UID uid, String locale) {
            this.uid = uid;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LocalizedChannelTypeKey other = (LocalizedChannelTypeKey) obj;
            if (locale == null) {
                if (other.locale != null) {
                    return false;
                }
            } else if (!locale.equals(other.locale)) {
                return false;
            }
            if (uid == null) {
                if (other.uid != null) {
                    return false;
                }
            } else if (!uid.equals(other.uid)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((locale == null) ? 0 : locale.hashCode());
            result = prime * result + ((uid == null) ? 0 : uid.hashCode());
            return result;
        }

    }

    private final Collection<ChannelGroupType> channelGroupTypes;
    private final Collection<ChannelType> channelTypes;

    private final Map<LocalizedChannelTypeKey, ChannelType> localizedChannelTypeCache = new ConcurrentHashMap<>();

    private ThingTypeI18nUtil thingTypeI18nUtil;
    private BundleResolver bundleResolver;

    public DefaultSystemChannelTypeProvider() {
        channelGroupTypes = Collections.emptyList();
        channelTypes = Collections.unmodifiableCollection(Arrays.asList(new ChannelType[] {
                SYSTEM_CHANNEL_SIGNAL_STRENGTH, SYSTEM_CHANNEL_LOW_BATTERY, SYSTEM_CHANNEL_BATTERY_LEVEL,
                SYSTEM_TRIGGER, SYSTEM_RAWBUTTON, SYSTEM_BUTTON, SYSTEM_RAWROCKER }));
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        final List<ChannelType> allChannelTypes = new ArrayList<>(10);
        final Bundle bundle = bundleResolver.resolveBundle(DefaultSystemChannelTypeProvider.class);

        for (final ChannelType channelType : channelTypes) {
            allChannelTypes.add(createLocalizedChannelType(bundle, channelType, locale));
        }

        return allChannelTypes;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        final Bundle bundle = bundleResolver.resolveBundle(DefaultSystemChannelTypeProvider.class);

        if (channelTypeUID.equals(SYSTEM_CHANNEL_SIGNAL_STRENGTH.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_SIGNAL_STRENGTH, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_LOW_BATTERY.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_LOW_BATTERY, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_BATTERY_LEVEL.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_BATTERY_LEVEL, locale);
        } else if (channelTypeUID.equals(SYSTEM_TRIGGER.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_TRIGGER, locale);
        } else if (channelTypeUID.equals(SYSTEM_RAWBUTTON.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_RAWBUTTON, locale);
        } else if (channelTypeUID.equals(SYSTEM_BUTTON.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_BUTTON, locale);
        } else if (channelTypeUID.equals(SYSTEM_RAWROCKER.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_RAWROCKER, locale);
        }
        return null;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return channelGroupTypes;
    }

    @Reference
    public void setTranslationProvider(TranslationProvider i18nProvider) {
        thingTypeI18nUtil = new ThingTypeI18nUtil(i18nProvider);
    }

    public void unsetTranslationProvider(TranslationProvider i18nProvider) {
        thingTypeI18nUtil = null;
    }

    @Reference
    public void setBundleResolver(BundleResolver bundleResolver) {
        this.bundleResolver = bundleResolver;
    }

    public void unsetBundleResolver(BundleResolver bundleResolver) {
        this.bundleResolver = bundleResolver;
    }

    private ChannelType createLocalizedChannelType(Bundle bundle, ChannelType channelType, Locale locale) {
        LocalizedChannelTypeKey localizedChannelTypeKey = getLocalizedChannelTypeKey(channelType.getUID(), locale);

        ChannelType cachedEntry = localizedChannelTypeCache.get(localizedChannelTypeKey);
        if (cachedEntry != null) {
            return cachedEntry;
        }

        if (thingTypeI18nUtil != null) {
            ChannelTypeUID channelTypeUID = channelType.getUID();

            String label = thingTypeI18nUtil.getChannelLabel(bundle, channelTypeUID, channelType.getLabel(), locale);
            String description = thingTypeI18nUtil.getChannelDescription(bundle, channelTypeUID,
                    channelType.getDescription(), locale);

            StateDescription state = createLocalizedChannelState(bundle, channelType, channelTypeUID, locale);

            ChannelType localizedChannelType = new ChannelType(channelTypeUID, channelType.isAdvanced(),
                    channelType.getItemType(), channelType.getKind(), label, description, channelType.getCategory(),
                    channelType.getTags(), state, channelType.getEvent(), channelType.getConfigDescriptionURI());

            localizedChannelTypeCache.put(localizedChannelTypeKey, localizedChannelType);

            return localizedChannelType;
        }

        return channelType;
    }

    private StateDescription createLocalizedChannelState(Bundle bundle, ChannelType channelType,
            ChannelTypeUID channelTypeUID, Locale locale) {
        StateDescription state = channelType.getState();

        if (state != null) {
            String pattern = thingTypeI18nUtil.getChannelStatePattern(bundle, channelTypeUID, state.getPattern(),
                    locale);

            List<StateOption> localizedOptions = new ArrayList<>();
            List<StateOption> options = state.getOptions();
            for (StateOption stateOption : options) {
                String optionLabel = thingTypeI18nUtil.getChannelStateOption(bundle, channelTypeUID,
                        stateOption.getValue(), stateOption.getLabel(), locale);
                localizedOptions.add(new StateOption(stateOption.getValue(), optionLabel));
            }

            return new StateDescription(state.getMinimum(), state.getMaximum(), state.getStep(), pattern,
                    state.isReadOnly(), localizedOptions);
        }
        return null;
    }

    private LocalizedChannelTypeKey getLocalizedChannelTypeKey(UID uid, Locale locale) {
        String localeString = locale != null ? locale.toLanguageTag() : null;
        LocalizedChannelTypeKey localizedChannelTypeKey = new LocalizedChannelTypeKey(uid,
                locale != null ? localeString : null);
        return localizedChannelTypeKey;
    }
}
