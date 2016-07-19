/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistryChangeListener;

/**
 * A {@link ThingTracker} can be used to track added and removed things. In
 * contrast to the {@link ThingRegistryChangeListener} the method
 * {@link ThingTracker#thingAdded(Thing, ThingTrackerEvent)} is called for every
 * thing, although it was added before the tracker was registered.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Added dynamic configuration update
 * @author Simon Kaufmann - Added THING_REMOVING state
 */
public interface ThingTracker {

    public enum ThingTrackerEvent {
        THING_ADDED, THING_REMOVING, THING_REMOVED, THING_UPDATED, TRACKER_ADDED, TRACKER_REMOVED
    }

    /**
     * This method is called for every thing that exists in the {@link ThingRegistryImpl} and for every added thing.
     *
     * @param thing the thing which was added
     * @param thingTrackerEvent the event that occurred
     */
    void thingAdded(Thing thing, ThingTrackerEvent thingTrackerEvent);

    /**
     * This method is called for every thing that is going to be removed from the {@link ThingRegistryImpl}. Moreover the method is
     * called for every thing,
     * that exists in the {@link ThingRegistryImpl}, when the tracker is
     * unregistered.
     *
     * @param thing the thing which was removed
     * @param thingTrackerEvent the event that occurred
     */
    void thingRemoving(Thing thing, ThingTrackerEvent thingTrackerEvent);

    /**
     * This method is called for every thing that was removed from the {@link ThingRegistryImpl}. Moreover the method is
     * called for every thing,
     * that exists in the {@link ThingRegistryImpl}, when the tracker is
     * unregistered.
     *
     * @param thing the thing which was removed
     * @param thingTrackerEvent the event that occurred
     */
    void thingRemoved(Thing thing, ThingTrackerEvent thingTrackerEvent);

    /**
     * This method is called for every thing that was updated within the {@link ThingRegistryImpl}.
     *
     * @param thing the thing which was updated
     * @param thingTrackerEvent the event that occurred
     */
    void thingUpdated(Thing thing, ThingTrackerEvent thingTrackerEvent);

}
