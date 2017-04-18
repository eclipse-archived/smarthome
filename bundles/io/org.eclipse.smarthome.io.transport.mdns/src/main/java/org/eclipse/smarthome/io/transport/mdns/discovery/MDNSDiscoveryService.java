/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mdns.discovery;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.MDNSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a {@link DiscoveryService} implementation, which can find mDNS services in the network. Support for further
 * devices can be added by implementing and registering a {@link MDNSDiscoveryParticipant}.
 *
 * @author Tobias Bräutigam - Initial contribution
 * @author Kai Kreuzer - Improved startup behavior and background discovery
 * @author Andre Fuechsel - make {@link #startScan()} asynchronous
 */
public class MDNSDiscoveryService extends AbstractDiscoveryService implements ServiceListener {
    private final Logger logger = LoggerFactory.getLogger(MDNSDiscoveryService.class);

    private Set<MDNSDiscoveryParticipant> participants = new CopyOnWriteArraySet<>();

    private MDNSClient mdnsClient;

    public MDNSDiscoveryService() {
        super(5);
    }

    public void setMDNSClient(MDNSClient mdnsClient) {
        this.mdnsClient = mdnsClient;
        if (isBackgroundDiscoveryEnabled()) {
            for (MDNSDiscoveryParticipant participant : participants) {
                mdnsClient.addServiceListener(participant.getServiceType(), this);
            }
        }
    }

    public void unsetMDNSClient(MDNSClient mdnsClient) {
        for (MDNSDiscoveryParticipant participant : participants) {
            mdnsClient.removeServiceListener(participant.getServiceType(), this);
        }
        this.mdnsClient = null;
    }

    @Override
    protected void startBackgroundDiscovery() {
        for (MDNSDiscoveryParticipant participant : participants) {
            mdnsClient.addServiceListener(participant.getServiceType(), this);
        }
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        for (MDNSDiscoveryParticipant participant : participants) {
            mdnsClient.removeServiceListener(participant.getServiceType(), this);
        }
    }

    @Override
    protected void startScan() {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                scan();
            }
        }, 0, TimeUnit.SECONDS);
    }

    private void scan() {
        for (MDNSDiscoveryParticipant participant : participants) {
            ServiceInfo[] services = mdnsClient.list(participant.getServiceType());
            logger.debug("{} services found for {}", services.length, participant.getServiceType());
            for (ServiceInfo service : services) {
                DiscoveryResult result = participant.createResult(service);
                if (result != null) {
                    thingDiscovered(result);
                }
            }
        }
    }

    protected void addMdnsDiscoveryParticipant(MDNSDiscoveryParticipant participant) {
        this.participants.add(participant);
        if (mdnsClient != null && isBackgroundDiscoveryEnabled()) {
            mdnsClient.addServiceListener(participant.getServiceType(), this);
        }
    }

    protected void removeMdnsDiscoveryParticipant(MDNSDiscoveryParticipant participant) {
        this.participants.remove(participant);
        if (mdnsClient != null) {
            mdnsClient.removeServiceListener(participant.getServiceType(), this);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        Set<ThingTypeUID> supportedThingTypes = new HashSet<>();
        for (MDNSDiscoveryParticipant participant : participants) {
            supportedThingTypes.addAll(participant.getSupportedThingTypeUIDs());
        }
        return supportedThingTypes;
    }

    @Override
    public void serviceAdded(ServiceEvent serviceEvent) {
        considerService(serviceEvent);
    }

    @Override
    public void serviceRemoved(ServiceEvent serviceEvent) {
        for (MDNSDiscoveryParticipant participant : participants) {
            try {
                ThingUID thingUID = participant.getThingUID(serviceEvent.getInfo());
                if (thingUID != null) {
                    thingRemoved(thingUID);
                }
            } catch (Exception e) {
                logger.error("Participant '{}' threw an exception", participant.getClass().getName(), e);
            }
        }
    }

    @Override
    public void serviceResolved(ServiceEvent serviceEvent) {
        considerService(serviceEvent);
    }

    private void considerService(ServiceEvent serviceEvent) {
        if (isBackgroundDiscoveryEnabled()) {
            for (MDNSDiscoveryParticipant participant : participants) {
                try {
                    DiscoveryResult result = participant.createResult(serviceEvent.getInfo());
                    if (result != null) {
                        thingDiscovered(result);
                    }
                } catch (Exception e) {
                    logger.error("Participant '{}' threw an exception", participant.getClass().getName(), e);
                }
            }
        }
    }
}
