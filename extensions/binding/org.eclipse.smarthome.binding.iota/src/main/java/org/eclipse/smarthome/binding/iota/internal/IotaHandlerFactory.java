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
package org.eclipse.smarthome.binding.iota.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.iota.IotaBindingConstants;
import org.eclipse.smarthome.binding.iota.handler.IotaBridgeHandler;
import org.eclipse.smarthome.binding.iota.handler.IotaTopicThingHandler;
import org.eclipse.smarthome.binding.iota.handler.IotaWalletThingHandler;
import org.eclipse.smarthome.binding.iota.handler.TransformationServiceProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link IotaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Theo Giovanna - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, name = "IotaHandlerFactory")
public class IotaHandlerFactory extends BaseThingHandlerFactory implements TransformationServiceProvider {

    private final IotaUtilsImpl utils = new IotaUtilsImpl();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(IotaBindingConstants.BRIDGE_THING_TYPES_UIDS, IotaBindingConstants.SUPPORTED_THING_TYPES_UIDS)
            .flatMap(x -> x.stream()).collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (IotaBindingConstants.THING_TYPE_IOTA_TOPIC.equals(thingTypeUID)) {
            return new IotaTopicThingHandler(thing, this);
        }
        if (IotaBindingConstants.THING_TYPE_IOTA_WALLET.equals(thingTypeUID)) {
            return new IotaWalletThingHandler(thing);
        }
        if (IotaBindingConstants.THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new IotaBridgeHandler((Bridge) thing, utils);
        }
        return null;
    }

    @Override
    public TransformationService getTransformationService(@NonNull String type) {
        return TransformationHelper.getTransformationService(bundleContext, type);
    }
}
