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

    private BigDecimal refresh;

    private final CoapClient coapClient = new CoapClient();

    String COAP_SERVER_IP_ADDRESS = "0.0.0.0";
    int COAP_SERVER_PORT = 5683;

    /*
     * String COAP_SERVER_IP_ADDRESS_LIGHT = "192.168.1.103";
     * String COAP_SERVER_PORT_LIGHT = "5683";
     */
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

        if ((deviceIPAddress != null) && (devicePortNumber != -1)) {
            if ((!deviceIPAddress.isEmpty()) && (devicePortNumber != -1)) {
                COAP_SERVER_IP_ADDRESS = deviceIPAddress;
                COAP_SERVER_PORT = devicePortNumber;
            } else {
                logger.error(this.getThing().getUID() + "-Invalid IP Address or Port Number");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Can not access device as the ip address or port number is invalid");
                updateStatus(ThingStatus.OFFLINE);
                return;
            }
        } else {
            logger.error(this.getThing().getUID() + "-NULL Configuration - IP Address or Port number is missing");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access device as the ip address or port number is not set (NULL)");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        stringChannelTemperatureUID = new ChannelUID(getThing().getUID(), CHANNEL_Temperature);
        stringChannelHumidityUID = new ChannelUID(getThing().getUID(), CHANNEL_Humidity);
        stringChannelPressureUID = new ChannelUID(getThing().getUID(), CHANNEL_Pressure);

        refresh = new BigDecimal(60);

        // Get the device properties
        Map<String, String> deviceProperties = editProperties();

        // Add the IP Address property
        deviceProperties.put("IP Address", COAP_SERVER_IP_ADDRESS);

        // Get the device functionality
        coapClient.setURI("coap://" + COAP_SERVER_IP_ADDRESS + ":" + COAP_SERVER_PORT);
        Set<WebLink> weblink = coapClient.discover();

        if (weblink != null) {
            Iterator<WebLink> links = weblink.iterator();

            StringBuilder weblinkString = new StringBuilder();

            while (links.hasNext()) {
                weblinkString.append(":" + links.next().getURI());
            }

            String resourcesCombined = weblinkString.toString();
            if (resourcesCombined.contains("actuators") && resourcesCombined.contains("sensors")) {
                deviceProperties.put("Is a Sensor ?", "Yes");
                deviceProperties.put("Is a Light ?", "Yes");
            } else {
                if (resourcesCombined.contains("actuators")) {
                    deviceProperties.put("Is a Light ?", "Yes");
                    deviceProperties.put("Is a Sensor ?", "No");
                } else if (resourcesCombined.contains("sensors")) {
                    deviceProperties.put("Is a Sensor ?", "Yes");
                    deviceProperties.put("Is a Light ?", "No");
                } else {
                    deviceProperties.put("Is a Sensor ?", "No");
                    deviceProperties.put("Is a Light ?", "No");
                }
            }

            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot identify the device functionality. Well-Known Core Discovery failed. Please remove the Thing and re-scan the device.");
        }

        // Update the device properties
        updateProperties(deviceProperties);
    }

    private void getTemprature() {
        coapClient.setURI(
                "coap://" + COAP_SERVER_IP_ADDRESS + ":" + COAP_SERVER_PORT + "/InternetOfTiny/sensors/temperature");
        logger.debug(coapClient.getURI());
        CoapResponse clientResponseTemperature = coapClient.get(TEXT_PLAIN);

        String temperatureText = clientResponseTemperature.getResponseText();
        String temperatureFirstHalf = temperatureText.substring(0, temperatureText.lastIndexOf(','));
        String temperature = temperatureFirstHalf.substring(temperatureFirstHalf.lastIndexOf(',') + 1);

        updateState(stringChannelTemperatureUID, new DecimalType(temperature));

        logger.debug(clientResponseTemperature.advanced().getSource().toString());
    }

    private void getHumidity() {
        coapClient.setURI(
                "coap://" + COAP_SERVER_IP_ADDRESS + ":" + COAP_SERVER_PORT + "/InternetOfTiny/sensors/humidity");

        logger.debug(coapClient.getURI());
        CoapResponse clientResponseHumidity = coapClient.get(TEXT_PLAIN);
        String humidityText = clientResponseHumidity.getResponseText();
        String humidityFirstHalf = humidityText.substring(0, humidityText.lastIndexOf(','));
        String humidity = humidityFirstHalf.substring(humidityFirstHalf.lastIndexOf(',') + 1);

        updateState(stringChannelHumidityUID, new DecimalType(humidity));

        logger.debug(clientResponseHumidity.advanced().getSource().toString());
    }

    private void getPressure() {
        coapClient.setURI(
                "coap://" + COAP_SERVER_IP_ADDRESS + ":" + COAP_SERVER_PORT + "/InternetOfTiny/sensors/pressure");

        logger.debug(coapClient.getURI());
        CoapResponse clientResponsePressure = coapClient.get(TEXT_PLAIN);
        String pressureText = clientResponsePressure.getResponseText();
        String pressureFirstHalf = pressureText.substring(0, pressureText.lastIndexOf(','));
        String pressure = pressureFirstHalf.substring(pressureFirstHalf.lastIndexOf(',') + 1);

        updateState(stringChannelPressureUID, new DecimalType(pressure));

        logger.debug(clientResponsePressure.advanced().getSource().toString());
    }

    private void putLCD(String message) {
        coapClient.setURI("coap://" + COAP_SERVER_IP_ADDRESS + ":" + COAP_SERVER_PORT + "/COAP/LCDResource");
        CoapResponse clientResponseLCD = coapClient.put(message, TEXT_PLAIN);

        logger.debug(clientResponseLCD.advanced().getSource().toString());
    }

    private void putLightOnOff(String message) {
        coapClient.setURI(
                "coap://" + COAP_SERVER_IP_ADDRESS + ":" + COAP_SERVER_PORT + "/InternetOfTiny/actuators/actuator1");
        StringBuilder coapLightSwitch = new StringBuilder();
        if (message.compareTo("ON") == 0) {
            coapLightSwitch.append(",1,pp,100,");
        } else if (message.compareTo("OFF") == 0) {
            coapLightSwitch.append(",1,pp,0,");
        }
        CoapResponse clientResponse = coapClient.put(coapLightSwitch.toString(), TEXT_PLAIN);

        logger.debug(clientResponse.advanced().getSource().toString());
    }

    private void putlightDimmer(String message) {
        coapClient.setURI(
                "coap://" + COAP_SERVER_IP_ADDRESS + ":" + COAP_SERVER_PORT + "/InternetOfTiny/actuators/actuator1");
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
                    getTemprature();
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
                    System.err.println(command.toString());
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
