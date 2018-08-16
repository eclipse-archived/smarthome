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
package org.eclipse.smarthome.binding.onewire.internal.discovery;

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.OwPageBuffer;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwDiscoveryItem} class defines a discovery item for OneWire devices
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwDiscoveryItem {
    private final Logger logger = LoggerFactory.getLogger(OwDiscoveryItem.class);
    private static final Pattern ASSOC_SENSOR_PATTERN = Pattern.compile("^(26|28|3A)([0-9A-Fa-f]{12})[0-9A-Fa-f]{2}$");

    private final String familyId;
    private final String sensorId;
    private OwSensorType sensorType = OwSensorType.UNKNOWN;
    private String vendor = "Dallas/Maxim";
    private String sensorTypeId = "";

    private String hwRevision = "";
    private String prodDate = "";

    private OwPageBuffer pages = new OwPageBuffer();

    private ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, "");

    private final List<String> associatedSensorIds = new ArrayList<>();
    private final List<OwSensorType> associatedSensorTypes = new ArrayList<>();
    private final List<OwDiscoveryItem> associatedSensors = new ArrayList<>();

    public OwDiscoveryItem(OwBaseBridgeHandler bridgeHandler, String sensorId) throws OwException {
        this.sensorId = sensorId;
        familyId = sensorId.substring(0, 2);
        sensorType = bridgeHandler.getType(sensorId);
        switch (sensorType) {
            case DS2438:
                pages = bridgeHandler.readPages(sensorId);
                sensorTypeId = pages.getPageString(3).substring(0, 2);
                for (int i = 4; i < 7; i++) {
                    Matcher matcher = ASSOC_SENSOR_PATTERN.matcher(pages.getPageString(i));
                    if (matcher.matches()) {
                        associatedSensorIds.add(matcher.group(1) + "." + matcher.group(2));
                    }
                }
                logger.trace("found associated sensors: {}", associatedSensorIds);
                switch (sensorTypeId) {
                    case "19":
                        vendor = "iButtonLink";
                        sensorType = OwSensorType.MS_TH;
                        break;
                    case "F1":
                    case "F3":
                        vendor = "Elaborated Networks";
                        sensorType = OwSensorType.MS_TH;
                        break;
                    case "F2":
                        vendor = "Elaborated Networks";
                        sensorType = OwSensorType.MS_TH_S;
                        break;
                    case "F4":
                        vendor = "Elaborated Networks";
                        sensorType = OwSensorType.MS_TV;
                        break;
                    default:
                        logger.info("unknown multisensor type {} (id: {})", sensorTypeId, sensorId);
                }
                break;
            default:
        }
        if (THING_TYPE_MAP.containsKey(sensorType)) {
            thingTypeUID = THING_TYPE_MAP.get(sensorType);
        } else {
            throw new OwException(sensorType + " cannot be mapped to thing type");
        }
    }

    /**
     * get family ID of this sensor (first two characters in sensor id)
     *
     * @return
     */
    public String getFamilyId() {
        return familyId;
    }

    /**
     * get sensor type
     *
     * @return
     */
    public OwSensorType getSensorType() {
        return sensorType;
    }

    /**
     * get sensor id (familyId.xxxxxxxxxx)
     *
     * @return
     */
    public String getSensorId() {
        return sensorId;
    }

    /**
     * normalized sensor id (for naming the discovery result)
     *
     * @return
     */
    public String getNormalizedSensorId() {
        return sensorId.replace(".", "_");
    }

    /**
     * get vendor name (if available)
     *
     * @return
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * get production date (available on some multisensors)
     *
     * @return
     */
    public String getProdDate() {
        return prodDate;
    }

    /**
     * get production date (available on some multisensors)
     *
     * @return
     */
    public String getHwRevision() {
        return hwRevision;
    }

    /**
     * get this sensors ThingTypeUID
     *
     * @return
     */
    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    /**
     * true if this sensors pages include other sensor ids
     *
     * @return
     */
    public boolean hasAssociatedSensorIds() {
        return !associatedSensorIds.isEmpty();
    }

    /**
     * get a list of all sensors associated to this sensor
     *
     * @return
     */
    public List<String> getAssociatedSensorIds() {
        return associatedSensorIds;
    }

    /**
     * check if secondary sensors have been added
     *
     * @return
     */
    public boolean hasAssociatedSensors() {
        return !associatedSensors.isEmpty();
    }

    /**
     * add a sensor as secondary to this sensor
     *
     * @param associatedSensor
     */
    public void addAssociatedSensor(OwDiscoveryItem associatedSensor) {
        associatedSensors.add(associatedSensor);
        associatedSensorTypes.add(associatedSensor.getSensorType());
    }

    /**
     * bulk add secondary sensors
     *
     * @param associatedSensors
     */
    public void addAssociatedSensors(List<OwDiscoveryItem> associatedSensors) {
        for (OwDiscoveryItem associatedSensor : associatedSensors) {
            this.associatedSensors.add(associatedSensor);
            associatedSensorTypes.add(associatedSensor.getSensorType());
        }
    }

    /**
     * get all secondary sensors
     *
     * @return
     */
    public List<OwDiscoveryItem> getAssociatedSensors() {
        return associatedSensors;
    }

    /**
     * get all secondary sensors of a given type
     *
     * @param sensorType filter for sensors
     * @return
     */
    public List<OwDiscoveryItem> getAssociatedSensors(OwSensorType sensorType) {
        List<OwDiscoveryItem> returnList = new ArrayList<>();
        for (OwDiscoveryItem owDiscoveryItem : associatedSensors) {
            if (sensorType == owDiscoveryItem.getSensorType()) {
                returnList.add(owDiscoveryItem);
            }
        }
        return returnList;
    }

    /**
     * get the number of secondary sensors
     *
     * @return
     */
    public int getAssociatedSensorCount() {
        return associatedSensors.size() + 1;
    }

    /**
     * clear all secondary sensors
     *
     */
    public void clearAssociatedSensors() {
        associatedSensors.clear();
    }

    /**
     * determine this sensors type
     */
    public void checkSensorType() {
        logger.debug("main sensor: {}/{} (id: {})", sensorType, sensorTypeId, sensorId);

        switch (associatedSensors.size()) {
            case 0:
                break;
            case 1:
                if (sensorType == OwSensorType.MS_TH_S && associatedSensorTypes.contains(OwSensorType.DS18B20)) {
                    sensorType = OwSensorType.BMS_S;
                    prodDate = String.format("%d/%d", pages.getByte(5, 0),
                            256 * pages.getByte(5, 1) + pages.getByte(5, 2));
                    hwRevision = String.valueOf(pages.getByte(5, 3));
                } else if (sensorType == OwSensorType.MS_TH && associatedSensorTypes.contains(OwSensorType.DS18B20)) {
                    sensorType = OwSensorType.BMS;
                    prodDate = String.format("%d/%d", pages.getByte(5, 0),
                            256 * pages.getByte(5, 1) + pages.getByte(5, 2));
                    hwRevision = String.valueOf(pages.getByte(5, 3));
                } else {
                    logger.info("unknown multisensor id: {} ", sensorId);
                }
                break;
            case 3:
                logger.debug("{}", associatedSensors);
                if (sensorType == OwSensorType.MS_TH_S && associatedSensorTypes.contains(OwSensorType.MS_TV)
                        && associatedSensorTypes.contains(OwSensorType.DS18B20)
                        && associatedSensorTypes.contains(OwSensorType.DS2413)) {
                    // two DS2438 (first THS, second TV), DS18B20, DS2413
                    sensorType = OwSensorType.AMS_S;
                    prodDate = String.format("%d/%d", pages.getByte(5, 0),
                            256 * pages.getByte(5, 1) + pages.getByte(5, 2));
                    hwRevision = String.valueOf(pages.getByte(5, 3));
                    logger.trace("{} {}", pages, prodDate);
                } else if (sensorType == OwSensorType.MS_TH && associatedSensorTypes.contains(OwSensorType.MS_TV)
                        && associatedSensorTypes.contains(OwSensorType.DS18B20)
                        && associatedSensorTypes.contains(OwSensorType.DS2413)) {
                    // two DS2438 (first TH, second TV), DS18B20, DS2413
                    sensorType = OwSensorType.AMS;
                    prodDate = String.format("%d/%d", pages.getByte(5, 0),
                            256 * pages.getByte(5, 1) + pages.getByte(5, 2));
                    hwRevision = String.valueOf(pages.getByte(5, 3));
                    logger.trace("{} {}", pages, prodDate);
                } else {
                    logger.info("unknown multisensor with id: {} ", sensorId);
                }
                break;
            default:
                logger.info("unknown multisensor with id: {}", sensorId);
        }

        if (THING_TYPE_MAP.containsKey(sensorType)) {
            thingTypeUID = THING_TYPE_MAP.get(sensorType);
        }
    }

    /**
     * get Label "thingtype (id)"
     *
     * @return the thing label
     */
    public String getLabel() {
        return THING_LABEL_MAP.get(thingTypeUID) + " (" + this.sensorId + ")";
    }

    @Override
    public String toString() {
        return String.format("type %s, id %s (associated: %d)", sensorType, sensorId, associatedSensors.size());
    }
}
