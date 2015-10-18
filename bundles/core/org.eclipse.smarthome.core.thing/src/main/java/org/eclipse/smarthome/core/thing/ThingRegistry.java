/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Map;

import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.common.registry.Registry;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.internal.ThingTracker;

/**
 * {@link ThingRegistry} tracks all {@link Thing}s from different {@link ThingProvider}s and provides access to them.
 * The {@link ThingRegistry} supports adding of listeners (see {@link ThingsChangeListener}) and trackers
 * (see {@link ThingTracker}).
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Extracted ManagedThingProvider
 * @auther Thomas Höfer - Added config description validation exception to updateConfiguration operation
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
    Thing get(ThingUID uid);

    /**
     * Updates the configuration of a thing for the given UID.
     *
     * @param thingUID thing UID
     * @param configurationParameters configuration parameters
     *
     * @throws ConfigValidationException if one or more of the given configuration parameters do not match
     *             their declarations in the configuration description
     */
    void updateConfiguration(ThingUID thingUID, Map<String, Object> configurationParameters)
            throws ConfigValidationException;

    /**
     * Initiates the removal process for the {@link Thing} specified by the given {@link ThingUID}.
     *
     * Unlike in other {@link Registry}s, {@link Thing}s don't get removed immediately.
     * Instead, the corresponding {@link ThingHandler} is given the chance to perform
     * any required removal handling before it actually gets removed.
     * <p>
     * If for any reasons the {@link Thing} should be removed immediately without any prior processing, use
     * {@link #forceRemove(ThingUID)} instead.
     *
     * @param thingUID Identificator of the {@link Thing} to be removed
     * @return the {@link Thing} that was removed, or null if no {@link Thing} with the given {@link ThingUID} exists
     */
    @Override
    Thing remove(ThingUID thingUID);

    /**
     * Removes the {@link Thing} specified by the given {@link ThingUID}.
     *
     * If the corresponding {@link ThingHandler} should be given the chance to perform
     * any removal operations, use {@link #remove(ThingUID)} instead.
     *
     * @param thingUID Identificator of the {@link Thing} to be removed
     * @return the {@link Thing} that was removed, or null if no {@link Thing} with the given {@link ThingUID} exists
     */
    Thing forceRemove(ThingUID thingUID);
}
