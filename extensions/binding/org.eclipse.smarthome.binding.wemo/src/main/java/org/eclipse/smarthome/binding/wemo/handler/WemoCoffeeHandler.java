/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.handler;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.wemo.internal.http.WemoHttpCall;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.collect.Sets;

/**
 * The {@link WemoCoffeeHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */

public class WemoCoffeeHandler extends BaseThingHandler implements UpnpIOParticipant, DiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(WemoCoffeeHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_COFFEE);

    private Map<String, Boolean> subscriptionState = new HashMap<String, Boolean>();

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());

    protected final static int SUBSCRIPTION_DURATION = 600;

    private UpnpIOService service;

    /**
     * The default refresh interval in Seconds.
     */
    private int REFRESH_INTERVAL = 60;

    private ScheduledFuture<?> refreshJob;

    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("WeMo UPnP device {} not yet registered", getUDN());
                }

                updateWemoState();
                onSubscription();

            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        }
    };

    public WemoCoffeeHandler(Thing thing, UpnpIOService upnpIOService) {

        super(thing);

        logger.debug("Creating a WemoCoffeeHandler V0.4 for thing '{}'", getThing().getUID());

        if (upnpIOService != null) {
            this.service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }

    }

    @Override
    public void initialize() {

        Configuration configuration = getConfig();

        if (configuration.get("udn") != null) {
            logger.debug("Initializing WemoCoffeeHandler for UDN '{}'", configuration.get("udn"));
            onSubscription();
            onUpdate();
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Cannot initalize WemoCoffeeHandler. UDN not set.");
        }

    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        if (result.getThingUID().equals(this.getThing().getUID())) {
            if (getThing().getConfiguration().get(UDN).equals(result.getProperties().get(UDN))) {
                logger.trace("Discovered UDN '{}' for thing '{}'", result.getProperties().get(UDN),
                        getThing().getUID());
                updateStatus(ThingStatus.ONLINE);
                onSubscription();
                onUpdate();
            }
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        if (thingUID.equals(this.getThing().getUID())) {
            logger.trace("Setting status for thing '{}' to OFFLINE", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("WeMoCoffeeHandler disposed.");

        removeSubscription();

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        if (command instanceof RefreshType) {
            try {
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        } else if (channelUID.getId().equals(CHANNEL_STATE)) {

            if (command instanceof OnOffType) {

                if (command.equals(OnOffType.ON)) {

                    try {

                        String soapHeader = "\"urn:Belkin:service:deviceevent:1#SetAttributes\"";

                        String content = "<?xml version=\"1.0\"?>"
                                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                                + "<s:Body>" + "<u:SetAttributes xmlns:u=\"urn:Belkin:service:deviceevent:1\">"
                                + "<attributeList>&lt;attribute&gt;&lt;name&gt;Brewed&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;"
                                + "&lt;attribute&gt;&lt;name&gt;LastCleaned&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;"
                                + "&lt;name&gt;ModeTime&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;Brewing&lt;/name&gt;"
                                + "&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;TimeRemaining&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;"
                                + "&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;WaterLevelReached&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;"
                                + "attribute&gt;&lt;name&gt;Mode&lt;/name&gt;&lt;value&gt;4&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;CleanAdvise&lt;/name&gt;"
                                + "&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;FilterAdvise&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;"
                                + "&lt;attribute&gt;&lt;name&gt;Cleaning&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;</attributeList>"
                                + "</u:SetAttributes>" + "</s:Body>" + "</s:Envelope>";

                        String wemoURL = getWemoURL("deviceevent");

                        if (wemoURL != null) {
                            String wemoCallResponse = WemoHttpCall.executeCall(wemoURL, soapHeader, content);
                            if (wemoCallResponse != null) {
                                updateState(CHANNEL_STATE, OnOffType.ON);
                                State newMode = new StringType("Brewing");
                                updateState(CHANNEL_COFFEEMODE, newMode);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to send command '{}' for device '{}': {}", command, getThing().getUID(),
                                e.getMessage());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    }

                } else if (command.equals(OnOffType.OFF)) {
                    // do nothing, as WeMo Coffee Maker cannot be switched off remotely
                }
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
        logger.debug("WeMo {}: Subscription to service {} {}", getUDN(), service, succeeded ? "succeeded" : "failed");
        subscriptionState.put(service, succeeded);
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
        // We can subscribe to GENA events, but there is no usefull response right now.
    }

    private synchronized void onSubscription() {
        if (service.isRegistered(this)) {
            logger.debug("Checking WeMo GENA subscription for '{}'", this);

            String subscription = "deviceevent1";
            if ((subscriptionState.get(subscription) == null) || !subscriptionState.get(subscription).booleanValue()) {
                logger.debug("Setting up GENA subscription {}: Subscribing to service {}...", getUDN(), subscription);
                service.addSubscription(this, subscription, SUBSCRIPTION_DURATION);
                subscriptionState.put(subscription, true);
            }

        } else {
            logger.debug("Setting up WeMo GENA subscription for '{}' FAILED - service.isRegistered(this) is FALSE",
                    this);
        }
    }

    private synchronized void removeSubscription() {
        logger.debug("Removing WeMo GENA subscription for '{}'", this);

        if (service.isRegistered(this)) {

            String subscription = "deviceevent1";
            if ((subscriptionState.get(subscription) != null) && subscriptionState.get(subscription).booleanValue()) {
                logger.debug("WeMo {}: Unsubscribing from service {}...", getUDN(), subscription);
                service.removeSubscription(this, subscription);
            }

            subscriptionState = new HashMap<String, Boolean>();
            service.unregisterParticipant(this);
        }
    }

    private synchronized void onUpdate() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Configuration config = getThing().getConfiguration();
            int refreshInterval = REFRESH_INTERVAL;
            Object refreshConfig = config.get("pollingInterval");
            if (refreshConfig != null) {
                refreshInterval = ((BigDecimal) refreshConfig).intValue();
                logger.debug("Setting WemoCoffeeHandler refreshInterval to '{}' seconds", refreshInterval);
            }
            refreshJob = scheduler.scheduleWithFixedDelay(refreshRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private boolean isUpnpDeviceRegistered() {
        return service.isRegistered(this);
    }

    @Override
    public String getUDN() {
        return (String) this.getThing().getConfiguration().get(UDN);
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo CoffeeMaker.
     */
    protected void updateWemoState() {

        String action = "GetAttributes";
        String actionService = "deviceevent";

        String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
        String content = "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>" + "<u:" + action + " xmlns:u=\"urn:Belkin:service:" + actionService + ":1\">" + "</u:"
                + action + ">" + "</s:Body>" + "</s:Envelope>";

        try {
            String wemoURL = getWemoURL(actionService);
            if (wemoURL != null) {
                String wemoCallResponse = WemoHttpCall.executeCall(wemoURL, soapHeader, content);
                if (wemoCallResponse != null) {
                    try {
                        String stringParser = StringUtils.substringBetween(wemoCallResponse, "<attributeList>",
                                "</attributeList>");

                        // Due to Belkins bad response formatting, we need to run this twice.
                        stringParser = StringEscapeUtils.unescapeXml(stringParser);
                        stringParser = StringEscapeUtils.unescapeXml(stringParser);

                        logger.trace("CoffeeMaker response '{}' for device '{}' received", stringParser,
                                getThing().getUID());

                        stringParser = "<data>" + stringParser + "</data>";

                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(stringParser));

                        Document doc = db.parse(is);
                        NodeList nodes = doc.getElementsByTagName("attribute");

                        // iterate the attributes
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Element element = (Element) nodes.item(i);

                            NodeList deviceIndex = element.getElementsByTagName("name");
                            Element line = (Element) deviceIndex.item(0);
                            String attributeName = getCharacterDataFromElement(line);
                            logger.trace("attributeName: {}", attributeName);

                            NodeList deviceID = element.getElementsByTagName("value");
                            line = (Element) deviceID.item(0);
                            String attributeValue = getCharacterDataFromElement(line);
                            logger.trace("attributeValue: {}", attributeValue);

                            switch (attributeName) {
                                case "Mode":
                                    State newMode = new StringType("Brewing");
                                    switch (attributeValue) {
                                        case "0":
                                            updateState(CHANNEL_STATE, OnOffType.ON);
                                            newMode = new StringType("Refill");
                                            updateState(CHANNEL_COFFEEMODE, newMode);
                                            break;
                                        case "1":
                                            updateState(CHANNEL_STATE, OnOffType.OFF);
                                            newMode = new StringType("PlaceCarafe");
                                            updateState(CHANNEL_COFFEEMODE, newMode);
                                            break;
                                        case "2":
                                            updateState(CHANNEL_STATE, OnOffType.OFF);
                                            newMode = new StringType("RefillWater");
                                            updateState(CHANNEL_COFFEEMODE, newMode);
                                            break;
                                        case "3":
                                            updateState(CHANNEL_STATE, OnOffType.OFF);
                                            newMode = new StringType("Ready");
                                            updateState(CHANNEL_COFFEEMODE, newMode);
                                            break;
                                        case "4":
                                            updateState(CHANNEL_STATE, OnOffType.ON);
                                            newMode = new StringType("Brewing");
                                            updateState(CHANNEL_COFFEEMODE, newMode);
                                            break;
                                        case "5":
                                            updateState(CHANNEL_STATE, OnOffType.OFF);
                                            newMode = new StringType("Brewed");
                                            updateState(CHANNEL_COFFEEMODE, newMode);
                                            break;
                                        case "6":
                                            updateState(CHANNEL_STATE, OnOffType.OFF);
                                            newMode = new StringType("CleaningBrewing");
                                            updateState(CHANNEL_COFFEEMODE, newMode);
                                            break;
                                        case "7":
                                            updateState(CHANNEL_STATE, OnOffType.OFF);
                                            newMode = new StringType("CleaningSoaking");
                                            updateState(CHANNEL_COFFEEMODE, newMode);
                                            break;
                                        case "8":
                                            updateState(CHANNEL_STATE, OnOffType.OFF);
                                            newMode = new StringType("BrewFailCarafeRemoved");
                                            updateState(CHANNEL_COFFEEMODE, newMode);
                                            break;
                                    }
                                    break;
                                case "ModeTime":
                                    if (attributeValue != null) {
                                        State newAttributeValue = new DecimalType(attributeValue);
                                        updateState(CHANNEL_MODETIME, newAttributeValue);
                                    }
                                    break;
                                case "TimeRemaining":
                                    if (attributeValue != null) {
                                        State newAttributeValue = new DecimalType(attributeValue);
                                        updateState(CHANNEL_TIMEREMAINING, newAttributeValue);
                                    }
                                    break;
                                case "WaterLevelReached":
                                    if (attributeValue != null) {
                                        State newAttributeValue = new DecimalType(attributeValue);
                                        updateState(CHANNEL_WATERLEVELREACHED, newAttributeValue);
                                    }
                                    break;
                                case "CleanAdvise":
                                    if (attributeValue != null) {
                                        State newAttributeValue = attributeValue.equals("0") ? OnOffType.OFF
                                                : OnOffType.ON;
                                        updateState(CHANNEL_CLEANADVISE, newAttributeValue);
                                    }
                                    break;
                                case "FilterAdvise":
                                    if (attributeValue != null) {
                                        State newAttributeValue = attributeValue.equals("0") ? OnOffType.OFF
                                                : OnOffType.ON;
                                        updateState(CHANNEL_FILTERADVISE, newAttributeValue);
                                    }
                                    break;
                                case "Brewed":
                                    if (attributeValue != null) {
                                        State newAttributeValue = getDateTimeState(attributeValue);
                                        if (newAttributeValue != null) {
                                            updateState(CHANNEL_BREWED, newAttributeValue);
                                        }
                                    }
                                    break;
                                case "LastCleaned":
                                    if (attributeValue != null) {
                                        State newAttributeValue = getDateTimeState(attributeValue);
                                        if (newAttributeValue != null) {
                                            updateState(CHANNEL_LASTCLEANED, newAttributeValue);
                                        }
                                    }
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to parse attributeList for WeMo CoffeMaker '{}'", this.getThing().getUID(),
                                e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get attributes for device '{}'", getThing().getUID(), e);
        }
    }

    public State getDateTimeState(String attributeValue) {
        if (attributeValue != null) {
            long value = 0;
            try {
                value = Long.parseLong(attributeValue) * 1000; // convert s to ms
            } catch (NumberFormatException e) {
                logger.error("Unable to parse attributeValue '{}' for device '{}'; expected long", attributeValue,
                        getThing().getUID());
                return null;
            }
            GregorianCalendar brewCal = new GregorianCalendar();
            brewCal.setTimeInMillis(value);
            State dateTimeState = new DateTimeType(brewCal);
            if (dateTimeState != null) {
                logger.trace("New attribute brewed '{}' received", dateTimeState);
                return dateTimeState;
            }
        }
        return null;
    }

    public String getWemoURL(String actionService) {
        URL descriptorURL = service.getDescriptorURL(this);
        String wemoURL = null;
        if (descriptorURL != null) {
            String deviceURL = StringUtils.substringBefore(descriptorURL.toString(), "/setup.xml");
            wemoURL = deviceURL + "/upnp/control/" + actionService + "1";
            return wemoURL;
        }
        return null;
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }

    @Override
    public void onStatusChanged(boolean status) {
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        return null;
    }

}
