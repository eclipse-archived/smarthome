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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DS2438Configuration} is ahelper class for the multisensor thing configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2438Configuration {
    private final Logger logger = LoggerFactory.getLogger(DS2438Configuration.class);
    private static final Pattern ASSOC_SENSOR_ID_PATTERN = Pattern
            .compile("^(26|28|3A)([0-9A-Fa-f]{12})[0-9A-Fa-f]{2}$");

    private OwSensorType sensorSubType = OwSensorType.DS2438;
    private String vendor = "";
    private String hwRevision = "";
    private String prodDate = "";

    private final List<String> associatedSensorIds = new ArrayList<>();
    private final List<OwSensorType> associatedSensorTypes = new ArrayList<>();

    public DS2438Configuration(OwPageBuffer pageBuffer) {
        String sensorTypeId = pageBuffer.getPageString(3).substring(0, 2);
        switch (sensorTypeId) {
            case "19":
                vendor = "iButtonLink";
                sensorSubType = OwSensorType.MS_TH;
                break;
            case "F1":
            case "F3":
                vendor = "Elaborated Networks";
                sensorSubType = OwSensorType.MS_TH;
                break;
            case "F2":
                vendor = "Elaborated Networks";
                sensorSubType = OwSensorType.MS_TH_S;
                break;
            case "F4":
                vendor = "Elaborated Networks";
                sensorSubType = OwSensorType.MS_TV;
                break;
            default:
        }

        for (int i = 4; i < 7; i++) {
            Matcher matcher = ASSOC_SENSOR_ID_PATTERN.matcher(pageBuffer.getPageString(i));
            if (matcher.matches()) {
                associatedSensorIds.add(matcher.group(1) + "." + matcher.group(2));
                switch (matcher.group(1)) {
                    case "26":
                        associatedSensorTypes.add(OwSensorType.DS2438);
                        break;
                    case "28":
                        associatedSensorTypes.add(OwSensorType.DS18B20);
                        break;
                    case "3A":
                        associatedSensorTypes.add(OwSensorType.DS2413);
                        break;
                }
            }
        }

        if (sensorSubType != OwSensorType.DS2438) {
            prodDate = String.format("%d/%d", pageBuffer.getByte(5, 0),
                    256 * pageBuffer.getByte(5, 1) + pageBuffer.getByte(5, 2));
            hwRevision = String.valueOf(pageBuffer.getByte(5, 3));
        }
    }

    /**
     * get a list of sensor ids associated with this sensor
     *
     * @return a list of the sensor ids (if found), empty list otherwise
     */
    public List<String> getAssociatedSensorIds() {
        return associatedSensorIds;
    }

    /**
     * get a list of sensor types associated with this sensor
     *
     * @return a list of the sensor typess (if found), empty list otherwise
     */
    public List<OwSensorType> getAssociatedSensorTypes() {
        return associatedSensorTypes;
    }

    /**
     * get the number of associated sensors
     *
     * @return the number
     */
    public int getAssociatedSensorCount() {
        return associatedSensorIds.size();
    }

    /**
     * get hardware revision (available on some multisensors)
     *
     * @return hardware revision
     */
    public String getHardwareRevision() {
        return hwRevision;
    }

    /**
     * get production date (available on some multisensors)
     *
     * @return production date in ww/yy
     */
    public String getProductionDate() {
        return prodDate;
    }

    /**
     * get sensor type (without associated sensors)
     *
     * @return basic sensor type
     */
    public OwSensorType getSensorSubType() {
        return sensorSubType;
    }

    /**
     * get vendor name (if available)
     *
     * @return the vendor name
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * determine multisensor type
     *
     * @param mainsensorType the type of the main sensor
     * @param associatedSensorTypes a list of OwSensorTypes of all associated sensors
     * @return the multisensor type (if known)
     */
    public static OwSensorType getMultisensorType(OwSensorType mainsensorType,
            List<OwSensorType> associatedSensorTypes) {
        OwSensorType multisensorType = OwSensorType.UNKNOWN;
        switch (associatedSensorTypes.size()) {
            case 0:
                multisensorType = mainsensorType;
                break;
            case 1:
                if (mainsensorType == OwSensorType.MS_TH_S && associatedSensorTypes.contains(OwSensorType.DS18B20)) {
                    multisensorType = OwSensorType.BMS_S;
                } else if (mainsensorType == OwSensorType.MS_TH
                        && associatedSensorTypes.contains(OwSensorType.DS18B20)) {
                    multisensorType = OwSensorType.BMS;
                }
                break;
            case 3:
                if (mainsensorType == OwSensorType.MS_TH_S && associatedSensorTypes.contains(OwSensorType.MS_TV)
                        && associatedSensorTypes.contains(OwSensorType.DS18B20)
                        && associatedSensorTypes.contains(OwSensorType.DS2413)) {
                    // two DS2438 (first THS, second TV), DS18B20, DS2413
                    multisensorType = OwSensorType.AMS_S;
                } else if (mainsensorType == OwSensorType.MS_TH && associatedSensorTypes.contains(OwSensorType.MS_TV)
                        && associatedSensorTypes.contains(OwSensorType.DS18B20)
                        && associatedSensorTypes.contains(OwSensorType.DS2413)) {
                    // two DS2438 (first TH, second TV), DS18B20, DS2413
                    multisensorType = OwSensorType.AMS;
                }
                break;
            default:
        }

        return multisensorType;
    }
}
