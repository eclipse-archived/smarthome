/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.raspbee.internal.discovery;

import static org.eclipse.smarthome.binding.raspbee.RaspBeeBindingConstants.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.raspbee.handler.LightStatusListener;
import org.eclipse.smarthome.binding.raspbee.handler.RaspBeeBridgeHandler;
import org.eclipse.smarthome.binding.raspbee.handler.RaspBeeLightHandler;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprisecoding.jraspbee.FullLight;
import com.enterprisecoding.jraspbee.RaspBeeBridge;

/**
 * The {@link HueBridgeServiceTracker} tracks for hue lights which are connected
 * to a paired hue bridge. The default search time for hue is 60 seconds.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Andre Fuechsel - changed search timeout
 * @author Thomas Höfer - Added representation
 * @author Fatih Boy - modified for raspbee
 */
public class RaspBeeLightDiscoveryService extends AbstractDiscoveryService implements LightStatusListener {

    private final Logger logger = LoggerFactory.getLogger(RaspBeeLightDiscoveryService.class);

    private final static int SEARCH_TIME = 60;

    private RaspBeeBridgeHandler raspBeeBridgeHandler;

    public RaspBeeLightDiscoveryService(RaspBeeBridgeHandler raspBeeBridgeHandler) {
        super(SEARCH_TIME);
        this.raspBeeBridgeHandler = raspBeeBridgeHandler;
    }

    public void activate() {
        raspBeeBridgeHandler.registerLightStatusListener(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        raspBeeBridgeHandler.unregisterLightStatusListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return RaspBeeLightHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        List<FullLight> lights = raspBeeBridgeHandler.getFullLights();
        if (lights != null) {
            for (FullLight l : lights) {
                onLightAddedInternal(l);
            }
        }
        // search for unpaired lights
        // raspBeeBridgeHandler.startSearch();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onLightAdded(RaspBeeBridge bridge, FullLight light) {
        onLightAddedInternal(light);
    }

    private void onLightAddedInternal(FullLight light) {
        ThingUID thingUID = getThingUID(light);
        if (thingUID != null) {
            ThingUID bridgeUID = raspBeeBridgeHandler.getThing().getUID();
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
    public void onLightRemoved(RaspBeeBridge bridge, FullLight light) {
        ThingUID thingUID = getThingUID(light);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    @Override
    public void onLightStateChanged(RaspBeeBridge bridge, FullLight light) {
        // nothing to do
    }

    private ThingUID getThingUID(FullLight light) {
        ThingUID bridgeUID = raspBeeBridgeHandler.getThing().getUID();
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
