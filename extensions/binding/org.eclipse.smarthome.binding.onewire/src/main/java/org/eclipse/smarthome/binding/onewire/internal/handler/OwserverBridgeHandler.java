/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.OwPageBuffer;
import org.eclipse.smarthome.binding.onewire.internal.SensorId;
import org.eclipse.smarthome.binding.onewire.internal.device.OwDeviceParameterMap;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwfsDirectChannelConfig;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverConnection;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverConnectionState;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwserverBridgeHandler} is responsible for the connection
 * to a owserver
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwserverBridgeHandler extends BaseBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_OWSERVER);

    private final Logger logger = LoggerFactory.getLogger(OwserverBridgeHandler.class);

    private static final int RECONNECT_AFTER_FAIL_TIME = 5000; // in ms
    private final OwserverConnection owserverConnection;

    private final List<OwfsDirectChannelConfig> channelConfigs = new ArrayList<>();

    public OwserverBridgeHandler(Bridge bridge) {
        super(bridge);
        this.owserverConnection = new OwserverConnection(this);
    }

    public OwserverBridgeHandler(Bridge bridge, OwserverConnection owserverConnection) {
        super(bridge);
        this.owserverConnection = owserverConnection;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    // set by implementation when bridge is ready
    protected boolean refreshable = false;

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

                logger.trace("refresh: getting handler for {} ({} to go)", owThing.getUID(), thingCount);
                OwBaseThingHandler owHandler = (OwBaseThingHandler) owThing.getHandler();
                if (owHandler != null) {
                    if (owHandler.isRefreshable()) {
                        logger.trace("{} initialized, refreshing", owThing.getUID());
                        owHandler.refresh(OwserverBridgeHandler.this, now);
                    } else {
                        logger.trace("{} not initialized, skipping refresh", owThing.getUID());
                    }
                } else {
                    logger.debug("{} handler missing", owThing.getUID());
                }
                thingCount--;
            }

            refreshBridgeChannels(now);

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

    /**
     * adds a thing to the property update list
     *
     * @param thing the thing to be updated
     */
    public void addToUpdatePropertyThingList(Thing thing) {
        updatePropertiesThingList.add(thing);
    }

    /**
     * read a BitSet value from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a BitSet
     * @throws OwException
     */
    public BitSet readBitSet(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException {
        return BitSet.valueOf(new long[] { ((DecimalType) readDecimalType(sensorId, parameter)).longValue() });
    }

    /**
     * writes a BitSet to the sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @throws OwException
     */
    public void writeBitSet(SensorId sensorId, OwDeviceParameterMap parameter, BitSet value) throws OwException {
        writeDecimalType(sensorId, parameter, new DecimalType(value.toLongArray()[0]));
    }

    /**
     * returns if this bridge is refreshable
     *
     * @return true if implementation reports communication ready
     * @throws OwException
     */
    public boolean isRefreshable() {
        return refreshable;
    }

    @Override
    public void initialize() {
        Configuration configuration = getConfig();

        if (configuration.get(CONFIG_ADDRESS) != null) {
            owserverConnection.setHost((String) configuration.get(CONFIG_ADDRESS));
        }
        if (configuration.get(CONFIG_PORT) != null) {
            owserverConnection.setPort(((BigDecimal) configuration.get(CONFIG_PORT)).intValue());
        }

        for (Channel channel : thing.getChannels()) {
            if (CHANNEL_TYPE_UID_OWFS_NUMBER.equals(channel.getChannelTypeUID())
                    || CHANNEL_TYPE_UID_OWFS_STRING.equals(channel.getChannelTypeUID())) {
                final OwfsDirectChannelConfig channelConfig = channel.getConfiguration()
                        .as(OwfsDirectChannelConfig.class);
                if (channelConfig.initialize(channel.getUID(), channel.getAcceptedItemType())) {
                    channelConfigs.add(channelConfig);
                } else {
                    logger.info("configuration mismatch: {}", channelConfig);
                }
            }
        }

        if (refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleWithFixedDelay(() -> refresh(), 1, 1000, TimeUnit.MILLISECONDS);
        }

        // makes it possible for unit tests to differentiate direct update and
        // postponed update through the owserverConnection:
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            owserverConnection.start();
        });
    }

    @Override
    public void dispose() {
        refreshable = false;
        if (!refreshTask.isCancelled()) {
            refreshTask.cancel(false);
        }

        owserverConnection.stop();
    }

    /**
     * get all sensors attached to this bridge
     *
     * @return a list of all sensor-IDs
     */
    public List<SensorId> getDirectory(String basePath) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.getDirectory(basePath);
        }
    }

    /**
     * check the presence of a sensor on the bus
     *
     * @param sensorId the sensor's full ID
     * @return ON if present, OFF if missing
     * @throws OwException
     */
    public State checkPresence(SensorId sensorId) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.checkPresence(sensorId.getFullPath());
        }
    }

    /**
     * get a sensors type string
     *
     * @param sensorId the sensor's full ID
     * @return a String containing the sensor type
     * @throws OwException
     */
    public OwSensorType getType(SensorId sensorId) throws OwException {
        OwSensorType sensorType = OwSensorType.UNKNOWN;
        synchronized (owserverConnection) {
            try {
                sensorType = OwSensorType.valueOf(owserverConnection.readString(sensorId + "/type"));
            } catch (IllegalArgumentException e) {
            }
        }
        return sensorType;
    }

    /**
     * read a single decimal value from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a DecimalType
     * @throws OwException
     */
    public State readDecimalType(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection
                    .readDecimalType(((OwserverDeviceParameter) parameter.get(THING_TYPE_OWSERVER)).getPath(sensorId));
        }
    }

    /**
     * read an array of decimal values from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a list of DecimalType values
     * @throws OwException
     */
    public List<State> readDecimalTypeArray(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.readDecimalTypeArray(
                    ((OwserverDeviceParameter) parameter.get(THING_TYPE_OWSERVER)).getPath(sensorId));
        }
    }

    /**
     * get full sensor information stored in pages (not available on all sensors)
     *
     * @param sensorId the sensor's full ID
     * @return a OwPageBuffer object containing the requested information
     * @throws OwException
     */
    public OwPageBuffer readPages(SensorId sensorId) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.readPages(sensorId.getFullPath());
        }
    }

    /**
     * read a string from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a String
     * @throws OwException
     */
    public String readString(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection
                    .readString(((OwserverDeviceParameter) parameter.get(THING_TYPE_OWSERVER)).getPath(sensorId));
        }
    }

    /**
     * writes a DecimalType to the sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @throws OwException
     */
    public void writeDecimalType(SensorId sensorId, OwDeviceParameterMap parameter, DecimalType value)
            throws OwException {
        synchronized (owserverConnection) {
            owserverConnection.writeDecimalType(
                    ((OwserverDeviceParameter) parameter.get(THING_TYPE_OWSERVER)).getPath(sensorId), value);
        }
    }

    /**
     * updates the thing status with the current connection state
     *
     * @param connectionState current connection state
     */
    public void reportConnectionState(OwserverConnectionState connectionState) {
        switch (connectionState) {
            case FAILED:
                refreshable = false;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                scheduler.schedule(() -> {
                    owserverConnection.start();
                }, RECONNECT_AFTER_FAIL_TIME, TimeUnit.MILLISECONDS);
                break;
            case STOPPED:
                refreshable = false;
                break;
            case OPENED:
            case CLOSED:
                refreshable = true;
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                break;
        }
    }

    /**
     * refreshes channels attached to the bridge
     *
     * @param now current time
     */
    public void refreshBridgeChannels(long now) {
        for (OwfsDirectChannelConfig channelConfig : channelConfigs) {
            if (now > channelConfig.lastRefresh + channelConfig.refreshCycle) {
                State value;
                try {
                    synchronized (owserverConnection) {
                        if (channelConfig.acceptedItemType.equals("String")) {
                            value = new StringType(owserverConnection.readString(channelConfig.path));
                        } else if (channelConfig.acceptedItemType.equals("Number")) {
                            value = owserverConnection.readDecimalType(channelConfig.path);
                        } else {
                            logger.debug("mismatched configuration, itemType unknown for channel {}",
                                    channelConfig.channelUID);
                            continue;
                        }
                    }

                    updateState(channelConfig.channelUID, value);
                    logger.trace("updated {} to {}", channelConfig.channelUID, value);

                    channelConfig.lastRefresh = now;
                } catch (OwException e) {
                    logger.debug("could not read direct channel {}: {}", channelConfig.channelUID, e.getMessage());
                }
            }
        }
    }
}
