/**
 *
 *  Copyright (c) 2016 by the respective copyright holders.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  @author Pascal Larin
 *  https://github.com/chaton78
 *
*/

package org.eclipse.smarthome.binding.sinope.handler;

import java.io.IOException;

import org.eclipse.smarthome.binding.sinope.SinopeBindingConstants;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SinopeThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeThermostatHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(SinopeThermostatHandler.class);
    private String deviceId;

    private SinopeGatewayHandler gatewayHandler;

    public SinopeThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel != null && SinopeBindingConstants.CHANNEL_SETTEMP.equals(channelUID.getId())) {
            try {
                if (command instanceof DecimalType) {
                    this.gatewayHandler.setSetPointTemp(this, ((DecimalType) command).floatValue());
                }
            } catch (IOException e) {
                logger.error("Cannot set point temp: {}", e.getLocalizedMessage());
            }
        }

        if (channel != null && SinopeBindingConstants.CHANNEL_SETMODE.equals(channelUID.getId())) {
            try {
                if (command instanceof StringType) {
                    this.gatewayHandler.setSetPointMode(this, Integer.parseInt(((StringType) command).toString()));
                }
            } catch (IOException e) {
                logger.error("Cannot set point mode: {}", e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Sinope Thermostat");
        super.initialize();
        initializeThing((getBridge() == null) ? null : getBridge().getStatus());

    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        String configDeviceId = (String) getConfig().get(SinopeBindingConstants.CONFIG_PROPERTY_DEVICE_ID);
        if (configDeviceId != null) {
            this.deviceId = configDeviceId;
            if (getSinopeGatewayHandler() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);

                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private synchronized SinopeGatewayHandler getSinopeGatewayHandler() {
        if (this.gatewayHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof SinopeGatewayHandler) {
                this.gatewayHandler = (SinopeGatewayHandler) handler;
                this.gatewayHandler.registerThermostatHandler(this);
            } else {
                return null;
            }
        }
        return this.gatewayHandler;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void updateOutsideTemp(double temp) {
        updateState(SinopeBindingConstants.CHANNEL_OUTTEMP, new DecimalType(temp));

    }

    public void updateRoomTemp(double temp) {
        updateState(SinopeBindingConstants.CHANNEL_INTEMP, new DecimalType(temp));
    }

    public void updateSetPointTemp(double temp) {
        updateState(SinopeBindingConstants.CHANNEL_SETTEMP, new DecimalType(temp));
    }

    public void updateSetPointMode(int mode) {
        updateState(SinopeBindingConstants.CHANNEL_SETMODE, new StringType(Integer.toString(mode)));
    }

    public void updateHeatLevel(int heatLevel) {
        updateState(SinopeBindingConstants.CHANNEL_HEATLEVEL, new DecimalType(heatLevel));
    }

}
