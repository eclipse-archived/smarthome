package org.eclipse.smarthome.core.thing;

import java.util.List;

import org.eclipse.smarthome.core.thing.internal.ThingTracker;

/**
 * {@link ThingRegistry} tracks all {@link Thing}s from different
 * {@link ThingProvider}s and provides access to them. The {@link ThingRegistry}
 * supports adding of listeners (see {@link ThingsChangeListener}) and trackers
 * (see {@link ThingTracker}).
 * 
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Extracted ManagedThingProvider
 */
public interface ThingRegistry {

	/**
	 * Adds a thing registry change listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public abstract void addThingRegistryChangeListener(
			ThingRegistryChangeListener listener);

	/**
	 * Returns a thing for a given UID or null if no thing was found.
	 * 
	 * @param uid
	 *            thing UID
	 * @return thing for a given UID or null if no thing was found
	 */
	public abstract Thing getByUID(ThingUID uid);

	/**
	 * Returns all things.
	 * 
	 * @return all things
	 */
	public abstract List<Thing> getThings();

	/**
	 * Removes a thing registry change listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public abstract void removeThingRegistryChangeListener(
			ThingRegistryChangeListener listener);

}