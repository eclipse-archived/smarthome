/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.discovery;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.MODEL_LCT001;
import static org.eclipse.smarthome.binding.hue.HueBindingConstants.THING_TYPE_LCT001;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.q42.jue.FullLight;
import nl.q42.jue.HueBridge;

import org.eclipse.smarthome.binding.hue.config.HueLightConfiguration;
import org.eclipse.smarthome.binding.hue.internal.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.internal.handler.LightStatusListener;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link HueBridgeServiceTracker} tracks for hue lights which are connected
 * to a paired hue bridge.
 * 
 */
public class HueLightDiscoveryService extends AbstractDiscoveryService implements LightStatusListener {

    private final static Logger logger = LoggerFactory.getLogger(HueLightDiscoveryService.class);

	private HueBridgeHandler hueBridgeHandler;

    public HueLightDiscoveryService(HueBridgeHandler hueBridgeHandler) {
        super(5);
    	this.hueBridgeHandler = hueBridgeHandler;
    }

    public void activate() {
        hueBridgeHandler.registerLightStatusListener(this);
    }

    public void deactivate() {
        hueBridgeHandler.unregisterLightStatusListener(this);
    }

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return ImmutableSet.copyOf(new ThingTypeUID[] { THING_TYPE_LCT001 });
	}

	@Override
	public int getScanTimeout() {
		return 1;
	}

	@Override
	public void startScan() {
	}

	@Override
	public void onLightAdded(HueBridge bridge, FullLight light) {
		
		ThingUID thingUID = getThingUID(light);
		if(thingUID!=null) {
			ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
	        Map<String, Object> properties = new HashMap<>(1);
	        properties.put(HueLightConfiguration.LIGHT_ID, light.getId());
	        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
	        		.withProperties(properties)
	        		.withBridge(bridgeUID)
	        		.withLabel(light.getName())
	        		.build();
	        
	        thingDiscovered(discoveryResult);
		} else {
			logger.debug("discovered unsupported light of type '{}' with id {}", light.getModelID(), light.getId());
		}
	}

	@Override
	public void onLightRemoved(HueBridge bridge, FullLight light) {
		ThingUID thingUID = getThingUID(light);
	    
		if(thingUID!=null) {
			thingRemoved(thingUID);
		}
	}

	@Override
	public void onLightStateChanged(HueBridge bridge, FullLight light) {
		// nothing to do
	}

	@Override
	protected boolean getBackgroundDiscoveryDefault() {
		return true;
	}

	private ThingUID getThingUID(FullLight light) {
        ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
		ThingTypeUID thingTypeUID = null;
		switch(light.getModelID()) {
			case MODEL_LCT001 : thingTypeUID = THING_TYPE_LCT001; break; 
		}
		
		if(thingTypeUID!=null) {
		    String thingLightId = "Light" + light.getId();
		    ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, thingLightId);
			return thingUID;
		} else {
			return null;
		}
	}
}
