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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OneWireDiscoveryService} implements the discovery service for the OneWire binding.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class OwDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(OwDiscoveryService.class);

    private final OwBaseBridgeHandler owBridgeHandler;

    public OwDiscoveryService(OwBaseBridgeHandler owBridgeHandler) {
        super(SUPPORTED_THING_TYPES, 60, false);
        this.owBridgeHandler = owBridgeHandler;
        logger.debug("registering discovery service for {}", owBridgeHandler);
    }

    @Override
    public void startScan() {
        List<String> directory;
        ThingUID bridgeUID = owBridgeHandler.getThing().getUID();

        try {
            directory = owBridgeHandler.getDirectory();
        } catch (OwException e) {
            logger.info("could not get directory for {}", bridgeUID);
            return;
        }

        Collections.sort(directory);

        Map<String, OwDiscoveryItem> owDiscoveryItems = new HashMap<>();
        Map<String, String> associationMap = new HashMap<>();

        // find all valid sensors
        for (String directoryEntry : directory) {
            Matcher discoveryMatch = SENSOR_ID_PATTERN.matcher(directoryEntry);

            if (discoveryMatch.matches()) {
                String sensorId = discoveryMatch.group(1);
                try {
                    OwDiscoveryItem owDiscoveryItem = new OwDiscoveryItem(owBridgeHandler, sensorId);
                    owDiscoveryItems.put(owDiscoveryItem.getSensorId(), owDiscoveryItem);
                    if (owDiscoveryItem.hasAssociatedSensorIds()) {
                        for (String associatedSensorId : owDiscoveryItem.getAssociatedSensorIds()) {
                            associationMap.put(associatedSensorId, owDiscoveryItem.getSensorId());
                        }
                    }
                    logger.trace("found sensor {} (id: {})", owDiscoveryItem.getSensorType(), sensorId);
                } catch (OwException e) {
                    logger.info("sensor-id {}: {}", sensorId, e.getMessage());
                }
            }
        }

        // resolve all non-DS2438
        Iterator<Entry<String, String>> associationMapIterator = associationMap.entrySet().iterator();
        while (associationMapIterator.hasNext()) {
            Entry<String, String> entry = associationMapIterator.next();
            String associatedSensor = entry.getKey();
            String mainSensor = entry.getValue();

            if (!associatedSensor.substring(0, 2).equals("26")) {
                if (owDiscoveryItems.containsKey(associatedSensor) && owDiscoveryItems.containsKey(mainSensor)) {
                    owDiscoveryItems.get(mainSensor).addAssociatedSensor(owDiscoveryItems.get(associatedSensor));
                    owDiscoveryItems.remove(associatedSensor);
                } else {
                    logger.info("cannot resolve association {}->{}, please check your sensor hardware",
                            associatedSensor, mainSensor);
                }
                associationMapIterator.remove();
            }
        }
        // resolve DS2438
        associationMapIterator = associationMap.entrySet().iterator();
        while (associationMapIterator.hasNext()) {
            Entry<String, String> entry = associationMapIterator.next();
            String associatedSensor = entry.getKey();
            String mainSensor = entry.getValue();

            if (owDiscoveryItems.containsKey(associatedSensor) && owDiscoveryItems.containsKey(mainSensor)) {
                if (owDiscoveryItems.get(associatedSensor).hasAssociatedSensors()) {
                    owDiscoveryItems.get(mainSensor)
                            .addAssociatedSensors(owDiscoveryItems.get(associatedSensor).getAssociatedSensors());
                    owDiscoveryItems.get(associatedSensor).clearAssociatedSensors();
                }
                owDiscoveryItems.get(mainSensor).addAssociatedSensor(owDiscoveryItems.get(associatedSensor));
                owDiscoveryItems.remove(associatedSensor);
            } else {
                logger.info("cannot resolve association {}->{}, please check your sensor hardware", entry.getKey(),
                        entry.getValue());
            }
        }

        // make discovery results
        for (OwDiscoveryItem owDiscoveryItem : owDiscoveryItems.values()) {
            owDiscoveryItem.checkSensorType();
            ThingTypeUID thingTypeUID = owDiscoveryItem.getThingTypeUID();

            String normalizedId = owDiscoveryItem.getNormalizedSensorId();
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, normalizedId);
            logger.debug("created thing UID {} for sensor {}, type {}", thingUID, owDiscoveryItem.getSensorId(),
                    owDiscoveryItem.getSensorType());

            Map<String, Object> properties = new HashMap<>(1);
            properties.put(PROPERTY_MODELID, owDiscoveryItem.getSensorType().toString());
            properties.put(PROPERTY_VENDOR, owDiscoveryItem.getVendor());
            properties.put(PROPERTY_SENSORCOUNT, String.valueOf(owDiscoveryItem.getAssociatedSensorCount()));
            if (thingTypeUID.equals(THING_TYPE_BMS)) {
                properties.put(CONFIG_ID, owDiscoveryItem.getSensorId());
                properties.put(CONFIG_ID + "1", owDiscoveryItem.getAssociatedSensors().get(0).getSensorId());
                properties.put(CONFIG_TEMPERATURESENSOR, "DS18B20");
                properties.put(CONFIG_LIGHTSENSOR,
                        String.valueOf(owDiscoveryItem.getSensorType() == OwSensorType.BMS_S));
            } else if (thingTypeUID.equals(THING_TYPE_AMS)) {
                properties.put(CONFIG_ID, owDiscoveryItem.getSensorId());
                properties.put(CONFIG_ID + "1",
                        owDiscoveryItem.getAssociatedSensors(OwSensorType.DS18B20).get(0).getSensorId());
                properties.put(CONFIG_ID + "2",
                        owDiscoveryItem.getAssociatedSensors(OwSensorType.MS_TV).get(0).getSensorId());
                properties.put(CONFIG_ID + "3",
                        owDiscoveryItem.getAssociatedSensors(OwSensorType.DS2413).get(0).getSensorId());
                properties.put(CONFIG_TEMPERATURESENSOR, "DS18B20");
                properties.put(CONFIG_LIGHTSENSOR,
                        String.valueOf(owDiscoveryItem.getSensorType() == OwSensorType.AMS_S));
            } else {
                properties.put(CONFIG_ID, owDiscoveryItem.getSensorId());
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(owDiscoveryItem.getLabel()).build();

            thingDiscovered(discoveryResult);
        }
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
    }

}
