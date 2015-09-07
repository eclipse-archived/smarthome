/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.enocean.handler;

import static org.eclipse.smarthome.binding.enocean.EnOceanBindingConstants.*;

import org.eclipse.smarthome.binding.enocean.rpc.CustomEnoceanRPC;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.osgi.service.enocean.EnOceanDevice;
import org.osgi.service.enocean.EnOceanEvent;
import org.osgi.service.enocean.EnOceanMessage;
import org.osgi.service.enocean.EnOceanRPC;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnOceanHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 */
public class EnOceanHandler extends BaseThingHandler implements EventHandler {

    private Logger logger = LoggerFactory.getLogger(EnOceanHandler.class);
    private EnOceanDevice device;

    public EnOceanHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing EnOcean handler.");
        super.initialize();

        // Configuration config = getThing().getConfiguration();
        // location = (String) config.get("location");
    }

    @Override
    public void dispose() {
        // refreshJob.cancel(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            //
            boolean success = true;
            if (success) {
                switch (channelUID.getId()) {
                    case CHANNEL_ELTAKO_SMOKE_DETECTOR:
                        // should not happen
                        break;
                    case CHANNEL_ON_OFF:
                        EnOceanRPC rpc = null;
                        if (command.equals(OnOffType.ON)) {
                            rpc = new CustomEnoceanRPC(CustomEnoceanRPC.ON_FRAME);
                        } else if (command.equals(OnOffType.OFF)) {

                            rpc = new CustomEnoceanRPC(CustomEnoceanRPC.OFF_FRAME);
                        }
                        org.osgi.service.enocean.EnOceanHandler handlerTurnOnRpc = new org.osgi.service.enocean.EnOceanHandler() {
                            @Override
                            public void notifyResponse(EnOceanRPC enOceanRPC, byte[] payload) {
                                System.out.println("enOceanRPC: " + enOceanRPC + ", payload: " + payload);
                            }
                        };
                        device.invoke(rpc, handlerTurnOnRpc);
                        break;
                    default:
                        logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                        break;
                }
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        if (topic.equals(EnOceanEvent.TOPIC_MSG_RECEIVED)) {
            String chipId = (String) event.getProperty(EnOceanDevice.CHIP_ID);

            if (device.getChipId() == Integer.valueOf(chipId)) {
                if (thing.getThingTypeUID().equals(THING_TYPE_ELTAKO_SMOKE_DETECTOR)) {
                    EnOceanMessage data = (EnOceanMessage) event.getProperty(EnOceanEvent.PROPERTY_MESSAGE);

                    byte[] payload = data.getBytes();
                    if (payload[1] == 0x10) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ELTAKO_SMOKE_DETECTOR), OnOffType.ON);

                    } else if (payload[1] == 0x00) {
                        System.out.println("normal situation");
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ELTAKO_SMOKE_DETECTOR), OnOffType.OFF);

                    }
                } else if (thing.getThingTypeUID().equals(THING_TYPE_ON_OFF_PLUG)) {

                }

            }
        }
    }

    public EnOceanDevice getDevice() {
        return device;
    }

    public void setDevice(EnOceanDevice device) {
        this.device = device;
    }
}
