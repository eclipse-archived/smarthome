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
package org.eclipse.smarthome.binding.bosesoundtouch.handler;

import static org.eclipse.smarthome.binding.bosesoundtouch.BoseSoundTouchBindingConstants.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketFrameListener;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.api.extensions.Frame.Type;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.binding.bosesoundtouch.BoseSoundTouchConfiguration;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.APIRequest;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.BoseSoundTouchNotificationChannelConfiguration;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.BoseStateDescriptionOptionProvider;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.CommandExecutor;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.OperationModeType;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.PresetContainer;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.RemoteKeyType;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.XMLResponseProcessor;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoseSoundTouchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 * @author Kai Kreuzer - code clean up
 * @author Alexander Kostadinov - Handling of websocket ping-pong mechanism for thing status check
 */
public class BoseSoundTouchHandler extends BaseThingHandler implements WebSocketListener, WebSocketFrameListener {

    private static final int MAX_MISSED_PONGS_COUNT = 2;

    private static final int RETRY_INTERVAL_IN_SECS = 30;

    private final Logger logger = LoggerFactory.getLogger(BoseSoundTouchHandler.class);

    private ScheduledFuture<?> connectionChecker;
    private WebSocketClient client;
    private volatile Session session;
    private volatile CommandExecutor commandExecutor;
    private volatile int missedPongsCount = 0;

    private XMLResponseProcessor xmlResponseProcessor;

    private PresetContainer presetContainer;
    private BoseStateDescriptionOptionProvider stateOptionProvider;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     * @param presetContainer the preset container instance to use for managing presets
     *
     * @throws IllegalArgumentException if thing or factory argument is null
     */
    public BoseSoundTouchHandler(Thing thing, PresetContainer presetContainer,
            BoseStateDescriptionOptionProvider stateOptionProvider) {
        super(thing);
        this.presetContainer = presetContainer;
        this.stateOptionProvider = stateOptionProvider;
        xmlResponseProcessor = new XMLResponseProcessor(this);
    }

    @Override
    public void initialize() {
        connectionChecker = scheduler.scheduleWithFixedDelay(() -> checkConnection(), 0, RETRY_INTERVAL_IN_SECS,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (connectionChecker != null && !connectionChecker.isCancelled()) {
            connectionChecker.cancel(true);
            connectionChecker = null;
        }
        closeConnection();
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        presetContainer.clear();
        super.handleRemoval();
    }

    @Override
    public void updateState(String channelID, State state) {
        // don't update channel if it's not linked (in case of Stereo Pair slave device)
        if (isLinked(channelID)) {
            super.updateState(channelID, state);
        } else {
            logger.debug("{}: Skipping state update because of not linked channel '{}'", getDeviceName(), channelID);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (commandExecutor == null) {
            logger.debug("{}: Can't handle command '{}' for channel '{}' because of not initialized connection.", getDeviceName(), command, channelUID);
            return;
        } else {
            logger.debug("{}: handleCommand({}, {});", getDeviceName(), channelUID, command);
        }

        if (command.equals(RefreshType.REFRESH)) {
            switch (channelUID.getIdWithoutGroup()) {
                case CHANNEL_BASS:
                    commandExecutor.getInformations(APIRequest.BASS);
                    break;
                case CHANNEL_KEY_CODE:
                    // refresh makes no sense... ?
                    break;
                case CHANNEL_NOWPLAYING_ALBUM:
                case CHANNEL_NOWPLAYING_ARTIST:
                case CHANNEL_NOWPLAYING_ARTWORK:
                case CHANNEL_NOWPLAYING_DESCRIPTION:
                case CHANNEL_NOWPLAYING_GENRE:
                case CHANNEL_NOWPLAYING_ITEMNAME:
                case CHANNEL_NOWPLAYING_STATIONLOCATION:
                case CHANNEL_NOWPLAYING_STATIONNAME:
                case CHANNEL_NOWPLAYING_TRACK:
                case CHANNEL_RATEENABLED:
                case CHANNEL_SKIPENABLED:
                case CHANNEL_SKIPPREVIOUSENABLED:
                    commandExecutor.getInformations(APIRequest.NOW_PLAYING);
                    break;
                case CHANNEL_VOLUME:
                    commandExecutor.getInformations(APIRequest.VOLUME);
                    break;
                default:
                    logger.debug("{} : Got command '{}' for channel '{}' which is unhandled!", getDeviceName(), command,
                            channelUID.getId());
            }
            return;
        }
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    commandExecutor.postPower((OnOffType) command);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_VOLUME:
                if (command instanceof PercentType) {
                    commandExecutor.postVolume((PercentType) command);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_MUTE:
                if (command instanceof OnOffType) {
                    commandExecutor.postVolumeMuted((OnOffType) command);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_OPERATIONMODE:
                if (command instanceof StringType) {
                    String cmd = command.toString().toUpperCase().trim();
                    try {
                        OperationModeType mode = OperationModeType.valueOf(cmd);
                        commandExecutor.postOperationMode(mode);
                    } catch (IllegalArgumentException iae) {
                        logger.warn("{}: OperationMode \"{}\" is not valid!", getDeviceName(), cmd);
                    }
                }
                break;
            case CHANNEL_PLAYER_CONTROL:
                if ((command instanceof PlayPauseType) || (command instanceof NextPreviousType)) {
                    commandExecutor.postPlayerControl(command);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_PRESET:
                if (command instanceof DecimalType) {
                    commandExecutor.postPreset((DecimalType) command);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_BASS:
                if (command instanceof DecimalType) {
                    commandExecutor.postBass((DecimalType) command);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_SAVE_AS_PRESET:
                if (command instanceof DecimalType) {
                    commandExecutor.addCurrentContentItemToPresetContainer((DecimalType) command);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_KEY_CODE:
                if (command instanceof StringType) {
                    String cmd = command.toString().toUpperCase().trim();
                    try {
                        RemoteKeyType keyCommand = RemoteKeyType.valueOf(cmd);
                        commandExecutor.postRemoteKey(keyCommand);
                    } catch (IllegalArgumentException e) {
                        logger.debug("{}: Unhandled remote key: {}", getDeviceName(), cmd);
                    }
                }
                break;
            default:
                Channel channel = getThing().getChannel(channelUID.getId());
                if (channel != null) {
                    ChannelTypeUID chTypeUid = channel.getChannelTypeUID();
                    if (chTypeUid != null) {
                        switch (channel.getChannelTypeUID().getId()) {
                            case CHANNEL_NOTIFICATION_SOUND:
                                String appKey = Objects.toString(getConfig().get(BoseSoundTouchConfiguration.APP_KEY), null);
                                if (appKey != null && !appKey.isEmpty()) {
                                    if (command instanceof StringType) {
                                        String url = command.toString();
                                        BoseSoundTouchNotificationChannelConfiguration notificationConfiguration = channel
                                                .getConfiguration().as(BoseSoundTouchNotificationChannelConfiguration.class);
                                        if (!url.isEmpty()) {
                                            commandExecutor.playNotificationSound(appKey, notificationConfiguration, url);
                                        }
                                    }
                                } else {
                                    logger.warn("Missing app key - cannot use notification api");
                                }
                                return;
                        }
                    }
                }
                logger.warn("{} : Got command '{}' for channel '{}' which is unhandled!", getDeviceName(), command,
                        channelUID.getId());
                break;
        }

    }

    /**
     * Returns the CommandExecutor of this handler
     *
     * @return the CommandExecutor of this handler
     */
    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    /**
     * Returns the Session this handler has opened
     *
     * @return the Session this handler has opened
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns the name of the device delivered from itself
     *
     * @return the name of the device delivered from itself
     */
    public String getDeviceName() {
        return getThing().getProperties().get(DEVICE_INFO_NAME);
    }

    /**
     * Returns the type of the device delivered from itself
     *
     * @return the type of the device delivered from itself
     */
    public String getDeviceType() {
        return getThing().getProperties().get(DEVICE_INFO_TYPE);
    }

    /**
     * Returns the MAC Address of this device
     *
     * @return the MAC Address of this device (in format "123456789ABC")
     */
    public String getMacAddress() {
        return ((String) getThing().getConfiguration().get(BoseSoundTouchConfiguration.MAC_ADDRESS)).replaceAll(":", "");
    }

    /**
     * Returns the IP Address of this device
     *
     * @return the IP Address of this device
     */
    public String getIPAddress() {
        return (String) getThing().getConfiguration().getProperties().get(BoseSoundTouchConfiguration.HOST);
    }

    /**
     * Provides the handler internal scheduler instance
     *
     * @return the {@link ScheduledExecutorService} instance used by this handler
     */
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public PresetContainer getPresetContainer() {
        return this.presetContainer;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.debug("{}: onWebSocketConnect('{}')", getDeviceName(), session);
        this.session = session;
        commandExecutor = new CommandExecutor(this);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onWebSocketError(Throwable e) {
        logger.debug("{}: Error during websocket communication: {}", getDeviceName(), e.getMessage(), e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        if (commandExecutor != null) {
            commandExecutor.postOperationMode(OperationModeType.OFFLINE);
            commandExecutor = null;
        }
        if (session != null) {
            session.close(StatusCode.SERVER_ERROR, getDeviceName() + ": Failure: " + e.getMessage());
            session = null;
        }
    }

    @Override
    public void onWebSocketText(String msg) {
        logger.debug("{}: onWebSocketText('{}')", getDeviceName(), msg);
        try {
            xmlResponseProcessor.handleMessage(msg);
        } catch (Exception e) {
            logger.warn("{}: Could not parse XML from string '{}'.", getDeviceName(), msg, e);
        }
    }

    @Override
    public void onWebSocketBinary(byte[] arr, int pos, int len) {
        // we don't expect binary data so just dump if we get some...
        logger.debug("{}: onWebSocketBinary({}, {}, '{}')", getDeviceName(), pos, len, Arrays.toString(arr));
    }

    @Override
    public void onWebSocketClose(int code, String reason) {
        logger.debug("{}: onClose({}, '{}')", getDeviceName(), code, reason);
        missedPongsCount = 0;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        if (commandExecutor != null) {
            commandExecutor.postOperationMode(OperationModeType.OFFLINE);
        }
    }

    @Override
    public void onWebSocketFrame(Frame frame) {
        if (frame.getType() == Type.PONG) {
            missedPongsCount = 0;
        }
    }

    private synchronized void openConnection() {
        closeConnection();
        try {
            client = new WebSocketClient();
            // we need longer timeouts for web socket.
            client.setMaxIdleTimeout(360 * 1000);
            // Port seems to be hard coded, therefore no user input or discovery is necessary
            String wsUrl = "ws://" + getIPAddress() + ":8080/";
            logger.debug("{}: Connecting to: {}", getDeviceName(), wsUrl);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setSubProtocols("gabbo");
            client.setStopTimeout(1000);
            client.start();
            client.connect(this, new URI(wsUrl), request);
        } catch (Exception e) {
            onWebSocketError(e);
        }
    }

    private synchronized void closeConnection() {
        if (session != null) {
            try {
                session.close(StatusCode.NORMAL, "Binding shutdown");
            } catch (Exception e) {
                logger.debug("{}: Error while closing websocket communication: {} ({})", getDeviceName(),
                        e.getClass().getName(), e.getMessage());
            }
            session = null;
        }
        if (client != null) {
            try {
                client.stop();
                client.destroy();
            } catch (Exception e) {
                logger.debug("{}: Error while closing websocket communication: {} ({})", getDeviceName(),
                        e.getClass().getName(), e.getMessage());
            }
            client = null;
        }

        commandExecutor = null;
    }

    private void checkConnection() {
        if (getThing().getStatus() != ThingStatus.ONLINE || session == null || client == null
                || commandExecutor == null) {
            openConnection(); // try to reconnect....
        }

        if (getThing().getStatus() == ThingStatus.ONLINE && this.session != null && this.session.isOpen()) {
            try {
                this.session.getRemote().sendPing(null);
                missedPongsCount++;
            } catch (IOException | NullPointerException e) {
                onWebSocketError(e);
                closeConnection();
                openConnection();
            }

            if (missedPongsCount >= MAX_MISSED_PONGS_COUNT) {
                logger.debug("{}: Closing connection because of too many missed PONGs: {} (max allowed {}) ",
                        getDeviceName(), missedPongsCount, MAX_MISSED_PONGS_COUNT);
                missedPongsCount = 0;
                closeConnection();
                openConnection();
            }
        }
    }

    public void refreshPresetChannel() {
        List<StateOption> stateOptions = presetContainer.getAllPresets().stream()
                .map(e -> e.toStateOption())
                .sorted(Comparator.comparing(StateOption::getValue)).collect(Collectors.toList());
        stateOptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_PRESET), stateOptions);
    }

    public void handleGroupUpdated(BoseSoundTouchConfiguration masterPlayerConfiguration) {
        String deviceId = getMacAddress();
        
        if (masterPlayerConfiguration != null && masterPlayerConfiguration.macAddress != null) {
            // Stereo pair
            if (Objects.equals(masterPlayerConfiguration.macAddress, deviceId)) {
                if (getThing().getThingTypeUID().equals(BST_10_THING_TYPE_UID)) {
                    logger.debug("{}: Stereo Pair was created and this is the master device.", getDeviceName());
                } else {
                    logger.debug("{}: Unsupported operation for player of type: {}", getDeviceName(),
                            getThing().getThingTypeUID());
                }
            } else {
                if (getThing().getThingTypeUID().equals(BST_10_THING_TYPE_UID)) {
                    logger.debug("{}: Stereo Pair was created and this is NOT the master device.", getDeviceName());
                    updateThing(editThing().withChannels(Collections.emptyList()).build());
                } else {
                    logger.debug("{}: Unsupported operation for player of type: {}", getDeviceName(),
                            getThing().getThingTypeUID());
                }
            }
        } else {
            // NO Stereo Pair
            if (getThing().getThingTypeUID().equals(BST_10_THING_TYPE_UID)) {
                if (getThing().getChannels().isEmpty()) {
                    logger.debug("{}: Stereo Pair was disbounded. Restoring channels", getDeviceName());
                    updateThing(editThing().withChannels(getAllChannels()).build());
                } else {
                    logger.debug("{}: Stereo Pair was disbounded.", getDeviceName());
                }
            } else {
                logger.debug("{}: Unsupported operation for player of type: {}", getDeviceName(),
                        getThing().getThingTypeUID());
            }
        }
    }
    
    private List<Channel> getAllChannels() {
        List<Channel> allChannels = new ArrayList<>();
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_POWER), new ChannelTypeUID(BINDING_ID, CHANNEL_POWER)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_VOLUME), new ChannelTypeUID(BINDING_ID, CHANNEL_VOLUME)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_MUTE), new ChannelTypeUID(BINDING_ID, CHANNEL_MUTE)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_OPERATIONMODE), new ChannelTypeUID(BINDING_ID, "operationMode_BST_10_20_30")).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_CONTROL), new ChannelTypeUID(BINDING_ID, CHANNEL_PLAYER_CONTROL)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_PRESET), new ChannelTypeUID(BINDING_ID, CHANNEL_PRESET)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_BASS), new ChannelTypeUID(BINDING_ID, CHANNEL_BASS)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_RATEENABLED), new ChannelTypeUID(BINDING_ID, CHANNEL_RATEENABLED)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_SKIPENABLED), new ChannelTypeUID(BINDING_ID, CHANNEL_SKIPENABLED)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_SKIPPREVIOUSENABLED), new ChannelTypeUID(BINDING_ID, CHANNEL_SKIPPREVIOUSENABLED)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_SAVE_AS_PRESET), new ChannelTypeUID(BINDING_ID, CHANNEL_SAVE_AS_PRESET)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_KEY_CODE), new ChannelTypeUID(BINDING_ID, CHANNEL_KEY_CODE)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOWPLAYING_ALBUM), new ChannelTypeUID(BINDING_ID, CHANNEL_NOWPLAYING_ALBUM)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOWPLAYING_ARTWORK), new ChannelTypeUID(BINDING_ID, CHANNEL_NOWPLAYING_ARTWORK)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOWPLAYING_ARTIST), new ChannelTypeUID(BINDING_ID, CHANNEL_NOWPLAYING_ARTIST)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOWPLAYING_DESCRIPTION), new ChannelTypeUID(BINDING_ID, CHANNEL_NOWPLAYING_DESCRIPTION)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOWPLAYING_GENRE), new ChannelTypeUID(BINDING_ID, CHANNEL_NOWPLAYING_GENRE)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOWPLAYING_ITEMNAME), new ChannelTypeUID(BINDING_ID, CHANNEL_NOWPLAYING_ITEMNAME)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOWPLAYING_STATIONLOCATION), new ChannelTypeUID(BINDING_ID, CHANNEL_NOWPLAYING_STATIONLOCATION)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOWPLAYING_STATIONNAME), new ChannelTypeUID(BINDING_ID, CHANNEL_NOWPLAYING_STATIONNAME)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOWPLAYING_TRACK), new ChannelTypeUID(BINDING_ID, CHANNEL_NOWPLAYING_TRACK)).build());
        allChannels.add(getCallback().createChannelBuilder(new ChannelUID(getThing().getUID(), CHANNEL_NOTIFICATION_SOUND), new ChannelTypeUID(BINDING_ID, CHANNEL_NOTIFICATION_SOUND)).build());
        
        return allChannels;
    }
}
