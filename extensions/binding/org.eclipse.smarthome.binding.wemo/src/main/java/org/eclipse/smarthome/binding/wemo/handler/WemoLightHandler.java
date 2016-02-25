/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.handler;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;

import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.wemo.internal.http.WemoHttpCall;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WemoLightHandler} is the handler for a WeMo light, responsible for handling commands and state updates for the
 * different channels of a WeMo light.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
public class WemoLightHandler extends BaseThingHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(WemoLightHandler.class);

    private UpnpIOService service;

    private WemoBridgeHandler wemoBridgeHandler;

    private String wemoLightID;

    private int currentBrightness;

    /**
     * Set dimming stepsize to 5%
     */
    private static final int DIM_STEPSIZE = 5;

    protected final static int SUBSCRIPTION_DURATION = 600;

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_MZ100);

    /**
     * The default refresh interval in Seconds.
     */
    private int DEFAULT_REFRESH_INTERVAL = 60;

    private ScheduledFuture<?> refreshJob;

    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                getDeviceState();
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        }
    };

    public WemoLightHandler(Thing thing, UpnpIOService upnpIOService) {
        super(thing);

        if (upnpIOService != null) {
            logger.debug("UPnPIOService '{}'", upnpIOService);
            this.service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }
    }

    @Override
    public void initialize() {

        logger.debug("Initializing WemoLightHandler");

        final String configLightId = (String) getConfig().get(DEVICE_ID);

        if (DEVICE_ID != null) {
            wemoLightID = configLightId;
            logger.debug("Initializing WemoLightHandler for LightID '{}'", wemoLightID);

        }
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        if (thingHandler instanceof WemoBridgeHandler) {
            this.wemoBridgeHandler = (WemoBridgeHandler) thingHandler;
            updateStatus(ThingStatus.ONLINE);
            logger.debug("bridgeHandlerInitialized for WeMo LED {}", wemoLightID);
            if (SUPPORTED_THING_TYPES.contains(this.getThing().getThingTypeUID())) {
                logger.debug("Initialize Thing {}", this.getThing().getThingTypeUID());
                onSubscription();
                onUpdate();
            }
        }
    }

    @Override
    public void bridgeHandlerDisposed(ThingHandler thingHandler, Bridge bridge) {
        this.wemoBridgeHandler = null;
    }

    @Override
    public void dispose() {
        logger.debug("WeMoLightHandler disposed.");

        removeSubscription();

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    private synchronized WemoBridgeHandler getWemoBridgeHandler() {

        if (this.wemoBridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.error("Required bridge not defined for device {}.", wemoLightID);
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof WemoBridgeHandler) {
                this.wemoBridgeHandler = (WemoBridgeHandler) handler;
            } else {
                logger.debug("No available bridge handler found for {} bridge {} .", wemoLightID, bridge.getUID());
                return null;
            }
        }
        return this.wemoBridgeHandler;
    }

    /**
     * The {@link handleCommand}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        Configuration configuration = getConfig();
        configuration.get(DEVICE_ID);

        WemoBridgeHandler wemoBridge = getWemoBridgeHandler();
        if (wemoBridge == null) {
            logger.debug("wemoBridgeHandler not found, cannot handle command");
            return;
        }
        String devUDN = "uuid:" + wemoBridge.getThing().getConfiguration().get(UDN).toString();
        logger.trace("WeMo Bridge to send command to : {}", devUDN);

        String value = null;
        String capability = null;
        switch (channelUID.getId()) {
            case CHANNEL_BRIGHTNESS:
                capability = "10008";
                if (command instanceof PercentType) {
                    int newBrightness = ((PercentType) command).intValue();
                    logger.trace("wemoLight received Value {}", newBrightness);
                    int value1 = Math.round(newBrightness * 255 / 100);
                    value = value1 + ":0";
                    currentBrightness = newBrightness;
                } else if (command instanceof OnOffType) {
                    switch (command.toString()) {
                        case "ON":
                            value = "255:0";
                            break;
                        case "OFF":
                            value = "0:0";
                            break;
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    int newBrightness;
                    switch (command.toString()) {
                        case "INCREASE":
                            currentBrightness = currentBrightness + DIM_STEPSIZE;
                            newBrightness = Math.round(currentBrightness * 255 / 100);
                            if (newBrightness > 255) {
                                newBrightness = 255;
                            }
                            value = newBrightness + ":0";
                            break;
                        case "DECREASE":
                            currentBrightness = currentBrightness - DIM_STEPSIZE;
                            newBrightness = Math.round(currentBrightness * 255 / 100);
                            if (newBrightness < 0) {
                                newBrightness = 0;
                            }
                            value = newBrightness + ":0";
                            break;
                    }
                }
                break;
            case CHANNEL_STATE:
                capability = "10006";
                switch (command.toString()) {
                    case "ON":
                        value = "1";
                        break;
                    case "OFF":
                        value = "0";
                        break;
                }
                break;
        }
        try {
            String soapHeader = "\"urn:Belkin:service:bridge:1#SetDeviceStatus\"";
            String content = "<?xml version=\"1.0\"?>"
                    + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                    + "<s:Body>" + "<u:SetDeviceStatus xmlns:u=\"urn:Belkin:service:bridge:1\">" + "<DeviceStatusList>"
                    + "&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;DeviceStatus&gt;&lt;DeviceID&gt;"
                    + wemoLightID + "&lt;/DeviceID&gt;&lt;IsGroupAction&gt;NO&lt;/IsGroupAction&gt;&lt;CapabilityID&gt;"
                    + capability + "&lt;/CapabilityID&gt;&lt;CapabilityValue&gt;" + value
                    + "&lt;/CapabilityValue&gt;&lt;/DeviceStatus&gt;" + "</DeviceStatusList>" + "</u:SetDeviceStatus>"
                    + "</s:Body>" + "</s:Envelope>";

            String wemoURL = getWemoURL();

            if (wemoURL != null) {
                String wemoCallResponse = WemoHttpCall.executeCall(wemoURL, soapHeader, content);
                if (wemoCallResponse != null) {
                    if (capability != null && capability.equals("10008") && value != null) {
                        OnOffType binaryState = null;
                        binaryState = value.equals("0") ? OnOffType.OFF : OnOffType.ON;
                        if (binaryState != null) {
                            updateState(
                                    new ChannelUID(getThing().getThingTypeUID(), getThing().getUID(), CHANNEL_STATE),
                                    binaryState);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not send command to WeMo Bridge", e);
        }

    }

    @Override
    public String getUDN() {
        WemoBridgeHandler wemoBridge = getWemoBridgeHandler();
        if (wemoBridge == null) {
            logger.debug("wemoBridgeHandler not found");
            return null;
        }
        return (String) wemoBridge.getThing().getConfiguration().get(UDN);
    }

    /**
     * The {@link getDeviceState} is used for polling the actual state of a WeMo Light and updating the according
     * channel states.
     */
    public void getDeviceState() {
        logger.debug("Request actual state for LightID '{}'", wemoLightID);
        try {
            String soapHeader = "\"urn:Belkin:service:bridge:1#GetDeviceStatus\"";
            String content = "<?xml version=\"1.0\"?>"
                    + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                    + "<s:Body>" + "<u:GetDeviceStatus xmlns:u=\"urn:Belkin:service:bridge:1\">" + "<DeviceIDs>"
                    + wemoLightID + "</DeviceIDs>" + "</u:GetDeviceStatus>" + "</s:Body>" + "</s:Envelope>";

            String wemoURL = getWemoURL();

            if (wemoURL != null) {
                String wemoCallResponse = WemoHttpCall.executeCall(wemoURL, soapHeader, content);
                if (wemoCallResponse != null) {
                    wemoCallResponse = StringEscapeUtils.unescapeXml(wemoCallResponse);
                    String response = StringUtils.substringBetween(wemoCallResponse, "<CapabilityValue>",
                            "</CapabilityValue>");
                    logger.trace("wemoNewLightState = {}", response);
                    String[] splitResponse = response.split(",");
                    if (splitResponse[0] != null) {
                        OnOffType binaryState = null;
                        binaryState = splitResponse[0].equals("0") ? OnOffType.OFF : OnOffType.ON;
                        if (binaryState != null) {
                            updateState(
                                    new ChannelUID(getThing().getThingTypeUID(), getThing().getUID(), CHANNEL_STATE),
                                    binaryState);
                        }
                    }
                    if (splitResponse[1] != null) {
                        String splitBrightness[] = splitResponse[1].split(":");
                        if (splitBrightness[0] != null) {
                            int newBrightnessValue = Integer.valueOf(splitBrightness[0]);
                            int newBrightness = Math.round(newBrightnessValue * 100 / 255);
                            logger.trace("newBrightness = {}", newBrightness);
                            State newBrightnessState = new PercentType(newBrightness);
                            updateState(new ChannelUID(getThing().getThingTypeUID(), getThing().getUID(),
                                    CHANNEL_BRIGHTNESS), newBrightnessState);
                            currentBrightness = newBrightness;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve new Wemo light state", e);
        }
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
        logger.trace("Received pair '{}':'{}' (service '{}') for thing '{}'",
                new Object[] { variable, value, service, this.getThing().getUID() });
        String capabilityId = StringUtils.substringBetween(value, "<CapabilityId>", "</CapabilityId>");
        String newValue = StringUtils.substringBetween(value, "<Value>", "</Value>");
        switch (capabilityId) {
            case "10006":
                OnOffType binaryState = null;
                binaryState = newValue.equals("0") ? OnOffType.OFF : OnOffType.ON;
                if (binaryState != null) {
                    updateState(new ChannelUID(getThing().getThingTypeUID(), getThing().getUID(), CHANNEL_STATE),
                            binaryState);
                }
                break;
            case "10008":
                String splitValue[] = newValue.split(":");
                if (splitValue[0] != null) {
                    int newBrightnessValue = Integer.valueOf(splitValue[0]);
                    int newBrightness = Math.round(newBrightnessValue * 100 / 255);
                    State newBrightnessState = new PercentType(newBrightness);
                    updateState(new ChannelUID(getThing().getThingTypeUID(), getThing().getUID(), CHANNEL_BRIGHTNESS),
                            newBrightnessState);
                    currentBrightness = newBrightness;
                }
                break;
        }
    }

    @Override
    public void onStatusChanged(boolean status) {
    }

    private synchronized void onSubscription() {
        if (service.isRegistered(this)) {
            logger.debug("Setting up WeMo GENA subscription for '{}'", this);
            service.addSubscription(this, "bridge1", SUBSCRIPTION_DURATION);
        }
    }

    private synchronized void removeSubscription() {
        logger.debug("Removing WeMo GENA subscription for '{}'", this);
        if (service.isRegistered(this)) {
            service.removeSubscription(this, "bridge1");
            service.unregisterParticipant(this);
        }
    }

    private synchronized void onUpdate() {
        if (service.isRegistered(this)) {
            if (refreshJob == null || refreshJob.isCancelled()) {
                int refreshInterval = DEFAULT_REFRESH_INTERVAL;
                logger.trace("Start polling job for LightID '{}'", wemoLightID);
                refreshJob = scheduler.scheduleAtFixedRate(refreshRunnable, 15, refreshInterval, TimeUnit.SECONDS);

            }
        }
    }

    public String getWemoURL() {
        URL descriptorURL = service.getDescriptorURL(this);
        String wemoURL = null;
        if (descriptorURL != null) {
            String deviceURL = StringUtils.substringBefore(descriptorURL.toString(), "/setup.xml");
            wemoURL = deviceURL + "/upnp/control/bridge1";
            return wemoURL;
        }
        return null;
    }

}
