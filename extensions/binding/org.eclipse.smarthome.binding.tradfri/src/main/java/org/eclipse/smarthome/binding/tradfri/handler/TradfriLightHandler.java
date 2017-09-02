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
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
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
    private LightData state;

    // the unique instance id of the light
    private Integer id;

    // used to check whether we have already been disposed when receiving data asynchronously
    private volatile boolean active;

    private TradfriCoapClient coapClient;

    public TradfriLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void initialize() {
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
                updateState(CHANNEL_BRIGHTNESS, dimmer);
            }

            PercentType colorTemp = state.getColorTemperature();
            if (colorTemp != null) {
                updateState(CHANNEL_COLOR_TEMPERATURE, colorTemp);
            }
            
            String devicefirmware = state.getFirmwareVersion();
            if (devicefirmware != null) {
                getThing().setProperty(Thing.PROPERTY_FIRMWARE_VERSION, devicefirmware);
            }
            
            String modelId = state.getModelId();
            if (modelId != null) {
                getThing().setProperty(Thing.PROPERTY_MODEL_ID, modelId);
            }
            
            String vendor = state.getVendor();
            if (vendor != null) {
                getThing().setProperty(Thing.PROPERTY_VENDOR, vendor);
            }
            
            logger.debug("Updating thing for lightId {} to state {dimmer: {}, colorTemp: {}, devicefirmware: {}, modelId: {}, vendor: {}}"
                    , state.getLightId(), dimmer, colorTemp, devicefirmware, modelId, vendor);
        }
    }
    
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            scheduler.schedule(() -> {
                coapClient.startObserve(this);
            }, 0, TimeUnit.SECONDS);
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
                handleBrightnessCommand(command);
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                handleColorTemperatureCommand(command);
                break;
            default:
                logger.error("Unknown channel UID {}", channelUID);
        }
    }

    private void handleBrightnessCommand(Command command) {
        if (command instanceof PercentType) {
            setBrightness((PercentType) command);
        } else if (command instanceof OnOffType) {
            setState(((OnOffType) command));
        } else if (command instanceof IncreaseDecreaseType) {
            if (state != null && state.getBrightness() != null) {
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
            logger.debug("Cannot handle command {} for channel {}", command, CHANNEL_BRIGHTNESS);
        }
    }

    private void handleColorTemperatureCommand(Command command) {
        if (command instanceof PercentType) {
            setColorTemperature((PercentType) command);
        } else if (command instanceof IncreaseDecreaseType) {
            if (state != null && state.getColorTemperature() != null) {
                int current = state.getColorTemperature().intValue();
                if (IncreaseDecreaseType.INCREASE.equals(command)) {
                    setColorTemperature(new PercentType(Math.min(current + STEP, PercentType.HUNDRED.intValue())));
                } else {
                    setColorTemperature(new PercentType(Math.max(current - STEP, PercentType.ZERO.intValue())));
                }
            } else {
                logger.debug("Cannot handle inc/dec as current state is not known.");
            }
        } else {
            logger.debug("Can't handle command {} on channel {}", command, CHANNEL_COLOR_TEMPERATURE);
        }
    }

    /**
     * This class is a Java wrapper for the raw JSON data about the light state.
     */
    private static class LightData {

        // Tradfri uses the CIE color space (see https://en.wikipedia.org/wiki/CIE_1931_color_space),
        // which uses x,y-coordinates.
        // Its own app comes with 3 predefined color temperature settings (0,1,2), which have those values:
        private final static double[] X = new double[] { 24933.0, 30138.0, 33137.0 };
        private final static double[] Y = new double[] { 24691.0, 26909.0, 27211.0 };

        private final Logger logger = LoggerFactory.getLogger(LightData.class);

        JsonObject root;
        JsonArray array;
        JsonObject attributes;
        JsonObject generalInfo;

        public LightData() {
            root = new JsonObject();
            array = new JsonArray();
            attributes = new JsonObject();
            array.add(attributes);
            root.add(LIGHT, array);
            
            generalInfo = new JsonObject();
            root.add(DEVICE, generalInfo);
        }

        public LightData(JsonElement json) {
            try {
                root = json.getAsJsonObject();
                array = root.getAsJsonArray(LIGHT);
                attributes = array.get(0).getAsJsonObject();
                generalInfo = root.getAsJsonObject(DEVICE);
            } catch (JsonSyntaxException e) {
                logger.error("JSON error: {}", e.getMessage(), e);
            }
        }

        public LightData setBrightness(PercentType brightness) {
            attributes.add(DIMMER, new JsonPrimitive(Math.round(brightness.floatValue() / 100.0f * 254)));
            return this;
        }

        public PercentType getBrightness() {
            JsonElement dimmer = attributes.get(DIMMER);
            if (dimmer != null) {
                int b = dimmer.getAsInt();
                if (b == 1) {
                    return new PercentType(1);
                }
                return new PercentType((int) Math.round(b / 2.54));
            } else {
                return null;
            }
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
            JsonElement transitionTime = attributes.get(TRANSITION_TIME);
            if (transitionTime != null) {
                return transitionTime.getAsInt();
            } else {
                return 0;
            }
        }

        public LightData setColorTemperature(PercentType c) {
            double percent = c.doubleValue();

            long x, y;
            if (percent < 50.0) {
                // we calculate a value that is between preset 0 and 1
                double p = percent / 50.0;
                x = Math.round(X[0] + p * (X[1] - X[0]));
                y = Math.round(Y[0] + p * (Y[1] - Y[0]));
            } else {
                // we calculate a value that is between preset 1 and 2
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
            // we only need to check one of the coordinates and figure out where between the presets we are
            JsonElement colorX = attributes.get(COLOR_X);
            if (colorX != null) {
                double x = colorX.getAsInt();
                double value = 0.0;
                if (x > X[1]) {
                    // is it between preset 1 and 2?
                    value = (x - X[1]) / (X[2] - X[1]) / 2.0 + 0.5;
                } else {
                    // it is between preset 0 and 1
                    value = (x - X[0]) / (X[1] - X[0]) / 2.0;
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
        
        Integer getLightId() {
            return root.get(INSTANCE_ID).getAsInt();
        }
        
        String getFirmwareVersion() {
            if (generalInfo.get(DEVICE_FIRMWARE) != null) {
                return generalInfo.get(DEVICE_FIRMWARE).getAsString();
            } else {
                return null;
            }
        }
        
        String getModelId() {
            if (generalInfo.get(DEVICE_MODEL) != null) {
                return generalInfo.get(DEVICE_MODEL).getAsString();
            } else {
                return null;
            }
        }
        
        String getVendor() {
            if (generalInfo.get(DEVICE_VENDOR) != null) {
                return generalInfo.get(DEVICE_VENDOR).getAsString();
            } else {
                return null;
            }
        }

        String getJsonString() {
            return root.toString();
        }
    }

}
