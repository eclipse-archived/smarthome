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
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
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
public class OwserverBridgeHandler extends OwBaseBridgeHandler {
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

        // makes it possible for unit tests to differentiate direct update and
        // postponed update through the owserverConnection:
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            owserverConnection.start();
        });

        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        owserverConnection.stop();
    }

    @Override
    public List<SensorId> getDirectory(String basePath) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.getDirectory(basePath);
        }
    }

    @Override
    public State checkPresence(SensorId sensorId) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.checkPresence(sensorId.getFullPath());
        }
    }

    @Override
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

    @Override
    public State readDecimalType(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection
                    .readDecimalType(((OwserverDeviceParameter) parameter.get(THING_TYPE_OWSERVER)).getPath(sensorId));
        }
    }

    @Override
    public List<State> readDecimalTypeArray(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.readDecimalTypeArray(
                    ((OwserverDeviceParameter) parameter.get(THING_TYPE_OWSERVER)).getPath(sensorId));
        }
    }

    @Override
    public OwPageBuffer readPages(SensorId sensorId) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.readPages(sensorId.getFullPath());
        }
    }

    @Override
    public String readString(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection
                    .readString(((OwserverDeviceParameter) parameter.get(THING_TYPE_OWSERVER)).getPath(sensorId));
        }
    }

    @Override
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

    @Override
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
