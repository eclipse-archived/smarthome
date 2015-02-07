/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.internal.ThingTracker.ThingTrackerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ThingRegistry}.
 *
 * @author Michael Grammling - Added dynamic configuration update
 */
public class ThingRegistryImpl extends AbstractRegistry<Thing, ThingUID> implements ThingRegistry {

    private Logger logger = LoggerFactory.getLogger(ThingRegistryImpl.class.getName());

    private List<ThingTracker> thingTrackers = new CopyOnWriteArrayList<>();

    /**
     * Adds a thing tracker.
     *
     * @param thingTracker
     *            the thing tracker
     */
    public void addThingTracker(ThingTracker thingTracker) {
        notifyTrackerAboutAllThingsAdded(thingTracker);
        thingTrackers.add(thingTracker);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.ThingRegistry#getByUID(java.lang.String)
     */
    @Override
    public Thing get(ThingUID uid) {
        for (Thing thing : getAll()) {
            if (thing.getUID().equals(uid)) {
                return thing;
            }
        }
        return null;
    }

    /**
     * Removes a thing tracker.
     *
     * @param thingTracker
     *            the thing tracker
     */
    public void removeThingTracker(ThingTracker thingTracker) {
        notifyTrackerAboutAllThingsRemoved(thingTracker);
        thingTrackers.remove(thingTracker);
    }

    @Override
    protected void notifyListenersAboutAddedElement(Thing element) {
        super.notifyListenersAboutAddedElement(element);
        notifyTrackers(element, ThingTrackerEvent.THING_ADDED);
    }

    @Override
    protected void notifyListenersAboutRemovedElement(Thing element) {
        super.notifyListenersAboutRemovedElement(element);
        notifyTrackers(element, ThingTrackerEvent.THING_REMOVED);
    }

    @Override
    protected void notifyListenersAboutUpdatedElement(Thing oldElement, Thing element) {
        super.notifyListenersAboutUpdatedElement(oldElement, element);
        notifyTrackers(element, ThingTrackerEvent.THING_UPDATED);
    }

    @Override
    protected void onAddElement(Thing thing) throws IllegalArgumentException {
        addThingToBridge(thing);
        if (thing instanceof Bridge) {
            addThingsToBridge((Bridge) thing);
        }
    }

    @Override
    protected void onRemoveElement(Thing thing) {
        ThingUID bridgeUID = thing.getBridgeUID();
        if (bridgeUID != null) {
            Thing bridge = this.get(bridgeUID);
            if (bridge instanceof BridgeImpl) {
                ((BridgeImpl) bridge).removeThing(thing);
            }
        }
    }

    @Override
    protected void onUpdateElement(Thing oldThing, Thing thing) {
        onRemoveElement(thing);
        onAddElement(thing);
    }

    private void addThingsToBridge(Bridge bridge) {
        Collection<Thing> things = getAll();
        for (Thing thing : things) {
            ThingUID bridgeUID = thing.getBridgeUID();
            if (bridgeUID != null && bridgeUID.equals(bridge.getUID())) {
                if (bridge instanceof BridgeImpl && !bridge.getThings().contains(thing)) {
                    ((BridgeImpl) bridge).addThing(thing);
                }
            }
        }
    }

    private void addThingToBridge(Thing thing) {
        ThingUID bridgeUID = thing.getBridgeUID();
        if (bridgeUID != null) {
            Thing bridge = this.get(bridgeUID);
            if (bridge instanceof BridgeImpl && !((Bridge) bridge).getThings().contains(thing)) {
                ((BridgeImpl) bridge).addThing(thing);
            }
        }
    }

    private void notifyTrackers(Thing thing, ThingTrackerEvent event) {
        for (ThingTracker thingTracker : thingTrackers) {
            try {
                switch (event) {
                    case THING_ADDED:
                        thingTracker.thingAdded(thing, ThingTrackerEvent.THING_ADDED);
                        break;
                    case THING_REMOVED:
                        thingTracker.thingRemoved(thing, ThingTrackerEvent.THING_REMOVED);
                        break;
                    case THING_UPDATED:
                        thingTracker.thingUpdated(thing, ThingTrackerEvent.THING_UPDATED);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                logger.error("Could not inform the ThingTracker '" + thingTracker + "' about the '" + event.name()
                        + "' event!", ex);
            }
        }
    }

    private void notifyTrackerAboutAllThingsAdded(ThingTracker thingTracker) {
        for (Thing thing : getAll()) {
            thingTracker.thingAdded(thing, ThingTrackerEvent.TRACKER_ADDED);
        }
    }

    private void notifyTrackerAboutAllThingsRemoved(ThingTracker thingTracker) {
        for (Thing thing : getAll()) {
            thingTracker.thingRemoved(thing, ThingTrackerEvent.TRACKER_REMOVED);
        }
    }

}
