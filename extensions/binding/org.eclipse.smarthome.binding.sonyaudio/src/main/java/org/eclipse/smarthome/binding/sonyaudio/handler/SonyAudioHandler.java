/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.sonyaudio.handler;

import static org.eclipse.smarthome.binding.sonyaudio.SonyAudioBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.sonyaudio.internal.SonyAudioEventListener;
import org.eclipse.smarthome.binding.sonyaudio.internal.protocol.SonyAudioConnection;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyAudioHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Åberg - Initial contribution
 */
public class SonyAudioHandler extends BaseThingHandler implements SonyAudioEventListener {

    private final Logger logger = LoggerFactory.getLogger(SonyAudioHandler.class);

    private SonyAudioConnection connection;

    private ScheduledFuture<?> connectionCheckerFuture;
    private ScheduledFuture<?> refreshJob;

    private int currentRadioStation = 0;
    private String input_zone_1 = "";
    private String input_zone_2 = "";

    public SonyAudioHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connection == null) {
            logger.debug("Thing not yet initialized!");
            return;
        }

        String id = channelUID.getId();

        logger.debug("Handle command {} {}", channelUID, command);

        try {
            switch (id) {
                case CHANNEL_POWER:
                case CHANNEL_MASTER_POWER:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getPower() ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setPower(((OnOffType) command) == OnOffType.ON);
                    }
                    break;
                case CHANNEL_ZONE1_POWER:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getPower(1) ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setPower(((OnOffType) command) == OnOffType.ON, 1);
                    }
                    break;
                case CHANNEL_ZONE2_POWER:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getPower(2) ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setPower(((OnOffType) command) == OnOffType.ON, 2);
                    }
                    break;
                case CHANNEL_ZONE3_POWER:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getPower(3) ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setPower(((OnOffType) command) == OnOffType.ON, 3);
                    }
                    break;
                case CHANNEL_ZONE4_POWER:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getPower(4) ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setPower(((OnOffType) command) == OnOffType.ON, 4);
                    }
                    break;
                case CHANNEL_INPUT:
                    if (command instanceof RefreshType) {

                        updateState(channelUID, new StringType(connection.getInput()));
                    }
                    if (command instanceof StringType) {
                        connection.setInput(((StringType) command).toString());
                    }
                    break;
                case CHANNEL_ZONE1_INPUT:
                    if (command instanceof RefreshType) {
                        input_zone_1 = connection.getInput(1);
                        updateState(channelUID, new StringType(input_zone_1));
                    }
                    if (command instanceof StringType) {
                        connection.setInput(((StringType) command).toString(), 1);
                    }
                    break;
                case CHANNEL_ZONE2_INPUT:
                    if (command instanceof RefreshType) {
                        input_zone_2 = connection.getInput(2);
                        updateState(channelUID, new StringType(input_zone_2));
                    }
                    if (command instanceof StringType) {
                        connection.setInput(((StringType) command).toString(), 2);
                    }
                    break;
                case CHANNEL_ZONE3_INPUT:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new StringType(connection.getInput(3)));
                    }
                    if (command instanceof StringType) {
                        connection.setInput(((StringType) command).toString(), 3);
                    }
                    break;
                case CHANNEL_ZONE4_INPUT:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new StringType(connection.getInput(4)));
                    }
                    if (command instanceof StringType) {
                        connection.setInput(((StringType) command).toString(), 4);
                    }
                    break;
                case CHANNEL_VOLUME:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new DecimalType(connection.getVolume() / 100.0));
                    }
                    if (command instanceof DecimalType) {
                        connection.setVolume(((DecimalType) command).intValue());
                    }
                    break;
                case CHANNEL_ZONE1_VOLUME:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new DecimalType(connection.getVolume(1) / 100.0));
                    }
                    if (command instanceof DecimalType) {
                        connection.setVolume(((DecimalType) command).intValue(), 1);
                    }
                    break;
                case CHANNEL_ZONE2_VOLUME:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new DecimalType(connection.getVolume(2) / 100.0));
                    }
                    if (command instanceof DecimalType) {
                        connection.setVolume(((DecimalType) command).intValue(), 2);
                    }
                    break;
                case CHANNEL_ZONE3_VOLUME:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new DecimalType(connection.getVolume(3) / 100.0));
                    }
                    if (command instanceof DecimalType) {
                        connection.setVolume(((DecimalType) command).intValue(), 3);
                    }
                    break;
                case CHANNEL_ZONE4_VOLUME:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new DecimalType(connection.getVolume(4) / 100.0));
                    }
                    if (command instanceof DecimalType) {
                        connection.setVolume(((DecimalType) command).intValue(), 4);
                    }
                    break;
                case CHANNEL_MUTE:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getMute() ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setMute(((OnOffType) command) == OnOffType.ON);
                    }
                    break;
                case CHANNEL_ZONE1_MUTE:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getMute(1) ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setMute(((OnOffType) command) == OnOffType.ON, 1);
                    }
                    break;
                case CHANNEL_ZONE2_MUTE:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getMute(2) ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setMute(((OnOffType) command) == OnOffType.ON, 2);
                    }
                    break;
                case CHANNEL_ZONE3_MUTE:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getMute(3) ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setMute(((OnOffType) command) == OnOffType.ON, 3);
                    }
                    break;
                case CHANNEL_ZONE4_MUTE:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getMute(4) ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setMute(((OnOffType) command) == OnOffType.ON, 4);
                    }
                    break;
                case CHANNEL_MASTER_SOUND_FIELD:
                case CHANNEL_SOUND_FIELD:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new StringType(connection.getSoundField()));
                    }
                    if (command instanceof StringType) {
                        connection.setSoundField(((StringType) command).toString());
                    }
                    break;
                case CHANNEL_MASTER_PURE_DIRECT:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getPureDirect() ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setPureDirect(((OnOffType) command) == OnOffType.ON);
                    }
                    break;
                case CHANNEL_CLEAR_AUDIO:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, connection.getClearAudio() ? OnOffType.ON : OnOffType.OFF);
                    }
                    if (command instanceof OnOffType) {
                        connection.setClearAudio(((OnOffType) command) == OnOffType.ON);
                    }
                    break;
                case CHANNEL_RADIO_FREQ:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new DecimalType(connection.getRadioFreq() / 1000000.0));
                    }
                    break;
                case CHANNEL_RADIO_STATION:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new DecimalType(currentRadioStation));
                    }
                    if (command instanceof DecimalType) {
                        currentRadioStation = ((DecimalType) command).intValue();

                        if (input_zone_1.startsWith("radio:fm")) {
                            connection.setInput("radio:fm?contentId=" + currentRadioStation, 1);
                        } else if (input_zone_2.startsWith("radio:fm")) {
                            connection.setInput("radio:fm?contentId=" + currentRadioStation, 2);
                        }
                    }
                    break;
                case CHANNEL_RADIO_SEEK_STATION:
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new StringType(""));
                    }
                    if (command instanceof StringType) {
                        switch (((StringType) command).toString()) {
                            case "fwdSeeking":
                                connection.radioSeekFwd();
                                break;
                            case "bwdSeeking":
                                connection.radioSeekBwd();
                                break;
                        }

                    }
                    break;
                default:
                    logger.error("Channel {} not supported!", id);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        String ipAddress = (String) config.get(HOST_PARAMETER);
        String path = (String) config.get(SCALAR_PATH_PARAMETER);
        Object port_o = config.get(SCALAR_PORT_PARAMETER);
        int port;
        if (port_o instanceof BigDecimal) {
            port = ((BigDecimal) port_o).intValue();
        } else {
            port = (int) port_o;
        }

        Object refresh_o = config.get(REFRESHINTERVAL);
        int refresh = 0;
        if (refresh_o instanceof BigDecimal) {
            refresh = ((BigDecimal) refresh_o).intValue();
        } else if (refresh_o instanceof Integer) {
            refresh = (int) refresh_o;
        }

        try {
            connection = new SonyAudioConnection(ipAddress, port, path, this);

            connection.connect(scheduler);

            // Start the connection checker
            Runnable connectionChecker = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!connection.checkConnection()) {
                            updateStatus(ThingStatus.OFFLINE);
                        }
                    } catch (Exception ex) {
                        logger.warn("Exception in check connection to @{}. Cause: {}", connection.getConnectionName(),
                                ex.getMessage());

                    }
                }
            };
            connectionCheckerFuture = scheduler.scheduleWithFixedDelay(connectionChecker, 1, 10, TimeUnit.SECONDS);

            // Start the status updater
            startAutomaticRefresh(refresh);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (connectionCheckerFuture != null) {
            connectionCheckerFuture.cancel(true);
        }
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void updateConnectionState(boolean connected) {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void updateInputSource(int zone, String source) {
        switch (zone) {
            case 0:
                updateState(CHANNEL_INPUT, new StringType(source));
                break;
            case 1:
                updateState(CHANNEL_ZONE1_INPUT, new StringType(source));
                break;
            case 2:
                updateState(CHANNEL_ZONE2_INPUT, new StringType(source));
                break;
            case 3:
                updateState(CHANNEL_ZONE3_INPUT, new StringType(source));
                break;
            case 4:
                updateState(CHANNEL_ZONE4_INPUT, new StringType(source));
                break;
        }
    }

    @Override
    public void updateBroadcastFreq(int freq) {
        updateState(CHANNEL_RADIO_FREQ, new DecimalType(freq / 1000000.0));
    }

    @Override
    public void updateCurrentRadioStation(int radioStation) {
        currentRadioStation = radioStation;
        updateState(CHANNEL_RADIO_STATION, new DecimalType(currentRadioStation));
    }

    @Override
    public void updateSeekStation(String seek) {
        updateState(CHANNEL_RADIO_SEEK_STATION, new StringType(seek));
    }

    @Override
    public void updateVolume(int zone, int volume) {
        switch (zone) {
            case 0:
                updateState(CHANNEL_VOLUME, new DecimalType(volume / 100.0));
                break;
            case 1:
                updateState(CHANNEL_ZONE1_VOLUME, new DecimalType(volume / 100.0));
                break;
            case 2:
                updateState(CHANNEL_ZONE2_VOLUME, new DecimalType(volume / 100.0));
                break;
            case 3:
                updateState(CHANNEL_ZONE3_VOLUME, new DecimalType(volume / 100.0));
                break;
            case 4:
                updateState(CHANNEL_ZONE4_VOLUME, new DecimalType(volume / 100.0));
                break;
        }
    }

    @Override
    public void updateMute(int zone, boolean mute) {
        switch (zone) {
            case 0:
                updateState(CHANNEL_MUTE, mute ? OnOffType.ON : OnOffType.OFF);
                break;
            case 1:
                updateState(CHANNEL_ZONE1_MUTE, mute ? OnOffType.ON : OnOffType.OFF);
                break;
            case 2:
                updateState(CHANNEL_ZONE2_MUTE, mute ? OnOffType.ON : OnOffType.OFF);
                break;
            case 3:
                updateState(CHANNEL_ZONE3_MUTE, mute ? OnOffType.ON : OnOffType.OFF);
                break;
            case 4:
                updateState(CHANNEL_ZONE4_MUTE, mute ? OnOffType.ON : OnOffType.OFF);
                break;
        }
    }

    @Override
    public void updatePowerStatus(int zone, boolean power) {
        switch (zone) {
            case 0:
                updateState(CHANNEL_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
            case 1:
                updateState(CHANNEL_ZONE1_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
            case 2:
                updateState(CHANNEL_ZONE2_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
            case 3:
                updateState(CHANNEL_ZONE3_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
            case 4:
                updateState(CHANNEL_ZONE4_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
        }
    }

    private void startAutomaticRefresh(int refresh) {
        if (refresh <= 0) {
            return;
        }

        refreshJob = scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Channel> channels = getThing().getChannels();
                for (Channel channel : channels) {
                    handleCommand(channel.getUID(), RefreshType.REFRESH);
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 5, refresh, TimeUnit.SECONDS);
    }
}
