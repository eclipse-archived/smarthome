/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.handler;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.wemo.internal.http.WemoHttpCall;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
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

public class WemoHandler extends BaseThingHandler implements UpnpIOParticipant, DiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(WemoHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_SOCKET, THING_TYPE_INSIGHT,
            THING_TYPE_LIGHTSWITCH, THING_TYPE_MOTION);

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());

    protected final static int SUBSCRIPTION_DURATION = 600;

    private UpnpIOService service;

    /**
     * The default refresh interval in Seconds.
     */
    private int DEFAULT_REFRESH_INTERVAL = 120;

    private ScheduledFuture<?> refreshJob;

    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        }
    };

    public WemoHandler(Thing thing, UpnpIOService upnpIOService) {

        super(thing);

        logger.debug("Creating a WemoHandler for thing '{}'", getThing().getUID());

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
            logger.debug("Initializing WemoHandler for UDN '{}'", configuration.get("udn"));
            onSubscription();
            onUpdate();
            super.initialize();
        } else {
            logger.debug("Cannot initalize WemoHandler. UDN not set.");
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
        logger.debug("WeMoHandler disposed.");

        removeSubscription();

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);
        if (channelUID.getId().equals(CHANNEL_STATE)) {
            if (command instanceof OnOffType) {

                try {

                    String binaryState = null;

                    if (command.equals(OnOffType.ON)) {
                        binaryState = "1";
                    } else if (command.equals(OnOffType.OFF)) {
                        binaryState = "0";
                    }

                    String soapHeader = "\"urn:Belkin:service:basicevent:1#SetBinaryState\"";

                    String content = "<?xml version=\"1.0\"?>"
                            + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                            + "<s:Body>" + "<u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">"
                            + "<BinaryState>" + binaryState + "</BinaryState>" + "</u:SetBinaryState>" + "</s:Body>"
                            + "</s:Envelope>";

                    String wemoURL = getWemoURL("basicevent");

                    if (wemoURL != null) {
                        WemoHttpCall.executeCall(wemoURL, soapHeader, content);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send command '{}' for device '{}' ", command, getThing().getUID(), e);
                }
            }
        }
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {

        if (getThing().getStatus() == ThingStatus.ONLINE) {

            logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'",
                    new Object[] { variable, value, service, this.getThing().getUID() });

            this.stateMap.put(variable, value);

            if (getThing().getThingTypeUID().getId().equals("insight")) {
                String insightParams = stateMap.get("InsightParams");

                if (insightParams != null) {

                    String[] splitInsightParams = insightParams.split("\\|");

                    if (splitInsightParams[0] != null) {
                        OnOffType binaryState = null;
                        binaryState = splitInsightParams[0].equals("0") ? OnOffType.OFF : OnOffType.ON;
                        if (binaryState != null) {
                            logger.trace("New InsightParam binaryState '{}' for device '{}' received", binaryState,
                                    getThing().getUID());
                            updateState(CHANNEL_STATE, binaryState);
                        }
                    }

                    long lastChangedAt = 0;
                    try {
                        lastChangedAt = Long.parseLong(splitInsightParams[1]) * 1000; // convert s to ms
                    } catch (NumberFormatException e) {
                        logger.error("Unable to parse lastChangedAt value '{}' for device '{}'; expected long",
                                splitInsightParams[1], getThing().getUID());
                    }
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(lastChangedAt);
                    State lastChangedAtState = new DateTimeType(cal);
                    if (lastChangedAt != 0) {
                        logger.trace("New InsightParam lastChangedAt '{}' for device '{}' received", lastChangedAtState,
                                getThing().getUID());
                        updateState(CHANNEL_LASTCHANGEDAT, lastChangedAtState);
                    }

                    State lastOnFor = DecimalType.valueOf(splitInsightParams[2]);
                    if (lastOnFor != null) {
                        logger.trace("New InsightParam lastOnFor '{}' for device '{}' received", lastOnFor,
                                getThing().getUID());
                        updateState(CHANNEL_LASTONFOR, lastOnFor);
                    }

                    State onToday = DecimalType.valueOf(splitInsightParams[3]);
                    if (onToday != null) {
                        logger.trace("New InsightParam onToday '{}' for device '{}' received", onToday,
                                getThing().getUID());
                        updateState(CHANNEL_ONTODAY, onToday);
                    }

                    State onTotal = DecimalType.valueOf(splitInsightParams[4]);
                    if (onTotal != null) {
                        logger.trace("New InsightParam onTotal '{}' for device '{}' received", onTotal,
                                getThing().getUID());
                        updateState(CHANNEL_ONTOTAL, onTotal);
                    }

                    State timespan = DecimalType.valueOf(splitInsightParams[5]);
                    if (timespan != null) {
                        logger.trace("New InsightParam timespan '{}' for device '{}' received", timespan,
                                getThing().getUID());
                        updateState(CHANNEL_TIMESPAN, timespan);
                    }

                    State averagePower = DecimalType.valueOf(splitInsightParams[6]); // natively given in W
                    if (averagePower != null) {
                        logger.trace("New InsightParam averagePower '{}' for device '{}' received", averagePower,
                                getThing().getUID());
                        updateState(CHANNEL_AVERAGEPOWER, averagePower);
                    }

                    BigDecimal currentMW = new BigDecimal(splitInsightParams[7]);
                    State currentPower = new DecimalType(currentMW.divide(new BigDecimal(1000), RoundingMode.HALF_UP)); // recalculate
                    // mW to W
                    if (currentPower != null) {
                        logger.trace("New InsightParam currentPower '{}' for device '{}' received", currentPower,
                                getThing().getUID());
                        updateState(CHANNEL_CURRENTPOWER, currentPower);
                    }

                    BigDecimal energyTodayMWMin = new BigDecimal(splitInsightParams[8]);
                    // recalculate mW-mins to Wh
                    State energyToday = new DecimalType(
                            energyTodayMWMin.divide(new BigDecimal(60000), RoundingMode.HALF_UP));
                    if (energyToday != null) {
                        logger.trace("New InsightParam energyToday '{}' for device '{}' received", energyToday,
                                getThing().getUID());
                        updateState(CHANNEL_ENERGYTODAY, energyToday);
                    }

                    BigDecimal energyTotalMWMin = new BigDecimal(splitInsightParams[9]);
                    // recalculate mW-mins to Wh
                    State energyTotal = new DecimalType(
                            energyTotalMWMin.divide(new BigDecimal(60000), RoundingMode.HALF_UP));
                    if (energyTotal != null) {
                        logger.trace("New InsightParam energyTotal '{}' for device '{}' received", energyTotal,
                                getThing().getUID());
                        updateState(CHANNEL_ENERGYTOTAL, energyTotal);
                    }

                    BigDecimal standByLimitMW = new BigDecimal(splitInsightParams[10]);
                    State standByLimit = new DecimalType(
                            standByLimitMW.divide(new BigDecimal(1000), RoundingMode.HALF_UP)); // recalculate
                    // mW to W
                    if (standByLimit != null) {
                        logger.trace("New InsightParam standByLimit '{}' for device '{}' received", standByLimit,
                                getThing().getUID());
                        updateState(CHANNEL_STANDBYLIMIT, standByLimit);
                    }
                }

            } else {
                State state = stateMap.get("BinaryState").equals("0") ? OnOffType.OFF : OnOffType.ON;

                logger.debug("State '{}' for device '{}' received", state, getThing().getUID());

                if (state != null) {
                    if (getThing().getThingTypeUID().getId().equals("motion")) {
                        updateState(CHANNEL_MOTIONDETECTION, state);
                    } else {
                        updateState(CHANNEL_STATE, state);
                    }
                }
            }
        }
    }

    private synchronized void onSubscription() {
        if (service.isRegistered(this)) {
            logger.debug("Setting up WeMo GENA subscription for '{}'", this);

            ThingTypeUID thingTypeUID = thing.getThingTypeUID();

            if (thingTypeUID.equals(THING_TYPE_INSIGHT)) {
                service.addSubscription(this, "insight1", SUBSCRIPTION_DURATION);
            } else {
                service.addSubscription(this, "basicevent1", SUBSCRIPTION_DURATION);
            }
        }
    }

    private synchronized void removeSubscription() {
        logger.debug("Removing WeMo GENA subscription for '{}'", this);
        if (service.isRegistered(this)) {
            ThingTypeUID thingTypeUID = thing.getThingTypeUID();

            if (thingTypeUID.equals(THING_TYPE_INSIGHT)) {
                service.removeSubscription(this, "insight1");
            } else {
                service.removeSubscription(this, "basicevent1");
            }
            service.unregisterParticipant(this);
        }
    }

    private synchronized void onUpdate() {
        if (service.isRegistered(this)) {
            if (refreshJob == null || refreshJob.isCancelled()) {
                Configuration config = getThing().getConfiguration();
                int refreshInterval = DEFAULT_REFRESH_INTERVAL;
                Object refreshConfig = config.get("refresh");
                if (refreshConfig != null) {
                    refreshInterval = ((BigDecimal) refreshConfig).intValue();
                }
                refreshJob = scheduler.scheduleAtFixedRate(refreshRunnable, 0, refreshInterval, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public String getUDN() {
        return (String) this.getThing().getConfiguration().get(UDN);
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo device and
     * calls {@link onValueReceived} to update the statemap and channels..
     *
     */
    protected void updateWemoState() {

        String action = "GetBinaryState";
        String variable = "BinaryState";
        String actionService = "basicevent";
        String value = null;

        if (getThing().getThingTypeUID().getId().equals("insight")) {
            action = "GetInsightParams";
            variable = "InsightParams";
            actionService = "insight";
        }

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
                    logger.trace("State response '{}' for device '{}' received", wemoCallResponse, getThing().getUID());
                    if (variable.equals("InsightParams")) {
                        value = StringUtils.substringBetween(wemoCallResponse, "<InsightParams>", "</InsightParams>");
                    } else {
                        value = StringUtils.substringBetween(wemoCallResponse, "<BinaryState>", "</BinaryState>");
                    }
                    if (value != null) {
                        logger.trace("New state '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get actual state for device '{}'", getThing().getUID(), e);
        }
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

    @Override
    public void onStatusChanged(boolean status) {
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        return null;
    }

}
