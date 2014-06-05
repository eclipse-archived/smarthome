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
