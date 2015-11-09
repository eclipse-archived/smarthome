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
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.events.ThingEventFactory;
import org.eclipse.smarthome.core.thing.internal.ThingTracker.ThingTrackerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ThingRegistry}.
 *
 * @author Michael Grammling - Added dynamic configuration update
 * @author Simon Kaufmann - Added forceRemove
 * @author Chris Jackson - ensure thing added event is sent before linked events
 * @auther Thomas Höfer - Added config description validation exception to updateConfiguration operation
 */
public class ThingRegistryImpl extends AbstractRegistry<Thing, ThingUID>implements ThingRegistry {

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

    @Override
    public void updateConfiguration(ThingUID thingUID, Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        Thing thing = get(thingUID);
        if (thing != null) {
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler != null) {
                thingHandler.handleConfigurationUpdate(configurationParameters);
            } else {
                throw new IllegalStateException("Thing with UID " + thingUID + " has no handler attached.");
            }
        } else {
            throw new IllegalArgumentException("Thing with UID " + thingUID + " does not exists.");
        }
    }

    @Override
    public Thing forceRemove(ThingUID thingUID) {
        return super.remove(thingUID);
    }

    @Override
    public Thing remove(ThingUID thingUID) {
        Thing thing = get(thingUID);
        if (thing != null) {
            notifyTrackers(thing, ThingTrackerEvent.THING_REMOVING);
        }
        return thing;
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
        postEvent(ThingEventFactory.createAddedEvent(element));
        notifyTrackers(element, ThingTrackerEvent.THING_ADDED);
    }

    @Override
    protected void notifyListenersAboutRemovedElement(Thing element) {
        super.notifyListenersAboutRemovedElement(element);
        notifyTrackers(element, ThingTrackerEvent.THING_REMOVED);
        postEvent(ThingEventFactory.createRemovedEvent(element));
    }

    @Override
    protected void notifyListenersAboutUpdatedElement(Thing oldElement, Thing element) {
        super.notifyListenersAboutUpdatedElement(oldElement, element);
        notifyTrackers(element, ThingTrackerEvent.THING_UPDATED);
        postEvent(ThingEventFactory.createUpdateEvent(element, oldElement));
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
        // needed because the removed element was taken from the storage and lost its dynamic state
        preserveDynamicState(thing);
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
        // better call it explicitly here, even if it is called in onRemoveElement
        preserveDynamicState(thing);
        onRemoveElement(thing);
        onAddElement(thing);
    }

    private void preserveDynamicState(Thing thing) {
        final Thing existingThing = get(thing.getUID());
        if (existingThing != null) {
            thing.setHandler(existingThing.getHandler());
            thing.setStatusInfo(existingThing.getStatusInfo());
        }
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
                    case THING_REMOVING:
                        thingTracker.thingRemoving(thing, ThingTrackerEvent.THING_REMOVING);
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
