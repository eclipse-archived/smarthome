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
package org.eclipse.smarthome.binding.dmx.handler;

import static org.eclipse.smarthome.binding.dmx.internal.DmxBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.smarthome.binding.dmx.internal.DmxBindingConstants.ListenerType;
import org.eclipse.smarthome.binding.dmx.internal.DmxBridgeHandler;
import org.eclipse.smarthome.binding.dmx.internal.DmxThingHandler;
import org.eclipse.smarthome.binding.dmx.internal.Util;
import org.eclipse.smarthome.binding.dmx.internal.ValueSet;
import org.eclipse.smarthome.binding.dmx.internal.action.FadeAction;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.BaseDmxChannel;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.DmxChannel;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ColorThingHandler} is responsible for handling commands, which are
 * sent to the dimmer.
 *
 * @author Jan N. Klug - Initial contribution
 */

public class ColorThingHandler extends DmxThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_COLOR);

    private final Logger logger = LoggerFactory.getLogger(ColorThingHandler.class);

    private final List<DmxChannel> channels = new ArrayList<DmxChannel>();

    private final List<Integer> currentValues = new ArrayList<Integer>();
    private HSBType currentColor = new HSBType();

    private ValueSet turnOnValue = new ValueSet(0, -1, DmxChannel.MAX_VALUE);
    private ValueSet turnOffValue = new ValueSet(0, -1, DmxChannel.MIN_VALUE);

    private int fadeTime = 0;
    private int dimTime = 0;

    private boolean dynamicTurnOnValue = false;
    private boolean isDimming = false;

    public ColorThingHandler(Thing dimmerThing) {
        super(dimmerThing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("received command {} in channel {}", command, channelUID);
        ValueSet targetValueSet = new ValueSet(fadeTime, -1);
        switch (channelUID.getId()) {
            case CHANNEL_BRIGHTNESS_R:
                if (command instanceof RefreshType) {
                    logger.trace("sending update on refresh to channel {}:brightness_r", this.thing.getUID());
                    currentValues.set(0, channels.get(0).getValue());
                    updateCurrentColor();
                    updateState(channelUID, Util.toPercentValue(currentValues.get(0)));
                    return;
                } else {
                    logger.debug("command {} not supported in channel {}:brightness_r", command.getClass(),
                            this.thing.getUID());
                    return;
                }
            case CHANNEL_BRIGHTNESS_G:
                if (command instanceof RefreshType) {
                    logger.trace("sending update on refresh to channel {}:brightness_g", this.thing.getUID());
                    currentValues.set(1, channels.get(1).getValue());
                    updateCurrentColor();
                    updateState(channelUID, Util.toPercentValue(currentValues.get(1)));
                    return;
                } else {
                    logger.debug("command {} not supported in channel {}:brightness_g", command.getClass(),
                            this.thing.getUID());
                    return;
                }
            case CHANNEL_BRIGHTNESS_B:
                if (command instanceof RefreshType) {
                    logger.trace("sending update on refresh to channel {}:brightness_b", this.thing.getUID());
                    currentValues.set(2, channels.get(2).getValue());
                    updateCurrentColor();
                    updateState(channelUID, Util.toPercentValue(currentValues.get(2)));
                    return;
                } else {
                    logger.debug("command {} not supported in channel {}:brightness_b", command.getClass(),
                            this.thing.getUID());
                    return;
                }
            case CHANNEL_COLOR: {
                if (command instanceof OnOffType) {
                    logger.trace("adding {} fade to channels in thing {}", command, this.thing.getUID());
                    if (((OnOffType) command) == OnOffType.ON) {
                        targetValueSet = turnOnValue;
                    } else {
                        if (dynamicTurnOnValue) {
                            turnOnValue.clear();
                            for (DmxChannel channel : channels) {
                                turnOnValue.addValue(channel.getValue());
                            }
                            logger.trace("stored channel values fort next turn-on");
                        }
                        targetValueSet = turnOffValue;
                    }
                } else if (command instanceof HSBType) {
                    logger.trace("adding color fade to channels in thing {}", this.thing.getUID());
                    targetValueSet.addValue(((HSBType) command).getRed());
                    targetValueSet.addValue(((HSBType) command).getGreen());
                    targetValueSet.addValue(((HSBType) command).getBlue());
                } else if ((command instanceof PercentType) || (command instanceof DecimalType)) {
                    logger.trace("adding brightness fade to channels in thing {}", this.thing.getUID());
                    PercentType brightness = (command instanceof PercentType) ? (PercentType) command
                            : Util.toPercentValue(((DecimalType) command).intValue());
                    HSBType targetColor = new HSBType(currentColor.getHue(), currentColor.getSaturation(), brightness);
                    targetValueSet.addValue(targetColor.getRed());
                    targetValueSet.addValue(targetColor.getGreen());
                    targetValueSet.addValue(targetColor.getBlue());
                } else if (command instanceof IncreaseDecreaseType) {
                    if (isDimming && ((IncreaseDecreaseType) command).equals(IncreaseDecreaseType.INCREASE)) {
                        logger.trace("stopping fade in thing {}", this.thing.getUID());
                        channels.forEach(DmxChannel::clearAction);
                        isDimming = false;
                        return;
                    } else {
                        logger.trace("starting {} fade in thing {}", command, this.thing.getUID());
                        HSBType targetColor;
                        if (((IncreaseDecreaseType) command).equals(IncreaseDecreaseType.INCREASE)) {
                            targetColor = new HSBType(currentColor.getHue(), currentColor.getSaturation(),
                                    PercentType.HUNDRED);
                        } else {
                            targetColor = new HSBType(currentColor.getHue(), currentColor.getSaturation(),
                                    PercentType.ZERO);
                        }
                        targetValueSet.addValue(targetColor.getRed());
                        targetValueSet.addValue(targetColor.getGreen());
                        targetValueSet.addValue(targetColor.getBlue());
                        targetValueSet.setFadeTime(dimTime);
                        isDimming = true;
                    }
                } else if (command instanceof RefreshType) {
                    logger.trace("sending update on refresh to channel {}:color", this.thing.getUID());
                    currentValues.set(0, channels.get(0).getValue());
                    currentValues.set(1, channels.get(1).getValue());
                    currentValues.set(2, channels.get(2).getValue());
                    updateCurrentColor();
                    updateState(channelUID, currentColor);
                    return;
                } else {
                    logger.debug("command {} not supported in channel {}:color", command.getClass(),
                            this.thing.getUID());
                    return;
                }
                break;
            }
            default:
                logger.debug("channel {} not supported in thing {}", channelUID.getId(), this.thing.getUID());
                return;
        }
        final ValueSet valueSet = targetValueSet;
        IntStream.range(0, channels.size()).forEach(i -> {
            channels.get(i).setChannelAction(new FadeAction(valueSet.getFadeTime(), channels.get(i).getValue(),
                    valueSet.getValue(i), valueSet.getHoldTime()));
        });
    }

    @Override
    public void initialize() {
        Configuration configuration = getConfig();
        Bridge bridge = getBridge();
        DmxBridgeHandler bridgeHandler;
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        } else {
            bridgeHandler = (DmxBridgeHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge handler available");
                dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
                return;
            }
        }

        if (!configuration.containsKey(CONFIG_DMX_ID)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "DMX channel configuration missing");
            dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        }
        try {
            List<BaseDmxChannel> configChannels = BaseDmxChannel.fromString((String) configuration.get(CONFIG_DMX_ID),
                    bridgeHandler.getUniverseId());
            logger.trace("found {} channels in {}", configChannels.size(), this.thing.getUID());
            for (BaseDmxChannel channel : configChannels) {
                channels.add(bridgeHandler.getDmxChannel(channel, this.thing));
            }
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        }

        currentValues.add(DmxChannel.MIN_VALUE);
        currentValues.add(DmxChannel.MIN_VALUE);
        currentValues.add(DmxChannel.MIN_VALUE);

        if (configuration.containsKey(CONFIG_DIMMER_FADE_TIME)) {
            fadeTime = ((BigDecimal) configuration.get(CONFIG_DIMMER_FADE_TIME)).intValue();
            logger.debug("setting fadeTime to {} ms in {}", fadeTime, this.thing.getUID());
        }

        if (configuration.containsKey(CONFIG_DIMMER_DIM_TIME)) {
            dimTime = ((BigDecimal) configuration.get(CONFIG_DIMMER_DIM_TIME)).intValue();
            logger.trace("setting dimTime to {} ms in {}", fadeTime, this.thing.getUID());
        }

        if (configuration.containsKey(CONFIG_DIMMER_TURNONVALUE)) {
            String turnOnValueString = String.valueOf(fadeTime) + ":"
                    + ((String) configuration.get(CONFIG_DIMMER_TURNONVALUE)) + ":-1";
            ValueSet turnOnValue = ValueSet.fromString(turnOnValueString);
            if (turnOnValue.size() == 3) {
                this.turnOnValue = turnOnValue;
                logger.trace("set turnonvalue to {} in {}", turnOnValue, this.thing.getUID());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "turn-on value malformed");
                dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
                return;
            }
        }
        this.turnOnValue.setFadeTime(fadeTime);

        if (configuration.containsKey(CONFIG_DIMMER_DYNAMICTURNONVALUE)) {
            dynamicTurnOnValue = (Boolean) configuration.get(CONFIG_DIMMER_DYNAMICTURNONVALUE);
        }

        if (configuration.containsKey(CONFIG_DIMMER_TURNOFFVALUE)) {
            String turnOffValueString = String.valueOf(fadeTime) + ":"
                    + ((String) configuration.get(CONFIG_DIMMER_TURNOFFVALUE)) + ":-1";
            ValueSet turnOffValue = ValueSet.fromString(turnOffValueString);
            if (turnOffValue.size() == 3) {
                this.turnOffValue = turnOffValue;
                logger.trace("set turnoffvalue to {} in {}", turnOffValue, this.thing.getUID());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "turn-off value malformed");
                dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
                return;
            }
        }
        this.turnOffValue.setFadeTime(fadeTime);

        // register feedback listeners
        channels.get(0).addListener(new ChannelUID(this.thing.getUID(), CHANNEL_BRIGHTNESS_R), this,
                ListenerType.VALUE);
        channels.get(1).addListener(new ChannelUID(this.thing.getUID(), CHANNEL_BRIGHTNESS_G), this,
                ListenerType.VALUE);
        channels.get(2).addListener(new ChannelUID(this.thing.getUID(), CHANNEL_BRIGHTNESS_B), this,
                ListenerType.VALUE);

        if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
            dmxHandlerStatus = ThingStatusDetail.NONE;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        if (channels.size() != 0) {
            channels.get(0).removeListener(new ChannelUID(this.thing.getUID(), CHANNEL_BRIGHTNESS_R));
            channels.get(1).removeListener(new ChannelUID(this.thing.getUID(), CHANNEL_BRIGHTNESS_G));
            channels.get(2).removeListener(new ChannelUID(this.thing.getUID(), CHANNEL_BRIGHTNESS_B));
        }
        channels.clear();
        currentValues.clear();
        currentColor = new HSBType();
    }

    @Override
    public void updateChannelValue(ChannelUID channelUID, int value) {
        updateState(channelUID, Util.toPercentValue(value));
        switch (channelUID.getId()) {
            case CHANNEL_BRIGHTNESS_R:
                currentValues.set(0, value);
                break;
            case CHANNEL_BRIGHTNESS_G:
                currentValues.set(1, value);
                break;
            case CHANNEL_BRIGHTNESS_B:
                currentValues.set(2, value);
                break;
            default:
                logger.debug("don't know how to handle {} in RGB type", channelUID.getId());
                return;
        }
        updateCurrentColor();
        updateState(new ChannelUID(this.thing.getUID(), CHANNEL_COLOR), currentColor);
        logger.trace("received update {} in channel {}, result is {}", value, channelUID, currentColor);
    }

    private void updateCurrentColor() {
        currentColor = HSBType.fromRGB(currentValues.get(0), currentValues.get(1), currentValues.get(2));
    }
}
