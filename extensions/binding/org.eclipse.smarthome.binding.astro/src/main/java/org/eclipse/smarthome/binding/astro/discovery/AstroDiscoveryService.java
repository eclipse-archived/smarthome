/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.discovery;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.scheduler.CronExpression;
import org.eclipse.smarthome.core.scheduler.CronHelper;
import org.eclipse.smarthome.core.scheduler.Expression;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
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
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;
    private LocationProvider locationProvider;
    private final ExpressionThreadPoolExecutor scheduledExecutor;
    private Runnable backgroundJob;

    private static ThingUID SUN_THING = new ThingUID(THING_TYPE_SUN, LOCAL);
    private static ThingUID MOON_THING = new ThingUID(THING_TYPE_MOON, LOCAL);

    /**
     * Creates a AstroDiscoveryService with enabled autostart.
     */
    public AstroDiscoveryService() {
        super(new HashSet<>(Arrays.asList(new ThingTypeUID(BINDING_ID, "-"))), DISCOVER_TIMEOUT_SECONDS, true);

        scheduledExecutor = ExpressionThreadPoolManager.getExpressionScheduledPool("astro");
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Astro discovery scan");
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }
        createResults(location);
    }

    @Override
    protected void startBackgroundDiscovery() {
        Expression expression = null;
        try {
            expression = new CronExpression(
                    CronHelper.createCronForRepeatEverySeconds(LOCATION_CHANGED_CHECK_INTERVAL));
        } catch (ParseException e) {
            return;
        }
        if (expression != null && backgroundJob == null) {
            AstroDiscoveryLocationChangedTask job = new AstroDiscoveryLocationChangedTask(this);
            scheduledExecutor.schedule(job, expression);
            logger.info("Scheduled astro location-changed job every {} seconds", LOCATION_CHANGED_CHECK_INTERVAL);
            backgroundJob = job;
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (backgroundJob != null) {
            scheduledExecutor.remove(backgroundJob);
            backgroundJob = null;
        }
    }

    public void createResults(PointType location) {
        String propGeolocation;
        if (location.getAltitude() != null) {
            propGeolocation = String.format("%s,%s,%s", location.getLatitude(), location.getLongitude(),
                    location.getAltitude());
        } else {
            propGeolocation = String.format("%s,%s", location.getLatitude(), location.getLongitude());
        }
        thingDiscovered(DiscoveryResultBuilder.create(SUN_THING).withLabel("Local Sun")
                .withProperty("geolocation", propGeolocation).build());
        thingDiscovered(DiscoveryResultBuilder.create(MOON_THING).withLabel("Local Moon")
                .withProperty("geolocation", propGeolocation).build());
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    protected void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    protected void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }

}
