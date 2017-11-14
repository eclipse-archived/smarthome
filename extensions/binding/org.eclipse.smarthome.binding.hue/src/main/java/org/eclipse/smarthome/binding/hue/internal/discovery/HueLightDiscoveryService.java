/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.discovery;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.handler.HueLightHandler;
import org.eclipse.smarthome.binding.hue.handler.LightStatusListener;
import org.eclipse.smarthome.binding.hue.internal.FullLight;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
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
public class HueLightDiscoveryService extends AbstractDiscoveryService implements LightStatusListener {

    private final Logger logger = LoggerFactory.getLogger(HueLightDiscoveryService.class);

    private final static int SEARCH_TIME = 60;

    // @formatter:off
    private final static Map<String, String> TYPE_TO_ZIGBEE_ID_MAP = Stream.of(
            new SimpleEntry<>("on_off_light", "0000"),
            new SimpleEntry<>("on_off_plug_in_unit", "0010"),
            new SimpleEntry<>("dimmable_light", "0100"),
            new SimpleEntry<>("dimmable_plug_in_unit", "0110"),
            new SimpleEntry<>("color_light", "0200"),
            new SimpleEntry<>("extended_color_light", "0210"),
            new SimpleEntry<>("color_temperature_light", "0220")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));
    // @formatter:on

    private HueBridgeHandler hueBridgeHandler;

    public HueLightDiscoveryService(HueBridgeHandler hueBridgeHandler) {
        super(SEARCH_TIME);
        this.hueBridgeHandler = hueBridgeHandler;
    }

    public void activate() {
        hueBridgeHandler.registerLightStatusListener(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        hueBridgeHandler.unregisterLightStatusListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return HueLightHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        List<FullLight> lights = hueBridgeHandler.getFullLights();
        if (lights != null) {
            for (FullLight l : lights) {
                onLightAddedInternal(l);
            }
        }
        // search for unpaired lights
        hueBridgeHandler.startSearch();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onLightAdded(HueBridge bridge, FullLight light) {
        onLightAddedInternal(light);
    }

    private void onLightAddedInternal(FullLight light) {
        ThingUID thingUID = getThingUID(light);
        ThingTypeUID thingTypeUID = getThingTypeUID(light);

        String modelId = light.getModelID().replaceAll(HueLightHandler.NORMALIZE_ID_REGEX, "_");

        if (thingUID != null && thingTypeUID != null) {
            ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(LIGHT_ID, light.getId());
            properties.put(MODEL_ID, modelId);
            properties.put(LIGHT_UNIQUE_ID, light.getUniqueID());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty(LIGHT_UNIQUE_ID)
                    .withLabel(light.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported light of type '{}' and model '{}' with id {}", light.getType(),
                    modelId, light.getId());
        }
    }

    @Override
    public void onLightRemoved(HueBridge bridge, FullLight light) {
        ThingUID thingUID = getThingUID(light);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    @Override
    public void onLightStateChanged(HueBridge bridge, FullLight light) {
        // nothing to do
    }

    private ThingUID getThingUID(FullLight light) {
        ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = getThingTypeUID(light);

        if (thingTypeUID != null && getSupportedThingTypes().contains(thingTypeUID)) {
            return new ThingUID(thingTypeUID, bridgeUID, light.getId());
        } else {
            return null;
        }
    }

    private ThingTypeUID getThingTypeUID(FullLight light) {
        String thingTypeId = TYPE_TO_ZIGBEE_ID_MAP
                .get(light.getType().replaceAll(HueLightHandler.NORMALIZE_ID_REGEX, "_").toLowerCase());
        return thingTypeId != null ? new ThingTypeUID(BINDING_ID, thingTypeId) : null;
    }
}
