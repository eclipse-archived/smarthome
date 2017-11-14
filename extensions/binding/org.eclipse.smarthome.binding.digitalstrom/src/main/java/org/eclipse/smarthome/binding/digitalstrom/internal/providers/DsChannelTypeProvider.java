/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * The {@link DsChannelTypeProvider} implements the {@link ChannelTypeProvider} generates all supported
 * {@link Channel}'s for digitalSTROM.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DsChannelTypeProvider implements ChannelTypeProvider {

    public static final List<String> SUPPORTED_CHANNEL_TYPES = Lists.newArrayList(
            DigitalSTROMBindingConstants.CHANNEL_ID_BRIGHTNESS, DigitalSTROMBindingConstants.CHANNEL_ID_LIGHT_SWITCH,
            DigitalSTROMBindingConstants.CHANNEL_ID_COMBINED_2_STAGE_SWITCH,
            DigitalSTROMBindingConstants.CHANNEL_ID_COMBINED_3_STAGE_SWITCH,
            DigitalSTROMBindingConstants.CHANNEL_ID_GENERAL_DIMM,
            DigitalSTROMBindingConstants.CHANNEL_ID_GENERAL_SWITCH,
            DigitalSTROMBindingConstants.CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH,
            DigitalSTROMBindingConstants.CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH,
            DigitalSTROMBindingConstants.CHANNEL_ID_SCENE, DigitalSTROMBindingConstants.CHANNEL_ID_SHADE,
            DigitalSTROMBindingConstants.CHANNEL_ID_ELECTRIC_METER,
            DigitalSTROMBindingConstants.CHANNEL_ID_OUTPUT_CURRENT,
            DigitalSTROMBindingConstants.CHANNEL_ID_ACTIVE_POWER,
            DigitalSTROMBindingConstants.CHANNEL_ID_TOTAL_ACTIVE_POWER,
            DigitalSTROMBindingConstants.CHANNEL_ID_TOTAL_ELECTRIC_METER,
            DigitalSTROMBindingConstants.CHANNEL_ID_SHADE_ANGLE);

    private TranslationProvider i18n = null;
    private Bundle bundle = null;

    // item types
    private final String DIMMER = "Dimmer";
    private final String SWITCH = "Switch";
    private final String SHADE = "Rollershutter";
    private final String STRING = "String";
    private final String NUMBER = "Number";

    private StateDescription getSensorStateDescription(String shortcutUnit) {
        return shortcutUnit.equals(SensorEnum.ELECTRIC_METER.getUnitShortcut())
                ? new StateDescription(null, null, null, "%.3f " + shortcutUnit, true, null)
                : new StateDescription(null, null, null, "%d " + shortcutUnit, true, null);
    }

    private StateDescription getCombinedStageDescription(short stages, boolean isLight, Locale locale) {
        List<StateOption> stateOptions = new ArrayList<StateOption>();
        if (isLight) {
            stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF,
                    getText("OPTION_BOTH_LIGHTS_OFF", locale)));
            stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON,
                    getText("OPTION_BOTH_LIGHTS_ON", locale)));
            stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON,
                    getText("OPTION_FIRST_LIGHT_ON", locale)));
            if (stages == 3) {
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON,
                        getText("OPTION_SECOND_LIGHT_ON", locale)));
            }
        } else {
            stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF,
                    getText("OPTION_BOTH_RELAIS_OFF", locale)));
            stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON,
                    getText("OPTION_BOTH_RELAIS_ON", locale)));
            stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON,
                    getText("OPTION_FIRST_RELAIS_ON", locale)));
            if (stages == 3) {
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON,
                        getText("OPTION_SECOND_RELAIS_ON", locale)));
            }
        }
        return new StateDescription(null, null, null, null, false, stateOptions);
    }

    protected void activate(ComponentContext componentContext) {
        this.bundle = componentContext.getBundleContext().getBundle();
    }

    protected void deactivate(ComponentContext componentContext) {
        this.bundle = null;
    }

    protected void setTranslationProvider(TranslationProvider i18n) {
        this.i18n = i18n;
    };

    protected void unsetTranslationProvider(TranslationProvider i18n) {
        this.i18n = null;
    };

    private String getText(String key, Locale locale) {
        return i18n != null ? i18n.getText(bundle, key, i18n.getText(bundle, key, key, Locale.ENGLISH), locale) : key;
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        List<ChannelType> channelTypeList = new ArrayList<ChannelType>();
        for (String channelTypeId : SUPPORTED_CHANNEL_TYPES) {
            channelTypeList.add(
                    getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, channelTypeId), locale));
        }
        return channelTypeList;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        if (channelTypeUID.getBindingId().equals(DigitalSTROMBindingConstants.BINDING_ID)) {
            switch (channelTypeUID.getId()) {
                case DigitalSTROMBindingConstants.CHANNEL_ID_BRIGHTNESS:
                    return new ChannelType(channelTypeUID, false, DIMMER, getText("CHANNEL_BRIGHTNESS_LABEL", locale),
                            getText("CHANNEL_BRIGHTNESS_DESCRIPTION", locale), "dimmableLight",
                            Sets.newHashSet(getText("YELLOW", locale), getText("DS", locale), getText("LIGHT", locale)),
                            null, null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_LIGHT_SWITCH:
                    return new ChannelType(channelTypeUID, false, SWITCH, getText("CHANNEL_LIGHT_SWITCH_LABEL", locale),
                            getText("CHANNEL_LIGHT_SWITCH_DESCRIPTION", locale), "light",
                            Sets.newHashSet(getText("YELLOW", locale), getText("DS", locale), getText("LIGHT", locale)),
                            null, null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_GENERAL_DIMM:
                    return new ChannelType(channelTypeUID, false, DIMMER, getText("CHANNEL_GENERAL_DIMM_LABEL", locale),
                            getText("CHANNEL_GENERAL_DIMM_DESCRIPTION", locale), null,
                            Sets.newHashSet(getText("BLACK", locale), getText("DS", locale), getText("JOKER", locale)),
                            null, null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_GENERAL_SWITCH:
                    return new ChannelType(channelTypeUID, false, SWITCH,
                            getText("CHANNEL_GENERAL_SWITCH_LABEL", locale),
                            getText("CHANNEL_GENERAL_SWITCH_DESCRIPTION", locale), null,
                            Sets.newHashSet(getText("BLACK", locale), getText("DS", locale), getText("JOKER", locale)),
                            null, null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_COMBINED_2_STAGE_SWITCH:
                    return new ChannelType(channelTypeUID, false, STRING,
                            getText("CHANNEL_COMBINED_2_STAGE_SWITCH_LABEL", locale),
                            getText("CHANNEL_COMBINED_2_STAGE_SWITCH_DESCRIPTION", locale), "Lights",
                            Sets.newHashSet(getText("YELLOW", locale), getText("DS", locale), getText("LIGHT", locale),
                                    getText("UMR", locale)),
                            getCombinedStageDescription((short) 2, true, locale), null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_COMBINED_3_STAGE_SWITCH:
                    return new ChannelType(channelTypeUID, false, STRING,
                            getText("CHANNEL_COMBINED_3_STAGE_SWITCH_LABEL", locale),
                            getText("CHANNEL_COMBINED_3_STAGE_SWITCH_DESCRIPTION", locale), "Lights",
                            Sets.newHashSet(getText("YELLOW", locale), getText("DS", locale), getText("LIGHT", locale),
                                    getText("UMR", locale)),
                            getCombinedStageDescription((short) 3, true, locale), null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH:
                    return new ChannelType(channelTypeUID, false, STRING,
                            getText("CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_LABEL", locale),
                            getText("CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_DESCRIPTION", locale), null,
                            Sets.newHashSet(getText("BLACK", locale), getText("DS", locale), getText("UMR", locale)),
                            getCombinedStageDescription((short) 2, true, locale), null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH:
                    return new ChannelType(channelTypeUID, false, STRING,
                            getText("CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_LABEL", locale),
                            getText("CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_DESCRIPTION", locale), null,
                            Sets.newHashSet(getText("BLACK", locale), getText("DS", locale), getText("UMR", locale)),
                            getCombinedStageDescription((short) 3, true, locale), null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_SHADE:
                    return new ChannelType(channelTypeUID, false, SHADE, getText("CHANNEL_SHADE_LABEL", locale),
                            getText("CHANNEL_SHADE_DESCRIPTION", locale), "Blinds",
                            Sets.newHashSet(getText("GREY", locale), getText("DS", locale), getText("SHADE", locale)),
                            null, null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_SHADE_ANGLE:
                    return new ChannelType(channelTypeUID, false, DIMMER, getText("CHANNEL_SHADE_ANGLE_LABEL", locale),
                            getText("CHANNEL_SHADE_ANGLE_DESCRIPTION", locale), "Blinds",
                            Sets.newHashSet(getText("GREY", locale), getText("DS", locale), getText("SHADE", locale)),
                            null, null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_ACTIVE_POWER:
                    return new ChannelType(channelTypeUID, false, NUMBER, getText("CHANNEL_ACTIVE_POWER_LABEL", locale),
                            getText("CHANNEL_ACTIVE_POWER_DESCRIPTION", locale), null,
                            Sets.newHashSet(getText("ACTIVE_POWER", locale), getText("POWER_CONSUMPTION", locale),
                                    getText("DS", locale)),
                            getSensorStateDescription(SensorEnum.ACTIVE_POWER.getUnitShortcut()), null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_ELECTRIC_METER:
                    return new ChannelType(channelTypeUID, false, NUMBER,
                            getText("CHANNEL_ELECTRIC_METER_LABEL", locale),
                            getText("CHANNEL_ELECTRIC_METER_DESCRIPTION", locale), "Energy",
                            Sets.newHashSet(getText("ELECTRIC_METER", locale), getText("DS", locale)),
                            getSensorStateDescription(SensorEnum.ELECTRIC_METER.getUnitShortcut()), null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_OUTPUT_CURRENT:
                    return new ChannelType(channelTypeUID, false, NUMBER,
                            getText("CHANNEL_OUTPUT_CURRENT_LABEL", locale),
                            getText("CHANNEL_OUTPUT_CURRENT_DESCRIPTION", locale), "Energy",
                            Sets.newHashSet(getText("OUTPUT_CURRENT", locale), getText("DS", locale)),
                            getSensorStateDescription(SensorEnum.OUTPUT_CURRENT.getUnitShortcut()), null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_TOTAL_ACTIVE_POWER:
                    return new ChannelType(channelTypeUID, false, NUMBER,
                            getText("CHANNEL_TOTAL_ACTIVE_POWER_LABEL", locale),
                            getText("CHANNEL_TOTAL_ACTIVE_POWER_DESCRIPTION", locale), "Energy",
                            Sets.newHashSet(getText("ACTIVE_POWER", locale), getText("POWER_CONSUMPTION", locale),
                                    getText("DS", locale)),
                            getSensorStateDescription(SensorEnum.ACTIVE_POWER.getUnitShortcut()), null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_TOTAL_ELECTRIC_METER:
                    return new ChannelType(channelTypeUID, false, NUMBER,
                            getText("CHANNEL_TOTAL_ELECTRIC_METER_LABEL", locale),
                            getText("CHANNEL_TOTAL_ELECTRIC_METER_DESCRIPTION", locale), "Energy",
                            Sets.newHashSet(getText("ELECTRIC_METER", locale), getText("DS", locale)),
                            getSensorStateDescription(SensorEnum.ELECTRIC_METER.getUnitShortcut()), null);
                case DigitalSTROMBindingConstants.CHANNEL_ID_SCENE:
                    return new ChannelType(channelTypeUID, false, SWITCH, getText("CHANNEL_SCENE_LABEL", locale),
                            getText("CHANNEL_SCENE_DESCRIPTION", locale), "Energy",
                            Sets.newHashSet(getText("SCENE", locale), getText("DS", locale)), null, null);
            }
        }
        return null;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return null;
    }
}
