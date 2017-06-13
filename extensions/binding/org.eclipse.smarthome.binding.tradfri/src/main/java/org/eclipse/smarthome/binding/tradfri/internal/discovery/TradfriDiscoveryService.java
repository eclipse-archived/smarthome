/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal.discovery;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants;
import org.eclipse.smarthome.binding.tradfri.handler.TradfriGatewayHandler;
import org.eclipse.smarthome.binding.tradfri.internal.DeviceUpdateListener;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
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

    public TradfriDiscoveryService(TradfriGatewayHandler bridgeHandler) {
        super(TradfriBindingConstants.SUPPORTED_LIGHT_TYPES_UIDS, 10, true);
        this.handler = bridgeHandler;
    }

    @Override
    protected void startScan() {
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
                ThingUID thingId = null;

                if (type.equals(TYPE_LIGHT) && data.has(LIGHT)) {
                    JsonObject state = data.get(LIGHT).getAsJsonArray().get(0).getAsJsonObject();

                    // Color temperature light
                    if (state.has(COLOR)) {
                        thingId = new ThingUID(THING_TYPE_COLOR_TEMP_LIGHT, bridge, Integer.toString(id));
                    } else {
                        thingId = new ThingUID(THING_TYPE_DIMMABLE_LIGHT, bridge, Integer.toString(id));
                    }
                }

                if (thingId == null) {
                    // we didn't identify any device, so let's quit
                    return;
                }

                String label = "Unknown device"; // this is only a default as an unlikely fallback situation
                try {
                    label = data.get(NAME).getAsString();
                } catch (JsonSyntaxException e) {
                    logger.error("JSON error: {}", e.getMessage());
                }

                Map<String, Object> properties = new HashMap<>(1);
                properties.put("id", id);
                logger.debug("Adding device {} to inbox", thingId);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingId).withBridge(bridge)
                        .withLabel(label).withProperties(properties).withRepresentationProperty("id").build();
                thingDiscovered(discoveryResult);
            }
        } catch (JsonSyntaxException e) {
            logger.error("JSON error: {}", e.getMessage());
        }
    }
}
