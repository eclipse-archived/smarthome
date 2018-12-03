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
package org.eclipse.smarthome.binding.openweathermap.internal.discovery;

import static org.eclipse.smarthome.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.openweathermap.internal.handler.AbstractOpenWeatherMapHandler;
import org.eclipse.smarthome.binding.openweathermap.internal.handler.OpenWeatherMapAPIHandler;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWeatherMapDiscoveryService} creates things based on the configured location.
 *
 * @author Christoph Weitkamp - Initial Contribution
 */
@NonNullByDefault
public class OpenWeatherMapDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 2;
    private static final int DISCOVERY_INTERVAL_SECONDS = 60;
    private @Nullable ScheduledFuture<?> discoveryJob;
    private final LocationProvider locationProvider;
    private @Nullable PointType previousLocation;

    private final OpenWeatherMapAPIHandler bridgeHandler;

    /**
     * Creates an OpenWeatherMapLocationDiscoveryService.
     */
    public OpenWeatherMapDiscoveryService(OpenWeatherMapAPIHandler bridgeHandler, LocationProvider locationProvider,
            LocaleProvider localeProvider, TranslationProvider i18nProvider) {
        super(AbstractOpenWeatherMapHandler.SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS);
        this.bridgeHandler = bridgeHandler;
        this.locationProvider = locationProvider;
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
        activate(null);
    }

    @Override
    protected void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime(), bridgeHandler.getThing().getUID());
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Start manual OpenWeatherMap Location discovery scan.");
        scanForNewLocation();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual OpenWeatherMap Location discovery scan.");
        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            logger.debug("Start OpenWeatherMap Location background discovery job at interval {} s.",
                    DISCOVERY_INTERVAL_SECONDS);
            discoveryJob = scheduler.scheduleWithFixedDelay(this::scanForNewLocation, 0, DISCOVERY_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            logger.debug("Stop OpenWeatherMap Location background discovery job.");
            if (discoveryJob.cancel(true)) {
                discoveryJob = null;
            }
        }
    }

    private void scanForNewLocation() {
        PointType currentLocation = locationProvider.getLocation();
        if (currentLocation == null) {
            logger.debug("Location is not set -> Will not provide any discovery results.");
        } else if (!Objects.equals(currentLocation, previousLocation)) {
            logger.debug("Location has been changed from {} to {} -> Creating new discovery results.", previousLocation,
                    currentLocation);
            createResults(currentLocation);
            previousLocation = currentLocation;
        } else {
            createResults(currentLocation);
        }
    }

    private void createResults(PointType location) {
        String locationString = location.toFullString();
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        createWeatherAndForecastResult(locationString, bridgeUID);
    }

    private void createWeatherAndForecastResult(String location, ThingUID bridgeUID) {
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_WEATHER_AND_FORECAST, bridgeUID, LOCAL))
                .withLabel("Local weather and forecast").withProperty(CONFIG_LOCATION, location)
                .withRepresentationProperty(CONFIG_LOCATION).withBridge(bridgeUID).build());
    }
}
