/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a {@link DiscoveryService} implementation, which can find UPnP devices in the network.
 * Support for further devices can be added by implementing and registering a {@link UpnpDiscoveryParticipant}.
 *  
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class UpnpDiscoveryService extends AbstractDiscoveryService implements RegistryListener {

	private static final Logger logger = LoggerFactory.getLogger(UpnpDiscoveryService.class);
	
	private Set<UpnpDiscoveryParticipant> participants = new HashSet<>();
	
	public UpnpDiscoveryService() {
		super(Collections.EMPTY_SET, 5);
	}

	private UpnpService upnpService;

	protected void activate() {
		setBackgroundDiscoveryEnabled(true);
		startScan();
	}

	protected void deactivate() {
		this.upnpService.getRegistry().removeListener(this);
	}
	
	protected void setUpnpService(UpnpService upnpService) {
		this.upnpService = upnpService;
	}

	protected void unsetUpnpService(UpnpService upnpService) {
		this.upnpService = null;
	}

	protected void addUpnpDiscoveryParticipant(UpnpDiscoveryParticipant participant) {
		this.participants.add(participant);
	}

	protected void removeUpnpDiscoveryParticipant(UpnpDiscoveryParticipant participant) {
		this.participants.remove(participant);
	}

	@Override
	public void setBackgroundDiscoveryEnabled(boolean enabled) {
		if(enabled) {
			if(!isBackgroundDiscoveryEnabled()) {
				upnpService.getRegistry().addListener(this);
			}
		} else {
			if(isBackgroundDiscoveryEnabled()) {
				upnpService.getRegistry().removeListener(this);
			}
		}
		super.setBackgroundDiscoveryEnabled(enabled);
			
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		Set<ThingTypeUID> supportedThingTypes = new HashSet<>();
		for(UpnpDiscoveryParticipant participant : participants) {
			supportedThingTypes.addAll(participant.getSupportedThingTypeUIDs());
		}
		return supportedThingTypes;
	}
	
	@Override
	protected void startScan() {
		for(RemoteDevice device : upnpService.getRegistry().getRemoteDevices()) {
			remoteDeviceAdded(upnpService.getRegistry(), device);
		}
		upnpService.getRegistry().addListener(this);
		upnpService.getControlPoint().search();
	}

	@Override
	protected synchronized void stopScan() {
		super.stopScan();
		if(!isBackgroundDiscoveryEnabled()) {
			upnpService.getRegistry().removeListener(this);
		}
	}
	@Override
	protected boolean getBackgroundDiscoveryDefault() {
		// UPnP can receive notifications in the background, so that it does not
		// mean any additional system load
		return true;
	}

	@Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		for(UpnpDiscoveryParticipant participant : participants) {
			try {
				DiscoveryResult result = participant.createResult(device);
				if(result!=null) {
					thingDiscovered(result);
				}
			} catch(Exception e) {
				logger.error("Participant '{}' threw an exception", participant.getClass().getName(), e);
			}
		}	    
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		for(UpnpDiscoveryParticipant participant : participants) {
			try {
				ThingUID thingUID = participant.getThingUID(device);
				if(thingUID!=null) {
					thingRemoved(thingUID);
				}
			} catch(Exception e) {
				logger.error("Participant '{}' threw an exception", participant.getClass().getName(), e);
			}
		}	    
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {}

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {}

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {}

    @Override
    public void beforeShutdown(Registry registry) {}

    @Override
    public void afterShutdown() {}

    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {}

    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {}

}
