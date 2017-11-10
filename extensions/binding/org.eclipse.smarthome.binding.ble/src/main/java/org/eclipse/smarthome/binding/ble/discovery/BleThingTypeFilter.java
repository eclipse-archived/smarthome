/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.discovery;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.smarthome.binding.ble.BleBindingConstants;
import org.eclipse.smarthome.binding.ble.BleDevice;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BleThingTypeFilter} handles the BLE filter which is used to select a thing type from the device
 * attributes.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BleThingTypeFilter {
    private final static Logger logger = LoggerFactory.getLogger(BleThingTypeFilter.class);

    static final String FILTER_NAME = "NAME";
    static final String FILTER_SVC = "SVC";
    static final String FILTER_MANUFACTURER = "MANUFACTURER";

    private static ThingTypeRegistry thingTypeRegistry;

    private static Set<ThingTypeUID> bleThingTypeUIDList = new HashSet<ThingTypeUID>();

    protected static void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        BleThingTypeFilter.thingTypeRegistry = thingTypeRegistry;
    }

    protected static void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        BleThingTypeFilter.thingTypeRegistry = null;
    }

    private static void initialiseThingTypeList() {
        logger.debug("initialiseThingTypeList 1");
        // Check that we know about the registry
        if (thingTypeRegistry == null) {
            return;
        }
        logger.debug("initialiseThingTypeList 2");

        bleThingTypeUIDList = new HashSet<ThingTypeUID>();

        // Get all the thing types
        Collection<ThingType> thingTypes = thingTypeRegistry.getThingTypes();
        for (ThingType thingType : thingTypes) {
            // Is this for our binding?
            if (BleBindingConstants.BINDING_ID.equals(thingType.getBindingId()) == false) {
                continue;
            }

            // Add to the list of all things supported by this binding
            bleThingTypeUIDList.add(thingType.getUID());
        }
        logger.debug("initialiseThingTypeList 3");
    }

    public static Set<ThingTypeUID> getSupportedThingTypes() {
        logger.debug("getSupportedThingTypes 1");
        if (bleThingTypeUIDList.isEmpty()) {
            initialiseThingTypeList();
        }
        return bleThingTypeUIDList;
    }

    public static boolean checkFilters(ThingTypeUID uid, BleDevice device, String[] filterArray) {
        logger.debug("checkFilters 1");
        for (String filterOption : filterArray) {
            String[] filter = filterOption.split("=");
            if (filter == null || filter.length != 2) {
                logger.debug("BLE thing {} has invalid filter option {}", uid, filterOption);
                return false;
            }

            switch (filter[0].trim().toUpperCase()) {
                case FILTER_NAME:
                    if (!filter[1].trim().equals(device.getName())) {
                        return false;
                    }
                    break;
                case FILTER_SVC:
                    if (!device.supportsService(UUID.fromString(filter[1]))) {
                        return false;
                    }
                    break;
                case FILTER_MANUFACTURER:
                    if (device.getManufacturerId() != Integer.parseInt(filter[1], 16)) {
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    /**
     * Check all BLE thing types to find one that matches the filters of the new device.
     *
     * @param device the newly discovered {@link BleDevice}
     * @return the {@link ThingTypeUID} of the new thing or null if no filters matches
     */
    public static ThingTypeUID findThingType(BleDevice device) {
        logger.debug("findThingType 1");
        // Check that we know about the registry
        if (thingTypeRegistry == null) {
            logger.debug("BLE thing discovered, but thingTypeRegistry not set.");
            return null;
        }

        bleThingTypeUIDList = new HashSet<ThingTypeUID>();

        // Get all the thing types
        Collection<ThingType> thingTypes = thingTypeRegistry.getThingTypes();
        for (ThingType thingType : thingTypes) {
            // Is this for our binding?
            if (BleBindingConstants.BINDING_ID.equals(thingType.getBindingId()) == false) {
                continue;
            }

            // Get the properties and process filters
            Map<String, String> thingProperties = thingType.getProperties();

            if (thingProperties.get(BleBindingConstants.XMLPROPERTY_BLE_FILTER) == null) {
                logger.debug("BLE thing {} has no filter so can't be discovered!", thingType.getUID());
                continue;
            }

            String[] filterArray = thingProperties.get(BleBindingConstants.XMLPROPERTY_BLE_FILTER).split(",");
            if (checkFilters(thingType.getUID(), device, filterArray)) {
                return thingType.getUID();
            }
        }

        return null;
    }

}
