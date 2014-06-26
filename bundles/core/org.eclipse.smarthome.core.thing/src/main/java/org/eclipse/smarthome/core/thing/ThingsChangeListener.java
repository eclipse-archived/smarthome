/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;



/**
 * 
 * This is a listener interface which should be implemented wherever thing providers or
 * the thing registry are used in order to be notified of any dynamic changes in the provided things.
 * 
 * @author Oliver Libutzki - Initital contribution
 *
 */
public interface ThingsChangeListener {
	
    /**
     * Notifies the listener that a single thing has been added
     * 
     * @param provider
     *            the concerned thing provider
     * @param thing
     *            the thing that has been added
     */
	public void thingAdded(ThingProvider provider, Thing thing);
	
	/**
	 * Notifies the listener that a single thing has been removed
	 * 
	 * @param provider the concerned thing provider 
	 * @param thing the thing that has been removed
	 */
	public void thingRemoved(ThingProvider provider, Thing thing);
	
	/**
	 * Notifies the listener that a single thing has been updated
	 * 
	 * @param provider the concerned thing provider 
	 * @param thing the thing before update
	 * @param thing the thing after update
	 */
	public void thingUpdated(ThingProvider provider, Thing oldThing, Thing newThing);
}
