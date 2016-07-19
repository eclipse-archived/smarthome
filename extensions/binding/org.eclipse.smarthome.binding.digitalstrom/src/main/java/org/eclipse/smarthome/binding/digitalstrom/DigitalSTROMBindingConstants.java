/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneTypes;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link DigitalSTROMBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Ochel - Initial contribution
 * @author Mathias Siegele - Initial contribution
 */
public class DigitalSTROMBindingConstants {

    public static final String BINDING_ID = "digitalstrom";

    // List of all Thing Type IDs
    public static final String THING_TYPE_ID_DSS_BRIDGE = "dssBridge";

    public static final String THING_TYPE_ID_GE_DEVICE = "GE";
    public static final String THING_TYPE_ID_SW_DEVICE = "SW";
    public static final String THING_TYPE_ID_GR_DEVICE = "GR";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_DSS_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_DSS_BRIDGE);

    public final static ThingTypeUID THING_TYPE_GE_DEVICE = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_GE_DEVICE);
    public final static ThingTypeUID THING_TYPE_SW_DEVICE = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_SW_DEVICE);
    public final static ThingTypeUID THING_TYPE_GR_DEVICE = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_GR_DEVICE);

    public final static ThingTypeUID THING_TYPE_APP_SCENE = new ThingTypeUID(BINDING_ID, SceneTypes.APARTMENT_SCENE);
    public final static ThingTypeUID THING_TYPE_ZONE_SCENE = new ThingTypeUID(BINDING_ID, SceneTypes.ZONE_SCENE);
    public final static ThingTypeUID THING_TYPE_GROUP_SCENE = new ThingTypeUID(BINDING_ID, SceneTypes.GROUP_SCENE);
    public final static ThingTypeUID THING_TYPE_NAMED_SCENE = new ThingTypeUID(BINDING_ID, SceneTypes.NAMED_SCENE);

    /* List of all Channels */

    // Light
    public static final String CHANNEL_ID_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ID_LIGHT_SWITCH = "lightSwitch";
    public static final String CHANNEL_ID_COMBINED_2_STAGE_SWITCH = "combined2StageSwitch";
    public static final String CHANNEL_ID_COMBINED_3_STAGE_SWITCH = "combined3StageSwitch";

    public static final ChannelTypeUID CHANNEL_TYPE_BRIGHTNESS = new ChannelTypeUID(BINDING_ID, CHANNEL_ID_BRIGHTNESS);
    public static final ChannelTypeUID CHANNEL_TYPE_LIGHT_SWITCH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_LIGHT_SWITCH);
    public static final ChannelTypeUID CHANNEL_TYPE_COMBINED_2_STAGE_SWITCH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_COMBINED_2_STAGE_SWITCH);
    public static final ChannelTypeUID CHANNEL_TYPE_COMBINED_3_STAGE_SWITCH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_COMBINED_3_STAGE_SWITCH);

    // black
    public static final String CHANNEL_ID_GENERAL_DIMM = "generalDimm";
    public static final String CHANNEL_ID_GENERAL_SWITCH = "generalSwitch";
    public static final String CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH = "generalCombined2StageSwitch";
    public static final String CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH = "generalCombined3StageSwitch";

    public static final ChannelTypeUID CHANNEL_TYPE_GENERAL_DIMM = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_GENERAL_DIMM);
    public static final ChannelTypeUID CHANNEL_TYPE_GENERAL_SWITCH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_GENERAL_SWITCH);
    public static final ChannelTypeUID CHANNEL_TYPE_GENERAL_COMBINED_2_STAGE_SWITCH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH);
    public static final ChannelTypeUID CHANNEL_TYPE_GENERAL_COMBINED_3_STAGE_SWITCH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH);

    // shade
    public static final String CHANNEL_ID_SHADE = "shade";
    public static final String CHANNEL_ID_SHADE_ANGLE = "shadeAngle";

    public static final ChannelTypeUID CHANNEL_TYPE_SHADE_ANGLE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_SHADE_ANGLE);

    // scene
    public static final String CHANNEL_ID_SCENE = "scene";

    // sensor
    public static final String CHANNEL_ID_ELECTRIC_METER = "electricMeter";
    public static final String CHANNEL_ID_OUTPUT_CURRENT = "outputCurrent";
    public static final String CHANNEL_ID_ACTIVE_POWER = "activePower";
    public static final String CHANNEL_ID_TOTAL_ACTIVE_POWER = "totalActivePower";
    public static final String CHANNEL_ID_TOTAL_ELECTRIC_METER = "totalElectricMeter";

    public static final ChannelTypeUID CHANNEL_TYPE_ELECTRIC_METER = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_ELECTRIC_METER);
    public static final ChannelTypeUID CHANNEL_TYPE_OUTPUT_CURRENT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_OUTPUT_CURRENT);
    public static final ChannelTypeUID CHANNEL_TYPE_ACTIVE_POWER = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_ACTIVE_POWER);

    // options combined switches
    public static final String OPTION_COMBINED_BOTH_OFF = "0";
    public static final String OPTION_COMBINED_BOTH_ON = "200";
    public static final String OPTION_COMBINED_FIRST_ON = "90";
    public static final String OPTION_COMBINED_SECOND_ON = "130";

    /* config URIs */
    public static final String DEVICE_CONFIG = "binding:digitalstrom:device";
    public static final String GRAY_DEVICE_CONFIG = "binding:digitalstrom:grayDevice";
    public static final String DSS_BRIDE_CONFIG = "binding:digitalstrom:dssBridge";

    /* Bridge config properties */

    public static final String HOST = "ipAddress";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String APPLICATION_TOKEN = "applicationToken";
    public static final String DS_ID = "dSID";
    public static final String DS_NAME = "dsName";
    public static final String SENSOR_DATA_UPDATE_INTERVAL = "sensorDataUpdateInterval";
    public static final String TOTAL_POWER_UPDATE_INTERVAL = "totalPowerUpdateInterval";
    public static final String DEFAULT_TRASH_DEVICE_DELETE_TIME_KEY = "defaultTrashBinDeleteTime";
    public final static String SENSOR_WAIT_TIME = "sensorWaitTime";

    public static final String SERVER_CERT = "serverCert";

    /* Device config properties */

    public static final String DEVICE_UID = "dSUID";
    public static final String DEVICE_NAME = "deviceName";
    public static final String DEVICE_DSID = "dSID";
    public static final String DEVICE_HW_INFO = "hwInfo";
    public static final String DEVICE_ZONE_ID = "zoneID";
    public static final String DEVICE_GROUPS = "groups";
    public static final String DEVICE_OUTPUT_MODE = "outputmode";
    public static final String DEVICE_FUNCTIONAL_COLOR_GROUP = "funcColorGroup";
    public static final String DEVICE_METER_ID = "meterDSID";

    // Device properties scene
    public static final String DEVICE_SCENE = "scene"; // + number of scene

    // Sensor data channel properties
    public static final String ACTIVE_POWER_REFRESH_PRIORITY = "activePowerRefreshPriority";
    public static final String ELECTRIC_METER_REFRESH_PRIORITY = "electricMeterRefreshPriority";
    public static final String OUTPUT_CURRENT_REFRESH_PRIORITY = "outputCurrentRefreshPriority";
    // options
    public static final String REFRESH_PRIORITY_NEVER = "never";
    public static final String REFRESH_PRIORITY_LOW = "low";
    public static final String REFRESH_PRIORITY_MEDIUM = "medium";
    public static final String REFRESH_PRIORITY_HIGH = "high";

    /* Scene config */
    public static final String SCENE_NAME = "sceneName";
    public static final String SCENE_ZONE_ID = "zoneID";
    public static final String SCENE_GROUP_ID = "groupID";
    public static final String SCENE_ID = "sceneID";
}
