/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

/**
 * A {@link ThingTracker} can be used to track added and removed things. In
 * contrast to the {@link ThingRegistryChangeListener} the method
 * {@link ThingTracker#thingAdded(Thing, ThingTrackerEvent)} is called for every
 * thing, although it was added before the tracker was registered.
 * 
 * @author Dennis Nobel - Initial contribution 
 */
public interface ThingTracker {

    public enum ThingTrackerEvent {
        THING_ADDED, THING_REMOVED, TRACKER_ADDED, TRACKER_REMOVED
    }

    /**
     * This method is called for every thing that exists in the
     * {@link ThingRegistry} and for every added thing.
     * 
     * @param thing
     *            thing
     * @param thingTrackerEvent
     *            thing tracker event
     */
    void thingAdded(Thing thing, ThingTrackerEvent thingTrackerEvent);

    /**
     * This method is called for every thing that was removed from the
     * {@link ThingRegistry}. Moreover the method is called for every thing,
     * that exists in the {@link ThingRegistry}, when the tracker is
     * unregistered.
     * 
     * @param thing
     *            thing
     * @param thingTrackerEvent
     *            thing tracker event
     */
    void thingRemoved(Thing thing, ThingTrackerEvent thingTrackerEvent);

}
