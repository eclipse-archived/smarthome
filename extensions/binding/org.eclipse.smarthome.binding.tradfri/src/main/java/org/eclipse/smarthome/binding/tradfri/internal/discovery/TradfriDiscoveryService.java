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
package org.eclipse.smarthome.binding.tradfri.internal.discovery;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;
import static org.eclipse.smarthome.core.thing.Thing.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.binding.tradfri.handler.TradfriGatewayHandler;
import org.eclipse.smarthome.binding.tradfri.internal.DeviceUpdateListener;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * This class identifies devices that are available on the gateway and adds discovery results for them.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Christoph Weitkamp - Added support for remote controller and motion sensor devices (read-only battery level)
 * @author Andre Fuechsel - fixed the results removal
 */
public class TradfriDiscoveryService extends AbstractDiscoveryService implements DeviceUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(TradfriDiscoveryService.class);

    private final TradfriGatewayHandler handler;

    private static final String REMOTE_CONTROLLER_MODEL = "TRADFRI remote control";

    private static final String[] COLOR_TEMP_MODELS = new String[] { "TRADFRI bulb E27 WS opal 980lm",
            "TRADFRI bulb E27 WS clear 950lm", "TRADFRI bulb GU10 WS 400lm", "TRADFRI bulb E14 WS opal 400lm",
            "FLOALT panel WS 30x30", "FLOALT panel WS 60x60", "FLOALT panel WS 30x90",
            "TRADFRI bulb E12 WS opal 400lm" };

    private static final String COLOR_MODELS_IDENTIFIER = "CWS";

    public TradfriDiscoveryService(TradfriGatewayHandler bridgeHandler) {
        super(Stream.concat(SUPPORTED_LIGHT_TYPES_UIDS.stream(), SUPPORTED_CONTROLLER_TYPES_UIDS.stream())
                .collect(Collectors.toSet()), 10, true);
        this.handler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        handler.startScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    public void activate() {
        handler.registerDeviceUpdateListener(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        handler.unregisterDeviceUpdateListener(this);
    }

    @Override
    public void onUpdate(String instanceId, JsonObject data) {
        ThingUID bridge = handler.getThing().getUID();
        try {
            if (data.has(INSTANCE_ID)) {
                int id = data.get(INSTANCE_ID).getAsInt();
                String type = data.get(TYPE).getAsString();
                JsonObject deviceInfo = data.get(DEVICE).getAsJsonObject();
                String model = deviceInfo.get(DEVICE_MODEL).getAsString();
                ThingUID thingId = null;

                if (TYPE_LIGHT.equals(type) && data.has(LIGHT)) {
                    JsonObject state = data.get(LIGHT).getAsJsonArray().get(0).getAsJsonObject();

                    // Color temperature light:
                    // We do not always receive a COLOR attribute, even the light supports it - but the gateway does not
                    // seem to have this information, if the bulb is unreachable. We therefore also check against
                    // concrete model names.
                    // Color light:
                    // As the protocol does not distinguishes between color and full-color lights,
                    // we check if the "CWS" identifier is given in the model name
                    ThingTypeUID thingType = null;
                    if (model != null && model.contains(COLOR_MODELS_IDENTIFIER)) {
                        thingType = THING_TYPE_COLOR_LIGHT;
                    }
                    if (thingType == null && //
                            (state.has(COLOR) || (model != null && Arrays.asList(COLOR_TEMP_MODELS).contains(model)))) {
                        thingType = THING_TYPE_COLOR_TEMP_LIGHT;
                    }
                    if (thingType == null) {
                        thingType = THING_TYPE_DIMMABLE_LIGHT;
                    }
                    thingId = new ThingUID(thingType, bridge, Integer.toString(id));
                } else if (TYPE_PLUG.equals(type) && data.has(PLUG)) {
                    // Smart plug
                    ThingTypeUID thingType = THING_TYPE_ONOFF_PLUG;
                    thingId = new ThingUID(thingType, bridge, Integer.toString(id));
                } else if (TYPE_SWITCH.equals(type) && data.has(SWITCH)) {
                    // Remote control and wireless dimmer
                    // As protocol does not distinguishes between remote control and wireless dimmer,
                    // we check for the whole model name
                    ThingTypeUID thingType = (model != null && REMOTE_CONTROLLER_MODEL.equals(model))
                            ? THING_TYPE_REMOTE_CONTROL
                            : THING_TYPE_DIMMER;
                    thingId = new ThingUID(thingType, bridge, Integer.toString(id));
                } else if (TYPE_SENSOR.equals(type) && data.has(SENSOR)) {
                    // Motion sensor
                    thingId = new ThingUID(THING_TYPE_MOTION_SENSOR, bridge, Integer.toString(id));
                }

                if (thingId == null) {
                    // we didn't identify any device, so let's quit
                    logger.debug("Ignoring unknown device on TRADFRI gateway:\n\ttype : {}\n\tmodel: {}\n\tinfo : {}",
                            type, model, deviceInfo.getAsString());
                    return;
                }

                String label = data.get(NAME).getAsString();

                Map<String, Object> properties = new HashMap<>(1);
                properties.put("id", id);
                properties.put(PROPERTY_MODEL_ID, model);

                if (deviceInfo.get(DEVICE_VENDOR) != null) {
                    properties.put(PROPERTY_VENDOR, deviceInfo.get(DEVICE_VENDOR).getAsString());
                }
                if (deviceInfo.get(DEVICE_FIRMWARE) != null) {
                    properties.put(PROPERTY_FIRMWARE_VERSION, deviceInfo.get(DEVICE_FIRMWARE).getAsString());
                }

                logger.debug("Adding device {} to inbox", thingId);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingId).withBridge(bridge)
                        .withLabel(label).withProperties(properties).withRepresentationProperty("id").build();
                thingDiscovered(discoveryResult);
            }
        } catch (JsonSyntaxException e) {
            logger.debug("JSON error during discovery: {}", e.getMessage());
        }
    }
}
