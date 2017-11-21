/**
 * Copyright (c) 2016 Microchip Technology Inc. and its subsidiaries and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.binding.coapconnectedlighting.handler;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;
import static org.eclipse.smarthome.binding.coapconnectedlighting.CoAPConnectedLightingBindingConstants.*;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CoAPConnectedLightingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Microchip Technology - I nitial contribution
 */
public class CoAPConnectedLightingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(CoAPConnectedLightingHandler.class);

    private ChannelUID stringChannelTemperatureUID;
    private ChannelUID stringChannelHumidityUID;
    private ChannelUID stringChannelPressureUID;

    private ScheduledFuture<?> refreshTemperatureJob;
    private ScheduledFuture<?> refreshHumidityJob;
    private ScheduledFuture<?> refreshPressureJob;

    private final BigDecimal refresh = new BigDecimal(60);

    private final CoapClient coapClient = new CoapClient();

    private String coapServerIpAddress = "0.0.0.0";
    private int coapServerPort = 5683;

    public CoAPConnectedLightingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // Device Configuration Check
        String deviceIPAddress = (String) this.getConfig().get("ipAddress");
        int devicePortNumber = -1;

        if (((BigDecimal) this.getConfig().get("portNumber")) != null) {
            devicePortNumber = ((BigDecimal) this.getConfig().get("portNumber")).intValue();
        }

        if (StringUtils.isBlank(deviceIPAddress) || (devicePortNumber == -1)) {
            logger.debug(this.getThing().getUID() + "-Invalid IP Address or Port Number");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access device as the ip address or port number is invalid");
            return;
        }

        coapServerIpAddress = deviceIPAddress;
        coapServerPort = devicePortNumber;

        stringChannelTemperatureUID = new ChannelUID(getThing().getUID(), CHANNEL_Temperature);
        stringChannelHumidityUID = new ChannelUID(getThing().getUID(), CHANNEL_Humidity);
        stringChannelPressureUID = new ChannelUID(getThing().getUID(), CHANNEL_Pressure);

        // Get the device properties
        Map<String, String> deviceProperties = editProperties();

        // Add the IP Address property
        deviceProperties.put("IP Address", coapServerIpAddress);

        // Get the device functionality
        coapClient.setURI("coap://" + coapServerIpAddress + ":" + coapServerPort);
        Set<WebLink> weblink = coapClient.discover();

        if (weblink != null) {
            Iterator<WebLink> links = weblink.iterator();

            boolean isActuator = weblink.stream().map(WebLink::getURI).anyMatch(uri -> uri.contains("actuators"));
            boolean isSensor = weblink.stream().map(WebLink::getURI).anyMatch(uri -> uri.contains("sensors"));

            deviceProperties.put("Is a Sensor ?", isSensor ? "Yes" : "No");
            deviceProperties.put("Is a Light ?", isActuator ? "Yes" : "No");

            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot identify the device functionality. Well-Known Core Discovery failed. Please remove the Thing and re-scan the device.");
        }

        // Update the device properties
        updateProperties(deviceProperties);
    }

    private void getTemperature() {
        updateState(stringChannelTemperatureUID, getSensorData("temperature"));
    }

    private void getHumidity() {
        updateState(stringChannelHumidityUID, getSensorData("humidity"));
    }

    private void getPressure() {
        updateState(stringChannelPressureUID, getSensorData("pressure"));
    }

    private DecimalType getSensorData(String sensorType) {
        coapClient.setURI(
                "coap://" + coapServerIpAddress + ":" + coapServerPort + "/InternetOfTiny/sensors/" + sensorType);
        logger.debug(coapClient.getURI());
        CoapResponse clientResponse = coapClient.get(TEXT_PLAIN);

        String sensorText = clientResponse.getResponseText();
        String sensorFirstHalf = sensorText.substring(0, sensorText.lastIndexOf(','));
        String sensorDataString = sensorFirstHalf.substring(sensorFirstHalf.lastIndexOf(',') + 1);

        logger.debug(clientResponse.advanced().getSource().toString());

        return new DecimalType(sensorDataString);
    }

    private void putLCD(String message) {
        coapClient.setURI("coap://" + coapServerIpAddress + ":" + coapServerPort + "/COAP/LCDResource");
        CoapResponse clientResponseLCD = coapClient.put(message, TEXT_PLAIN);

        logger.debug(clientResponseLCD.advanced().getSource().toString());
    }

    private void putLightOnOff(String message) {
        coapClient
                .setURI("coap://" + coapServerIpAddress + ":" + coapServerPort + "/InternetOfTiny/actuators/actuator1");
        String coapLightSwitch;

        if (message.compareTo("ON") == 0) {
            coapLightSwitch = new String(",1,pp,100,");
        } else if (message.compareTo("OFF") == 0) {
            coapLightSwitch = new String(",1,pp,0,");
        } else {
            coapLightSwitch = new String("");
        }

        if (!coapLightSwitch.isEmpty()) {
            CoapResponse clientResponse = coapClient.put(coapLightSwitch, TEXT_PLAIN);
            logger.debug(clientResponse.advanced().getSource().toString());
        }
    }

    private void putlightDimmer(String message) {
        coapClient
                .setURI("coap://" + coapServerIpAddress + ":" + coapServerPort + "/InternetOfTiny/actuators/actuator1");
        StringBuilder coapLightSwitch = new StringBuilder();
        coapLightSwitch.append(",1,pp," + message + ",");

        CoapResponse clientResponse = coapClient.put(coapLightSwitch.toString(), TEXT_PLAIN);

        logger.debug(clientResponse.advanced().getSource().toString());
    }

    private void startBackgroundTemperatureRefresh() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    getTemperature();
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                }
            }
        };

        refreshTemperatureJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    private void startBackgroundHumidityRefresh() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    getHumidity();
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                }
            }
        };

        refreshHumidityJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    private void startBackgroundPressureRefresh() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    getPressure();
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                }
            }
        };

        refreshPressureJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals(CHANNEL_DimmableLight)) {
                if (command instanceof PercentType) {
                    PercentType commandString = ((PercentType) command);
                    putlightDimmer(commandString.toString());
                } else if (command instanceof DecimalType) {
                    IncreaseDecreaseType commandString = ((IncreaseDecreaseType) command);
                    putlightDimmer(commandString.toString());
                }
            } else if (channelUID.getId().equals(CHANNEL_LCD)) {
                if (command instanceof StringType) {
                    StringType commandString = ((StringType) command);
                    putLCD(commandString.toString());
                }
            } else if (channelUID.getId().equals(CHANNEL_WallSwitch)) {
                if (command instanceof OnOffType) {
                    OnOffType commandString = ((OnOffType) command);
                    putLightOnOff(commandString.toString());
                }
            } else {
                logger.error("Linked Channel does not support PUT. Invalid channel linked ");
            }
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not control device at IP address: " + this.getConfig().get("ipAddress"));
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_Temperature:
                startBackgroundTemperatureRefresh();
                break;
            case CHANNEL_Humidity:
                startBackgroundHumidityRefresh();
                break;
            case CHANNEL_Pressure:
                startBackgroundPressureRefresh();
                break;
            default:
                // do something
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_Temperature:
                if (refreshTemperatureJob != null) {
                    refreshTemperatureJob.cancel(true);
                }
                break;
            case CHANNEL_Humidity:
                if (refreshHumidityJob != null) {
                    refreshHumidityJob.cancel(true);
                }
                break;
            case CHANNEL_Pressure:
                if (refreshPressureJob != null) {
                    refreshPressureJob.cancel(true);
                }
                break;
            default:
                // do something
        }
    }

    @Override
    public void dispose() {
        if (refreshTemperatureJob != null) {
            refreshTemperatureJob.cancel(true);
        }

        if (refreshHumidityJob != null) {
            refreshHumidityJob.cancel(true);
        }

        if (refreshPressureJob != null) {
            refreshPressureJob.cancel(true);
        }
    }

}
