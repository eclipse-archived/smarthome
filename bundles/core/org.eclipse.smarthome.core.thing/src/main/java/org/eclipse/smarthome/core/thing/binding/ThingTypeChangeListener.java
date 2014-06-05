package org.eclipse.smarthome.core.thing.binding;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingType;

public interface ThingTypeChangeListener {

    /**
     * Notifies the listener that a single thing type has been added
     * 
     * @param provider
     *            the concerned thing type provider
     * @param thing
     *            the thing type that has been added
     */
    public void thingTypeAdded(ThingTypeProvider provider, ThingType thingType);

    /**
     * Notifies the listener that a single thing type has been removed
     * 
     * @param provider
     *            the concerned thing type provider
     * @param thing
     *            the thing type that has been removed
     */
    public void thingTypeRemoved(ThingTypeProvider provider, Thing thingType);
}
