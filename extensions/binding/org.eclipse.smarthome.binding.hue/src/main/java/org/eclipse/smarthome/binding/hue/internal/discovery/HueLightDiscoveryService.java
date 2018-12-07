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
package org.eclipse.smarthome.binding.hue.internal.discovery;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.BINDING_ID;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.HueBindingConstants;
import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.handler.HueLightHandlerConfig;
import org.eclipse.smarthome.binding.hue.internal.LightStatusListener;
import org.eclipse.smarthome.binding.hue.internal.dto.Light;
import org.eclipse.smarthome.binding.hue.internal.exceptions.ApiException;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueBridgeServiceTracker} tracks for hue lights which are connected
 * to a paired hue bridge. The default search time for hue is 60 seconds.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Andre Fuechsel - changed search timeout, changed discovery result creation to support generic thing types;
 *         added representationProperty to discovery result
 * @author Thomas Höfer - Added representation
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class })
public class HueLightDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, LightStatusListener, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(HueLightDiscoveryService.class);

    private static final int SEARCH_TIME = 10;

    // @formatter:off
    private static final Map<String, @Nullable String> TYPE_TO_ZIGBEE_ID_MAP = Stream.of(
            new SimpleEntry<>("on_off_light", "0000"),
            new SimpleEntry<>("on_off_plug_in_unit", "0010"),
            new SimpleEntry<>("dimmable_light", "0100"),
            new SimpleEntry<>("dimmable_plug_in_unit", "0110"),
            new SimpleEntry<>("color_light", "0200"),
            new SimpleEntry<>("extended_color_light", "0210"),
            new SimpleEntry<>("color_temperature_light", "0220")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));
    // @formatter:on

    private @NonNullByDefault({}) HueBridgeHandler hueBridgeHandler;

    public HueLightDiscoveryService() {
        super(SEARCH_TIME);
    }

    @Override
    public void deactivate() {
        if (hueBridgeHandler != null) {
            removeOlderResults(new Date().getTime(), hueBridgeHandler.getThing().getUID());
            hueBridgeHandler.getHueBridge().unregisterLightStatusListener(this);
            hueBridgeHandler = null;
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return HueBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        try {
            List<Light> lights = hueBridgeHandler.getHueBridge().updateLights();
            for (Light l : lights) {
                onLightAdded(l);
            }
            hueBridgeHandler.getHueBridge().startSearch(null);
        } catch (IOException | ApiException e) {
            logger.warn("Discovery could not be started", e);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onLightAdded(Light light) {
        ThingTypeUID thingTypeUID = getThingTypeUID(light);

        String modelId = light.modelid.replaceAll(HueBindingConstants.NORMALIZE_ID_REGEX, "_");

        if (thingTypeUID == null) {
            logger.debug("discovered unsupported light of type '{}' and model '{}' with id {}", light.type, modelId,
                    light.id);
            return;
        }

        ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, light.id);

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(HueLightHandlerConfig.LIGHT_ID, light.id);
        properties.put(HueLightHandlerConfig.MODEL_ID, modelId);
        properties.put(HueLightHandlerConfig.LIGHT_UNIQUE_ID, light.uniqueid);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withBridge(bridgeUID)
                .withRepresentationProperty(HueLightHandlerConfig.LIGHT_UNIQUE_ID).withLabel(light.name).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    public void onLightRemoved(Light light) {
        ThingUID thingUID = getThingUID(light);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    @Override
    public void onLightStateChanged(Light light) {
    }

    private @Nullable ThingUID getThingUID(Light light) {
        final ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
        final @Nullable ThingTypeUID thingTypeUID = getThingTypeUID(light);

        if (thingTypeUID != null) {
            return new ThingUID(thingTypeUID, bridgeUID, light.id);
        } else {
            return null;
        }
    }

    private @Nullable ThingTypeUID getThingTypeUID(Light light) {
        String thingTypeId = TYPE_TO_ZIGBEE_ID_MAP
                .get(light.type.replaceAll(HueBindingConstants.NORMALIZE_ID_REGEX, "_").toLowerCase());
        ThingTypeUID t = thingTypeId != null ? new ThingTypeUID(BINDING_ID, thingTypeId) : null;
        if (!getSupportedThingTypes().contains(t)) {
            return null;
        }
        return t;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler != null) {
            hueBridgeHandler = (HueBridgeHandler) handler;
            hueBridgeHandler.getHueBridge().registerLightStatusListener(this);
        } else if (hueBridgeHandler != null) {
            deactivate();
            hueBridgeHandler = null;
        }
    }

    @Override
    public ThingHandler getThingHandler() {
        return hueBridgeHandler;
    }

    // For tests: Return true if handler got removed
    public boolean isInactive() {
        return hueBridgeHandler == null;
    }
}
