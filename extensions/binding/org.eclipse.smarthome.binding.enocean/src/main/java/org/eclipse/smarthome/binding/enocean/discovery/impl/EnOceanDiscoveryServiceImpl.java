/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.enocean.discovery.impl;

import static org.eclipse.smarthome.binding.enocean.EnOceanBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.enocean.discovery.EnOceanDiscoveryService;
import org.eclipse.smarthome.binding.enocean.internal.EnOceanHandlerFactory;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.enocean.EnOceanDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnOceanDiscoveryServiceImpl} is responsible for discovering a EnOcean Thing
 * 
 *
 */

public class EnOceanDiscoveryServiceImpl extends AbstractDiscoveryService implements EnOceanDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(EnOceanDiscoveryServiceImpl.class);
    private List<EnOceanDevice> deviceList = new ArrayList<EnOceanDevice>();    

    public EnOceanDiscoveryServiceImpl() {
        super(EnOceanHandlerFactory.SUPPORTED_THING_TYPES, 10);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return EnOceanHandlerFactory.SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startBackgroundDiscovery() {
        // no scan available for EnOcean example devices
    }

    @Override
    protected void startScan() {
        // no scan available for EnOcean example devices
    }

    public void setEnoceanDevice(EnOceanDevice device) {
        deviceList.add(device);
        
        String chipId = Integer.toString(device.getChipId());
        
        logger.debug("new device - chip Id: "+chipId);
        
        
        ThingTypeUID thingTypeUID = getThingTypeUIDFromDevice(device);
        ThingUID th = new ThingUID(thingTypeUID,chipId);
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("deviceId", chipId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(th).withProperties(properties).build();

        thingDiscovered(discoveryResult);

    }

    private ThingTypeUID getThingTypeUIDFromDevice(EnOceanDevice device) {

        int rorg = device.getRorg();
        int type = device.getType();
        int func = device.getFunc();

        if (rorg == 246 && type == -1 && func == -1) {
        	logger.debug("Eltako smoke detector has been asked - rorg - type - func: "+ rorg +" - " + type +" - " + func);        	
            return THING_TYPE_ELTAKO_SMOKE_DETECTOR;
        } else if (rorg == 160 && type == -1 && func == 62) {        	
        	logger.debug("on/off plug has been asked - rorg - type - func: "+ rorg +" - " + type +" - " + func);       	
        	return THING_TYPE_ON_OFF_PLUG;
        }

        return null;

    }

    public void unsetEnoceanDevice(EnOceanDevice device) {
        deviceList.remove(device);
        
        //TODO kill the handler
    }

    @Override
    public EnOceanDevice getEnOceanDevice(ThingUID thingUID) {
        String deviceId = thingUID.getId();

        for (EnOceanDevice device : deviceList) {

            if (deviceId.equals(Integer.toString(device.getChipId()))) {
                return device;
            }
        }
        return null;
    }

}
