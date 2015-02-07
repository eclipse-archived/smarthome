/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ManagedThingProvider} is an OSGi service, that allows to add or remove
 * things at runtime by calling {@link ManagedThingProvider#addThing(Thing)} or
 * {@link ManagedThingProvider#removeThing(Thing)}. An added thing is
 * automatically exposed to the {@link ThingRegistry}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Dennis Nobel - Integrated Storage
 * @author Michael Grammling - Added dynamic configuration update
 */
public class ManagedThingProvider extends DefaultAbstractManagedProvider<Thing, ThingUID> implements ThingProvider {

    private final Logger logger = LoggerFactory.getLogger(ManagedThingProvider.class);

    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<>();

    /**
     * Creates a thing based on the given configuration properties, adds it and
     * informs all listeners.
     *
     * @param thingTypeUID
     *            thing type unique id
     * @param thingUID
     *            thing unique id which should be created. This id might be
     *            null.
     * @param bridge
     *            the thing's bridge. Null if there is no bridge or if the thing
     *            is a bridge by itself.
     * @param properties
     *            the configuration
     * @return the created thing
     */
    public Thing createThing(ThingTypeUID thingTypeUID, ThingUID thingUID, ThingUID bridgeUID,
            Configuration configuration) {
        logger.debug("Creating thing for type '{}'.", thingTypeUID);
        for (ThingHandlerFactory thingHandlerFactory : thingHandlerFactories) {
            if (thingHandlerFactory.supportsThingType(thingTypeUID)) {
                Thing thing = thingHandlerFactory.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
                add(thing);
                return thing;
            }
        }
        logger.warn("Cannot create thing. No binding found that supports creating a thing" + " of type {}.",
                thingTypeUID);
        return null;
    }

    protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.add(thingHandlerFactory);
    }

    protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.remove(thingHandlerFactory);
    }

    @Override
    protected ThingUID getKey(Thing thing) {
        return thing.getUID();
    }

    @Override
    protected String getStorageName() {
        return Thing.class.getName();
    }

    @Override
    protected String keyToString(ThingUID key) {
        return key.toString();
    }

}
