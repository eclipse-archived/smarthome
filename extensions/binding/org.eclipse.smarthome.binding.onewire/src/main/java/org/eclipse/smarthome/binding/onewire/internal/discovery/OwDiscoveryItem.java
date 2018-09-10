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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.DS2438Configuration;
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

    private final String familyId;
    private final String sensorId;
    private OwSensorType sensorType = OwSensorType.UNKNOWN;
    private String vendor = "Dallas/Maxim";
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
                DS2438Configuration config = new DS2438Configuration(pages);
                associatedSensorIds.addAll(config.getAssociatedSensorIds());
                logger.trace("found associated sensors: {}", associatedSensorIds);
                vendor = config.getVendor();
                hwRevision = config.getHardwareRevision();
                prodDate = config.getProductionDate();
                sensorType = config.getSensorSubType();
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
     * @return the sensors family id
     */
    public String getFamilyId() {
        return familyId;
    }

    /**
     * get sensor type
     *
     * @return full sensor type
     */
    public OwSensorType getSensorType() {
        return sensorType;
    }

    /**
     * get sensor id (familyId.xxxxxxxxxx)
     *
     * @return sensor id
     */
    public String getSensorId() {
        return sensorId;
    }

    /**
     * normalized sensor id (for naming the discovery result)
     *
     * @return sensor id in format familyId_xxxxxxxxxx
     */
    public String getNormalizedSensorId() {
        return sensorId.replace(".", "_");
    }

    /**
     * get vendor name (if available)
     *
     * @return vendor name
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * get production date (available on some multisensors)
     *
     * @return production date in format ww/yy
     */
    public String getProductionDate() {
        return prodDate;
    }

    /**
     * get hardware revision (available on some multisensors)
     *
     * @return hardware revision (where available)
     */
    public String getHardwareRevision() {
        return hwRevision;
    }

    /**
     * get this sensors ThingTypeUID
     *
     * @return ThingTypeUID if mapping successful
     */
    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    /**
     * check if associated sensors have been found
     *
     * @return true if this sensors pages include other sensor ids
     */
    public boolean hasAssociatedSensorIds() {
        return !associatedSensorIds.isEmpty();
    }

    /**
     * get a list of all sensors associated to this sensor
     *
     * @return list of strings
     */
    public List<String> getAssociatedSensorIds() {
        return associatedSensorIds;
    }

    /**
     * check if secondary sensors have been added
     *
     * @return true if sensors have been added
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
            addAssociatedSensor(associatedSensor);
        }
    }

    /**
     * get all secondary sensors
     *
     * @return a list of OwDiscoveryItems
     */
    public List<OwDiscoveryItem> getAssociatedSensors() {
        return associatedSensors;
    }

    /**
     * get all secondary sensors of a given type
     *
     * @param sensorType filter for sensors
     * @return a list of OwDiscoveryItems
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
     * @return number of sensors
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
        logger.debug("checkSensorType: {} with {}", this, associatedSensors);

        switch (sensorType) {
            case AMS:
            case AMS_S:
            case BMS:
            case BMS_S:
            case MS_TH:
            case MS_TH_S:
            case MS_TV:
                sensorType = DS2438Configuration.getMultisensorType(sensorType, associatedSensorTypes);
                break;
            default:
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
        return String.format("%s/%s (associated: %d)", sensorId, sensorType, associatedSensors.size());
    }
}
