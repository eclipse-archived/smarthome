/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal.discovery;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants;
import org.eclipse.smarthome.binding.tradfri.handler.TradfriGatewayHandler;
import org.eclipse.smarthome.binding.tradfri.internal.DeviceUpdateListener;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * This class identifies devices that are available on the gateway and adds discovery results for them.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriDiscoveryService extends AbstractDiscoveryService implements DeviceUpdateListener {

    private Logger logger = LoggerFactory.getLogger(TradfriDiscoveryService.class);

    private TradfriGatewayHandler handler;

    private String[] COLOR_TEMP_MODELS = new String[] { "TRADFRI bulb E27 WS opal 980lm",
            "TRADFRI bulb GU10 WS 400lm" };

    public TradfriDiscoveryService(TradfriGatewayHandler bridgeHandler) {
        super(TradfriBindingConstants.SUPPORTED_LIGHT_TYPES_UIDS, 10, true);
        this.handler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        handler.startScan();
    }

    public void activate() {
        handler.registerDeviceUpdateListener(this);
    }

    @Override
    public void deactivate() {
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

                if (type.equals(TYPE_LIGHT) && data.has(LIGHT)) {
                    JsonObject state = data.get(LIGHT).getAsJsonArray().get(0).getAsJsonObject();

                    // Color temperature light
                    // We do not always receive a COLOR attribute, even the light supports it - but the gateway does not
                    // seem to have this information, if the bulb is unreachable. We therefore also check against
                    // concrete model names.
                    if (state.has(COLOR) || (model != null && Arrays.asList(COLOR_TEMP_MODELS).contains(model))) {
                        thingId = new ThingUID(THING_TYPE_COLOR_TEMP_LIGHT, bridge, Integer.toString(id));
                    } else {
                        thingId = new ThingUID(THING_TYPE_DIMMABLE_LIGHT, bridge, Integer.toString(id));
                    }
                }

                if (thingId == null) {
                    // we didn't identify any device, so let's quit
                    return;
                }

                String label = data.get(NAME).getAsString();

                Map<String, Object> properties = new HashMap<>(1);
                properties.put("id", id);
                properties.put(Thing.PROPERTY_MODEL_ID, model);
                
                if (deviceInfo.get(DEVICE_VENDOR) != null) {
                    properties.put(Thing.PROPERTY_VENDOR, deviceInfo.get(DEVICE_VENDOR).getAsString());
                }
                if (deviceInfo.get(DEVICE_FIRMWARE) != null) {
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.get(DEVICE_FIRMWARE).getAsString());
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
