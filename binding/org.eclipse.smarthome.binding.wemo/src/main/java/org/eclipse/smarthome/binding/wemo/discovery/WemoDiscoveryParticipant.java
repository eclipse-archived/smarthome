/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.binding.wemo.discovery;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;
import static org.eclipse.smarthome.binding.wemo.config.WemoConfiguration.UDN;
import static org.eclipse.smarthome.binding.wemo.config.WemoConfiguration.FRIENDLY_NAME;
import static org.eclipse.smarthome.binding.wemo.config.WemoConfiguration.SERIAL_NUMBER;
import static org.eclipse.smarthome.binding.wemo.config.WemoConfiguration.DESCRIPTOR_URL;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.wemo.handler.WemoHandler;
import org.eclipse.smarthome.config.discovery.*;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link WemoDiscoveryParticipant} is responsible for discovering new and
 * removed Wemo devices. It uses the central {@link UpnpDiscoveryService}.
 * 
 * @author Hans-Jörg Merk - Initial contribution
 * 
 */
public class WemoDiscoveryParticipant implements UpnpDiscoveryParticipant {
	
    private Logger logger = LoggerFactory.getLogger(WemoDiscoveryParticipant.class);


   
	@Override
	public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
		return WemoHandler.SUPPORTED_THING_TYPES;
	}

	@Override
	public DiscoveryResult createResult(RemoteDevice device) {
		ThingUID uid = getThingUID(device);
		if(uid!=null) {
	        Map<String, Object> properties = new HashMap<>(4);
	        properties.put(FRIENDLY_NAME, device.getDetails().getFriendlyName());
	        properties.put(UDN, device.getIdentity().getUdn().getIdentifierString());
	        properties.put(SERIAL_NUMBER, device.getDetails().getSerialNumber());
	        properties.put(DESCRIPTOR_URL, device.getIdentity().getDescriptorURL().toString());

	        DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties)
					.withLabel(device.getDetails().getFriendlyName())
					.build();
	        logger.debug("Created a DiscoveryResult for device '{}' with serialNumber '{}'",
	        		device.getDetails().getFriendlyName(),device
	        		.getDetails().getSerialNumber());
	        return result;
		} else {
			return null;
		}
	}

	@Override
	public ThingUID getThingUID(RemoteDevice device) {
		DeviceDetails details = device.getDetails();
		if(details != null) {
			ModelDetails modelDetails = details.getModelDetails();
			if(modelDetails != null) {
				String modelName = modelDetails.getModelName();
				if(modelName != null) {
					if(modelName.startsWith("Socket")) {
						logger.debug("Discovered a WeMo Socket thing with serialNumber '{}'",
								device.getDetails().getSerialNumber());
						return new ThingUID(WEMO_SOCKET_TYPE_UID, device
								.getDetails().getSerialNumber());
					}
					if(modelName.startsWith("Insight")) {
						logger.debug("Discovered a WeMo Insight thing with serialNumber '{}'",
								device.getDetails().getSerialNumber());
						return new ThingUID(WEMO_INSIGHT_TYPE_UID, device
								.getDetails().getSerialNumber());
					}
				}
			}
		}
		return null;
	}
}
