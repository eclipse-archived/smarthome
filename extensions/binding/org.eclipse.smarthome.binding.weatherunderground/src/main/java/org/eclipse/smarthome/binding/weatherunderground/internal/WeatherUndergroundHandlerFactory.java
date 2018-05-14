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
package org.eclipse.smarthome.binding.weatherunderground.internal;

import static org.eclipse.smarthome.binding.weatherunderground.WeatherUndergroundBindingConstants.*;

import java.util.Hashtable;
import java.util.Set;

import org.eclipse.smarthome.binding.weatherunderground.WeatherUndergroundBindingConstants;
import org.eclipse.smarthome.binding.weatherunderground.handler.WeatherUndergroundBridgeHandler;
import org.eclipse.smarthome.binding.weatherunderground.handler.WeatherUndergroundHandler;
import org.eclipse.smarthome.binding.weatherunderground.internal.discovery.WeatherUndergroundDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.collect.Sets;

/**
 * The {@link WeatherUndergroundHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Theo Giovanna - Added a bridge for the API key
 */
<<<<<<< HEAD
<<<<<<< HEAD
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.weatherunderground")
=======
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.weatherunderground")
>>>>>>> e39f3d9f5... Autodiscovery service added. Things are automatically configured with a bridge if existing
=======
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.weatherunderground")
>>>>>>> 70f066ddf... Resolved conflict
public class WeatherUndergroundHandlerFactory extends BaseThingHandlerFactory {

    private LocaleProvider localeProvider;
    private LocationProvider locationProvider;
    private UnitProvider unitProvider;

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    @Reference
    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    @Reference
    protected void setUnitProvider(final UnitProvider unitProvider) {
        this.unitProvider = unitProvider;
    }

    protected void unsetUnitProvider(final UnitProvider unitProvider) {
        this.unitProvider = null;
    }

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(BRIDGE_THING_TYPES_UIDS,
            WeatherUndergroundBindingConstants.SUPPORTED_THING_TYPES_UIDS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_WEATHER)) {
            return new WeatherUndergroundHandler(thing, localeProvider, unitProvider);
        }

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            WeatherUndergroundBridgeHandler handler = new WeatherUndergroundBridgeHandler((Bridge) thing);
            registerWeatherDiscoveryService(handler);
            return handler;
        }

        return null;
    }

    // adds the bridge to the discovery service
    private synchronized void registerWeatherDiscoveryService(WeatherUndergroundBridgeHandler bridgeHandler) {
        WeatherUndergroundDiscoveryService discoveryService = new WeatherUndergroundDiscoveryService(bridgeHandler,
                locationProvider);
        bridgeHandler.getDiscoveryServiceRegs().put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

}
