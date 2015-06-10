/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.discovery;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.BINDING_ID;
import static org.eclipse.smarthome.binding.hue.HueBindingConstants.LIGHT_ID;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.q42.jue.FullLight;
import nl.q42.jue.HueBridge;

import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.handler.HueLightHandler;
import org.eclipse.smarthome.binding.hue.handler.LightStatusListener;
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
 * @author Andre Fuechsel - changed search timeout
 * @author Thomas Höfer - Added representation
 */
public class HueLightDiscoveryService extends AbstractDiscoveryService implements LightStatusListener {

    private final Logger logger = LoggerFactory.getLogger(HueLightDiscoveryService.class);

    private final static int SEARCH_TIME = 60;

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
        if (thingUID != null) {
            ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(LIGHT_ID, light.getId());

            /*
             * TODO retrieve the light´s unique id (available since Hue bridge versions > 1.3) and set the mac address
             * as discovery result representationÏ. For this purpose the jue library has to be modified.
             */

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(light.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported light of type '{}' with id {}", light.getModelID(), light.getId());
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
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, light.getModelID().replaceAll("[^a-zA-Z0-9_]", "_"));

        if (getSupportedThingTypes().contains(thingTypeUID)) {
            String thingLightId = light.getId();
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, thingLightId);
            return thingUID;
        } else {
            return null;
        }
    }
}
