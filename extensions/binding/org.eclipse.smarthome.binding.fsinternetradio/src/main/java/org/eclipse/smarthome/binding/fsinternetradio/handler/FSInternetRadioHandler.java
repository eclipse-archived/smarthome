/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.handler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.smarthome.binding.fsinternetradio.FSInternetRadioBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.fsinternetradio.internal.radio.FrontierSiliconRadio;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FSInternetRadioHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Koenemann - Initial contribution
 * @author Mihaela Memova - removed the unused boolean parameter, changed the check for the PIN
 * @author Svilen Valkanov - changed handler initialization
 */
public class FSInternetRadioHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(FSInternetRadioHandler.class);

    FrontierSiliconRadio radio;

    /** Job that runs {@link #updateRunnable}. */
    private ScheduledFuture<?> updateJob;

    /** Runnable for job {@link #updateJob} for periodic refresh. */
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!radio.isLoggedIn()) {
                // radio is not set, so set all channels to 'undefined'
                for (Channel channel : getThing().getChannels()) {
                    updateState(channel.getUID(), UnDefType.UNDEF);
                }
                // now let's silently check if it's back online
                radioLogin();
                return; // if login is successful, this method is called again :-)
            }
            try {
                final boolean radioOn = radio.getPower();
                for (Channel channel : getThing().getChannels()) {
                    if (!radioOn && !CHANNEL_POWER.equals(channel.getUID().getId())) {
                        // if radio is off, set all channels (except for 'POWER') to 'undefined'
                        updateState(channel.getUID(), UnDefType.UNDEF);
                    } else if (isLinked(channel.getUID().getId())) {
                        // update all channels that are linked
                        switch (channel.getUID().getId()) {
                            case CHANNEL_POWER:
                                updateState(channel.getUID(), radioOn ? OnOffType.ON : OnOffType.OFF);
                                break;
                            case CHANNEL_VOLUME_ABSOLUTE:
                                updateState(channel.getUID(),
                                        DecimalType.valueOf(String.valueOf(radio.getVolumeAbsolute())));
                                break;
                            case CHANNEL_VOLUME_PERCENT:
                                updateState(channel.getUID(),
                                        PercentType.valueOf(String.valueOf(radio.getVolumePercent())));
                                break;
                            case CHANNEL_MODE:
                                updateState(channel.getUID(), DecimalType.valueOf(String.valueOf(radio.getMode())));
                                break;
                            case CHANNEL_MUTE:
                                updateState(channel.getUID(), radio.getMuted() ? OnOffType.ON : OnOffType.OFF);
                                break;
                            case CHANNEL_PRESET:
                                // preset is write-only, ignore
                                break;
                            case CHANNEL_PLAY_INFO_NAME:
                                updateState(channel.getUID(), StringType.valueOf(radio.getPlayInfoName()));
                                break;
                            case CHANNEL_PLAY_INFO_TEXT:
                                updateState(channel.getUID(), StringType.valueOf(radio.getPlayInfoText()));
                                break;
                            default:
                                logger.warn("Ignoring unknown channel during update: {}", channel.getLabel());
                        }
                    }
                }
                updateStatus(ThingStatus.ONLINE); // set it back online, maybe it was offline before
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    };

    public FSInternetRadioHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // read configuration
        final String ip = (String) getThing().getConfiguration().get(CONFIG_PROPERTY_IP);
        final BigDecimal port = (BigDecimal) getThing().getConfiguration().get(CONFIG_PROPERTY_PORT);
        final String pin = (String) getThing().getConfiguration().get(CONFIG_PROPERTY_PIN);

        if (ip == null || StringUtils.isEmpty(pin) || port.intValue() == 0) {
            // configuration incomplete
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration incomplete");
        } else {
            radio = new FrontierSiliconRadio(ip, port.intValue(), pin);
            logger.debug("Initializing connection to {}:{}", ip, port);

            // Long running initialization should be done asynchronously in background
            radioLogin();

            // also schedule a thread for polling with configured refresh rate
            final BigDecimal period = (BigDecimal) getThing().getConfiguration().get(CONFIG_PROPERTY_REFRESH);
            if (period != null && period.intValue() > 0) {
                updateJob = scheduler.scheduleWithFixedDelay(updateRunnable, period.intValue(), period.intValue(),
                        SECONDS);
            }
        }
    }

    private void radioLogin() {
        scheduler.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (radio.login()) {
                        // Thing initialized. If done set status to ONLINE to indicate proper working.
                        updateStatus(ThingStatus.ONLINE);

                        // now update all channels!
                        updateRunnable.run();
                    }
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        });

    }

    @Override
    public void dispose() {
        if (updateJob != null) {
            updateJob.cancel(true);
        }
        updateJob = null;

        if (radio != null) {
            radio.closeClient();
        }
        radio = null;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (!radio.isLoggedIn()) {
            // connection to radio is not initialized, log ignored command and set status, if it is not already offline
            logger.debug("Ignoring command {} = {} because device is offline.", channelUID.getId(), command);
            if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
            return;
        }
        try {
            switch (channelUID.getId()) {
                case CHANNEL_POWER:
                    if (OnOffType.ON.equals(command)) {
                        radio.setPower(true);
                    } else if (OnOffType.OFF.equals(command)) {
                        radio.setPower(false);
                    }
                    // now all items should be updated! (wait some seconds so that text items are up-to-date)
                    scheduler.schedule(updateRunnable, 4, SECONDS);
                    break;
                case CHANNEL_VOLUME_PERCENT:
                    if (IncreaseDecreaseType.INCREASE.equals(command) || UpDownType.UP.equals(command)) {
                        radio.increaseVolumeAbsolute();
                    } else if (IncreaseDecreaseType.DECREASE.equals(command) || UpDownType.DOWN.equals(command)) {
                        radio.decreaseVolumeAbsolute();
                    } else if (command instanceof PercentType) {
                        radio.setVolumePercent(((PercentType) command).intValue());
                    }
                    // absolute value should also be updated now, so let's update all items
                    scheduler.schedule(updateRunnable, 1, SECONDS);
                    break;
                case CHANNEL_VOLUME_ABSOLUTE:
                    if (IncreaseDecreaseType.INCREASE.equals(command) || UpDownType.UP.equals(command)) {
                        radio.increaseVolumeAbsolute();
                    } else if (IncreaseDecreaseType.DECREASE.equals(command) || UpDownType.DOWN.equals(command)) {
                        radio.decreaseVolumeAbsolute();
                    } else if (command instanceof DecimalType) {
                        radio.setVolumeAbsolute(((DecimalType) command).intValue());
                    }
                    // percent value should also be updated now, so let's update all items
                    scheduler.schedule(updateRunnable, 1, SECONDS);
                    break;
                case CHANNEL_MODE:
                    if (command instanceof DecimalType) {
                        radio.setMode(((DecimalType) command).intValue());
                    }
                    break;
                case CHANNEL_PRESET:
                    if (command instanceof DecimalType) {
                        radio.setPreset(((DecimalType) command).intValue());
                    }
                    break;
                case CHANNEL_MUTE:
                    if (command instanceof OnOffType) {
                        radio.setMuted(OnOffType.ON.equals(command));
                    }
                    break;
                default:
                    logger.warn("Ignoring unknown command: {}", command);
            }
            // make sure that device state is online
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            // set device state to offline
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
