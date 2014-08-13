/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

/**
 * {@link ThingRegistryChangeListener} can be implemented to listen for things
 * beeing added or removed. The listener must be added and removed via
 * {@link ThingRegistry#addThingRegistryChangeListener(ThingRegistryChangeListener)}
 * and {@link ThingRegistry#removeThingRegistryChangeListener(ThingRegistryChangeListener)}.
 * 
 * @author Dennis Nobel - Initial Contribution
 * @author Michael Grammling - Added dynamic configuration update
 * 
 * @see ThingRegistry
 */
public interface ThingRegistryChangeListener {

    /**
     * Notifies the listener that a single thing has been added.
     * 
     * @param thing the thing that has been added
     */
    public void thingAdded(Thing thing);

    /**
     * Notifies the listener that a single thing has been removed.
     * 
     * @param thing the thing that has been removed
     */
    public void thingRemoved(Thing thing);

    /**
     * Notifies the listener that a single thing has been updated.
     * 
     * @param thing the thing that has been updated
     */
    public void thingUpdated(Thing thing);

}
