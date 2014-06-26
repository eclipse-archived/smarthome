/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingRegistryChangeListener;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.ThingsChangeListener;
import org.eclipse.smarthome.core.thing.internal.ThingTracker.ThingTrackerEvent;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Default implementation of {@link ThingRegistry}.
 */
public class ThingRegistryImpl implements ThingsChangeListener, ThingRegistry {

    private Logger logger = LoggerFactory.getLogger(ThingRegistryImpl.class
			.getName());
    private Collection<ThingRegistryChangeListener> thingListeners = new CopyOnWriteArraySet<>();

	private Map<ThingProvider, Collection<Thing>> thingMap = new ConcurrentHashMap<>();
	
    private List<ThingTracker> thingTrackers = new CopyOnWriteArrayList<>();



    /* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.thing.ThingRegistry#addThingRegistryChangeListener(org.eclipse.smarthome.core.thing.ThingRegistryChangeListener)
	 */
	@Override
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

    /* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.thing.ThingRegistry#getByUID(java.lang.String)
	 */
	@Override
	public Thing getByUID(ThingUID uid) {
        for (Thing thing : getThings()) {
			if (thing.getUID().equals(uid)) {
				return thing;
			}
		}
		return null;
	}

    /* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.thing.ThingRegistry#getThings()
	 */
	@Override
	public List<Thing> getThings() {
        return Lists.newArrayList(Iterables.concat(thingMap.values()));
	}





    /* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.thing.ThingRegistry#removeThingRegistryChangeListener(org.eclipse.smarthome.core.thing.ThingRegistryChangeListener)
	 */
	@Override
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
            // TODO some cleanup in order to remove dead references
            thing.setBridge(null);
            if (thing instanceof Bridge) {
            	List<Thing> children = ((Bridge)thing).getThings();
            	for (Thing child : children) {
					child.setBridge(null);
				}
            }
		}
	}

	@Override
	public void thingUpdated(ThingProvider provider, Thing oldThing, Thing newThing) {
		thingRemoved(provider, oldThing);
		thingAdded(provider, newThing);
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
			thingProvider.addThingsChangeListener(this);
			thingMap.put(thingProvider, things);
            logger.debug("Thing provider '{}' has been added.", thingProvider.getClass().getName());
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
			thingProvider.removeThingsChangeListener(this);
            logger.debug("Thing provider '{}' has been removed.", thingProvider.getClass()
                    .getName());
		}
	}



//	@Override
//	public void allThingsChanged(ThingProvider provider,
//			Collection<ThingUID> selectedOldThingUIDs) {
//		if(selectedOldThingUIDs==null || selectedOldThingUIDs.isEmpty()) {
//			selectedOldThingUIDs = new HashSet<ThingUID>();
//            Collection<Thing> oldThings;
//            oldThings = thingMap.get(provider);
//			if(oldThings!=null && oldThings.size() > 0) {
//				for(Thing oldThing : oldThings) {
//					selectedOldThingUIDs.add(oldThing.getUID());
//				}
//			}
//		}
//
//		Collection<Thing> things = new CopyOnWriteArrayList<>();
//    	thingMap.put(provider, things);
//    	things.addAll(provider.getThings());
//
//		for(ThingRegistryChangeListener listener : thingListeners) {
//			listener.allItemsChanged(oldItemNames);
//		}
//	}

}
