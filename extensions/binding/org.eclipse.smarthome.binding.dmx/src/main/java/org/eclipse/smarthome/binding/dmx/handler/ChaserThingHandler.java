/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.binding.dmx.handler;

import static org.eclipse.smarthome.binding.dmx.DmxBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.smarthome.binding.dmx.DmxBindingConstants.ListenerType;
import org.eclipse.smarthome.binding.dmx.internal.DmxBridgeHandler;
import org.eclipse.smarthome.binding.dmx.internal.DmxThingHandler;
import org.eclipse.smarthome.binding.dmx.internal.ValueSet;
import org.eclipse.smarthome.binding.dmx.internal.action.FadeAction;
import org.eclipse.smarthome.binding.dmx.internal.action.ResumeAction;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.BaseChannel;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.Channel;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChaserThingHandler} is responsible for handling commands, which are
 * sent to the chaser.
 *
 * @author Jan N. Klug - Initial contribution
 */

public class ChaserThingHandler extends DmxThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_CHASER);

    private static Logger logger = LoggerFactory.getLogger(ChaserThingHandler.class);

    private List<Channel> channels = new ArrayList<Channel>();
    private List<ValueSet> values = new ArrayList<ValueSet>();

    private boolean resumeAfter = false;
    private OnOffType isRunning = OnOffType.OFF;

    public ChaserThingHandler(Thing dimmerThing) {
        super(dimmerThing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    if (((OnOffType) command).equals(OnOffType.ON)) {
                        Integer channelCounter = 0;
                        for (Channel channel : channels) {
                            if (resumeAfter) {
                                channel.suspendAction();
                            } else {
                                channel.clearAction();
                            }
                            for (ValueSet value : values) {
                                channel.addChannelAction(new FadeAction(value.getFadeTime(),
                                        value.getValue(channelCounter), value.getHoldTime()));
                            }
                            if (resumeAfter) {
                                channel.addChannelAction(new ResumeAction());
                            }
                            if (isLinked(channelUID.getId())) {
                                channel.addListener(channelUID, this, ListenerType.ACTION);
                            }
                            channelCounter++;
                        }
                    } else {
                        for (Channel channel : channels) {
                            if (resumeAfter && channel.isSuspended()) {
                                channel.setChannelAction(new ResumeAction());
                            } else {
                                channel.clearAction();
                            }
                        }
                    }
                } else if (command instanceof RefreshType) {
                    updateState(channelUID, isRunning);
                } else {
                    logger.debug("command {} not supported in channel {}:switch", command.getClass(),
                            this.thing.getUID());
                }
                break;
            case CHANNEL_CONTROL:
                if (command instanceof StringType) {
                    Vector<ValueSet> oldValues = new Vector<ValueSet>(values);
                    if (parseChaserConfig(((StringType) command).toString())) {
                        logger.debug("updated chase config in {}", this.thing.getUID());
                    } else {
                        // restore old chase config
                        values = oldValues;
                        logger.debug("could not update chase config in {}, malformed", this.thing.getUID());
                    }
                } else {
                    logger.debug("command {} not supported in channel {}:control", command.getClass(),
                            this.thing.getUID());
                }
                break;
            default:
                logger.debug("Channel {} not supported in thing {}", channelUID.getId(), this.thing.getUID());
        }
    }

    private boolean parseChaserConfig(String configString) {
        values.clear();
        String strippedConfig = configString.replaceAll("(\\s)+", "");
        for (String singleStepString : strippedConfig.split("\\|")) {
            ValueSet value = ValueSet.fromString(singleStepString);
            if (value != null) {
                values.add(value);
                logger.trace("added step value {} to thing {}", value, this.thing.getUID());
            } else {
                logger.debug("could not add step value to thing {}, malformed", singleStepString, this.thing.getUID());
            }
        }
        return (values.size() > 0);
    }

    @Override
    public void initialize() {
        Configuration configuration = getConfig();
        if (getBridge() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            return;
        }
        if (configuration.get(CONFIG_DMX_ID) != null) {
            channels.clear();
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
            if (configuration.get(CONFIG_CHASER_STEPS) != null) {
                if (parseChaserConfig((String) configuration.get(CONFIG_CHASER_STEPS))) {
                    if (this.getBridge().getStatus().equals(ThingStatus.ONLINE)) {
                        updateStatus(ThingStatus.ONLINE);
                        dmxHandlerStatus = ThingStatusDetail.NONE;
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Chase configuration malformed");
                    dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Chase configuration missing");
                dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            }
            if (configuration.get(CONFIG_CHASER_RESUME_AFTER) != null) {
                resumeAfter = (Boolean) configuration.get(CONFIG_CHASER_RESUME_AFTER);
                logger.trace("set resumeAfter to {}", resumeAfter);
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
    public void channelUnlinked(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
                for (Channel channel : channels) {
                    channel.removeListener(channelUID);
                }
                break;
            case CHANNEL_CONTROL:
                break;
            default:
                logger.debug("channel {} not supported in thing {}", channelUID.getId(), this.thing.getUID());
        }
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        if (channelUID.getId().equals(CHANNEL_SWITCH) && (state instanceof OnOffType)) {
            this.isRunning = (OnOffType) state;
            super.updateState(channelUID, state);
        } else {
            logger.debug("unknown state received: {} in channel {} thing {}", state, channelUID, this.thing.getUID());
        }
    }

}
