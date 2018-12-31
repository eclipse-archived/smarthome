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
package org.eclipse.smarthome.binding.onewire.internal.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.OwPageBuffer;
import org.eclipse.smarthome.binding.onewire.internal.device.OwDeviceParameterMap;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwBaseBridgeHandler} class implements the refresher and the interface for reading from the bridge
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class OwBaseBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OwBaseBridgeHandler.class);

    // set by implementation when bridge is ready
    protected boolean refreshable = false;

    public OwBaseBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    protected ScheduledFuture<?> refreshTask = scheduler.scheduleWithFixedDelay(() -> refresh(), 1, 1000,
            TimeUnit.MILLISECONDS);

    // thing update
    private final List<Thing> updatePropertiesThingList = new CopyOnWriteArrayList<>();
    private Iterator<Thing> updatePropertiesThingListIterator = updatePropertiesThingList.iterator();

    /**
     * refresh all sensors on this bridge
     */
    private void refresh() {
        if (refreshable) {
            long now = System.currentTimeMillis();

            // refresh thing channels
            List<Thing> thingList = getThing().getThings();
            int thingCount = thingList.size();
            Iterator<Thing> childListIterator = thingList.iterator();
            logger.trace("refreshTask starts at {}, {} childs", now, thingCount);
            while (childListIterator.hasNext() && refreshable) {
                Thing owThing = childListIterator.next();

                logger.trace("refresh: etting handler for {} ({} to go)", owThing.getUID(), thingCount);
                OwBaseThingHandler owHandler = (OwBaseThingHandler) owThing.getHandler();
                if (owHandler != null) {
                    if (owHandler.isRefreshable()) {
                        logger.trace("{} initialized, refreshing", owThing.getUID());
                        owHandler.refresh(OwBaseBridgeHandler.this, now);
                    } else {
                        logger.trace("{} not initialized, skipping refresh", owThing.getUID());
                    }
                } else {
                    logger.debug("{} handler missing", owThing.getUID());
                }
                thingCount--;
            }

            // update thing properties (only one per refresh cycle)
            if (updatePropertiesThingListIterator.hasNext()) {
                Thing owThing = updatePropertiesThingListIterator.next();
                logger.trace("update: getting handler for {} ({} total in list)", owThing.getUID(),
                        updatePropertiesThingList.size());
                OwBaseThingHandler owHandler = (OwBaseThingHandler) owThing.getHandler();
                if (owHandler != null) {
                    try {
                        Map<String, String> properties = new HashMap<String, String>();
                        properties.putAll(owThing.getProperties());
                        properties.putAll(owHandler.updateSensorProperties(this));
                        owThing.setProperties(properties);
                        owHandler.initialize();

                        updatePropertiesThingList.remove(owThing);
                        logger.debug("{} sucessfully updated properties, removing from property update list",
                                owThing.getUID());
                    } catch (OwException e) {
                        logger.debug("updating thing properties for {} failed: {}", owThing.getUID(), e.getMessage());
                    }
                } else {
                    updatePropertiesThingList.remove(owThing);
                    logger.debug("{} is missing handler, removing from property update list", owThing.getUID());
                }
            } else {
                // old iterator is empty, check if we have new things to update
                updatePropertiesThingListIterator = updatePropertiesThingList.iterator();
            }
        }

    };

    @Override
    public void initialize() {
        if (refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleWithFixedDelay(() -> refresh(), 1, 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void dispose() {
        refreshable = false;
        if (!refreshTask.isCancelled()) {
            refreshTask.cancel(false);
        }
    }

    /**
     * adds a thing to the property update list
     *
     * @param thing the thing to be updated
     */
    public void addToUpdatePropertyThingList(Thing thing) {
        updatePropertiesThingList.add(thing);
    }

    /**
     * get all sensors attached to this bridge
     *
     * @return a list of all sensor-IDs
     */
    public abstract List<String> getDirectory() throws OwException;

    /**
     * check the presence of a sensor on the bus
     *
     * @param sensorId the sensor's full ID
     * @return ON if present, OFF if missing
     */
    public abstract State checkPresence(String sensorId) throws OwException;

    /**
     * get a sensors type string
     *
     * @param sensorId the sensor's full ID
     * @return a String containing the sensor type
     */
    public abstract OwSensorType getType(String sensorId) throws OwException;

    /**
     * get full sensor information stored in pages (not available on all sensors)
     *
     * @param sensorId the sensor's full ID
     * @return a OwPageBuffer object containing the requested information
     */
    public abstract OwPageBuffer readPages(String sensorId) throws OwException;

    /**
     * read a single decimal value from a sensor
     *
     * @param sensorId  sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a DecimalType
     */
    public abstract State readDecimalType(String sensorId, OwDeviceParameterMap parameter) throws OwException;

    /**
     * read an array of decimal values from a sensor
     *
     * @param sensorId  sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a list of DecimalType values
     */
    public abstract List<State> readDecimalTypeArray(String sensorId, OwDeviceParameterMap parameter)
            throws OwException;

    /**
     * read a string from a sensor
     *
     * @param sensorId  sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a String
     */
    public abstract String readString(String sensorId, OwDeviceParameterMap parameter) throws OwException;

    /**
     * writes a DecimalType to the sensor
     *
     * @param sensorId  sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     */
    public abstract void writeDecimalType(String sensorId, OwDeviceParameterMap parameter, DecimalType value)
            throws OwException;

    /**
     * returns if this bridge is refreshable
     *
     * @return true if implementation reports communication ready
     */
    public boolean isRefreshable() {
        return refreshable;
    }
}
