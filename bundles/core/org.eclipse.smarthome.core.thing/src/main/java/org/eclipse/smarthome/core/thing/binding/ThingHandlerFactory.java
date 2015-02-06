/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * {@link ThingHandlerFactory} is responsible for creating {@link Thing}s and
 * registering {@link ThingHandler}s. Therefore it must be registered as OSGi
 * service.
 *
 * @author Dennis Nobel - Initial contribution
 */
public interface ThingHandlerFactory {

    /**
     * Returns whether the handler is able to create a thing or register a thing
     * handler for the given type.
     *
     * @param thingTypeUID
     *            thing type
     * @return true, if the handler supports the thing type, false otherwise
     */
    boolean supportsThingType(ThingTypeUID thingTypeUID);

    /**
     * This method is called, if the {@link ThingHandlerFactory} supports the
     * type of the given thing. A {@link ThingHandler} must be registered as
     * OSGi service for the given thing.
     *
     * @param thing
     *            thing for which a new handler must be registered
     */
    void registerHandler(Thing thing);

    /**
     * This method is called, when a {@link ThingHandler} must be unregistered.
     *
     * @param thing
     *            thing, which handler must be unregistered
     */
    void unregisterHandler(Thing thing);

    /**
     * Creates a thing for given arguments.
     *
     * @param thingTypeUID
     *            thing type uid (not null)
     * @param configuration
     *            configuration
     * @param thingUID
     *            thing uid, which can be null
     * @param bridgeUID
     *            bridge uid, which can be null
     * @return thing
     */
    Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID, ThingUID bridgeUID);

    /**
     * A thing with the given {@link Thing} UID was removed.
     *
     * @param thingUID
     *            thing UID of the removed object
     */
    void removeThing(ThingUID thingUID);

}
