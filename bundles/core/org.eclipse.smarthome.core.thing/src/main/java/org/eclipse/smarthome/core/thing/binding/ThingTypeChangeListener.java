/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import org.eclipse.smarthome.core.thing.type.ThingType;

public interface ThingTypeChangeListener {

    /**
     * Notifies the listener that a single thing type has been added
     * 
     * @param provider
     *            the concerned thing type provider
     * @param thingType
     *            the thing type that has been added
     */
    public void thingTypeAdded(ThingTypeProvider provider, ThingType thingType);

    /**
     * Notifies the listener that a single thing type has been removed
     * 
     * @param provider
     *            the concerned thing type provider
     * @param thingType
     *            the thing type that has been removed
     */
    public void thingTypeRemoved(ThingTypeProvider provider, ThingType thingType);
}
