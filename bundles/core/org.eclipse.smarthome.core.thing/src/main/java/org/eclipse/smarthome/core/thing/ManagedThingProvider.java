/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageSelector;
import org.eclipse.smarthome.core.storage.StorageSelector.StorageSelectionListener;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.internal.Activator;
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
 * 
 */
public class ManagedThingProvider implements ThingProvider, StorageSelectionListener<Thing> {

    private final static Logger logger = LoggerFactory.getLogger(ManagedThingProvider.class);

    private Storage<Thing> storage;

    private StorageSelector<Thing> storageSelector;

    private List<ThingChangeListener> thingChangeListeners = new CopyOnWriteArrayList<>();

    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<>();

    public ManagedThingProvider() {
        this.storageSelector = new StorageSelector<>(Activator.getContext(), Thing.class.getName(),
                this);
    }

    /**
     * Adds a things and informs all listeners.
     * 
     * @param thing
     *            thing that should be added.
     */
    public void addThing(Thing thing) {
        logger.info("Adding thing to managed thing provider '{}'.", thing.getUID());
        Thing oldThing = storage.put(thing.getUID().toString(), thing);
        if (oldThing != null) {
            notifyThingChangeListenersAboutRemovedThing(oldThing);
        }
        notifyThingChangeListenersAboutAddedThing(thing);
    }

    @Override
    public void addThingChangeListener(ThingChangeListener listener) {
        thingChangeListeners.add(listener);
    }

    /**
     * Creates a thing based on the given configuration properties, adds it and
     * informs all listeners.
     * 
     * @param thingTypeUID
     *            thing type unique id
     * @param thingUID
     *            thing unique id which should be created. This id might be null.
     * @param bridge
     *            the thing's bridge. Null if there is no bridge or if the thing
     *            is a bridge by itself.
     * @param properties
     *            the configuration
     * @return the created thing
     */
    public Thing createThing(ThingTypeUID thingTypeUID, ThingUID thingUID, Bridge bridge,
            Configuration configuration) {
        logger.debug("Creating thing for type '{}'.", thingTypeUID);
        for (ThingHandlerFactory thingHandlerFactory : thingHandlerFactories) {
            if (thingHandlerFactory.supportsThingType(thingTypeUID)) {
                Thing thing = thingHandlerFactory.createThing(thingTypeUID, configuration, thingUID,
                        bridge);
                addThing(thing);
                return thing;
            }
        }
        logger.warn(
                "Cannot create thing. No binding found that supports creating a thing for the thing type {}.",
                thingTypeUID);
        return null;
    }

    @Override
    public Collection<Thing> getThings() {
        return storage.getValues();
    }

    /**
     * Removes a thing and informs all listeners.
     * 
     * @param uid
     *            UID of the thing that should be removed
     * @return thing that was removed or null if no thing was the given id
     *         exists
     */
    public Thing removeThing(ThingUID uid) {
        logger.debug("Removing thing from managed thing provider '{}'.", uid);
        Thing removedThing = storage.remove(uid.toString());
        if (removedThing != null) {
            notifyThingChangeListenersAboutRemovedThing(removedThing);
        }
        return removedThing;
    }

    @Override
    public void removeThingChangeListener(ThingChangeListener listener) {
        thingChangeListeners.remove(listener);
    }

    @Override
    public void storageSelected(Storage<Thing> storage) {
        this.storage = storage;
    }

    private void notifyThingChangeListenersAboutAddedThing(Thing thing) {
        for (ThingChangeListener thingChangeListener : this.thingChangeListeners) {
            thingChangeListener.thingAdded(this, thing);
        }
    }

    private void notifyThingChangeListenersAboutRemovedThing(Thing thing) {
        for (ThingChangeListener thingChangeListener : this.thingChangeListeners) {
            thingChangeListener.thingRemoved(this, thing);
        }
    }

    protected void addStorageService(StorageService storageService) {
        this.storageSelector.addStorageService(storageService);
    }

    protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.add(thingHandlerFactory);
    }

    protected void removeStorageService(StorageService storageService) {
        this.storageSelector.removeStorageService(storageService);
    }

    protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.remove(thingHandlerFactory);
    }

}
