/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.thing.ThingTracker.ThingTrackerEvent;
import org.eclipse.smarthome.core.thing.binding.ThingTypeChangeListener;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * {@link ThingRegistry} tracks all {@link Thing}s from different
 * {@link ThingProvider}s and provides access to them. The {@link ThingRegistry}
 * supports adding of listeners (see {@link ThingChangeListener}) and trackers
 * (see {@link ThingTracker}).
 * 
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Extracted ManagedThingProvider
 */
public class ThingRegistry implements ThingChangeListener, ThingTypeChangeListener {

    private Logger logger = LoggerFactory.getLogger(ThingRegistry.class
			.getName());
    private Collection<ThingRegistryChangeListener> thingListeners = new CopyOnWriteArraySet<>();

	private Map<ThingProvider, Collection<Thing>> thingMap = new ConcurrentHashMap<>();
	
    private List<ThingTracker> thingTrackers = new CopyOnWriteArrayList<>();

	private Map<ThingTypeProvider, Collection<ThingType>> thingTypeMap = new ConcurrentHashMap<>();

    /**
     * Adds a thing registry change listener.
     * 
     * @param listener
     *            the listener
     */
	public void addThingRegistryChangeListener(ThingRegistryChangeListener listener) {
		thingListeners.add(listener);
	}

    /**
     * Adds a thing tracker.
     * 
     * @param thingTracker
     *            the thing tracker
     */
	public void addThingTracker(ThingTracker thingTracker) {
		notifyListenerAboutAllThingsAdded(thingTracker);
		thingTrackers.add(thingTracker);
	}

    /**
     * Returns a thing for a given UID or null if no thing was found.
     * 
     * @param uid
     *            thing UID
     * @return thing for a given UID or null if no thing was found
     */
	public Thing getByUID(String uid) {
        for (Thing thing : getThings()) {
			if (thing.getUID().toString().equals(uid)) {
				return thing;
			}
		}
		return null;
	}

    /**
     * Returns all things.
     * 
     * @return all things
     */
	public List<Thing> getThings() {
        return Lists.newArrayList(Iterables.concat(thingMap.values()));
	}

    /**
     * Returns all thing types.
     * 
     * @return all thing types
     */
	public List<ThingType> getThingTypes() {
        return Collections.unmodifiableList(Lists.newArrayList(Iterables.concat(thingTypeMap
                .values())));
    }

    /**
     * Returns thing types for a given binding id.
     * 
     * @param bindingId
     *            binding id
     * @return thing types for given binding id
     */
	public List<ThingType> getThingTypes(String bindingId) {
        List<ThingType> thingTypesForBinding = Lists.newArrayList();

        for (ThingType thingType : getThingTypes()) {
            if (thingType.getBindingId().equals(bindingId)) {
                thingTypesForBinding.add(thingType);
            }
        }

        return thingTypesForBinding;
    }

    /**
     * Returns a thing type for a given thing type UID.
     * 
     * @param thingTypeUID
     *            thing type UID
     * @return thing type for given UID or null if no thing type with this UID
     *         was found
     */
    public ThingType getThingType(ThingTypeUID thingTypeUID) {

        for (ThingType thingType : getThingTypes()) {
            if (thingType.getUID().equals(thingTypeUID)) {
                return thingType;
            }
        }

        return null;
    }

    /**
     * Removes a thing registry change listener.
     * 
     * @param listener
     *            the listener
     */
	public void removeThingRegistryChangeListener(ThingRegistryChangeListener listener) {
		thingListeners.remove(listener);
	}

    /**
     * Removes a thing tracker.
     * 
     * @param thingTracker
     *            the thing tracker
     */
	public void removeThingTracker(ThingTracker thingTracker) {
		notifyListenerAboutAllThingsRemoved(thingTracker);
		thingTrackers.remove(thingTracker);
	}

	@Override
	public void thingAdded(ThingProvider provider, Thing thing) {
		Collection<Thing> things = thingMap.get(provider);
		if (things != null) {
			things.add(thing);
            notifyListenersAboutAddedThing(thing);
		}
	}

	@Override
	public void thingRemoved(ThingProvider provider, Thing thing) {
		Collection<Thing> things = thingMap.get(provider);
		if(things != null) {
			things.remove(thing);
            notifyListenersAboutRemovedThing(thing);
		}
	}

	@Override
    public void thingTypeAdded(ThingTypeProvider provider, ThingType thingType) {
        Collection<ThingType> thingTypes = thingTypeMap.get(provider);
        if (thingTypes != null) {
            thingTypes.add(thingType);
        }
    }
	
	@Override
    public void thingTypeRemoved(ThingTypeProvider provider, Thing thingType) {
        Collection<ThingType> thingTypes = thingTypeMap.get(provider);
        if (thingTypes != null) {
            thingTypes.remove(thingType);
        }
    }
	
	private void notifyListenerAboutAllThingsAdded(ThingTracker thingTracker) {
        for (Thing thing : getThings()) {
			thingTracker.thingAdded(thing, ThingTrackerEvent.TRACKER_ADDED);
		}
	}
	
    private void notifyListenerAboutAllThingsRemoved(ThingTracker thingTracker) {
        for (Thing thing : getThings()) {
			thingTracker.thingRemoved(thing, ThingTrackerEvent.TRACKER_REMOVED);
		}
	}
	
    private void notifyListenersAboutAddedThing(Thing thing) {
		for (ThingTracker thingTracker : thingTrackers) {
			thingTracker.thingAdded(thing, ThingTrackerEvent.THING_ADDED);
		}
		for (ThingRegistryChangeListener listener : thingListeners) {
			listener.thingAdded(thing);
		}
	}

    private void notifyListenersAboutRemovedThing(Thing thing) {
		for (ThingTracker thingTracker : thingTrackers) {
			thingTracker.thingRemoved(thing, ThingTrackerEvent.THING_REMOVED);
		}
		for (ThingRegistryChangeListener listener : thingListeners) {
			listener.thingRemoved(thing);
		}
	}

    protected void addThingProvider(ThingProvider thingProvider) {
		// only add this provider if it does not already exist
		if(!thingMap.containsKey(thingProvider)) {
			Collection<Thing> things = new CopyOnWriteArraySet<Thing>(thingProvider.getThings());
			thingProvider.addThingChangeListener(this);
			thingMap.put(thingProvider, things);
            logger.debug("Thing provider '{}' has been added.", thingProvider.getClass().getName());
		}
	}

    protected void addThingTypeProvider(ThingTypeProvider thingTypeProvider) {
        // only add this provider if it does not already exist
        if (!thingTypeMap.containsKey(thingTypeProvider)) {
            Collection<ThingType> thingTypes = new CopyOnWriteArraySet<>(
                    thingTypeProvider.getThingTypes());
            thingTypeProvider.addThingTypeChangeListener(this);
            thingTypeMap.put(thingTypeProvider, thingTypes);
            logger.debug("Thing type provider '{}' has been added.", thingTypeProvider.getClass()
                    .getName());
        }
    }

    protected void deactivate(ComponentContext componentContext) {
		for (ThingTracker thingTracker : thingTrackers) {
			removeThingTracker(thingTracker);
		}
	}

    protected void removeThingProvider(ThingProvider thingProvider) {
		if(thingMap.containsKey(thingProvider)) {
			thingMap.remove(thingProvider);
			thingProvider.removeThingChangeListener(this);
            logger.debug("Thing provider '{}' has been removed.", thingProvider.getClass()
                    .getName());
		}
	}

    protected void removeThingTypeProvider(ThingTypeProvider thingTypeProvider) {
        if (thingTypeMap.containsKey(thingTypeProvider)) {
            thingTypeMap.remove(thingTypeProvider);
            thingTypeProvider.removeThingTypeChangeListener(this);
            logger.debug("Thing type provider '{}' has been removed.", thingTypeProvider.getClass()
                    .getName());
        }
    }
}
