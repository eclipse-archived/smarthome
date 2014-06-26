package org.eclipse.smarthome.core.thing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract base class for {@link ThingProvider}s. In particular it handles
 * the management and notification of {@link ThingsChangeListener}s.  
 * 
 * @author Oliver Libutzki - Initial Contribution
 */
public abstract class AbstractThingProvider implements ThingProvider {

	private List<ThingsChangeListener> thingsChangeListeners = new CopyOnWriteArrayList<>();


	@Override
	public void addThingsChangeListener(ThingsChangeListener listener) {
	    thingsChangeListeners.add(listener);
	}

	@Override
	public void removeThingsChangeListener(ThingsChangeListener listener) {
	    thingsChangeListeners.remove(listener);
	}

	protected void notifyThingsChangeListenersAboutAddedThing(Thing thing) {
	    for (ThingsChangeListener thingChangeListener : this.thingsChangeListeners) {
	        thingChangeListener.thingAdded(this, thing);
	    }
	}

	protected void notifyThingsChangeListenersAboutRemovedThing(Thing thing) {
	    for (ThingsChangeListener thingChangeListener : this.thingsChangeListeners) {
	        thingChangeListener.thingRemoved(this, thing);
	    }
	}
	
	protected void notifyThingsChangeListenersAboutUpdatedThing(Thing oldThing, Thing newThing) {
		for (ThingsChangeListener thingChangeListener : this.thingsChangeListeners) {
			thingChangeListener.thingUpdated(this, oldThing, newThing);
		}
	}

}