/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.core.common.registry.Registry;
import org.eclipse.smarthome.core.thing.internal.ThingTracker;

/**
 * {@link ThingRegistry} tracks all {@link Thing}s from different {@link ThingProvider}s and provides access to them.
 * The {@link ThingRegistry} supports adding of listeners (see {@link ThingsChangeListener}) and trackers
 * (see {@link ThingTracker}).
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Extracted ManagedThingProvider
 */
public interface ThingRegistry extends Registry<Thing, ThingUID> {

    /**
     * Returns a thing for a given UID or null if no thing was found.
     * 
     * @param uid
     *            thing UID
     * @return thing for a given UID or null if no thing was found
     */
    @Override
    public abstract Thing get(ThingUID uid);

}
