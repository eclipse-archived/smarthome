/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Collection;

/**
 * The {@link ThingProvider} is responsible for providing things.
 * 
 * @author Oliver Libutzki - Initial contribution
 *
 */
public interface ThingProvider {

	/**
	 * Provides a collection of things
	 * @return the things provided by the {@link ThingProvider}
	 */
	Collection<Thing> getThings();
	
	/**
	 * Adds a {@link ThingChangeListener} which is notified if there are changes concerning the things provides by the {@link ThingProvider}.
	 * @param listener The listener to be added
	 */
	public void addThingChangeListener(ThingChangeListener listener);
	
	/**
	 * Removes a {@link ThingChangeListener} which is notified if there are changes concerning the things provides by the {@link ThingProvider}.
	 * @param listener The listener to be removed.
	 */
	public void removeThingChangeListener(ThingChangeListener listener);
}
