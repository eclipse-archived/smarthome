/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.handler;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.tradfri.DeviceConfig;
import org.eclipse.smarthome.binding.tradfri.internal.CoapCallback;
import org.eclipse.smarthome.binding.tradfri.internal.TradfriCoapClient;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TradfriGroupHandler} is responsible for handling commands for
 * individual lights.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Mario Smit - Group Handler added
 */
public class TradfriGroupHandler extends BaseThingHandler implements CoapCallback {

    private final Logger logger = LoggerFactory.getLogger(TradfriGroupHandler.class);

    // keeps track of the current state for handling of increase/decrease
    private GroupData state;

    // the unique instance id of the light
    private Integer id;

    // used to check whether we have already been disposed when receiving data asynchronously
    private volatile boolean active;

    private TradfriCoapClient coapClient;

    public TradfriGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void initialize() {
        this.id = getConfigAs(DeviceConfig.class).id;
        TradfriGatewayHandler handler = (TradfriGatewayHandler) getBridge().getHandler();
        String uriString = handler.groups.getGatewayURI() + "/" + id;
        try {
            URI uri = new URI(uriString);
            coapClient = new TradfriCoapClient(uri);
            coapClient.setEndpoint(handler.groups.getEndpoint());
        } catch (URISyntaxException e) {
            logger.debug("Illegal device URI `{}`: {}", uriString, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        active = true;

        scheduler.schedule(() -> {
            coapClient.startObserve(this);
        }, 3, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void dispose() {
        active = false;
        if (coapClient != null) {
            coapClient.shutdown();
        }
        super.dispose();
    }

    @Override
    public void setStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        if (active && getBridge().getStatus() != ThingStatus.OFFLINE && status != ThingStatus.ONLINE) {
            updateStatus(status, statusDetail);
            // we are offline and lost our observe relation - let's try to establish the connection in 10 seconds again
            scheduler.schedule(() -> {
                coapClient.startObserve(this);
            }, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onUpdate(JsonElement data) {
        if (active && !(data.isJsonNull())) {
            state = new GroupData(data);
            updateStatus(ThingStatus.ONLINE);

            if (state.getOnOffState()) {
                updateState(CHANNEL_GROUP_STATE, OnOffType.ON);
            } else {
                updateState(CHANNEL_GROUP_STATE, OnOffType.OFF);
            }
        }
    }

    private void set(String payload) {
        logger.debug("Sending payload: {}", payload);
        coapClient.asyncPut(payload, this);
    }

    private void setState(OnOffType onOff) {
        GroupData data = new GroupData();
        data.setOnOffState(onOff == OnOffType.ON);
        set(data.getJsonString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID);
            coapClient.asyncGet(this);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_GROUP_STATE:
                handleGroupStateCommand(command);
                break;

            default:
                logger.error("Unknown channel UID {}", channelUID);
        }
    }

    private void handleGroupStateCommand(Command command) {
        if (command instanceof OnOffType) {
            setState(((OnOffType) command));
        } else {
            logger.debug("Cannot handle command {} for channel {}", command, CHANNEL_GROUP_STATE);
        }
    }

    /**
     * This class is a Java wrapper for the raw JSON data about the group state.
     */
    private static class GroupData {

        private final Logger logger = LoggerFactory.getLogger(GroupData.class);

        JsonObject root;
        boolean ison;

        public GroupData() {
            root = new JsonObject();
        }

        public GroupData(JsonElement json) {
            try {
                root = json.getAsJsonObject();
                ison = (root.get(ONOFF).getAsInt() == 1) ? true : false;
            } catch (JsonSyntaxException e) {
                logger.error("JSON error: {}", e.getMessage(), e);
            }
        }

        GroupData setOnOffState(boolean on) {
            ison = on;
            root.addProperty(ONOFF, ison ? 1 : 0);
            return this;
        }

        boolean getOnOffState() {
            return ison;
        }

        String getJsonString() {
            return root.toString();
        }
    }

}
