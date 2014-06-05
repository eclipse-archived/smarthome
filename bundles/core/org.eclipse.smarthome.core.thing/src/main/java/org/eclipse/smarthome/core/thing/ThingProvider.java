package org.eclipse.smarthome.core.thing;

import java.util.Collection;

/**
 * The {@link ThingProvider} is responsible for providing things.
 * @author Oliver Libutzki
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
