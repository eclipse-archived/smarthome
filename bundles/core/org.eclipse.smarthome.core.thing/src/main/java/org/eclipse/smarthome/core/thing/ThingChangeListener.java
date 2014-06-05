/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;


public interface ThingChangeListener {

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
}
