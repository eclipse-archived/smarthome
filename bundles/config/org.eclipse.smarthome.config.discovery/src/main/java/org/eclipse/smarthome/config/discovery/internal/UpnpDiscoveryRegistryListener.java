/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import static org.osgi.service.component.annotations.ReferenceCardinality.AT_LEAST_ONE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is primarily used to track all available {@link UpnpDiscoveryParticipant} instances. This
 * component will only be enabled if and only if the optional dependencies are available to this
 * bundle's classloader.
 * <br>
 * <br>
 * The Optional Dependency Packages are as follows:
 * <ul>
 * <li>org.jupnp.model.meta</li>
 * <li>org.jupnp.registry</li>
 * <li>org.jupnp</li>
 * </ul>
 * 
 * @see RegistryListener
 * 
 * @author Amit Kumar Mondal - Extracted to separate class due to JUPnP optional dependencies
 */
@Component(enabled = false, name = "UpnpDiscoveryRegistryListener", service = UpnpDiscoveryRegistryListener.class)
public final class UpnpDiscoveryRegistryListener implements RegistryListener {

    private final Logger logger = LoggerFactory.getLogger(UpnpDiscoveryRegistryListener.class);

    private final Set<UpnpDiscoveryParticipant> participants = new CopyOnWriteArraySet<>();

    private UpnpService upnpService;
    private DiscoveryService discoveryService;

    @Reference
    protected void setUpnpService(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    protected void unsetUpnpService(UpnpService upnpService) {
        this.upnpService = null;
    }

    @Reference(target = "(serviceType=upnp)")
    protected void setDiscoveryService(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    protected void unsetDiscoveryService(DiscoveryService discoveryService) {
        this.discoveryService = null;
    }

    @Reference(cardinality = AT_LEAST_ONE, policy = DYNAMIC)
    protected void addUpnpDiscoveryParticipant(UpnpDiscoveryParticipant participant) {
        this.participants.add(participant);

        Collection<RemoteDevice> devices = upnpService.getRegistry().getRemoteDevices();
        for (RemoteDevice device : devices) {
            DiscoveryResult result = participant.createResult(device);
            if (result != null) {
                ((UpnpDiscoveryService) discoveryService).thingDiscovered(result);
            }
        }
    }

    protected void removeUpnpDiscoveryParticipant(UpnpDiscoveryParticipant participant) {
        this.participants.remove(participant);
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        for (UpnpDiscoveryParticipant participant : participants) {
            try {
                DiscoveryResult result = participant.createResult(device);
                if (result != null) {
                    ((UpnpDiscoveryService) discoveryService).thingDiscovered(result);
                }
            } catch (Exception e) {
                logger.error("Participant '{}' threw an exception", participant.getClass().getName(), e);
            }
        }
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        for (UpnpDiscoveryParticipant participant : participants) {
            try {
                ThingUID thingUID = participant.getThingUID(device);
                if (thingUID != null) {
                    ((UpnpDiscoveryService) discoveryService).thingRemoved(thingUID);
                }
            } catch (Exception e) {
                logger.error("Participant '{}' threw an exception", participant.getClass().getName(), e);
            }
        }
    }

    Set<UpnpDiscoveryParticipant> getParticipants() {
        return Collections.unmodifiableSet(participants);
    }

    void upnpDeviceSearch() {
        for (RemoteDevice device : upnpService.getRegistry().getRemoteDevices()) {
            remoteDeviceAdded(upnpService.getRegistry(), device);
        }
        upnpService.getRegistry().addListener(this);
        upnpService.getControlPoint().search();
    }

    void addRegistryListener() {
        upnpService.getRegistry().addListener(this);
    }

    void removeRegistryListener() {
        upnpService.getRegistry().removeListener(this);
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
    }

    @Override
    public void beforeShutdown(Registry registry) {
    }

    @Override
    public void afterShutdown() {
    }

    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
    }

}