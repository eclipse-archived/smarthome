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

import java.util.Iterator;
import java.util.List;
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

    /**
     * refresh all sensors on this bridge
     */
    private void refresh() {
        if (isRefreshable()) {
            long now = System.currentTimeMillis();
            List<Thing> thingList = getThing().getThings();
            int thingCount = thingList.size();
            logger.trace("called owRefresher at {}, {} childs", now, thingCount);

            Iterator<Thing> childListIterator = thingList.iterator();
            while (childListIterator.hasNext()) {
                Thing owThing = childListIterator.next();

                logger.trace("getting handler for {} ({} to go)", owThing.getLabel(), thingCount);
                OwBaseThingHandler owHandler = (OwBaseThingHandler) owThing.getHandler();
                if (owHandler != null) {
                    if (owHandler.isRefreshable()) {
                        logger.trace("{} initialized, refreshing", owThing.getLabel());
                        owHandler.refresh(OwBaseBridgeHandler.this, now);
                    } else {
                        logger.trace("{} not initialized, skipping refresh", owThing.getLabel());
                    }
                } else {
                    logger.debug("{} handler missing", owThing.getLabel());
                }
                thingCount--;
            }
        }
    };

    @Override
    public void dispose() {
        refreshable = false;
        if (!refreshTask.isCancelled()) {
            refreshTask.cancel(false);
        }
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
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a DecimalType
     */
    public abstract State readDecimalType(String sensorId, OwDeviceParameterMap parameter) throws OwException;

    /**
     * read an array of decimal values from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a list of DecimalType values
     */
    public abstract List<State> readDecimalTypeArray(String sensorId, OwDeviceParameterMap parameter) throws OwException;

    /**
     * read a string from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a String
     */
    public abstract String readString(String sensorId, OwDeviceParameterMap parameter) throws OwException;

    /**
     * writes a DecimalType to the sensor
     *
     * @param sensorId sensorId the sensor's full ID
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
