/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.discovery;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.smarthome.binding.astro.AstroBindingConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AstroDiscoveryService} creates things based on the configured location.
 *
 * @author Gerhard Riegler - Initial Contribution
 * @author Stefan Triller - Use configured location
 */
public class AstroDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(AstroDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private LocationProvider locationProvider;

    /**
     * Creates a AstroDiscoveryService with disabled autostart.
     */
    public AstroDiscoveryService() {
        super(new HashSet<>(Arrays.asList(new ThingTypeUID(BINDING_ID, "-"))), DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Astro discovery scan");

        PointType location = locationProvider.getLocation();

        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }

        ThingUID sunThing = new ThingUID(AstroBindingConstants.THING_TYPE_SUN, LOCAL);
        ThingUID moonThing = new ThingUID(AstroBindingConstants.THING_TYPE_MOON, LOCAL);

        String propGeolocation;
        if (location.getAltitude() != null) {
            propGeolocation = String.format("%s,%s,%s", location.getLatitude(), location.getLongitude(),
                    location.getAltitude());
        } else {
            propGeolocation = String.format("%s,%s", location.getLatitude(), location.getLongitude());
        }
        thingDiscovered(DiscoveryResultBuilder.create(sunThing).withLabel("Local Sun")
                .withProperty("geolocation", propGeolocation).build());
        thingDiscovered(DiscoveryResultBuilder.create(moonThing).withLabel("Local Moon")
                .withProperty("geolocation", propGeolocation).build());
    }

    protected void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    protected void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }

}
