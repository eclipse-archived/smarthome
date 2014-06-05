package org.eclipse.smarthome.core.thing.binding;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.ThingType;

/**
 * The {@link ThingTypeProvider} is responsible for providing thing types.
 * 
 * @author Dennis Nobel
 * 
 */
public interface ThingTypeProvider {

    /**
     * Provides a collection of thing types
     * 
     * @return the thing types provided by the {@link ThingTypeProvider}
     */
    Collection<ThingType> getThingTypes();

    /**
     * Adds a {@link ThingTypeChangeListener} which is notified if there are
     * changes concerning the thing types provided by the
     * {@link ThingTypeProvider}.
     * 
     * @param listener
     *            The listener to be added
     */
    public void addThingTypeChangeListener(ThingTypeChangeListener listener);

    /**
     * Removes a {@link ThingTypeChangeListener} which is notified if there are
     * changes concerning the thing types provided by the
     * {@link ThingTypeProvider}.
     * 
     * @param listener
     *            The listener to be removed.
     */
    public void removeThingTypeChangeListener(ThingTypeChangeListener listener);
}
