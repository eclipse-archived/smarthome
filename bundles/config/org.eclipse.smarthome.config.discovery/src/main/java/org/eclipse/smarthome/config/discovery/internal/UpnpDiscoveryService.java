/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import static org.osgi.service.component.annotations.ReferenceCardinality.OPTIONAL;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a {@link DiscoveryService} implementation, which can find UPnP devices in the network.
 * Support for further devices can be added by implementing and registering a {@link UpnpDiscoveryParticipant}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Andre Fuechsel - Added call of removeOlderResults
 * @author Amit Kumar Mondal - Extracted Registry Listener due to JUPnP optional dependencies
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.upnp", property = "serviceType=upnp")
public class UpnpDiscoveryService extends AbstractDiscoveryService {
    
    private final Logger logger = LoggerFactory.getLogger(UpnpDiscoveryService.class);

    private UpnpDiscoveryRegistryListener discoveryRegistryListener;
    private ComponentContext context;
    private final AtomicBoolean isComponentEnabled = new AtomicBoolean();;

    public UpnpDiscoveryService() {
        super(5);
    }

    @Activate
    protected void activate(ComponentContext context, Map<String, Object> configProperties) {
        super.activate(configProperties);
        this.context = context;
        startScan();
    }

    @Override
    @Modified
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Reference(cardinality = OPTIONAL)
    protected void setUpnpDiscoveryRegistryListener(UpnpDiscoveryRegistryListener discoveryRegistryListener) {
        this.discoveryRegistryListener = discoveryRegistryListener;
    }

    protected void unsetUpnpDiscoveryRegistryListener(UpnpDiscoveryRegistryListener discoveryRegistryListener) {
        this.discoveryRegistryListener = null;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        Set<ThingTypeUID> supportedThingTypes = new HashSet<>();
        if (isComponentEnabled.get()) {
            for (UpnpDiscoveryParticipant participant : discoveryRegistryListener.getParticipants()) {
                supportedThingTypes.addAll(participant.getSupportedThingTypeUIDs());
            }
        }
        return supportedThingTypes;
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (isComponentEnabled.get()) {
            discoveryRegistryListener.addRegistryListener();
        } else if (areDependenciesAvailable()) {
            enableComponent();
            discoveryRegistryListener.addRegistryListener();
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (isComponentEnabled.get()) {
            discoveryRegistryListener.removeRegistryListener();
        }
    }

    @Override
    protected void startScan() {
        if (isComponentEnabled.get()) {
            discoveryRegistryListener.upnpDeviceSearch();
        } else if (areDependenciesAvailable()) {
            enableComponent();
            discoveryRegistryListener.upnpDeviceSearch();
        }
    }

    @Override
    protected void stopScan() {
        if (isComponentEnabled.get()) {
            removeOlderResults(getTimestampOfLastScan());
            super.stopScan();
            if (!isBackgroundDiscoveryEnabled()) {
                discoveryRegistryListener.removeRegistryListener();
            }
        }
    }

    @Override
    public void thingDiscovered(DiscoveryResult discoveryResult) {
        super.thingDiscovered(discoveryResult);
    }

    @Override
    public void thingRemoved(ThingUID thingUID) {
        super.thingRemoved(thingUID);
    }

    /**
     * Checks for JUPnP dependencies that are specified as optional
     * dependencies to this bundle. Before operating on the class
     * that might not be available any point of time, it is required
     * to check its availability in the runtime.
     * 
     * @return {@code true} if all the JUPnP optional dependencies are available, otherwise {@code false}
     */
    private boolean areDependenciesAvailable() {
        try {
            Class.forName("org.jupnp.registry.RegistryListener");
            Class.forName("org.jupnp.UpnpService");
            Class.forName("org.jupnp.model.meta.LocalDevice");
            return true;
        } catch (ClassNotFoundException ex) {
            logger.warn("Cannot locate JUPnP Optional Dependency in runtime", ex);
            return false;
        }
    }

    private void enableComponent() {
        if (isComponentEnabled.compareAndSet(false, true)) {
            context.enableComponent("UpnpDiscoveryRegistryListener");
        }
    }

}
