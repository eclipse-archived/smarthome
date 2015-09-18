/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.handler;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link WemoHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution; Added support for WeMo Insight energy measurement
 * @author Kai Kreuzer - some refactoring for performance and simplification
 * @author Stefan Bußweiler - Added new thing status handling
 */

public class WemoHandler extends BaseThingHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(WemoHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(WEMO_SOCKET_TYPE_UID,
            WEMO_INSIGHT_TYPE_UID, WEMO_LIGHTSWITCH_TYPE_UID, WEMO_MOTION_TYPE_UID);

    private static String getInsightParamsXML;
    private static String getRequestXML;
    private static String setRequestXML;

    static {
        try {
            getInsightParamsXML = IOUtils.toString(WemoHandler.class
                    .getResourceAsStream("/org/eclipse/smarthome/binding/wemo/internal/GetInsightParams.xml"));
            getRequestXML = IOUtils.toString(WemoHandler.class
                    .getResourceAsStream("/org/eclipse/smarthome/binding/wemo/internal/GetRequest.xml"));
            setRequestXML = IOUtils.toString(WemoHandler.class
                    .getResourceAsStream("/org/eclipse/smarthome/binding/wemo/internal/SetRequest.xml"));
        } catch (Exception e) {
            LoggerFactory.getLogger(WemoHandler.class).error("Cannot read XML files!", e);
        }
    }

    private UpnpIOService service;

    /**
     * The default refresh interval in Seconds.
     */
    private int refresh = 15;

    private ScheduledFuture<?> refreshJob;

    public WemoHandler(Thing thing, UpnpIOService upnpIOService) {
        super(thing);

        logger.debug("Create a WemoHandler for thing '{}'", getThing().getUID());

        if (upnpIOService != null) {
            this.service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }
    }

    @Override
    public void initialize() {
        Configuration configuration = getConfig();

        if (configuration.get(UDN) != null) {
            logger.debug("Initializing WemoHandler for UDN '{}'", configuration.get(UDN));
        } else {
            logger.debug("Cannot initalize WemoHandler. UDN not set.");
        }

        logger.debug("Setting status for thing '{}' to ONLINE", getThing().getUID());
        updateStatus(ThingStatus.ONLINE);
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
        logger.debug("Setting status for thing '{}' to OFFLINE", getThing().getUID());
        updateStatus(ThingStatus.OFFLINE);
    }

    private void startAutomaticRefresh() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    logger.debug("Refreshing thing '{}'", getThing().getUID());

                    if (getThing().getThingTypeUID().getId().equals("insight")) {
                        String insightParams = getInsightParams(new ChannelUID(getThing().getUID(), CHANNEL_STATE));
                        logger.debug("New insightParams '{}' for device '{}' received", insightParams,
                                getThing().getUID());

                        if (insightParams != null) {

                            String[] splitInsightParams = insightParams.split("\\|");

                            if (splitInsightParams[0] != null) {
                                OnOffType binaryState = null;
                                binaryState = splitInsightParams[0].equals("0") ? OnOffType.OFF : OnOffType.ON;
                                if (binaryState != null) {
                                    logger.trace("New InsightParam binaryState '{}' for device '{}' received",
                                            binaryState, getThing().getUID());
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_STATE), binaryState);
                                }
                            }

                            State lastOnFor = DecimalType.valueOf(splitInsightParams[2]);
                            if (lastOnFor != null) {
                                logger.trace("New InsightParam lastOnFor '{}' for device '{}' received", lastOnFor,
                                        getThing().getUID());
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_LASTONFOR), lastOnFor);
                            }

                            State onToday = DecimalType.valueOf(splitInsightParams[3]);
                            if (onToday != null) {
                                logger.trace("New InsightParam onToday '{}' for device '{}' received", onToday,
                                        getThing().getUID());
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_ONTODAY), onToday);
                            }

                            State onTotal = DecimalType.valueOf(splitInsightParams[4]);
                            if (onTotal != null) {
                                logger.trace("New InsightParam onTotal '{}' for device '{}' received", onTotal,
                                        getThing().getUID());
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_ONTOTAL), onTotal);
                            }

                            BigDecimal currentMW = new BigDecimal(splitInsightParams[7]);
                            State currentPower = new DecimalType(currentMW.divide(new BigDecimal(1000))); // recalculate
                                                                                                          // mW to W
                            if (currentPower != null) {
                                logger.trace("New InsightParam currentPower '{}' for device '{}' received",
                                        currentPower, getThing().getUID());
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_CURRENTPOWER), currentPower);
                            }
                        }

                    } else {
                        State state = getWemoState(new ChannelUID(getThing().getUID(), CHANNEL_STATE));
                        logger.debug("New state '{}' for device '{}' received", state, getThing().getUID());
                        if (state != null) {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_STATE), state);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Exception occurred during Refresh: {}", e);
                }
            }
        };

        refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command '{}' received for channel '{}'", command, channelUID);
        if (channelUID.getId().equals(CHANNEL_STATE)) {
            if (command instanceof OnOffType) {
                try {
                    boolean onOff = OnOffType.ON.equals(command);
                    logger.trace("command '{}' transformed to '{}'", command, onOff);
                    String wemoCallResponse = wemoCall(channelUID, "urn:Belkin:service:basicevent:1#SetBinaryState",
                            setRequestXML.replace("{{state}}", onOff ? "1" : "0"));

                    logger.trace("WeMo setOn = {}", wemoCallResponse);

                } catch (Exception e) {
                    logger.error("Failed to send command '{}' for device '{}' ", command, getThing().getUID(), e);
                }
            }
        }
    }

    /**
     * The {@link wemoCall} is responsible for communicating with the WeMo devices by sending SOAP messages
     * sent to one of the channels.
     *
     */
    private String wemoCall(ChannelUID channelUID, String soapAction, String content) {

        try {
            String wemoLocation = null;

            URL wemoDescriptorURL = service.getDescriptorURL(this);

            if (wemoDescriptorURL != null) {
                logger.trace("WeMo descriptorURL found as '{}' ", wemoDescriptorURL);
                wemoLocation = StringUtils.substringBefore(wemoDescriptorURL.toString(), "/setup.xml");
            }

            String soapHeader = "SOAPACTION: \"" + soapAction + "\"";
            String contentHeader = "Content-Type: text/xml; charset=\"utf-8\"";
            String endpoint = "/upnp/control/basicevent1";

            if (soapAction.contains("insight")) {
                endpoint = "/upnp/control/insight1";
            }

            logger.trace("Calling WeMo device '{}' with configuration :", getThing().getUID());
            logger.trace("    Location = '{}'", wemoLocation);
            logger.trace("    EndPoint = '{}'", endpoint);

            if (wemoLocation != null) {

                String wemoURL = wemoLocation + endpoint;

                Properties wemoHeaders = new Properties();
                wemoHeaders.setProperty(soapHeader, contentHeader);

                InputStream wemoContent = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));

                String wemoCallResponse = HttpUtil.executeUrl("POST", wemoURL, wemoHeaders, wemoContent, "text/xml",
                        2000);
                logger.trace("wemoresp: {}", wemoCallResponse);
                return wemoCallResponse;

            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
                return null;
            }

        } catch (Exception e) {
            logger.error("Could not call WeMo device '{}'", getThing().getUID(), e);
            return null;

        }
    }

    private State getWemoState(ChannelUID channelUID) {
        String stateRequest = null;
        String returnState = null;

        try {
            stateRequest = wemoCall(channelUID, "urn:Belkin:service:basicevent:1#GetBinaryState", getRequestXML);
            if (stateRequest != null) {
                returnState = StringUtils.substringBetween(stateRequest, "<BinaryState>", "</BinaryState>");

                logger.debug("New binary state '{}' for device '{}' received", returnState, getThing().getUID());
            }
        } catch (Exception e) {
            logger.error("Failed to get binary state for device '{}'", getThing().getUID(), e);
        }

        if (returnState != null) {
            OnOffType newState = null;
            newState = returnState.equals("0") ? OnOffType.OFF : OnOffType.ON;
            logger.trace("New state '{}' for device '{}' received", newState, getThing().getUID());
            return newState;
        } else {
            return null;
        }
    }

    private String getInsightParams(ChannelUID channelUID) {
        String insightParamsRequest = null;
        String returnInsightParams = null;

        try {
            insightParamsRequest = wemoCall(channelUID, "urn:Belkin:service:insight:1#GetInsightParams",
                    getInsightParamsXML);
            if (insightParamsRequest != null) {

                returnInsightParams = StringUtils.substringBetween(insightParamsRequest, "<InsightParams>",
                        "</InsightParams>");
                logger.trace("New raw InsightParams '{}' for device '{}' received", returnInsightParams,
                        getThing().getUID());
                return returnInsightParams;
            }
        } catch (Exception e) {
            logger.error("Failed to get InsightParams for device '{}'", getThing().getUID(), e);
        }
        return null;
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
        // Due to the bad UPnP implementation of the WeMo devices, this will be done at a later time.
    }

    @Override
    public void onStatusChanged(boolean status) {
    }

    private synchronized void onSubscription() {
        // Set up GENA Subscriptions
        // Due to the bad UPnP implementation of the WeMo devices, this will be done at a later time.
    }

    private synchronized void onUpdate() {
        if (service.isRegistered(this)) {
            // Due to the bad UPnP implementation of the WeMo devices, this will be done at a later time.
        }
    }

    @Override
    public String getUDN() {
        return (String) this.getThing().getConfiguration().get(UDN);
    }

}
