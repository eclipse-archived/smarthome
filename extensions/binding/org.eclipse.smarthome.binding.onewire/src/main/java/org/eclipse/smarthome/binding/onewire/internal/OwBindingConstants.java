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
package org.eclipse.smarthome.binding.onewire.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link OneWireBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwBindingConstants {

    public static final String BINDING_ID = "onewire";
    public static final int BINDING_THING_TYPE_VERSION = 1;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OWSERVER = new ThingTypeUID(BINDING_ID, "owserver");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    public static final ThingTypeUID THING_TYPE_IBUTTON = new ThingTypeUID(BINDING_ID, "ibutton");
    public static final ThingTypeUID THING_TYPE_DIGITALIO = new ThingTypeUID(BINDING_ID, "digitalio");
    public static final ThingTypeUID THING_TYPE_DIGITALIO2 = new ThingTypeUID(BINDING_ID, "digitalio2");
    public static final ThingTypeUID THING_TYPE_DIGITALIO8 = new ThingTypeUID(BINDING_ID, "digitalio8");
    public static final ThingTypeUID THING_TYPE_MS_TH = new ThingTypeUID(BINDING_ID, "ms-th");
    public static final ThingTypeUID THING_TYPE_MS_THS = new ThingTypeUID(BINDING_ID, "ms-ths");
    public static final ThingTypeUID THING_TYPE_MS_TV = new ThingTypeUID(BINDING_ID, "ms-tv");
    public static final ThingTypeUID THING_TYPE_BMS = new ThingTypeUID(BINDING_ID, "bms");
    public static final ThingTypeUID THING_TYPE_AMS = new ThingTypeUID(BINDING_ID, "ams");
    public static final ThingTypeUID THING_TYPE_COUNTER2 = new ThingTypeUID(BINDING_ID, "counter2");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_OWSERVER, THING_TYPE_TEMPERATURE, THING_TYPE_IBUTTON, THING_TYPE_DIGITALIO,
                    THING_TYPE_DIGITALIO2, THING_TYPE_DIGITALIO8, THING_TYPE_AMS, THING_TYPE_BMS, THING_TYPE_MS_TH,
                    THING_TYPE_MS_THS, THING_TYPE_MS_TV, THING_TYPE_COUNTER2));

    // List of all config options
    public static final String CONFIG_ADDRESS = "network-address";
    public static final String CONFIG_PORT = "port";

    public static final String CONFIG_ID = "id";
    public static final String CONFIG_RESOLUTION = "resolution";
    public static final String CONFIG_IGNORE_POR = "ignorepor";
    public static final String CONFIG_REFRESH = "refresh";
    public static final String CONFIG_DIGITALREFRESH = "digitalrefresh";
    public static final String CONFIG_OFFSET = "offset";
    public static final String CONFIG_HUMIDITY = "humidity";
    public static final String CONFIG_DIGITAL_MODE = "mode";
    public static final String CONFIG_DIGITAL_LOGIC = "logic";
    public static final String CONFIG_TEMPERATURESENSOR = "temperaturesensor";
    public static final String CONFIG_LIGHTSENSOR = "lightsensor";

    // list of all properties
    public static final String PROPERTY_MODELID = "modelId";
    public static final String PROPERTY_VENDOR = "vendor";
    public static final String PROPERTY_SENSORCOUNT = "sensorCount";
    public static final String PROPERTY_PROD_DATE = "prodDate";
    public static final String PROPERTY_HW_REVISION = "hwRevision";
    public static final String PROPERTY_THING_TYPE_VERSION = "thingTypeVersion";

    // List of all channel ids
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_ABSOLUTE_HUMIDITY = "absolutehumidity";
    public static final String CHANNEL_DEWPOINT = "dewpoint";
    public static final String CHANNEL_PRESENT = "present";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_LIGHT = "light";
    public static final String CHANNEL_SUPPLYVOLTAGE = "supplyvoltage";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_DIGITAL = "digital";
    public static final String CHANNEL_COUNTER = "counter";

    // Maps for Discovery
    public static final Map<OwSensorType, ThingTypeUID> THING_TYPE_MAP;
    public static final Map<ThingTypeUID, String> THING_LABEL_MAP;

    static {
        Map<OwSensorType, ThingTypeUID> initThingTypeMap = new HashMap<OwSensorType, ThingTypeUID>();
        initThingTypeMap.put(OwSensorType.DS1420, THING_TYPE_IBUTTON);
        initThingTypeMap.put(OwSensorType.DS18B20, THING_TYPE_TEMPERATURE);
        initThingTypeMap.put(OwSensorType.DS18S20, THING_TYPE_TEMPERATURE);
        initThingTypeMap.put(OwSensorType.DS1822, THING_TYPE_TEMPERATURE);
        initThingTypeMap.put(OwSensorType.DS1923, THING_TYPE_MS_TH);
        initThingTypeMap.put(OwSensorType.DS2401, THING_TYPE_IBUTTON);
        initThingTypeMap.put(OwSensorType.DS2405, THING_TYPE_DIGITALIO);
        initThingTypeMap.put(OwSensorType.DS2406, THING_TYPE_DIGITALIO2);
        initThingTypeMap.put(OwSensorType.DS2408, THING_TYPE_DIGITALIO8);
        initThingTypeMap.put(OwSensorType.DS2413, THING_TYPE_DIGITALIO2);
        initThingTypeMap.put(OwSensorType.MS_TH, THING_TYPE_MS_TH);
        initThingTypeMap.put(OwSensorType.MS_TH_S, THING_TYPE_MS_THS);
        initThingTypeMap.put(OwSensorType.MS_TV, THING_TYPE_MS_TV);
        initThingTypeMap.put(OwSensorType.BMS, THING_TYPE_BMS);
        initThingTypeMap.put(OwSensorType.BMS_S, THING_TYPE_BMS);
        initThingTypeMap.put(OwSensorType.AMS, THING_TYPE_AMS);
        initThingTypeMap.put(OwSensorType.AMS_S, THING_TYPE_AMS);
        initThingTypeMap.put(OwSensorType.DS2423, THING_TYPE_COUNTER2);
        THING_TYPE_MAP = Collections.unmodifiableMap(initThingTypeMap);

        Map<ThingTypeUID, String> initThingLabelMap = new HashMap<ThingTypeUID, String>();
        initThingLabelMap.put(THING_TYPE_TEMPERATURE, "Temperature sensor");
        initThingLabelMap.put(THING_TYPE_IBUTTON, "iButton");
        initThingLabelMap.put(THING_TYPE_DIGITALIO, "Digital I/O");
        initThingLabelMap.put(THING_TYPE_DIGITALIO2, "Dual Digital I/O");
        initThingLabelMap.put(THING_TYPE_DIGITALIO8, "Octal Digital I/O");
        initThingLabelMap.put(THING_TYPE_MS_TH, "Multisensor");
        initThingLabelMap.put(THING_TYPE_MS_THS, "Multisensor");
        initThingLabelMap.put(THING_TYPE_MS_TV, "Multisensor");
        initThingLabelMap.put(THING_TYPE_BMS, "Elaborated Networks BMS");
        initThingLabelMap.put(THING_TYPE_AMS, "Elaborated Networks AMS");
        initThingLabelMap.put(THING_TYPE_COUNTER2, "Dual Counter");
        THING_LABEL_MAP = Collections.unmodifiableMap(initThingLabelMap);
    }

    public static final Pattern SENSOR_ID_PATTERN = Pattern.compile("^\\/?([0-9A-Fa-f]{2}\\.[0-9A-Fa-f]{12})$");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_TEMPERATURE = new ChannelTypeUID(BINDING_ID, "temperature");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_TEMPERATURE_POR = new ChannelTypeUID(BINDING_ID,
            "temperature-por");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_TEMPERATURE_POR_RES = new ChannelTypeUID(BINDING_ID,
            "temperature-por-res");
}
