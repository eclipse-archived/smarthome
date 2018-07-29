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
package org.eclipse.smarthome.binding.hue.handler;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.HueBindingConstants;
import org.eclipse.smarthome.binding.hue.internal.FullHueObject;
import org.eclipse.smarthome.binding.hue.internal.FullSensor;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Sensor Handler
 *
 * @author Samuel Leisering - Added sensor support
 *
 */
public abstract class HueSensorHandler extends BaseThingHandler implements SensorStatusListener {

    private final Logger logger = LoggerFactory.getLogger(HueSensorHandler.class);

    private String sensorId;

    private HueClient hueClient;

    private boolean propertiesInitializedSuccessfully;

    public HueSensorHandler(Thing thing) {
        super(thing);
    }

    protected abstract String getVendor(String modelId);

    @Override
    public void initialize() {
        logger.debug("Initializing hue sensor handler.");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        final String configLightId = (String) getConfig().get(LIGHT_ID);
        if (configLightId != null) {
            sensorId = configLightId;

            // note: this call implicitly registers our handler as a listener on
            // the bridge
            if (getHueClient() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    initializeProperties();
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-light-id");
        }
    }

    private synchronized void initializeProperties() {
        if (!propertiesInitializedSuccessfully) {
            FullHueObject fullLight = getSensor();
            if (fullLight != null) {
                String modelId = fullLight.getModelID().replaceAll(HueLightHandler.NORMALIZE_ID_REGEX, "_");
                updateProperty(Thing.PROPERTY_MODEL_ID, modelId);
                updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, fullLight.getSoftwareVersion());
                String vendor = getVendor(modelId);
                if (vendor != null) {
                    updateProperty(Thing.PROPERTY_VENDOR, vendor);
                }
                String uniqueID = fullLight.getUniqueID();
                if (uniqueID != null) {
                    updateProperty(LIGHT_UNIQUE_ID, uniqueID);
                }

                propertiesInitializedSuccessfully = true;
            }
        }
    }

    private @Nullable FullSensor getSensor() {
        HueClient bridgeHandler = getHueClient();
        if (bridgeHandler != null) {
            return bridgeHandler.getSensorById(sensorId);
        }
        return null;
    }

    protected synchronized @Nullable HueClient getHueClient() {
        if (this.hueClient == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HueClient) {
                this.hueClient = (HueClient) handler;
                this.hueClient.registerSensorStatusListener(this);
            } else {
                return null;
            }
        }
        return this.hueClient;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // no commands
    }

    @Override
    public void onSensorStateChanged(@Nullable HueBridge bridge, @NonNull FullSensor sensor) {
        logger.trace("onSensorStateChanged() was called");

        if (!sensor.getId().equals(sensorId)) {
            logger.trace("Received state change for another handler's sensor ({}). Will be ignored.", sensor.getId());
            return;
        }

        initializeProperties();

        if (Objects.equals(sensor.getConfig().get(FullSensor.CONFIG_REACHABLE), Boolean.TRUE)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.sensor-not-reachable");
        }

        // update generic sensor config
        Configuration config = getConfig();
        if (sensor.getConfig().containsKey(FullSensor.CONFIG_ALERT)) {
            config.put(FullSensor.CONFIG_ALERT, sensor.getConfig().get(FullSensor.CONFIG_ALERT));
        }
        if (sensor.getConfig().containsKey(FullSensor.CONFIG_BATTERY)) {
            config.put(FullSensor.CONFIG_BATTERY, sensor.getConfig().get(FullSensor.CONFIG_BATTERY));
        }
        if (sensor.getConfig().containsKey(FullSensor.CONFIG_ON)) {
            config.put(FullSensor.CONFIG_ON, sensor.getConfig().get(FullSensor.CONFIG_ON));
        }
        if (sensor.getConfig().containsKey(FullSensor.CONFIG_REACHABLE)) {
            config.put(FullSensor.CONFIG_REACHABLE, sensor.getConfig().get(FullSensor.CONFIG_REACHABLE));
        }

        doSensorStateChanged(bridge, sensor, config);

        Object valueObject = sensor.getState().get("lastupdated");
        if (valueObject != null) {
            // DateFormat.getDateTimeInstance().parse(String.valueOf(valueObject));
            DateTimeType type = DateTimeType.valueOf(String.valueOf(valueObject));
            updateState(HueBindingConstants.CHANNEL_LAST_UPDATED, type);
        }

        updateConfiguration(config);

    }

    /**
     * handle the sensor change. implementation should also update sensor-specific configuration that changed since the
     * last update
     *
     * @param bridge the bridge
     * @param sensor the sensor
     * @param config the configuration in which to update the config states of the sensor
     */
    protected abstract void doSensorStateChanged(@Nullable HueBridge bridge, @NonNull FullSensor sensor,
            Configuration config);

    @Override
    public void onSensorRemoved(@Nullable HueBridge bridge, @NonNull FullSensor sensor) {
        if (sensor.getId().equals(sensorId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "offline.sensor-removed");
        }
    }

    @Override
    public void onSensorAdded(@Nullable HueBridge bridge, @NonNull FullSensor sensor) {
        if (sensor.getId().equals(sensorId)) {
            onSensorStateChanged(bridge, sensor);
        }
    }

}
