/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.handler;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.smarthome.binding.tradfri.DeviceConfig;
import org.eclipse.smarthome.binding.tradfri.internal.CoapCallback;
import org.eclipse.smarthome.binding.tradfri.internal.TradfriCoapClient;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TradfriLightHandler} is responsible for handling commands for
 * individual lights.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriLightHandler extends BaseThingHandler implements CoapCallback {

    private final Logger logger = LoggerFactory.getLogger(TradfriLightHandler.class);

    // step size for increase/decrease commands
    private static final int STEP = 10;

    // keeps track of the current state for handling of increase/decrease
    private LightData state = null;

    // the unique instance id of the light
    private Integer id;

    // used to check whether we have already been disposed when receiving data asynchronously
    private volatile boolean active = false;

    private TradfriCoapClient coapClient;

    public TradfriLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.id = getConfigAs(DeviceConfig.class).id;
        TradfriGatewayHandler handler = (TradfriGatewayHandler) getBridge().getHandler();
        String uriString = handler.getGatewayURI() + "/" + id;
        try {
            URI uri = new URI(uriString);
            coapClient = new TradfriCoapClient(uri);
            coapClient.setEndpoint(handler.getEndpoint());
        } catch (URISyntaxException e) {
            logger.debug("Illegal device URI `{}`: {}", uriString, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        active = true;
        coapClient.startObserve(this);
    }

    @Override
    public void dispose() {
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
        }
    }

    @Override
    public void onUpdate(JsonElement data) {
        if (active && !(data.isJsonNull())) {
            state = new LightData(data);
            updateStatus(state.getReachabilityStatus() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

            if (!state.getOnOffState()) {
                logger.debug("Setting state to OFF");
                updateState(CHANNEL_BRIGHTNESS, PercentType.ZERO);
                // if we are turned off, we do not set any brightness value
                return;
            }

            PercentType dimmer = state.getBrightness();
            if (dimmer != null) {
                logger.debug("Updating brightness to {}", dimmer);
                updateState(CHANNEL_BRIGHTNESS, dimmer);
            }

            PercentType colorTemp = state.getColorTemperature();
            if (colorTemp != null) {
                logger.debug("Updating color temperature to {} ", colorTemp);
                updateState(CHANNEL_COLOR_TEMPERATURE, colorTemp);
            }
        }
    }

    private void set(String payload) {
        logger.debug("Sending payload: {}", payload);
        coapClient.asyncPut(payload, this);
    }

    private void setBrightness(PercentType percent) {
        LightData data = new LightData();
        data.setBrightness(percent).setTransitionTime(DEFAULT_DIMMER_TRANSITION_TIME);
        set(data.getJsonString());
    }

    private void setState(OnOffType onOff) {
        LightData data = new LightData();
        data.setOnOffState(onOff == OnOffType.ON);
        set(data.getJsonString());
    }

    private void setColorTemperature(PercentType percent) {
        LightData data = new LightData();
        data.setColorTemperature(percent).setTransitionTime(DEFAULT_DIMMER_TRANSITION_TIME);
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
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    setBrightness((PercentType) command);
                } else if (command instanceof OnOffType) {
                    setState(((OnOffType) command));
                } else if (command instanceof IncreaseDecreaseType) {
                    if (state != null) {
                        int current = state.getBrightness().intValue();
                        if (IncreaseDecreaseType.INCREASE.equals(command)) {
                            setBrightness(new PercentType(Math.min(current + STEP, PercentType.HUNDRED.intValue())));
                        } else {
                            setBrightness(new PercentType(Math.max(current - STEP, PercentType.ZERO.intValue())));
                        }
                    } else {
                        logger.debug("Cannot handle inc/dec as current state is not known.");
                    }
                } else {
                    logger.debug("Cannot handle command {} for channel {}", command, channelUID);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof PercentType) {
                    setColorTemperature((PercentType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    if (state != null) {
                        int current = state.getColorTemperature().intValue();
                        if (IncreaseDecreaseType.INCREASE.equals(command)) {
                            setColorTemperature(
                                    new PercentType(Math.min(current + STEP, PercentType.HUNDRED.intValue())));
                        } else {
                            setColorTemperature(new PercentType(Math.max(current - STEP, PercentType.ZERO.intValue())));
                        }
                    } else {
                        logger.debug("Cannot handle inc/dec as current state is not known.");
                    }
                } else {
                    logger.debug("Can't handle command {} on channel {}", command, channelUID);
                }
                break;
            default:
                logger.error("Unknown channel UID {}", channelUID);
        }
    }

    /**
     * This class is a Java wrapper for the raw JSON data about the light state.
     */
    private static class LightData {

        private final Logger logger = LoggerFactory.getLogger(LightData.class);

        JsonObject root;
        JsonArray array;
        JsonObject attributes;

        public LightData() {
            root = new JsonObject();
            array = new JsonArray();
            attributes = new JsonObject();
            array.add(attributes);
            root.add(LIGHT, array);
        }

        public LightData(JsonElement json) {
            try {
                root = json.getAsJsonObject();
                array = root.getAsJsonArray(LIGHT);
                attributes = array.get(0).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                logger.error("JSON error: {}", e.getMessage(), e);
            }
        }

        public LightData setBrightness(PercentType brightness) {
            attributes.add(DIMMER, new JsonPrimitive(Math.round(brightness.floatValue() / 100.0f * 254)));
            return this;
        }

        public PercentType getBrightness() {
            int b = attributes.get(DIMMER).getAsInt();
            if (b == 1) {
                return new PercentType(1);
            }
            return new PercentType((int) Math.round(b / 2.54));
        }

        public boolean getReachabilityStatus() {
            if (root.get(REACHABILITY_STATE) != null) {
                return root.get(REACHABILITY_STATE).getAsInt() == 1;
            } else {
                return false;
            }
        }

        public LightData setTransitionTime(int seconds) {
            attributes.add(TRANSITION_TIME, new JsonPrimitive(seconds));
            return this;
        }

        @SuppressWarnings("unused")
        public int getTransitionTime() {
            return attributes.get(TRANSITION_TIME).getAsInt();
        }

        private final static double[] X = new double[] { 33137.0, 30138.0, 24933.0 };
        private final static double[] Y = new double[] { 27211.0, 26909.0, 24691.0 };

        public LightData setColorTemperature(PercentType c) {
            double percent = c.doubleValue();

            long x, y;
            if (percent < 50.0) {
                double p = percent / 50.0;
                x = Math.round(X[0] + p * (X[1] - X[0]));
                y = Math.round(Y[0] + p * (Y[1] - Y[0]));
            } else {
                double p = (percent - 50) / 50.0;
                x = Math.round(X[1] + p * (X[2] - X[1]));
                y = Math.round(Y[1] + p * (Y[2] - Y[1]));
            }
            logger.debug("New color temperature: {},{} ({} %)", x, y, percent);

            attributes.add(COLOR_X, new JsonPrimitive(x));
            attributes.add(COLOR_Y, new JsonPrimitive(y));
            return this;
        }

        PercentType getColorTemperature() {
            JsonElement colorX = attributes.get(COLOR_X);
            if (colorX != null) {
                double x = attributes.get(COLOR_X).getAsInt();
                double value = 0.0;
                if (x > X[1]) {
                    value = (x - X[0]) / (X[1] - X[0]) / 2.0;
                } else {
                    value = (x - X[1]) / (X[2] - X[1]) / 2.0 + 0.5;
                }
                return new PercentType((int) Math.round(value * 100.0));
            } else {
                return null;
            }
        }

        LightData setOnOffState(boolean on) {
            attributes.add(ONOFF, new JsonPrimitive(on ? 1 : 0));
            return this;
        }

        boolean getOnOffState() {
            JsonElement onOff = attributes.get(ONOFF);
            if (onOff != null) {
                return attributes.get(ONOFF).getAsInt() == 1;
            } else {
                return false;
            }
        }

        String getJsonString() {
            return root.toString();
        }
    }

}
