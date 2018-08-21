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
package org.eclipse.smarthome.binding.onewire.internal.device;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseThingHandler;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractOwClass} class defines an abstract onewire device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractOwDevice {
    private final Logger logger = LoggerFactory.getLogger(AbstractOwDevice.class);

    protected String sensorId;
    protected OwSensorType sensorType = OwSensorType.UNKNOWN;
    protected OwBaseThingHandler callback;
    protected Boolean isConfigured = false;

    protected Set<String> enabledChannels = new HashSet<String>();

    /**
     * constructor for the onewire device
     *
     * @param sensorId onewire ID of the sensor
     * @param callback ThingHandler callback for posting updates
     */
    public AbstractOwDevice(String sensorId, OwBaseThingHandler callback) {
        this.sensorId = sensorId;
        this.callback = callback;
    }

    /**
     * configures the onewire devices channels
     *
     */
    public abstract void configureChannels() throws OwException;

    /**
     * refresh this sensor
     *
     * @param bridgeHandler for sending requests
     * @param forcedRefresh post update even if state did not change
     * @throws OwException in case of communication error
     */
    public abstract void refresh(OwBaseBridgeHandler owBridgeHandler, Boolean forcedRefresh) throws OwException;

    /**
     * enables a channel on this device
     *
     * @param channelID the channels channelID
     */
    public void enableChannel(String channelID) {
        if (!enabledChannels.contains(channelID)) {
            enabledChannels.add(channelID);
        }
    }

    /**
     * disables a channel on this device
     *
     * @param channelID the channels channelID
     */
    public void disableChannel(String channelID) {
        if (enabledChannels.contains(channelID)) {
            enabledChannels.remove(channelID);
        }
    }

    /**
     * get onewire ID of this sensor
     *
     * @return sensor ID
     */
    public String getSensorId() {
        return sensorId;
    }

    /**
     * check sensor presence and update thing state
     *
     * @param owServerConnection
     * @return sensors presence state
     */

    public Boolean checkPresence(OwBaseBridgeHandler bridgeHandler) {
        try {
            State present = bridgeHandler.checkPresence(sensorId);
            callback.updatePresenceStatus(present);
            return OnOffType.ON.equals(present);
        } catch (OwException e) {
            logger.debug("error refreshing presence {} on bridge {}: {}", this.sensorId,
                    bridgeHandler.getThing().getUID(), e.getMessage());
            return false;
        }
    }

    /**
     * get this sensors type
     *
     * @param bridgeHandler bridge handler to request from if type formerly unknown
     * @return this sensors type
     * @throws OwException
     */
    public OwSensorType getSensorType(OwBaseBridgeHandler bridgeHandler) throws OwException {
        if (sensorType == OwSensorType.UNKNOWN) {
            sensorType = bridgeHandler.getType(sensorId);
        }
        return sensorType;
    }

}
