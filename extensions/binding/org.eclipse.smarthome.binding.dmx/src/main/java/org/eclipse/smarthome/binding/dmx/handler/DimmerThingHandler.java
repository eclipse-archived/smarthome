/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.binding.dmx.handler;

import static org.eclipse.smarthome.binding.dmx.DmxBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.smarthome.binding.dmx.DmxBindingConstants.ListenerType;
import org.eclipse.smarthome.binding.dmx.internal.DmxBridgeHandler;
import org.eclipse.smarthome.binding.dmx.internal.DmxThingHandler;
import org.eclipse.smarthome.binding.dmx.internal.Util;
import org.eclipse.smarthome.binding.dmx.internal.ValueSet;
import org.eclipse.smarthome.binding.dmx.internal.action.FadeAction;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.BaseChannel;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.Channel;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DimmerThingHandler} is responsible for handling commands, which are
 * sent to the dimmer.
 *
 * @author Jan N. Klug - Initial contribution
 */

public class DimmerThingHandler extends DmxThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DIMMER);

    private final Logger logger = LoggerFactory.getLogger(DimmerThingHandler.class);

    private List<Channel> channels = new ArrayList<Channel>();
    private ValueSet turnOnValue = new ValueSet(0, -1, Channel.MAX_VALUE);
    private ValueSet turnOffValue = new ValueSet(0, -1, Channel.MIN_VALUE);

    private int fadeTime = 0, dimTime = 0;

    private boolean isDimming = false;

    public DimmerThingHandler(Thing dimmerThing) {
        super(dimmerThing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    logger.trace("adding fade to channels in thing {}", this.thing.getUID());
                    int targetValue = Util.toDmxValue((PercentType) command);
                    channels.forEach(channel -> channel
                            .setChannelAction(new FadeAction(fadeTime, channel.getValue(), targetValue, -1)));
                } else if (command instanceof DecimalType) {
                    logger.trace("adding fade to channels in thing {}", this.thing.getUID());
                    int targetValue = ((DecimalType) command).intValue();
                    channels.forEach(channel -> channel
                            .setChannelAction(new FadeAction(fadeTime, channel.getValue(), targetValue, -1)));
                } else if (command instanceof OnOffType) {
                    logger.trace("adding {} fade to channels in thing {}", command, this.thing.getUID());
                    ValueSet targetValueSet = ((OnOffType) command).equals(OnOffType.ON) ? turnOnValue : turnOffValue;
                    IntStream.range(0, channels.size()).forEach(i -> {
                        channels.get(i).setChannelAction(
                                new FadeAction(fadeTime, channels.get(i).getValue(), targetValueSet.getValue(i), -1));
                    });
                } else if (command instanceof IncreaseDecreaseType) {
                    if (isDimming && ((IncreaseDecreaseType) command).equals(IncreaseDecreaseType.INCREASE)) {
                        logger.trace("stopping fade in thing {}", this.thing.getUID());
                        channels.forEach(Channel::clearAction);
                        isDimming = false;
                    } else {
                        logger.trace("starting {} fade in thing {}", command, this.thing.getUID());
                        ValueSet targetValueSet = ((IncreaseDecreaseType) command).equals(IncreaseDecreaseType.INCREASE)
                                ? turnOnValue
                                : turnOffValue;
                        IntStream.range(0, channels.size()).forEach(i -> {
                            channels.get(i).setChannelAction(new FadeAction(dimTime, channels.get(i).getValue(),
                                    targetValueSet.getValue(i), -1));
                        });
                        isDimming = true;
                    }
                } else if (command instanceof RefreshType) {
                    logger.trace("sending update on refresh to channel {}:brightness", this.thing.getUID());
                    updateState(channelUID, Util.toPercentValue(channels.get(0).getValue()));
                } else {
                    logger.debug("command {} not supported in channel {}:brightness", command.getClass(),
                            this.thing.getUID());
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof OnOffType) {
                    logger.trace("adding {} fade to channels in thing {}", command, this.thing.getUID());
                    ValueSet targetValueSet = ((OnOffType) command).equals(OnOffType.ON) ? turnOnValue : turnOffValue;
                    IntStream.range(0, channels.size()).forEach(i -> {
                        channels.get(i).setChannelAction(
                                new FadeAction(fadeTime, channels.get(i).getValue(), targetValueSet.getValue(i), -1));
                    });
                } else if (command instanceof HSBType) {
                    logger.trace("adding color fade to channels in thing {}", this.thing.getUID());
                    HSBType thisCommand = (HSBType) command;
                    ValueSet rgbValue = new ValueSet(fadeTime, -1);
                    rgbValue.addValue(thisCommand.getRed());
                    rgbValue.addValue(thisCommand.getGreen());
                    rgbValue.addValue(thisCommand.getBlue());
                    IntStream.range(0, channels.size()).forEach(i -> {
                        channels.get(i).setChannelAction(new FadeAction(fadeTime, rgbValue.getValue(i), -1));
                    });
                } else if (command instanceof RefreshType) {
                    logger.trace("sending update on refresh to channel {}:color", this.thing.getUID());
                    updateState(channelUID, UnDefType.NULL);
                } else {
                    logger.debug("command {} not supported in channel {}:color", command.getClass(),
                            this.thing.getUID());
                }
                break;
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    logger.trace("adding {} fade to channels in thing {}", command, this.thing.getUID());
                    ValueSet targetValueSet = ((OnOffType) command).equals(OnOffType.ON) ? turnOnValue : turnOffValue;
                    IntStream.range(0, channels.size()).forEach(i -> {
                        channels.get(i).setChannelAction(
                                new FadeAction(fadeTime, channels.get(i).getValue(), targetValueSet.getValue(i), -1));
                    });
                } else if (command instanceof RefreshType) {
                    logger.trace("sending update on refresh to channel {}:switch", this.thing.getUID());
                    updateState(channelUID, channels.get(0).getValue() > 0 ? OnOffType.ON : OnOffType.OFF);
                } else {
                    logger.debug("command {} not supported in channel {}:switch", command.getClass(),
                            this.thing.getUID());
                }
                break;
            default:
                logger.debug("channel {} not supported in thing {}", channelUID.getId(), this.thing.getUID());
        }
    }

    @Override
    public void initialize() {
        logger.debug("thing {} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        if (getBridge() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        }
        if (configuration.get(CONFIG_DMX_ID) != null) {
            DmxBridgeHandler bridgeHandler = (DmxBridgeHandler) getBridge().getHandler();
            try {
                List<BaseChannel> configChannels = BaseChannel.fromString((String) configuration.get(CONFIG_DMX_ID),
                        bridgeHandler.getUniverseId());
                logger.trace("found {} channels in {}", configChannels.size(), this.thing.getUID());
                for (BaseChannel channel : configChannels) {
                    channels.add(bridgeHandler.getDmxChannel(channel, this.thing));
                }
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
                return;
            }
            if (configuration.get(CONFIG_DIMMER_FADE_TIME) != null) {
                fadeTime = ((BigDecimal) configuration.get(CONFIG_DIMMER_FADE_TIME)).intValue();
                logger.debug("setting fadeTime to {} ms in {}", fadeTime, this.thing.getUID());
            }
            if (configuration.get(CONFIG_DIMMER_DIM_TIME) != null) {
                dimTime = ((BigDecimal) configuration.get(CONFIG_DIMMER_DIM_TIME)).intValue();
                logger.trace("setting dimTime to {} ms in {}", fadeTime, this.thing.getUID());
            }
            if (configuration.get(CONFIG_DIMMER_TURNONVALUE) != null) {
                String turnOnValueString = String.valueOf(fadeTime) + ":"
                        + ((String) configuration.get(CONFIG_DIMMER_TURNONVALUE)) + ":-1";
                ValueSet turnOnValue = ValueSet.fromString(turnOnValueString);
                if (turnOnValue != null) {
                    this.turnOnValue = turnOnValue;
                    logger.trace("set turnonvalue to {} in {}", turnOnValue, this.thing.getUID());
                } else {
                    logger.warn("could not set turnonvalue to {} in {}, malformed", turnOnValueString,
                            this.thing.getUID());
                }
            }
            if (configuration.get(CONFIG_DIMMER_TURNOFFVALUE) != null) {
                String turnOffValueString = String.valueOf(fadeTime) + ":"
                        + ((String) configuration.get(CONFIG_DIMMER_TURNOFFVALUE)) + ":-1";
                ValueSet turnOffValue = ValueSet.fromString(turnOffValueString);
                if (turnOffValue != null) {
                    this.turnOffValue = turnOffValue;
                    logger.trace("set turnoffvalue to {} in {}", turnOffValue, this.thing.getUID());
                } else {
                    logger.warn("could not set turnoffvalue to {} in {}, malformed", turnOffValueString,
                            this.thing.getUID());
                }
            }
            // check if we need to update the channel configuration and update again if necessary
            if ((channels.size() % 3 == 0) && (thing.getChannel(CHANNEL_COLOR) == null)) {
                logger.debug("adding color channel to {}", this.thing.getUID());
                ThingBuilder thingBuilder = editThing();
                ChannelUID colorChannelUID = new ChannelUID(this.thing.getUID(), CHANNEL_COLOR);
                thingBuilder.withChannel(
                        ChannelBuilder.create(colorChannelUID, "Color").withType(colorChannelTypeUID).build());
                updateThing(thingBuilder.build());
            } else if ((channels.size() % 3 != 0) && (thing.getChannel(CHANNEL_COLOR) != null)) {
                logger.debug("removing color channel from {}", this.thing.getUID());
                ThingBuilder thingBuilder = editThing();
                ChannelUID colorChannelUID = new ChannelUID(this.thing.getUID(), CHANNEL_COLOR);
                thingBuilder.withoutChannel(colorChannelUID);
                updateThing(thingBuilder.build());
            } else {
                logger.debug("no need to update color channel");
            }
            if (this.getBridge().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
                dmxHandlerStatus = ThingStatusDetail.NONE;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "DMX channel configuration missing");
            dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
        }
    }

    @Override
    public void dispose() {
        if (channels.size() != 0) {
            ((DmxBridgeHandler) getBridge().getHandler()).unregisterDmxChannels(this.thing);
            logger.debug("removing {} channels from {}", channels.size(), this.thing.getUID());
            channels.clear();
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.trace("trying to add listeners for {} (Thing [})", channelUID, this.thing.getUID());
        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
                if (channels.size() > 0) {
                    channels.get(0).addListener(channelUID, this, ListenerType.ONOFF);
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (channels.size() > 0) {
                    channels.get(0).addListener(channelUID, this, ListenerType.VALUE);
                }
                break;
            case CHANNEL_COLOR:
                if (channels.size() > 3) {
                    channels.get(0).addListener(channelUID, this, ListenerType.VALUE);
                    channels.get(1).addListener(channelUID, this, ListenerType.VALUE);
                    channels.get(2).addListener(channelUID, this, ListenerType.VALUE);
                    break;
                }
            default:
                logger.warn("channel {} not supported in thing {}", channelUID.getId(), this.thing.getUID());
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.trace("trying to remove listeners for {} (Thing [})", channelUID, this.thing.getUID());
        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
            case CHANNEL_BRIGHTNESS:
                if (channels.size() > 0) {
                    channels.get(0).removeListener(channelUID);
                }
                break;
            case CHANNEL_COLOR:
                if (channels.size() > 2) {
                    channels.get(0).removeListener(channelUID);
                    channels.get(1).removeListener(channelUID);
                    channels.get(2).removeListener(channelUID);
                }
                break;
            default:
                logger.debug("channel {} not supported in thing {}", channelUID.getId(), this.thing.getUID());
        }
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
            case CHANNEL_BRIGHTNESS:
                logger.trace("received {} for {}", state, channelUID);
                super.updateState(channelUID, state);
                break;
            case CHANNEL_COLOR:
                logger.trace("received RGB update for {}", channelUID.getId());
                HSBType color = HSBType.fromRGB(channels.get(0).getValue(), channels.get(1).getValue(),
                        channels.get(2).getValue());
                super.updateState(channelUID, color);
                break;
            default:
                logger.warn("channel {} not supported in thing {}", channelUID.getId(), this.thing.getUID());
        }
    }

}
