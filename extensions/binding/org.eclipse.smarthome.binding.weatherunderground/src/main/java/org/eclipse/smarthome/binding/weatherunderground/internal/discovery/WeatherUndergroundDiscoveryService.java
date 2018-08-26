/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.weatherunderground.internal.discovery;

import static org.eclipse.smarthome.binding.weatherunderground.WeatherUndergroundBindingConstants.*;
import static org.eclipse.smarthome.binding.weatherunderground.internal.config.WeatherUndergroundConfiguration.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.weatherunderground.handler.WeatherUndergroundHandler;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherUndergroundDiscoveryService} creates things based on the configured location.
 *
 * @author Laurent Garnier - Initial Contribution
 * @author Laurent Garnier - Consider locale (language) when discovering a new thing
 */
public class WeatherUndergroundDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(WeatherUndergroundDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_WEATHER);
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;

    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;
    private ScheduledFuture<?> discoveryJob;
    private PointType previousLocation;

    private final ThingUID bridgeUID;

    /**
     * Creates a WeatherUndergroundDiscoveryService with enabled autostart.
     */

    public WeatherUndergroundDiscoveryService(ThingUID bridgeUID, LocaleProvider localeProvider,
            LocationProvider locationProvider) {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS, true);
        this.bridgeUID = bridgeUID;
        this.localeProvider = localeProvider;
        this.locationProvider = locationProvider;
    }

    @Override
    public void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Weather Underground discovery scan");
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }
        createResults(location);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Weather Underground device background discovery");
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                PointType currentLocation = locationProvider.getLocation();
                if (!Objects.equals(currentLocation, previousLocation)) {
                    logger.debug("Location has been changed from {} to {}: Creating new discovery results",
                            previousLocation, currentLocation);
                    createResults(currentLocation);
                    previousLocation = currentLocation;
                }
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL, TimeUnit.SECONDS);
            logger.debug("Scheduled Weather Underground location-changed job every {} seconds",
                    LOCATION_CHANGED_CHECK_INTERVAL);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Weather Underground device background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
            logger.debug("Stopped Weather Underground device background discovery");
        }
    }

    public void createResults(PointType location) {
        ThingUID localWeatherThing = new ThingUID(THING_TYPE_WEATHER, LOCAL);
        Map<String, Object> properties = new HashMap<>(3);
        properties.put(LOCATION, String.format("%s,%s", location.getLatitude(), location.getLongitude()));
        String language = WeatherUndergroundHandler.getCodeFromLanguage(localeProvider.getLocale().getLanguage());
        if (!language.isEmpty()) {
            properties.put(LANGUAGE, language);
        }
        thingDiscovered(DiscoveryResultBuilder.create(localWeatherThing).withLabel("Local Weather")
                .withProperties(properties).withBridge(bridgeUID).build());
    }

}
