/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.EventOption;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
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
 *
 */
@Component(immediate = true)
public class DefaultSystemChannelTypeProvider implements ChannelTypeProvider {

    /**
     * Signal strength default system wide {@link ChannelType}. Represents signal strength of a device as a number
     * with values 0, 1, 2, 3 or 4, 0 being worst strength and 4 being best strength.
     */
    public static final ChannelType SYSTEM_CHANNEL_SIGNAL_STRENGTH = new ChannelType(
            new ChannelTypeUID("system:signal-strength"), false, "Number", "Signal Strength", null, "QualityOfService",
            null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(4), BigDecimal.ONE, null, true,
                    Arrays.asList(new StateOption("0", "no signal"), new StateOption("1", "weak"),
                            new StateOption("2", "average"), new StateOption("3", "good"),
                            new StateOption("4", "excellent"))),
            null);

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
            new ChannelTypeUID("system:battery-level"), false, "Number", "Battery Level", null, "Battery", null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ONE, "%.0f %%", true, null), null);

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

    public DefaultSystemChannelTypeProvider() {
        channelGroupTypes = Collections.emptyList();
        channelTypes = Collections.unmodifiableCollection(
                Arrays.asList(new ChannelType[] { SYSTEM_CHANNEL_SIGNAL_STRENGTH, SYSTEM_CHANNEL_LOW_BATTERY,
                        SYSTEM_CHANNEL_BATTERY_LEVEL, SYSTEM_TRIGGER, SYSTEM_RAWBUTTON, SYSTEM_BUTTON }));

    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        final List<ChannelType> allChannelTypes = new ArrayList<>(10);
        final Bundle bundle = FrameworkUtil.getBundle(DefaultSystemChannelTypeProvider.class);

        for (final ChannelType channelType : channelTypes) {
            allChannelTypes.add(createLocalizedChannelType(bundle, channelType, locale));
        }

        return allChannelTypes;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        final Bundle bundle = FrameworkUtil.getBundle(DefaultSystemChannelTypeProvider.class);

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
