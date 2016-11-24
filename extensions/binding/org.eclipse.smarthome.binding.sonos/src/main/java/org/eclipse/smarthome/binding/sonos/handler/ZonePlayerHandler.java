/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.sonos.handler;

import static org.eclipse.smarthome.binding.sonos.SonosBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.sonos.SonosBindingConstants;
import org.eclipse.smarthome.binding.sonos.config.ZonePlayerConfiguration;
import org.eclipse.smarthome.binding.sonos.internal.SonosAlarm;
import org.eclipse.smarthome.binding.sonos.internal.SonosEntry;
import org.eclipse.smarthome.binding.sonos.internal.SonosMetaData;
import org.eclipse.smarthome.binding.sonos.internal.SonosXMLParser;
import org.eclipse.smarthome.binding.sonos.internal.SonosZoneGroup;
import org.eclipse.smarthome.binding.sonos.internal.SonosZonePlayerState;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The {@link ZonePlayerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public class ZonePlayerHandler extends BaseThingHandler implements UpnpIOParticipant, DiscoveryListener {

    private Logger logger = LoggerFactory.getLogger(ZonePlayerHandler.class);

    private final static String LINE_IN_URI = "x-rincon-stream:";
    private final static String QUEUE_URI = "x-rincon-queue:";
    private final static String GROUP_URI = "x-rincon:";
    private final static String STREAM_URI = "x-sonosapi-stream:";
    private final static String FILE_URI = "x-file-cifs:";

    private UpnpIOService service;
    private DiscoveryServiceRegistry discoveryServiceRegistry;
    private ScheduledFuture<?> pollingJob;
    private Calendar lastOPMLQuery = null;
    private SonosZonePlayerState savedState = null;

    private final static Collection<String> SERVICE_SUBSCRIPTIONS = Lists.newArrayList("DeviceProperties",
            "AVTransport", "ZoneGroupTopology", "GroupManagement", "RenderingControl", "AudioIn");
    private Map<String, Boolean> subscriptionState = new HashMap<String, Boolean>();
    protected final static int SUBSCRIPTION_DURATION = 1800;
    private static final int SOCKET_TIMEOUT = 5000;

    /**
     * Default notification timeout
     */
    private static final int NOTIFICATION_TIMEOUT = 20000;

    /**
     * Intrinsic lock used to synchronize the execution of notification sounds
     */
    private final Object notificationLock = new Object();

    /**
     * Separate sound volume used for the notification
     */
    private String notificationSoundVolume = null;

    /**
     * {@link ThingHandler} instance of the coordinator speaker used for control delegation
     */
    private ZonePlayerHandler coordinatorHandler;

    /**
     * The default refresh interval when not specified in channel configuration.
     */
    private static final int DEFAULT_REFRESH_INTERVAL = 60;

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());

    private final Object upnpLock = new Object();

    private final Object stateLock = new Object();

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                logger.debug("Polling job");

                // First check if the Sonos zone is set in the UPnP service registry
                // If not, set the thing state to OFFLINE and wait for the next poll
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("UPnP device {} not yet registered", getUDN());
                    updateStatus(ThingStatus.OFFLINE);
                    return;
                }

                // Check if the Sonos zone can be joined
                // If not, set the thing state to OFFLINE and do nothing else
                updatePlayerState();
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    return;
                }

                addSubscription();

                updateZoneInfo();
                updateRunningAlarmProperties();
                updateLed();
                updateMediaInfo();
                updateSleepTimerDuration();
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        }
    };

    private String opmlUrl;

    public ZonePlayerHandler(Thing thing, UpnpIOService upnpIOService,
            DiscoveryServiceRegistry discoveryServiceRegistry, String opmlUrl) {
        super(thing);
        this.opmlUrl = opmlUrl;
        logger.debug("Creating a ZonePlayerHandler for thing '{}'", getThing().getUID());
        if (upnpIOService != null) {
            this.service = upnpIOService;
        }
        if (discoveryServiceRegistry != null) {
            this.discoveryServiceRegistry = discoveryServiceRegistry;
        }

    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed for thing {}", getThing().getUID());

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        this.discoveryServiceRegistry.removeDiscoveryListener(this);
        removeSubscription();
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());

        if (migrateThingType()) {
            // we change the type, so we might need a different handler -> let's finish
            return;
        }

        if (getUDN() != null) {
            updateStatus(ThingStatus.ONLINE);
            this.discoveryServiceRegistry.addDiscoveryListener(this);
            onUpdate();
            super.initialize();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            logger.warn("Cannot initalize the zoneplayer. UDN not set.");
        }
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        if (result.getThingUID().equals(this.getThing().getUID())) {
            if (getUDN().equals(result.getProperties().get(ZonePlayerConfiguration.UDN))) {
                logger.debug("Discovered UDN '{}' for thing '{}'",
                        result.getProperties().get(ZonePlayerConfiguration.UDN), getThing().getUID());
                updateStatus(ThingStatus.ONLINE);
                onUpdate();
            }
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        if (thingUID.equals(this.getThing().getUID())) {
            logger.debug("Setting status for thing '{}' to OFFLINE", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case LED:
                setLed(command);
                break;
            case MUTE:
                setMute(command);
                break;
            case NOTIFICATIONSOUND:
                scheduleNotificationSound(command);
                break;
            case NOTIFICATIONVOLUME:
                setNotificationSoundVolume(command);
                break;
            case STOP:
                try {
                    getCoordinatorHandler().stop();
                } catch (IllegalStateException e) {
                    logger.warn("Cannot handle stop command ({})", e.getMessage());
                }
                break;
            case VOLUME:
                setVolumeForGroup(command);
                break;
            case ADD:
                addMember(command);
                break;
            case REMOVE:
                removeMember(command);
                break;
            case STANDALONE:
                becomeStandAlonePlayer();
                break;
            case PUBLICADDRESS:
                publicAddress();
                break;
            case RADIO:
                playRadio(command);
                break;
            case FAVORITE:
                playFavorite(command);
                break;
            case ALARM:
                setAlarm(command);
                break;
            case SNOOZE:
                snoozeAlarm(command);
                break;
            case SAVEALL:
                saveAllPlayerState();
                break;
            case RESTOREALL:
                restoreAllPlayerState();
                break;
            case SAVE:
                saveState();
                break;
            case RESTORE:
                restoreState();
                break;
            case PLAYLIST:
                playPlayList(command);
                break;
            case PLAYQUEUE:
                playQueue();
                break;
            case PLAYTRACK:
                playTrack(command);
                break;
            case PLAYURI:
                playURI(command);
                break;
            case PLAYLINEIN:
                playLineIn(command);
                break;
            case CONTROL:
                try {
                    if (command instanceof PlayPauseType) {
                        if (command == PlayPauseType.PLAY) {
                            getCoordinatorHandler().play();
                        } else if (command == PlayPauseType.PAUSE) {
                            getCoordinatorHandler().pause();
                        }
                    }
                    if (command instanceof NextPreviousType) {
                        if (command == NextPreviousType.NEXT) {
                            getCoordinatorHandler().next();
                        } else if (command == NextPreviousType.PREVIOUS) {
                            getCoordinatorHandler().previous();
                        }
                    }
                    if (command instanceof RewindFastforwardType) {
                        // Rewind and Fast Forward are currently not implemented by the binding
                    }
                } catch (IllegalStateException e) {
                    logger.warn("Cannot handle control command ({})", e.getMessage());
                }
                break;
            case SLEEPTIMER:
                setSleepTimer(command);
                break;
            case SHUFFLE:
                setShuffle(command);
                break;
            case REPEAT:
                setRepeat(command);
                break;
            default:
                break;

        }
    }

    private void restoreAllPlayerState() {
        Collection<Thing> allThings = thingRegistry.getAll();
        for (Thing aThing : allThings) {
            if (SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(aThing.getThingTypeUID())) {
                ZonePlayerHandler handler = (ZonePlayerHandler) aThing.getHandler();
                handler.restoreState();
            }
        }
    }

    private void saveAllPlayerState() {
        Collection<Thing> allThings = thingRegistry.getAll();
        for (Thing aThing : allThings) {
            if (SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(aThing.getThingTypeUID())) {
                ZonePlayerHandler handler = (ZonePlayerHandler) aThing.getHandler();
                handler.saveState();
            }
        }
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {

        if (getThing().getStatus() == ThingStatus.ONLINE) {

            logger.trace("Received pair '{}':'{}' (service '{}') for thing '{}'",
                    new Object[] { variable, value, service, this.getThing().getUID() });

            String oldValue = this.stateMap.get(variable);
            if (shouldIgnoreVariableUpdate(variable, value, oldValue)) {
                return;
            }

            this.stateMap.put(variable, value);

            // pre-process some variables, eg XML processing
            if (service.equals("AVTransport") && variable.equals("LastChange")) {
                Map<String, String> parsedValues = SonosXMLParser.getAVTransportFromXML(value);
                for (String parsedValue : parsedValues.keySet()) {
                    onValueReceived(parsedValue, parsedValues.get(parsedValue), "AVTransport");
                }
            }

            if (service.equals("RenderingControl") && variable.equals("LastChange")) {
                Map<String, String> parsedValues = SonosXMLParser.getRenderingControlFromXML(value);
                for (String parsedValue : parsedValues.keySet()) {
                    onValueReceived(parsedValue, parsedValues.get(parsedValue), "RenderingControl");
                }
            }

            // update the appropriate channel
            switch (variable) {
                case "TransportState": {
                    updateState(STATE, (stateMap.get("TransportState") != null)
                            ? new StringType(stateMap.get("TransportState")) : UnDefType.UNDEF);
                    if (stateMap.get("TransportState").equals("PLAYING")) {
                        updateState(CONTROL, PlayPauseType.PLAY);
                    }
                    if (stateMap.get("TransportState").equals("STOPPED")) {
                        updateState(CONTROL, PlayPauseType.PAUSE);
                    }
                    if (stateMap.get("TransportState").equals("PAUSED_PLAYBACK")) {
                        updateState(CONTROL, PlayPauseType.PAUSE);
                    }

                    if (isCoordinator()) {
                        // update member states
                        for (String member : getOtherZoneGroupMembers()) {
                            try {
                                ZonePlayerHandler memberHandler = getHandlerByName(member);
                                if (memberHandler != null && memberHandler.getThing() != null
                                        && ThingStatus.ONLINE.equals(memberHandler.getThing().getStatus())) {
                                    memberHandler.onValueReceived("TransportState", value, service);
                                }
                            } catch (IllegalStateException e) {
                                logger.warn("Cannot update playback state for group member ({})", e.getMessage());
                            }
                        }
                    }
                    break;
                }
                case "CurrentPlayMode": {
                    State newState = UnDefType.UNDEF;
                    if (stateMap.get("CurrentPlayMode") != null) {
                        newState = isShuffleActive() ? OnOffType.ON : OnOffType.OFF;
                    }
                    updateState(SHUFFLE, newState);
                    newState = UnDefType.UNDEF;
                    if (stateMap.get("CurrentPlayMode") != null) {
                        newState = new StringType(getRepeatMode());
                    }
                    updateState(REPEAT, newState);
                    break;
                }
                case "CurrentLEDState": {
                    State newState = UnDefType.UNDEF;
                    if (stateMap.get("CurrentLEDState") != null) {
                        if (stateMap.get("CurrentLEDState").equals("On")) {
                            newState = OnOffType.ON;
                        } else {
                            newState = OnOffType.OFF;
                        }
                    }
                    updateState(LED, newState);
                    break;
                }
                case "ZoneName": {
                    updateState(ZONENAME, (stateMap.get("ZoneName") != null) ? new StringType(stateMap.get("ZoneName"))
                            : UnDefType.UNDEF);
                    break;
                }
                case "CurrentZoneName": {
                    updateState(ZONENAME, (stateMap.get("CurrentZoneName") != null)
                            ? new StringType(stateMap.get("CurrentZoneName")) : UnDefType.UNDEF);
                    break;
                }
                case "ZoneGroupState": {
                    updateState(ZONEGROUP, (stateMap.get("ZoneGroupState") != null)
                            ? new StringType(stateMap.get("ZoneGroupState")) : UnDefType.UNDEF);

                    // Update coordinator after a change is made to the grouping of Sonos players
                    updateGroupCoordinator();
                    break;
                }
                case "LocalGroupUUID": {
                    updateState(ZONEGROUPID, (stateMap.get("LocalGroupUUID") != null)
                            ? new StringType(stateMap.get("LocalGroupUUID")) : UnDefType.UNDEF);
                    break;
                }
                case "GroupCoordinatorIsLocal": {
                    State newState = UnDefType.UNDEF;
                    if (stateMap.get("GroupCoordinatorIsLocal") != null) {
                        if (stateMap.get("GroupCoordinatorIsLocal").equals("true")) {
                            newState = OnOffType.ON;
                        } else {
                            newState = OnOffType.OFF;
                        }
                    }
                    updateState(LOCALCOORDINATOR, newState);
                    break;
                }
                case "VolumeMaster": {
                    updateState(VOLUME, (stateMap.get("VolumeMaster") != null)
                            ? new PercentType(stateMap.get("VolumeMaster")) : UnDefType.UNDEF);
                    break;
                }
                case "MuteMaster": {
                    State newState = UnDefType.UNDEF;
                    if (stateMap.get("MuteMaster") != null) {
                        if (stateMap.get("MuteMaster").equals("1")) {
                            newState = OnOffType.ON;
                        } else {
                            newState = OnOffType.OFF;
                        }
                    }
                    updateState(MUTE, newState);
                    break;
                }
                case "LineInConnected": {
                    State newState = UnDefType.UNDEF;
                    if (stateMap.get("LineInConnected") != null) {
                        if (stateMap.get("LineInConnected").equals("true")) {
                            newState = OnOffType.ON;
                        } else {
                            newState = OnOffType.OFF;
                        }
                    }
                    updateState(LINEIN, newState);
                    break;
                }
                case "AlarmRunning": {
                    State newState = UnDefType.UNDEF;
                    if (stateMap.get("AlarmRunning") != null) {
                        if (stateMap.get("AlarmRunning").equals("1")) {
                            newState = OnOffType.ON;
                        } else {
                            newState = OnOffType.OFF;
                        }
                    }
                    updateState(ALARMRUNNING, newState);
                    break;
                }
                case "RunningAlarmProperties": {
                    updateState(ALARMPROPERTIES, (stateMap.get("RunningAlarmProperties") != null)
                            ? new StringType(stateMap.get("RunningAlarmProperties")) : UnDefType.UNDEF);
                    break;
                }
                case "CurrentURIFormatted": {
                    updateState(CURRENTTRACK, (stateMap.get("CurrentURIFormatted") != null)
                            ? new StringType(stateMap.get("CurrentURIFormatted")) : UnDefType.UNDEF);
                    break;
                }
                case "CurrentTitle": {
                    updateState(CURRENTTITLE, (stateMap.get("CurrentTitle") != null)
                            ? new StringType(stateMap.get("CurrentTitle")) : UnDefType.UNDEF);
                    break;
                }
                case "CurrentArtist": {
                    updateState(CURRENTARTIST, (stateMap.get("CurrentArtist") != null)
                            ? new StringType(stateMap.get("CurrentArtist")) : UnDefType.UNDEF);
                    break;
                }
                case "CurrentAlbum": {
                    updateState(CURRENTALBUM, (stateMap.get("CurrentAlbum") != null)
                            ? new StringType(stateMap.get("CurrentAlbum")) : UnDefType.UNDEF);
                    break;
                }
                case "CurrentTrackMetaData": {
                    updateTrackMetaData();
                    break;
                }
                case "CurrentURI": {
                    updateCurrentURIFormatted(value);
                    break;
                }
                case "AVTransportURI": {
                    updateState(CURRENTTRANSPORTURI, (stateMap.get("AVTransportURI") != null)
                            ? new StringType(stateMap.get("AVTransportURI")) : UnDefType.UNDEF);
                    break;
                }
                case "CurrentTrackURI": {
                    updateState(CURRENTTRACKURI, (stateMap.get("CurrentTrackURI") != null)
                            ? new StringType(stateMap.get("CurrentTrackURI")) : UnDefType.UNDEF);
                    break;
                }

                case "CurrentSleepTimerGeneration": {
                    if (value.equals("0")) {
                        updateState(SLEEPTIMER, new DecimalType(0));
                    }
                    break;
                }

                case "SleepTimerGeneration": {
                    if (value.equals("0")) {
                        updateState(SLEEPTIMER, new DecimalType(0));
                    } else {
                        updateSleepTimerDuration();
                    }
                    break;
                }

                case "RemainingSleepTimerDuration": {
                    updateState(SLEEPTIMER,
                            (stateMap.get("RemainingSleepTimerDuration") != null)
                                    ? new DecimalType(
                                            sleepStrTimeToSeconds(stateMap.get("RemainingSleepTimerDuration")))
                                    : UnDefType.UNDEF);
                    break;
                }
            }
        }

    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equalsIgnoreCase(COORDINATOR)) {
            updateGroupCoordinator();
        }
    }

    /**
     * CurrentURI will not change, but will trigger change of CurrentURIFormated
     * CurrentTrackMetaData will not change, but will trigger change of Title, Artist, Album
     */
    private boolean shouldIgnoreVariableUpdate(String variable, String value, String oldValue) {
        return !hasValueChanged(value, oldValue) && !isQueueEvent(variable) && !"CurrentURI".equals(variable)
                && !"CurrentTrackMetaData".equals(variable);
    }

    private boolean hasValueChanged(String value, String oldValue) {
        return oldValue != null ? !oldValue.equals(value) : value != null;
    }

    /**
     * Similar to the AVTransport eventing, the Queue events its state variables
     * as sub values within a synthesized LastChange state variable.
     */
    private boolean isQueueEvent(String variable) {
        return "LastChange".equals(variable);
    }

    private void updateGroupCoordinator() {
        updateState(COORDINATOR, new StringType(getCoordinator()));
        try {
            coordinatorHandler = getHandlerByName(getCoordinator());
        } catch (IllegalStateException e) {
            logger.warn("Cannot update the group coordinator ({})", e.getMessage());
            coordinatorHandler = null;
        }
    }

    private boolean isUpnpDeviceRegistered() {
        return service.isRegistered(this);
    }

    private void addSubscription() {
        synchronized (upnpLock) {
            // Set up GENA Subscriptions
            if (service.isRegistered(this)) {
                for (String subscription : SERVICE_SUBSCRIPTIONS) {
                    if ((subscriptionState.get(subscription) == null)
                            || !subscriptionState.get(subscription).booleanValue()) {
                        logger.debug("{}: Subscribing to service {}...", getUDN(), subscription);
                        service.addSubscription(this, subscription, SUBSCRIPTION_DURATION);
                        subscriptionState.put(subscription, true);
                    }
                }
            }
        }
    }

    private void removeSubscription() {
        synchronized (upnpLock) {
            // Set up GENA Subscriptions
            if (service.isRegistered(this)) {
                for (String subscription : SERVICE_SUBSCRIPTIONS) {
                    if ((subscriptionState.get(subscription) != null)
                            && subscriptionState.get(subscription).booleanValue()) {
                        logger.debug("{}: Unsubscribing from service {}...", getUDN(), subscription);
                        service.removeSubscription(this, subscription);
                    }
                }
            }
            subscriptionState = new HashMap<String, Boolean>();
            service.unregisterParticipant(this);
        }
    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
        synchronized (upnpLock) {
            logger.debug("{}: Subscription to service {} {}", getUDN(), service, succeeded ? "succeeded" : "failed");
            subscriptionState.put(service, succeeded);
        }
    }

    private void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            ZonePlayerConfiguration config = getConfigAs(ZonePlayerConfiguration.class);
            // use default if not specified
            int refreshInterval = DEFAULT_REFRESH_INTERVAL;
            if (config.refresh != null) {
                refreshInterval = config.refresh.intValue();
            }
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void updatePlayerState() {
        Map<String, String> result = service.invokeAction(this, "DeviceProperties", "GetZoneInfo", null);
        if (result.isEmpty()) {
            logger.debug("Sonos player " + getThing().getProperties().get(Thing.PROPERTY_MODEL_ID)
                    + " is not available in local network");
            updateStatus(ThingStatus.OFFLINE);
        } else if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
            logger.debug("Sonos player " + getThing().getProperties().get(Thing.PROPERTY_MODEL_ID)
                    + " has been found in local network");
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected void updateMediaInfo() {
        Map<String, String> inputs = new HashMap<String, String>();
        inputs.put("InstanceID", "0");

        Map<String, String> result = service.invokeAction(this, "AVTransport", "GetMediaInfo", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    protected void updateCurrentZoneName() {
        Map<String, String> result = service.invokeAction(this, "DeviceProperties", "GetZoneAttributes", null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "DeviceProperties");
        }
    }

    protected void updateLed() {
        Map<String, String> result = service.invokeAction(this, "DeviceProperties", "GetLEDState", null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "DeviceProperties");
        }
    }

    protected void updateTime() {
        Map<String, String> result = service.invokeAction(this, "AlarmClock", "GetTimeNow", null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AlarmClock");
        }
    }

    protected void updatePosition() {
        Map<String, String> result = service.invokeAction(this, "AVTransport", "GetPositionInfo", null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    protected void updateRunningAlarmProperties() {
        Map<String, String> result = service.invokeAction(this, "AVTransport", "GetRunningAlarmProperties", null);

        String alarmID = result.get("AlarmID");
        String loggedStartTime = result.get("LoggedStartTime");
        String newStringValue = null;
        if (alarmID != null && loggedStartTime != null) {
            newStringValue = alarmID + " - " + loggedStartTime;
        } else {
            newStringValue = "No running alarm";
        }
        result.put("RunningAlarmProperties", newStringValue);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    protected void updateZoneInfo() {
        Map<String, String> result = service.invokeAction(this, "DeviceProperties", "GetZoneInfo", null);
        Map<String, String> result2 = service.invokeAction(this, "DeviceProperties", "GetZoneAttributes", null);

        result.putAll(result2);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "DeviceProperties");
        }
    }

    public String getCoordinator() {

        if (stateMap.get("ZoneGroupState") != null) {

            Collection<SonosZoneGroup> zoneGroups = SonosXMLParser.getZoneGroupFromXML(stateMap.get("ZoneGroupState"));

            for (SonosZoneGroup zg : zoneGroups) {
                if (zg.getMembers().contains(getUDN())) {
                    return zg.getCoordinator();
                }
            }
        }

        return getUDN();
    }

    public boolean isCoordinator() {
        return getUDN().equals(getCoordinator());
    }

    protected void updateTrackMetaData() {

        try {
            ZonePlayerHandler coordinatorHandler = getCoordinatorHandler();
            SonosMetaData currentTrack = getTrackMetadata();
            SonosMetaData currentUriMetaData = getCurrentURIMetadata();

            if (coordinatorHandler != null && coordinatorHandler != this) {
                coordinatorHandler.updateMediaInfo();
                currentTrack = coordinatorHandler.getTrackMetadata();
            }

            String artist = null;
            String title = null;
            String album = null;

            if (currentTrack != null) {
                artist = !currentTrack.getAlbumArtist().isEmpty() ? currentTrack.getAlbumArtist()
                        : currentTrack.getCreator();

                if (!currentTrack.getTitle().contains("x-sonosapi-stream")) {
                    title = currentTrack.getTitle();
                } else if (opmlUrl == null && currentUriMetaData != null) {
                    artist = currentUriMetaData.getTitle();
                    title = currentTrack.getStreamContent();
                } else {
                    // For tune-in, don't reset the title as it was previously set in updateCurrentURIFormatted
                    // using the Opml HTTP query
                    title = stateMap.get("CurrentTitle");
                }

                album = currentTrack.getAlbum();
            }

            // update individual variables
            for (String member : getZoneGroupMembers()) {
                try {
                    ZonePlayerHandler memberHandler = getHandlerByName(member);
                    if (memberHandler != null && memberHandler.getThing() != null
                            && ThingStatus.ONLINE.equals(memberHandler.getThing().getStatus())) {
                        memberHandler.onValueReceived("CurrentArtist", (artist != null) ? artist : "", "AVTransport");
                        memberHandler.onValueReceived("CurrentTitle", (title != null) ? title : "", "AVTransport");
                        memberHandler.onValueReceived("CurrentAlbum", (album != null) ? album : "", "AVTransport");
                    }
                } catch (IllegalStateException e) {
                    logger.warn("Cannot update track data for group member ({})", e.getMessage());
                }
            }
            updateMediaInfo();

        } catch (IllegalStateException e) {
            logger.warn("Cannot update track data ({})", e.getMessage());
        }
    }

    protected void updateCurrentURIFormatted(String URI) {

        String currentURI = URI;
        SonosMetaData currentTrack = null;
        SonosMetaData currentUriMetaData = null;

        try {
            ZonePlayerHandler coordinatorHandler = getCoordinatorHandler();

            if (coordinatorHandler != null && coordinatorHandler != this) {
                if (currentURI.contains(LINE_IN_URI)) {
                    coordinatorHandler.updateMediaInfo();
                }
                currentURI = coordinatorHandler.getCurrentURI();
                currentTrack = coordinatorHandler.getTrackMetadata();
                currentUriMetaData = coordinatorHandler.getCurrentURIMetadata();
            } else {
                // currentURI = getCurrentURI();
                currentTrack = getTrackMetadata();
                currentUriMetaData = getCurrentURIMetadata();
            }

        } catch (IllegalStateException e) {
            logger.warn("Cannot handle current URI ({})", e.getMessage());
            currentURI = null;
            currentTrack = null;
        }

        if (currentURI != null) {
            String title = stateMap.get("CurrentTitle");
            String resultString = stateMap.get("CurrentURIFormatted");
            boolean needsUpdating = false;

            if (opmlUrl != null && currentURI.contains(STREAM_URI)) {
                String stationID = StringUtils.substringBetween(currentURI, ":s", "?sid");
                String previousStationID = stateMap.get("StationID");

                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.add(Calendar.MINUTE, -1);

                if (previousStationID == null || !previousStationID.equals(stationID) || lastOPMLQuery == null
                        || lastOPMLQuery.before(now)) {

                    this.onValueReceived("StationID", stationID, "AVTransport");

                    String url = opmlUrl;
                    url = StringUtils.replace(url, "%id", stationID);
                    url = StringUtils.replace(url, "%serial", getMACAddress());

                    String response = null;
                    try {
                        response = HttpUtil.executeUrl("GET", url, SOCKET_TIMEOUT);
                    } catch (IOException e) {
                        logger.debug("Request to device failed: {}", e);
                    }

                    if (lastOPMLQuery == null) {
                        lastOPMLQuery = Calendar.getInstance();
                    }
                    lastOPMLQuery.setTime(new Date());

                    if (response != null) {
                        List<String> fields = SonosXMLParser.getRadioTimeFromXML(response);

                        if (fields != null && fields.size() > 0) {

                            resultString = new String();
                            // radio name should be first field
                            title = fields.get(0);

                            Iterator<String> listIterator = fields.listIterator();
                            while (listIterator.hasNext()) {
                                String field = listIterator.next();
                                resultString = resultString + field;
                                if (listIterator.hasNext()) {
                                    resultString = resultString + " - ";
                                }
                            }

                            needsUpdating = true;
                        }
                    }
                }
            }

            if (currentURI.contains(LINE_IN_URI)) {
                if (currentTrack != null) {
                    resultString = stateMap.get("CurrentTitle");
                    needsUpdating = true;
                }
            }

            if (currentURI.contains(STREAM_URI) && opmlUrl == null) {
                if (currentUriMetaData != null && currentTrack != null) {
                    resultString = currentUriMetaData.getTitle() + " - " + currentTrack.getStreamContent();
                    needsUpdating = true;
                }
            }

            if (!currentURI.contains("x-rincon-mp3") && !currentURI.contains(LINE_IN_URI)
                    && !currentURI.contains("x-sonosapi")) {
                if (currentTrack == null) {
                    resultString = null;
                    title = null;
                } else {
                    if (currentTrack.getAlbumArtist().equals("")) {
                        resultString = currentTrack.getCreator() + " - " + currentTrack.getAlbum() + " - "
                                + currentTrack.getTitle();
                    } else {
                        resultString = currentTrack.getAlbumArtist() + " - " + currentTrack.getAlbum() + " - "
                                + currentTrack.getTitle();
                    }
                }
                needsUpdating = true;
            }

            if (needsUpdating) {
                for (String member : getZoneGroupMembers()) {
                    try {
                        ZonePlayerHandler memberHandler = getHandlerByName(member);
                        if (memberHandler != null && memberHandler.getThing() != null
                                && ThingStatus.ONLINE.equals(memberHandler.getThing().getStatus())) {
                            memberHandler.onValueReceived("CurrentURIFormatted",
                                    (resultString != null) ? resultString : "", "AVTransport");
                            memberHandler.onValueReceived("CurrentTitle", (title != null) ? title : "", "AVTransport");
                        }
                    } catch (IllegalStateException e) {
                        logger.warn("Cannot update title for group member ({})", e.getMessage());
                    }
                }
            }
        }
    }

    public boolean isGroupCoordinator() {
        String value = stateMap.get("GroupCoordinatorIsLocal");
        if (value != null) {
            return value.equals("true") ? true : false;
        }

        return false;

    }

    @Override
    public String getUDN() {
        return getConfigAs(ZonePlayerConfiguration.class).udn;
    }

    public String getCurrentURI() {
        return stateMap.get("CurrentURI");
    }

    public SonosMetaData getCurrentURIMetadata() {
        if (stateMap.get("CurrentURIMetaData") != null && !stateMap.get("CurrentURIMetaData").isEmpty()) {
            return SonosXMLParser.getMetaDataFromXML(stateMap.get("CurrentURIMetaData"));
        } else {
            return null;
        }
    }

    public SonosMetaData getTrackMetadata() {
        if (stateMap.get("CurrentTrackMetaData") != null && !stateMap.get("CurrentTrackMetaData").isEmpty()) {
            return SonosXMLParser.getMetaDataFromXML(stateMap.get("CurrentTrackMetaData"));
        } else {
            return null;
        }
    }

    public SonosMetaData getEnqueuedTransportURIMetaData() {

        if (stateMap.get("EnqueuedTransportURIMetaData") != null
                && !stateMap.get("EnqueuedTransportURIMetaData").isEmpty()) {
            return SonosXMLParser.getMetaDataFromXML(stateMap.get("EnqueuedTransportURIMetaData"));
        } else {
            return null;
        }
    }

    public String getMACAddress() {
        updateZoneInfo();
        return stateMap.get("MACAddress");
    }

    public String getPosition() {
        updatePosition();
        return stateMap.get("RelTime");
    }

    public long getCurrenTrackNr() {
        updatePosition();
        String value = stateMap.get("Track");
        if (value != null) {
            return Long.valueOf(value);
        } else {
            return -1;
        }
    }

    public String getVolume() {
        return stateMap.get("VolumeMaster");
    }

    public String getTransportState() {
        return stateMap.get("TransportState");
    }

    public List<SonosEntry> getArtists(String filter) {
        return getEntries("A:", filter);
    }

    public List<SonosEntry> getArtists() {
        return getEntries("A:", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public List<SonosEntry> getAlbums(String filter) {
        return getEntries("A:ALBUM", filter);
    }

    public List<SonosEntry> getAlbums() {
        return getEntries("A:ALBUM", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public List<SonosEntry> getTracks(String filter) {
        return getEntries("A:TRACKS", filter);
    }

    public List<SonosEntry> getTracks() {
        return getEntries("A:TRACKS", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public List<SonosEntry> getQueue(String filter) {
        return getEntries("Q:0", filter);
    }

    public List<SonosEntry> getQueue() {
        return getEntries("Q:0", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public List<SonosEntry> getPlayLists(String filter) {
        return getEntries("SQ:", filter);
    }

    public List<SonosEntry> getPlayLists() {
        return getEntries("SQ:", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public List<SonosEntry> getFavoriteRadios(String filter) {
        return getEntries("R:0/0", filter);
    }

    public List<SonosEntry> getFavoriteRadios() {
        return getEntries("R:0/0", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    /**
     * Searches for entries in the 'favorites' list on a sonos account
     *
     * @return
     */
    public List<SonosEntry> getFavorites() {
        return getEntries("FV:2", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    protected List<SonosEntry> getEntries(String type, String filter) {
        long startAt = 0;

        Map<String, String> inputs = new HashMap<String, String>();
        inputs.put("ObjectID", type);
        inputs.put("BrowseFlag", "BrowseDirectChildren");
        inputs.put("Filter", filter);
        inputs.put("StartingIndex", Long.toString(startAt));
        inputs.put("RequestedCount", Integer.toString(200));
        inputs.put("SortCriteria", "");

        List<SonosEntry> resultList = null;

        Map<String, String> result = service.invokeAction(this, "ContentDirectory", "Browse", inputs);

        long totalMatches = getResultEntry(result, "TotalMatches", type, filter);
        long initialNumberReturned = getResultEntry(result, "NumberReturned", type, filter);

        String initialResult = result.get("Result");

        resultList = SonosXMLParser.getEntriesFromString(initialResult);
        startAt = startAt + initialNumberReturned;

        while (startAt < totalMatches) {

            inputs.put("StartingIndex", Long.toString(startAt));
            result = service.invokeAction(this, "ContentDirectory", "Browse", inputs);

            // Execute this action synchronously
            String nextResult = result.get("Result");
            long numberReturned = getResultEntry(result, "NumberReturned", type, filter);

            resultList.addAll(SonosXMLParser.getEntriesFromString(nextResult));

            startAt = startAt + numberReturned;
        }

        return resultList;
    }

    /**
     * Handles value searching in a SONOS result map (called by {@link #getEntries(String, String)})
     *
     * @param resultInput - the map to be examined for the requestedKey
     * @param requestedKey - the key to be sought in the resultInput map
     * @param entriesType - the 'type' argument of {@link #getEntries(String, String)} method used for logging
     * @param entriesFilter - the 'filter' argument of {@link #getEntries(String, String)} method used for logging
     *
     * @return 0 as long or the value corresponding to the requiredKey if found
     */
    private Long getResultEntry(Map<String, String> resultInput, String requestedKey, String entriesType,
            String entriesFilter) {
        long result = 0;

        try {
            result = Long.valueOf(resultInput.get(requestedKey));
        } catch (NumberFormatException ex) {
            logger.warn("Could not fetch " + requestedKey + " result for type: " + entriesType + " and filter: "
                    + entriesFilter + ". Using default value '0': " + ex.getMessage(), ex);
        }

        return result;
    }

    /**
     * Save the state (track, position etc) of the Sonos Zone player.
     *
     * @return true if no error occurred.
     */
    protected void saveState() {

        synchronized (stateLock) {

            savedState = new SonosZonePlayerState();
            String currentURI = getCurrentURI();

            savedState.transportState = getTransportState();
            savedState.volume = getVolume();

            if (currentURI != null) {

                if (isPlayingStream(currentURI)) {
                    // we are streaming music
                    SonosMetaData track = getTrackMetadata();
                    SonosMetaData current = getCurrentURIMetadata();
                    if (track != null && current != null) {
                        savedState.entry = new SonosEntry("", current.getTitle(), "", "", track.getAlbumArtUri(), "",
                                current.getUpnpClass(), currentURI);
                    }
                } else if (currentURI.contains(GROUP_URI)) {
                    // we are a slave to some coordinator
                    savedState.entry = new SonosEntry("", "", "", "", "", "", "", currentURI);
                } else if (isPlayingLineIn(currentURI)) {
                    // we are streaming from the Line In connection
                    savedState.entry = new SonosEntry("", "", "", "", "", "", "", currentURI);
                } else if (isPlayingQueue(currentURI)) {
                    // we are playing something that sits in the queue
                    SonosMetaData queued = getEnqueuedTransportURIMetaData();
                    if (queued != null) {

                        savedState.track = getCurrenTrackNr();

                        if (queued.getUpnpClass().contains("object.container.playlistContainer")) {
                            // we are playing a real 'saved' playlist
                            List<SonosEntry> playLists = getPlayLists();
                            for (SonosEntry someList : playLists) {
                                if (someList.getTitle().equals(queued.getTitle())) {
                                    savedState.entry = new SonosEntry(someList.getId(), someList.getTitle(),
                                            someList.getParentId(), "", "", "", someList.getUpnpClass(),
                                            someList.getRes());
                                    break;
                                }
                            }

                        } else if (queued.getUpnpClass().contains("object.container")) {
                            // we are playing some other sort of
                            // 'container' - we will save that to a
                            // playlist for our convenience
                            logger.debug("Save State for a container of type {}", queued.getUpnpClass());

                            // save the playlist
                            String existingList = "";
                            List<SonosEntry> playLists = getPlayLists();
                            for (SonosEntry someList : playLists) {
                                if (someList.getTitle().equals(ESH_PREFIX + getUDN())) {
                                    existingList = someList.getId();
                                    break;
                                }
                            }

                            saveQueue(ESH_PREFIX + getUDN(), existingList);

                            // get all the playlists and a ref to our
                            // saved list
                            playLists = getPlayLists();
                            for (SonosEntry someList : playLists) {
                                if (someList.getTitle().equals(ESH_PREFIX + getUDN())) {
                                    savedState.entry = new SonosEntry(someList.getId(), someList.getTitle(),
                                            someList.getParentId(), "", "", "", someList.getUpnpClass(),
                                            someList.getRes());
                                    break;
                                }
                            }

                        }
                    } else {
                        savedState.entry = new SonosEntry("", "", "", "", "", "", "", QUEUE_URI + getUDN() + "#0");
                    }
                }

                savedState.relTime = getPosition();
            } else {
                savedState.entry = null;
            }
        }
    }

    /**
     * Restore the state (track, position etc) of the Sonos Zone player.
     *
     * @return true if no error occurred.
     */
    protected void restoreState() {

        synchronized (stateLock) {
            if (savedState != null) {
                // put settings back
                if (savedState.volume != null) {
                    setVolume(DecimalType.valueOf(savedState.volume));
                }

                if (isCoordinator()) {
                    if (savedState.entry != null) {
                        // check if we have a playlist to deal with
                        if (savedState.entry.getUpnpClass().contains("object.container.playlistContainer")) {

                            addURIToQueue(savedState.entry.getRes(),
                                    SonosXMLParser.compileMetadataString(savedState.entry), 0, true);
                            SonosEntry entry = new SonosEntry("", "", "", "", "", "", "", QUEUE_URI + getUDN() + "#0");
                            setCurrentURI(entry);
                            setPositionTrack(savedState.track);

                        } else {
                            setCurrentURI(savedState.entry);
                            setPosition(savedState.relTime);
                        }
                    }

                    if (savedState.transportState != null) {
                        if (savedState.transportState.equals("PLAYING")) {
                            play();
                        } else if (savedState.transportState.equals("STOPPED")) {
                            stop();
                        } else if (savedState.transportState.equals("PAUSED_PLAYBACK")) {
                            pause();
                        }
                    }
                }
            }
        }
    }

    public void saveQueue(String name, String queueID) {

        if (name != null && queueID != null) {

            Map<String, String> inputs = new HashMap<String, String>();
            inputs.put("Title", name);
            inputs.put("ObjectID", queueID);

            Map<String, String> result = service.invokeAction(this, "AVTransport", "SaveQueue", inputs);

            for (String variable : result.keySet()) {
                this.onValueReceived(variable, result.get(variable), "AVTransport");
            }
        }
    }

    public void setVolume(Command command) {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof IncreaseDecreaseType
                    || command instanceof DecimalType || command instanceof PercentType) {

                Map<String, String> inputs = new HashMap<String, String>();

                String newValue = null;
                if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
                    int i = Integer.valueOf(this.getVolume());
                    newValue = String.valueOf(Math.min(100, i + 1));
                } else if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.DECREASE) {
                    int i = Integer.valueOf(this.getVolume());
                    newValue = String.valueOf(Math.max(0, i - 1));
                } else if (command instanceof OnOffType && command == OnOffType.ON) {
                    newValue = "100";
                } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                    newValue = "0";
                } else if (command instanceof DecimalType) {
                    newValue = command.toString();
                } else {
                    return;
                }
                inputs.put("Channel", "Master");
                inputs.put("DesiredVolume", newValue);

                Map<String, String> result = service.invokeAction(this, "RenderingControl", "SetVolume", inputs);

                for (String variable : result.keySet()) {
                    this.onValueReceived(variable, result.get(variable), "RenderingControl");
                }
            }
        }
    }

    /**
     * Set the VOLUME command specific to the current grouping according to the Sonos behaviour.
     * AdHoc groups handles the volume specifically for each player.
     * Bonded groups delegate the volume to the coordinator which applies the same level to all group members.
     */
    public void setVolumeForGroup(Command command) {
        if (isAdHocGroup() || isStandalonePlayer()) {
            setVolume(command);
        } else {
            try {
                getCoordinatorHandler().setVolume(command);
            } catch (IllegalStateException e) {
                logger.warn("Cannot set group volume ({})", e.getMessage());
            }
        }
    }

    /**
     * Checks if the player receiving the command is part of a group that
     * consists of randomly added players or contains bonded players
     *
     * @return boolean
     */
    private boolean isAdHocGroup() {
        SonosZoneGroup currentZoneGroup = getCurrentZoneGroup();
        if (currentZoneGroup != null) {
            List<String> zoneGroupMemberNames = currentZoneGroup.getMemberZoneNames();

            if (zoneGroupMemberNames != null) {
                for (String zoneName : zoneGroupMemberNames) {
                    if (!zoneName.equals(zoneGroupMemberNames.get(0))) {
                        // At least one "ZoneName" differs so we have an AdHoc group
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the player receiving the command is a standalone player
     *
     * @return boolean
     */
    private boolean isStandalonePlayer() {
        return getCurrentZoneGroup() != null ? getCurrentZoneGroup().getMembers().size() == 1 : true;
    }

    /**
     * Returns the current zone group
     * (of which the player receiving the command is part)
     *
     * @return {@link SonosZoneGroup}
     */
    private SonosZoneGroup getCurrentZoneGroup() {
        String zoneGroupState = stateMap.get("ZoneGroupState");
        if (zoneGroupState != null) {
            Collection<SonosZoneGroup> zoneGroups = SonosXMLParser.getZoneGroupFromXML(zoneGroupState);

            for (SonosZoneGroup zoneGroup : zoneGroups) {
                if (zoneGroup.getMembers().contains(getUDN())) {
                    return zoneGroup;
                }
            }
        }
        logger.warn("Could not fetch Sonos group state information");
        return null;
    }

    /**
     * Sets the volume level for a notification sound
     * (initializes {@link #notificationSoundVolume})
     *
     * @param command
     */
    public void setNotificationSoundVolume(Command command) {
        if (command != null) {
            notificationSoundVolume = command.toString();
        }
    }

    /**
     * Gets the volume level for a notification sound
     */
    public PercentType getNotificationSoundVolume() {
        if (notificationSoundVolume == null) {
            // we need to initialize the value for the first time
            notificationSoundVolume = getVolume();
            if (notificationSoundVolume != null) {
                updateState(SonosBindingConstants.NOTIFICATIONVOLUME,
                        new PercentType(new BigDecimal(notificationSoundVolume)));
            }
        }
        if (notificationSoundVolume != null) {
            return new PercentType(new BigDecimal(notificationSoundVolume));
        } else {
            return null;
        }
    }

    public void addURIToQueue(String URI, String meta, int desiredFirstTrack, boolean enqueueAsNext) {

        if (URI != null && meta != null) {

            Map<String, String> inputs = new HashMap<String, String>();

            try {
                inputs.put("InstanceID", "0");
                inputs.put("EnqueuedURI", URI);
                inputs.put("EnqueuedURIMetaData", meta);
                inputs.put("DesiredFirstTrackNumberEnqueued", Integer.toString(desiredFirstTrack));
                inputs.put("EnqueueAsNext", Boolean.toString(enqueueAsNext));
            } catch (NumberFormatException ex) {
                logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
            }

            Map<String, String> result = service.invokeAction(this, "AVTransport", "AddURIToQueue", inputs);

            for (String variable : result.keySet()) {
                this.onValueReceived(variable, result.get(variable), "AVTransport");
            }
        }
    }

    public void setCurrentURI(SonosEntry newEntry) {
        setCurrentURI(newEntry.getRes(), SonosXMLParser.compileMetadataString(newEntry));
    }

    public void setCurrentURI(String URI, String URIMetaData) {
        if (URI != null && URIMetaData != null) {

            Map<String, String> inputs = new HashMap<String, String>();

            try {
                inputs.put("InstanceID", "0");
                inputs.put("CurrentURI", URI);
                inputs.put("CurrentURIMetaData", URIMetaData);
            } catch (NumberFormatException ex) {
                logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
            }

            Map<String, String> result = service.invokeAction(this, "AVTransport", "SetAVTransportURI", inputs);

            for (String variable : result.keySet()) {
                this.onValueReceived(variable, result.get(variable), "AVTransport");
            }
        }
    }

    public void setPosition(String relTime) {
        seek("REL_TIME", relTime);
    }

    public void setPositionTrack(long tracknr) {
        seek("TRACK_NR", Long.toString(tracknr));
    }

    public void setPositionTrack(String tracknr) {
        seek("TRACK_NR", tracknr);
    }

    protected void seek(String unit, String target) {
        if (unit != null && target != null) {

            Map<String, String> inputs = new HashMap<String, String>();

            try {
                inputs.put("InstanceID", "0");
                inputs.put("Unit", unit);
                inputs.put("Target", target);
            } catch (NumberFormatException ex) {
                logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
            }

            Map<String, String> result = service.invokeAction(this, "AVTransport", "Seek", inputs);

            for (String variable : result.keySet()) {
                this.onValueReceived(variable, result.get(variable), "AVTransport");
            }
        }
    }

    public void play() {

        Map<String, String> inputs = new HashMap<String, String>();
        inputs.put("Speed", "1");

        Map<String, String> result = service.invokeAction(this, "AVTransport", "Play", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public void stop() {
        Map<String, String> result = service.invokeAction(this, "AVTransport", "Stop", null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public void pause() {
        Map<String, String> result = service.invokeAction(this, "AVTransport", "Pause", null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public void setShuffle(Command command) {
        if ((command != null) && (command instanceof OnOffType || command instanceof OpenClosedType
                || command instanceof UpDownType)) {

            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();

                if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN)) {
                    switch (coordinator.getRepeatMode()) {
                        case "ALL":
                            coordinator.updatePlayMode("SHUFFLE");
                            break;
                        case "ONE":
                            coordinator.updatePlayMode("SHUFFLE_REPEAT_ONE");
                            break;
                        case "OFF":
                            coordinator.updatePlayMode("SHUFFLE_NOREPEAT");
                            break;
                    }
                } else if (command.equals(OnOffType.OFF) || command.equals(UpDownType.DOWN)
                        || command.equals(OpenClosedType.CLOSED)) {
                    switch (coordinator.getRepeatMode()) {
                        case "ALL":
                            coordinator.updatePlayMode("REPEAT_ALL");
                            break;
                        case "ONE":
                            coordinator.updatePlayMode("REPEAT_ONE");
                            break;
                        case "OFF":
                            coordinator.updatePlayMode("NORMAL");
                            break;
                    }
                }

            } catch (IllegalStateException e) {
                logger.warn("Cannot handle shuffle command ({})", e.getMessage());
            }
        }
    }

    public void setRepeat(Command command) {
        if ((command != null) && (command instanceof StringType)) {
            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();

                switch (command.toString()) {
                    case "ALL":
                        coordinator.updatePlayMode(coordinator.isShuffleActive() ? "SHUFFLE" : "REPEAT_ALL");
                        break;
                    case "ONE":
                        coordinator.updatePlayMode(coordinator.isShuffleActive() ? "SHUFFLE_REPEAT_ONE" : "REPEAT_ONE");
                        break;
                    case "OFF":
                        coordinator.updatePlayMode(coordinator.isShuffleActive() ? "SHUFFLE_NOREPEAT" : "NORMAL");
                        break;
                    default:
                        logger.warn("{}: unexpected repeat command; accepted values are ALL, ONE and OFF",
                                command.toString());
                        break;
                }

            } catch (IllegalStateException e) {
                logger.warn("Cannot handle repeat command ({})", e.getMessage());
            }
        }
    }

    public Boolean isShuffleActive() {
        return ((stateMap.get("CurrentPlayMode") != null) && stateMap.get("CurrentPlayMode").startsWith("SHUFFLE"))
                ? true : false;
    }

    public String getRepeatMode() {
        String mode = "OFF";
        if (stateMap.get("CurrentPlayMode") != null) {
            switch (stateMap.get("CurrentPlayMode")) {
                case "REPEAT_ALL":
                case "SHUFFLE":
                    mode = "ALL";
                    break;
                case "REPEAT_ONE":
                case "SHUFFLE_REPEAT_ONE":
                    mode = "ONE";
                    break;
                case "NORMAL":
                case "SHUFFLE_NOREPEAT":
                default:
                    mode = "OFF";
                    break;
            }
        }
        return mode;
    }

    protected void updatePlayMode(String playMode) {
        Map<String, String> inputs = new HashMap<String, String>();
        inputs.put("InstanceID", "0");
        inputs.put("NewPlayMode", playMode);

        Map<String, String> result = service.invokeAction(this, "AVTransport", "SetPlayMode", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    /**
     * Clear all scheduled music from the current queue.
     *
     */
    public void removeAllTracksFromQueue() {
        Map<String, String> inputs = new HashMap<String, String>();
        inputs.put("InstanceID", "0");

        Map<String, String> result = service.invokeAction(this, "AVTransport", "RemoveAllTracksFromQueue", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    /**
     * Play music from the line-in of the given Player referenced by the given UDN or name
     *
     * @param udn or name
     */
    public void playLineIn(Command command) {

        if (command != null && command instanceof StringType) {

            try {
                String remotePlayerName = command.toString();
                ZonePlayerHandler coordinatorHandler = getCoordinatorHandler();
                ZonePlayerHandler remoteHandler = getHandlerByName(remotePlayerName);

                // stop whatever is currently playing
                coordinatorHandler.stop();

                // set the URI
                coordinatorHandler.setCurrentURI(LINE_IN_URI + remoteHandler.getUDN(), "");

                // take the system off mute
                coordinatorHandler.setMute(OnOffType.OFF);

                // start jammin'
                coordinatorHandler.play();

            } catch (IllegalStateException e) {
                logger.warn("Cannot play line-in ({})", e.getMessage());
            }
        }
    }

    private ZonePlayerHandler getCoordinatorHandler() throws IllegalStateException {
        if (coordinatorHandler == null) {
            try {
                coordinatorHandler = getHandlerByName(getCoordinator());
            } catch (IllegalStateException e) {
                coordinatorHandler = null;
                throw new IllegalStateException("Missing group coordinator " + getCoordinator());
            }
        }
        return coordinatorHandler;
    }

    /**
     * Returns a list of all zone group members this particular player is member of
     * Or empty list if the players is not assigned to any group
     *
     * @return a list of Strings containing the UDNs of other group members
     */
    protected List<String> getZoneGroupMembers() {
        List<String> result = new ArrayList<>();

        if (stateMap.get("ZoneGroupState") != null) {
            Collection<SonosZoneGroup> zoneGroups = SonosXMLParser.getZoneGroupFromXML(stateMap.get("ZoneGroupState"));

            for (SonosZoneGroup zg : zoneGroups) {
                if (zg.getMembers().contains(getUDN())) {
                    result.addAll(zg.getMembers());
                    break;
                }
            }
        } else {
            // If the group topology was not yet received, return at least the current Sonos zone
            result.add(getUDN());
        }
        return result;
    }

    /**
     * Returns a list of other zone group members this particular player is member of
     * Or empty list if the players is not assigned to any group
     *
     * @return a list of Strings containing the UDNs of other group members
     */
    protected List<String> getOtherZoneGroupMembers() {
        List<String> zoneGroupMembers = getZoneGroupMembers();
        zoneGroupMembers.remove(getUDN());
        return zoneGroupMembers;
    }

    protected ZonePlayerHandler getHandlerByName(String remotePlayerName) throws IllegalStateException {
        if (thingRegistry != null) {
            for (ThingTypeUID supportedThingType : SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS) {
                Thing thing = thingRegistry.get(new ThingUID(supportedThingType, remotePlayerName));
                if (thing != null) {
                    return (ZonePlayerHandler) thing.getHandler();
                }
            }
            Collection<Thing> allThings = thingRegistry.getAll();
            for (Thing aThing : allThings) {
                if (SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(aThing.getThingTypeUID())
                        && aThing.getConfiguration().get(ZonePlayerConfiguration.UDN).equals(remotePlayerName)) {
                    return (ZonePlayerHandler) aThing.getHandler();
                }
            }
        }
        throw new IllegalStateException("Could not find handler for " + remotePlayerName);
    }

    public void setMute(Command command) {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {

                Map<String, String> inputs = new HashMap<String, String>();
                inputs.put("Channel", "Master");

                if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN)) {
                    inputs.put("DesiredMute", "True");
                } else if (command.equals(OnOffType.OFF) || command.equals(UpDownType.DOWN)
                        || command.equals(OpenClosedType.CLOSED)) {
                    inputs.put("DesiredMute", "False");

                }

                Map<String, String> result = service.invokeAction(this, "RenderingControl", "SetMute", inputs);

                for (String variable : result.keySet()) {
                    this.onValueReceived(variable, result.get(variable), "RenderingControl");
                }
            }
        }
    }

    public List<SonosAlarm> getCurrentAlarmList() {
        Map<String, String> result = service.invokeAction(this, "AlarmClock", "ListAlarms", null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AlarmClock");
        }

        return SonosXMLParser.getAlarmsFromStringResult(result.get("CurrentAlarmList"));
    }

    public void updateAlarm(SonosAlarm alarm) {
        if (alarm != null) {

            Map<String, String> inputs = new HashMap<String, String>();

            try {
                inputs.put("ID", Integer.toString(alarm.getID()));
                inputs.put("StartLocalTime", alarm.getStartTime());
                inputs.put("Duration", alarm.getDuration());
                inputs.put("Recurrence", alarm.getRecurrence());
                inputs.put("RoomUUID", alarm.getRoomUUID());
                inputs.put("ProgramURI", alarm.getProgramURI());
                inputs.put("ProgramMetaData", alarm.getProgramMetaData());
                inputs.put("PlayMode", alarm.getPlayMode());
                inputs.put("Volume", Integer.toString(alarm.getVolume()));
                if (alarm.getIncludeLinkedZones()) {
                    inputs.put("IncludeLinkedZones", "1");
                } else {
                    inputs.put("IncludeLinkedZones", "0");
                }

                if (alarm.getEnabled()) {
                    inputs.put("Enabled", "1");
                } else {
                    inputs.put("Enabled", "0");
                }
            } catch (NumberFormatException ex) {
                logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
            }

            Map<String, String> result = service.invokeAction(this, "AlarmClock", "UpdateAlarm", inputs);

            for (String variable : result.keySet()) {
                this.onValueReceived(variable, result.get(variable), "AlarmClock");
            }
        }
    }

    public void setAlarm(Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)) {
                setAlarm(true);
            } else if (command.equals(OnOffType.OFF) || command.equals(UpDownType.DOWN)
                    || command.equals(OpenClosedType.CLOSED)) {
                setAlarm(false);
            }
        }
    }

    public void setAlarm(boolean alarmSwitch) {

        List<SonosAlarm> sonosAlarms = getCurrentAlarmList();

        // find the nearest alarm - take the current time from the Sonos system,
        // not the system where we are running
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        String currentLocalTime = getTime();
        Date currentDateTime = null;
        try {
            currentDateTime = fmt.parse(currentLocalTime);
        } catch (ParseException e) {
            logger.error("An exception occurred while formatting a date");
            e.printStackTrace();
        }

        if (currentDateTime != null) {
            Calendar currentDateTimeCalendar = Calendar.getInstance();
            currentDateTimeCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            currentDateTimeCalendar.setTime(currentDateTime);
            currentDateTimeCalendar.add(Calendar.DAY_OF_YEAR, 10);
            long shortestDuration = currentDateTimeCalendar.getTimeInMillis() - currentDateTime.getTime();

            SonosAlarm firstAlarm = null;

            for (SonosAlarm anAlarm : sonosAlarms) {
                SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss");
                durationFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date durationDate;
                try {
                    durationDate = durationFormat.parse(anAlarm.getDuration());
                } catch (ParseException e) {
                    logger.error("An exception occurred while parsing a date : '{}'", e.getMessage());
                    continue;
                }

                long duration = durationDate.getTime();

                if (duration < shortestDuration && anAlarm.getRoomUUID().equals(getUDN())) {
                    shortestDuration = duration;
                    firstAlarm = anAlarm;
                }
            }

            // Set the Alarm
            if (firstAlarm != null) {

                if (alarmSwitch) {
                    firstAlarm.setEnabled(true);
                } else {
                    firstAlarm.setEnabled(false);
                }

                updateAlarm(firstAlarm);

            }
        }
    }

    public String getTime() {
        updateTime();
        return stateMap.get("CurrentLocalTime");
    }

    public Boolean isAlarmRunning() {
        return ((stateMap.get("AlarmRunning") != null) && stateMap.get("AlarmRunning").equals("1")) ? true : false;
    }

    public void snoozeAlarm(Command command) {
        if (isAlarmRunning() && command instanceof DecimalType) {

            int minutes = ((DecimalType) command).intValue();

            Map<String, String> inputs = new HashMap<String, String>();

            Calendar snoozePeriod = Calendar.getInstance();
            snoozePeriod.setTimeZone(TimeZone.getTimeZone("GMT"));
            snoozePeriod.setTimeInMillis(0);
            snoozePeriod.add(Calendar.MINUTE, minutes);
            SimpleDateFormat pFormatter = new SimpleDateFormat("HH:mm:ss");
            pFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

            try {
                inputs.put("Duration", pFormatter.format(snoozePeriod.getTime()));
            } catch (NumberFormatException ex) {
                logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
            }

            Map<String, String> result = service.invokeAction(this, "AVTransport", "SnoozeAlarm", inputs);

            for (String variable : result.keySet()) {
                this.onValueReceived(variable, result.get(variable), "AVTransport");
            }
        } else {
            logger.warn("There is no alarm running on {}", getUDN());
        }
    }

    public Boolean isLineInConnected() {
        return ((stateMap.get("LineInConnected") != null) && stateMap.get("LineInConnected").equals("true")) ? true
                : false;
    }

    public void becomeStandAlonePlayer() {
        Map<String, String> result = service.invokeAction(this, "AVTransport", "BecomeCoordinatorOfStandaloneGroup",
                null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public void addMember(Command command) {
        if (command != null && command instanceof StringType) {
            SonosEntry entry = new SonosEntry("", "", "", "", "", "", "", GROUP_URI + getUDN());
            try {
                getHandlerByName(command.toString()).setCurrentURI(entry);
            } catch (IllegalStateException e) {
                logger.warn("Cannot add group member ({})", e.getMessage());
            }
        }
    }

    public boolean publicAddress() {
        // check if sourcePlayer has a line-in connected
        if (isLineInConnected()) {

            // first remove this player from its own group if any
            becomeStandAlonePlayer();

            List<SonosZoneGroup> currentSonosZoneGroups = new ArrayList<SonosZoneGroup>();
            for (SonosZoneGroup grp : SonosXMLParser.getZoneGroupFromXML(stateMap.get("ZoneGroupState"))) {
                currentSonosZoneGroups.add((SonosZoneGroup) grp.clone());
            }

            // add all other players to this new group
            for (SonosZoneGroup group : currentSonosZoneGroups) {
                for (String player : group.getMembers()) {
                    try {
                        ZonePlayerHandler somePlayer = getHandlerByName(player);
                        if (somePlayer != this) {
                            somePlayer.becomeStandAlonePlayer();
                            somePlayer.stop();
                            addMember(StringType.valueOf(somePlayer.getUDN()));
                        }
                    } catch (IllegalStateException e) {
                        logger.warn("Cannot add to group ({})", e.getMessage());
                    }
                }
            }

            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();
                // set the URI of the group to the line-in
                SonosEntry entry = new SonosEntry("", "", "", "", "", "", "", LINE_IN_URI + getUDN());
                coordinator.setCurrentURI(entry);
                coordinator.play();

                return true;

            } catch (IllegalStateException e) {
                logger.warn("Cannot handle command ({})", e.getMessage());
                return false;
            }

        } else {
            logger.warn("Line-in of {} is not connected", getUDN());
            return false;
        }

    }

    /**
     * Play a given url to music in one of the music libraries.
     *
     * @param url
     *            in the format of //host/folder/filename.mp3
     */
    public void playURI(Command command) {

        if (command != null && command instanceof StringType) {

            try {
                String url = command.toString();

                ZonePlayerHandler coordinator = getCoordinatorHandler();

                // stop whatever is currently playing
                coordinator.stop();

                // clear any tracks which are pending in the queue
                coordinator.removeAllTracksFromQueue();

                // add the new track we want to play to the queue
                // The url will be prefixed with x-file-cifs if it is NOT a http URL
                if (!url.startsWith("x-") && (!url.startsWith("http"))) {
                    // default to file based url
                    url = FILE_URI + url;
                }
                coordinator.addURIToQueue(url, "", 0, true);

                // set the current playlist to our new queue
                coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");

                // take the system off mute
                coordinator.setMute(OnOffType.OFF);

                // start jammin'
                coordinator.play();

            } catch (IllegalStateException e) {
                logger.warn("Cannot play URI ({})", e.getMessage());
            }
        }

    }

    private void scheduleNotificationSound(final Command command) {
        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                synchronized (notificationLock) {
                    playNotificationSoundURI(command);
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Play a given notification sound
     *
     * @param url in the format of //host/folder/filename.mp3
     */
    public void playNotificationSoundURI(Command notificationURL) {

        if (notificationURL != null && notificationURL instanceof StringType) {
            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();

                String currentURI = coordinator.getCurrentURI();

                if (isPlayingStream(currentURI)) {
                    handleRadioStream(currentURI, notificationURL, coordinator);
                } else if (isPlayingLineIn(currentURI)) {
                    handleLineIn(currentURI, notificationURL, coordinator);
                } else if (isPlayingQueue(currentURI)) {
                    handleSharedQueue(notificationURL, coordinator);
                } else if (isPlaylistEmpty(coordinator)) {
                    handleEmptyQueue(notificationURL, coordinator);
                }
                synchronized (notificationLock) {
                    notificationLock.notify();
                }

            } catch (IllegalStateException e) {
                logger.warn("Cannot play sound ({})", e.getMessage());
            }
        }
    }

    private boolean isPlaylistEmpty(ZonePlayerHandler coordinator) {
        return coordinator.getQueue().isEmpty();
    }

    private boolean isPlayingQueue(String currentURI) {
        if (currentURI == null) {
            return false;
        }
        return currentURI.contains(QUEUE_URI);
    }

    private boolean isPlayingStream(String currentURI) {
        if (currentURI == null) {
            return false;
        }
        return currentURI.contains(STREAM_URI);
    }

    private boolean isPlayingLineIn(String currentURI) {
        if (currentURI == null) {
            return false;
        }
        return currentURI.contains(LINE_IN_URI);
    }

    /**
     * Does a chain of predefined actions when a Notification sound is played by
     * {@link ZonePlayerHandler#playNotificationSoundURI(Command)} in case
     * radio streaming is currently loaded
     *
     * @param currentStreamURI - the currently loaded stream's URI
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     */
    private void handleRadioStream(String currentStreamURI, Command notificationURL, ZonePlayerHandler coordinator) {
        String nextAction = coordinator.getTransportState();
        SonosMetaData track = coordinator.getTrackMetadata();
        SonosMetaData currentURI = coordinator.getCurrentURIMetadata();

        if (track != null && currentURI != null) {
            handleNotificationSound(notificationURL, coordinator);
            coordinator.setCurrentURI(new SonosEntry("", currentURI.getTitle(), "", "", track.getAlbumArtUri(), "",
                    currentURI.getUpnpClass(), currentStreamURI));

            restoreLastTransportState(coordinator, nextAction);
        }
    }

    /**
     * Does a chain of predefined actions when a Notification sound is played by
     * {@link ZonePlayerHandler#playNotificationSoundURI(Command)} in case
     * line in is currently loaded
     *
     * @param currentLineInURI - the currently loaded line-in URI
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     */
    private void handleLineIn(String currentLineInURI, Command notificationURL, ZonePlayerHandler coordinator) {
        String nextAction = coordinator.getTransportState();

        handleNotificationSound(notificationURL, coordinator);
        coordinator.setCurrentURI(currentLineInURI, "");

        restoreLastTransportState(coordinator, nextAction);
    }

    /**
     * Does a chain of predefined actions when a Notification sound is played by
     * {@link ZonePlayerHandler#playNotificationSoundURI(Command)} in case
     * shared queue is currently loaded
     *
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     */
    private void handleSharedQueue(Command notificationURL, ZonePlayerHandler coordinator) {
        String nextAction = coordinator.getTransportState();
        String trackPosition = coordinator.getPosition();
        long currentTrackNumber = coordinator.getCurrenTrackNr();

        handleNotificationSound(notificationURL, coordinator);
        coordinator.setPositionTrack(currentTrackNumber);
        coordinator.setPosition(trackPosition);

        restoreLastTransportState(coordinator, nextAction);
    }

    /**
     * Handle the execution of the notification sound by sequentially executing the required steps.
     *
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     */
    private void handleNotificationSound(Command notificationURL, ZonePlayerHandler coordinator) {
        String originalVolume = (isAdHocGroup() || isStandalonePlayer()) ? getVolume() : coordinator.getVolume();
        coordinator.stop();
        applyNotificationSoundVolume();
        int notificationPosition = coordinator.getQueue().size() + 1;
        coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");
        coordinator.addURIToQueue(notificationURL.toString(), "", notificationPosition, false);
        coordinator.setPositionTrack(notificationPosition);
        coordinator.play();
        waitForFinishedNotification();
        if (originalVolume != null) {
            setVolumeForGroup(DecimalType.valueOf(originalVolume));
        }
        coordinator.removeRangeOfTracksFromQueue(new StringType(Integer.toString(notificationPosition) + ",1"));
    }

    private void restoreLastTransportState(ZonePlayerHandler coordinator, String nextAction) {
        switch (nextAction) {
            case "PLAYING":
                coordinator.play();
                waitForTransportState("PLAYING");
                break;
            case "PAUSED_PLAYBACK":
                coordinator.pause();
                break;
        }
    }

    /**
     * Does a chain of predefined actions when a Notification sound is played by
     * {@link ZonePlayerHandler#playNotificationSoundURI(Command)} in case
     * empty queue is currently loaded
     *
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     */
    private void handleEmptyQueue(Command notificationURL, ZonePlayerHandler coordinator) {
        String originalVolume = coordinator.getVolume();
        coordinator.applyNotificationSoundVolume();
        coordinator.playURI(notificationURL);
        waitForFinishedNotification();
        coordinator.removeAllTracksFromQueue();
        coordinator.setVolume(DecimalType.valueOf(originalVolume));
    }

    /**
     * Applies the notification sound volume level to the group (if not null)
     *
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     */
    private void applyNotificationSoundVolume() {
        PercentType volume = getNotificationSoundVolume();
        if (volume != null) {
            setVolumeForGroup(volume);
        }
    }

    private void waitForFinishedNotification() {
        waitForTransportState("PLAYING");

        // check Sonos state events to determine the end of the notification sound
        String notificationTitle = stateMap.get("CurrentTitle");
        long playstart = System.currentTimeMillis();
        while (System.currentTimeMillis() - playstart < NOTIFICATION_TIMEOUT) {
            try {
                Thread.sleep(50);
                if (!notificationTitle.equals(stateMap.get("CurrentTitle"))
                        || !"PLAYING".equals(stateMap.get("TransportState"))) {
                    break;
                }
            } catch (InterruptedException e) {
                logger.error("InterruptedException during playing a notification sound");
            }
        }
    }

    private void waitForTransportState(String state) {
        if (stateMap.get("TransportState") != null) {
            long start = System.currentTimeMillis();
            while (!stateMap.get("TransportState").equals(state)) {
                try {
                    Thread.sleep(50);
                    if (System.currentTimeMillis() - start > NOTIFICATION_TIMEOUT) {
                        break;
                    }
                } catch (InterruptedException e) {
                    logger.error("InterruptedException during playing a notification sound");
                }
            }
        }
    }

    /**
     * Removes a range of tracks from the queue.
     * (<x,y> will remove y songs started by the song number x)
     *
     * @param command - must be in the format <startIndex, numberOfSongs>
     */
    public void removeRangeOfTracksFromQueue(Command command) {
        if (command != null && command instanceof StringType) {
            Map<String, String> inputs = new HashMap<String, String>();
            String[] rangeInputSplit = command.toString().split(",");

            // If range input is incorrect, remove the first song by default
            String startIndex = rangeInputSplit[0] != null ? rangeInputSplit[0] : "1";
            String numberOfTracks = rangeInputSplit[1] != null ? rangeInputSplit[1] : "1";

            inputs.put("InstanceID", "0");
            inputs.put("StartingIndex", startIndex);
            inputs.put("NumberOfTracks", numberOfTracks);

            Map<String, String> result = service.invokeAction(this, "AVTransport", "RemoveTrackRangeFromQueue", inputs);

            for (String variable : result.keySet()) {
                this.onValueReceived(variable, result.get(variable), "AVTransport");
            }
        }
    }

    public void playQueue() {
        try {
            ZonePlayerHandler coordinator = getCoordinatorHandler();

            // set the current playlist to our new queue
            coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");

            // take the system off mute
            coordinator.setMute(OnOffType.OFF);

            // start jammin'
            coordinator.play();

        } catch (IllegalStateException e) {
            logger.warn("Cannot play queue ({})", e.getMessage());
        }
    }

    public void setLed(Command command) {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {

                Map<String, String> inputs = new HashMap<String, String>();

                if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN)) {
                    inputs.put("DesiredLEDState", "On");
                } else if (command.equals(OnOffType.OFF) || command.equals(UpDownType.DOWN)
                        || command.equals(OpenClosedType.CLOSED)) {
                    inputs.put("DesiredLEDState", "Off");

                }

                Map<String, String> result = service.invokeAction(this, "DeviceProperties", "SetLEDState", inputs);
                Map<String, String> result2 = service.invokeAction(this, "DeviceProperties", "GetLEDState", null);

                result.putAll(result2);

                for (String variable : result.keySet()) {
                    this.onValueReceived(variable, result.get(variable), "DeviceProperties");
                }
            }
        }
    }

    public void removeMember(Command command) {
        if (command != null && command instanceof StringType) {
            try {
                ZonePlayerHandler oldmemberHandler = getHandlerByName(command.toString());

                oldmemberHandler.becomeStandAlonePlayer();
                SonosEntry entry = new SonosEntry("", "", "", "", "", "", "",
                        QUEUE_URI + oldmemberHandler.getUDN() + "#0");
                oldmemberHandler.setCurrentURI(entry);
            } catch (IllegalStateException e) {
                logger.warn("Cannot remove group member ({})", e.getMessage());
            }
        }
    }

    public void previous() {
        Map<String, String> result = service.invokeAction(this, "AVTransport", "Previous", null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public void next() {
        Map<String, String> result = service.invokeAction(this, "AVTransport", "Next", null);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public void playRadio(Command command) {

        if (command instanceof StringType) {
            String station = command.toString();
            List<SonosEntry> stations = getFavoriteRadios();

            SonosEntry theEntry = null;
            // search for the appropriate radio based on its name (title)
            for (SonosEntry someStation : stations) {
                if (someStation.getTitle().equals(station)) {
                    theEntry = someStation;
                    break;
                }
            }

            // set the URI of the group coordinator
            if (theEntry != null) {
                try {
                    ZonePlayerHandler coordinator = getCoordinatorHandler();
                    coordinator.setCurrentURI(theEntry);
                    coordinator.play();
                } catch (IllegalStateException e) {
                    logger.warn("Cannot play radio ({})", e.getMessage());
                }
            } else {
                logger.warn("Radio station '{}' not found", station);
            }
        }

    }

    /**
     * This will attempt to match the station string with a entry in the
     * favorites list, this supports both single entries and playlists
     *
     * @param favorite to match
     * @return true if a match was found and played.
     */
    public void playFavorite(Command command) {

        if (command instanceof StringType) {
            String favorite = command.toString();
            List<SonosEntry> favorites = getFavorites();

            SonosEntry theEntry = null;
            // search for the appropriate favorite based on its name (title)
            for (SonosEntry entry : favorites) {
                if (entry.getTitle().equals(favorite)) {
                    theEntry = entry;
                    break;
                }
            }

            // set the URI of the group coordinator
            if (theEntry != null) {
                try {
                    ZonePlayerHandler coordinator = getCoordinatorHandler();

                    /**
                     * If this is a playlist we need to treat it as such
                     */
                    if (theEntry.getResourceMetaData() != null
                            && theEntry.getResourceMetaData().getUpnpClass().startsWith("object.container")) {
                        coordinator.removeAllTracksFromQueue();
                        coordinator.addURIToQueue(theEntry);
                        coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");
                        String firstTrackNumberEnqueued = stateMap.get("FirstTrackNumberEnqueued");
                        if (firstTrackNumberEnqueued != null) {
                            coordinator.seek("TRACK_NR", firstTrackNumberEnqueued);
                        }
                    } else {
                        coordinator.setCurrentURI(theEntry);
                    }
                    coordinator.play();
                } catch (IllegalStateException e) {
                    logger.warn("Cannot paly favorite ({})", e.getMessage());
                }
            } else {
                logger.warn("Favorite '{}' not found", favorite);
            }

        }
    }

    public void playTrack(Command command) {

        if (command != null && command instanceof DecimalType) {
            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();

                String trackNumber = command.toString();

                coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");

                // seek the track - warning, we do not check if the tracknumber falls in the boundary of the queue
                coordinator.setPositionTrack(trackNumber);

                // take the system off mute
                coordinator.setMute(OnOffType.OFF);

                // start jammin'
                coordinator.play();
            } catch (IllegalStateException e) {
                logger.warn("Cannot play track ({})", e.getMessage());
            }
        }

    }

    public void playPlayList(Command command) {

        if (command != null && command instanceof StringType) {
            String playlist = command.toString();
            List<SonosEntry> playlists = getPlayLists();

            SonosEntry theEntry = null;
            // search for the appropriate play list based on its name (title)
            for (SonosEntry somePlaylist : playlists) {
                if (somePlaylist.getTitle().equals(playlist)) {
                    theEntry = somePlaylist;
                    break;
                }
            }

            // set the URI of the group coordinator
            if (theEntry != null) {
                try {
                    ZonePlayerHandler coordinator = getCoordinatorHandler();

                    coordinator.addURIToQueue(theEntry);

                    coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");

                    String firstTrackNumberEnqueued = stateMap.get("FirstTrackNumberEnqueued");
                    if (firstTrackNumberEnqueued != null) {
                        coordinator.seek("TRACK_NR", firstTrackNumberEnqueued);
                    }

                    coordinator.play();
                } catch (IllegalStateException e) {
                    logger.warn("Cannot play playlist ({})", e.getMessage());
                }
            } else {
                logger.warn("Playlist '{}' not found", playlist);
            }
        }
    }

    public void addURIToQueue(SonosEntry newEntry) {
        addURIToQueue(newEntry.getRes(), SonosXMLParser.compileMetadataString(newEntry), 1, true);
    }

    public String getZoneName() {
        return stateMap.get("ZoneName");
    }

    public String getZoneGroupID() {
        return stateMap.get("LocalGroupUUID");
    }

    public String getRunningAlarmProperties() {
        updateRunningAlarmProperties();
        return stateMap.get("RunningAlarmProperties");
    }

    public String getMute() {
        return stateMap.get("MuteMaster");
    }

    public boolean getLed() {
        return ((stateMap.get("CurrentLEDState") != null) && stateMap.get("CurrentLEDState").equals("On")) ? true
                : false;
    }

    public String getCurrentZoneName() {
        updateCurrentZoneName();
        return stateMap.get("CurrentZoneName");
    }

    public String getCurrentURIFormatted() {
        updateCurrentURIFormatted(getCurrentURI());
        return stateMap.get("CurrentURIFormatted");
    }

    @Override
    public void onStatusChanged(boolean status) {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getModelNameFromDescriptor() {
        URL descriptor = service.getDescriptorURL(this);
        if (descriptor != null) {
            String sonosModelDescription = SonosXMLParser.parseModelDescription(service.getDescriptorURL(this));
            return SonosXMLParser.extractModelName(sonosModelDescription);
        } else {
            return null;
        }
    }

    private boolean migrateThingType() {
        if (getThing().getThingTypeUID().equals(ZONEPLAYER_THING_TYPE_UID)) {
            String modelName = getModelNameFromDescriptor();
            if (isSupportedModel(modelName)) {
                updateSonosThingType(modelName);
                return true;
            }
        }
        return false;
    }

    private boolean isSupportedModel(String modelName) {
        for (ThingTypeUID thingTypeUID : SUPPORTED_KNOWN_THING_TYPES_UIDS) {
            if (thingTypeUID.getId().equalsIgnoreCase(modelName)) {
                return true;
            }
        }
        return false;
    }

    private void updateSonosThingType(String newThingTypeID) {
        changeThingType(new ThingTypeUID(SonosBindingConstants.BINDING_ID, newThingTypeID), getConfig());
    }

    /*
     * Set the sleeptimer duration
     * Use String command of format "HH:MM:SS" to set the timer to the desired duration
     * Use empty String "" to switch the sleep timer off
     */
    public void setSleepTimer(Command command) {
        if (command != null) {
            if (command instanceof DecimalType) {

                Map<String, String> inputs = new HashMap<String, String>();
                inputs.put("InstanceID", "0");
                inputs.put("NewSleepTimerDuration", sleepSecondsToTimeStr(Integer.parseInt(command.toString())));

                this.service.invokeAction(this, "AVTransport", "ConfigureSleepTimer", inputs);
            }
        }
    }

    protected void updateSleepTimerDuration() {
        Map<String, String> result = service.invokeAction(this, "AVTransport", "GetRemainingSleepTimerDuration", null);
        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    private String sleepSecondsToTimeStr(long sleepSeconds) {
        if (sleepSeconds == 0) {
            return "";
        } else if (sleepSeconds < 68400) {
            long hours = TimeUnit.SECONDS.toHours(sleepSeconds);
            sleepSeconds -= TimeUnit.HOURS.toSeconds(hours);
            long minutes = TimeUnit.SECONDS.toMinutes(sleepSeconds);
            sleepSeconds -= TimeUnit.MINUTES.toSeconds(minutes);
            long seconds = TimeUnit.SECONDS.toSeconds(sleepSeconds);
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            logger.error("Sonos SleepTimer: Invalid sleep time set. sleep time must be >=0 and < 68400s (24h)");
            return "ERR";
        }

    }

    private long sleepStrTimeToSeconds(String sleepTime) {
        String[] units = sleepTime.split(":");
        int hours = Integer.parseInt(units[0]);
        int minutes = Integer.parseInt(units[1]);
        int seconds = Integer.parseInt(units[2]);
        return 3600 * hours + 60 * minutes + seconds;
    }
}
