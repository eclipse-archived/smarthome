/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.binding.lirc.LIRCBindingConstants;
import org.eclipse.smarthome.binding.lirc.internal.LIRCMessageListener;
import org.eclipse.smarthome.binding.lirc.internal.config.LIRCRemoteConfiguration;
import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCButtonEvent;
import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCResponse;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LIRCRemoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCRemoteHandler extends BaseThingHandler implements LIRCMessageListener {

    private final Logger logger = LoggerFactory.getLogger(LIRCRemoteHandler.class);
    private static final Pattern UNKNOWN_REMOTE_PATTERN = Pattern.compile("^unknown remote: \"(.+)\"$");

    private LIRCBridgeHandler bridgeHandler;
    private LIRCRemoteConfiguration config;
    private String remoteName = null;

    public LIRCRemoteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);
        if (remoteName == null) {
            logger.error("Remote name is not set in {}", getThing().getUID());
            return;
        }
        if (channelUID.getId().equals(LIRCBindingConstants.CHANNEL_TRANSMIT)) {
            if (command instanceof RefreshType) {
                // not supported
            } else if (command instanceof StringType) {
                bridgeHandler.transmit(remoteName, command.toString());
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        config = getConfigAs(LIRCRemoteConfiguration.class);
        remoteName = config.getRemote();
        if (remoteName == null) {
            logger.error("Remote name is not set in {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Remote name is not set");
        } else {
            bridgeHandler = (LIRCBridgeHandler) getBridge().getHandler();
            bridgeHandler.registerMessageListener(this);
            if (getBridge().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
        if (bridgeHandler != null) {
            bridgeHandler.unregisterMessageListener(this);
        }
        bridgeHandler = null;
        super.dispose();
    }

    @Override
    public void onButtonPressed(ThingUID bridge, LIRCButtonEvent buttonEvent) {
        if (remoteName.equals(buttonEvent.getRemote())) {
            logger.debug("Remote {}: Button {} pressed {} times.", remoteName, buttonEvent.getButton(),
                    buttonEvent.getRepeats() + 1);
            updateStatus(ThingStatus.ONLINE);
            triggerChannel(LIRCBindingConstants.CHANNEL_EVENT, buttonEvent.getButton());
        }
    }

    @Override
    public void onMessageReceived(ThingUID bridge, LIRCResponse response) {
        String command = response.getCommand();
        if ("LIST".equals(command) && response.isSuccess()) {
            boolean found = false;
            for (String remote : response.getData()) {
                if (remoteName.equals(remote)) {
                    found = true;
                }
            }
            if (found) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.error("Remote {}: Remote was removed from LIRC server.", remoteName);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else if (!response.isSuccess()) {
            String error = response.getData()[0];
            Matcher m = UNKNOWN_REMOTE_PATTERN.matcher(error);
            if (m.matches() && remoteName.equals(m.group(1))) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unknown remote");
            }
        }
    }
}
