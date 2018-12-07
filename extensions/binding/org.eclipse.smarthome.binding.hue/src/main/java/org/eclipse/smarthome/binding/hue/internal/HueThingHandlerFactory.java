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
package org.eclipse.smarthome.binding.hue.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.HueBindingConstants;
import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.handler.HueLightHandler;
import org.eclipse.smarthome.binding.hue.internal.utils.AsyncHttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link HueThingHandlerFactory} is a factory for {@link HueBridgeHandler}s.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Kai Kreuzer - added supportsThingType method
 * @author Andre Fuechsel - implemented to use one discovery service per bridge
 *
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hue")
public class HueThingHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .concat(HueBridgeHandler.SUPPORTED_THING_TYPES.stream(), HueBindingConstants.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());

    private @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @NonNullByDefault({}) AsyncHttpClient asyncHttpClient;

    @Reference
    public void setHttpClientFactory(HttpClientFactory factory) {
        httpClientFactory = factory;
        asyncHttpClient = new AsyncHttpClient(httpClientFactory.getCommonHttpClient(), 5000);
    }

    public void unsetHttpClientFactory(HttpClientFactory factory) {
        httpClientFactory = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (HueBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new HueBridgeHandler((Bridge) thing, asyncHttpClient);
        } else if (HueBindingConstants.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new HueLightHandler(thing);
        } else {
            return null;
        }
    }
}
