/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.type.ThingType;

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
