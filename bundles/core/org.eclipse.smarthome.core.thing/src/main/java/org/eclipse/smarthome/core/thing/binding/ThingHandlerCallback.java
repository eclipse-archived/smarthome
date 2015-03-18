/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.State;

/**
 * {@link ThingHandlerCallback} is callback interface for {@link ThingHandler}s. The implementation of a
 * {@link ThingHandler} must use the callback to inform the framework about changes like state updates, status updated
 * or an update of the whole thing.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public interface ThingHandlerCallback {

    /**
     * Informs about an updated state for a channel.
     * 
     * @param channelUID channel UID (must not be null)
     * @param state state (must not be null)
     */
    void stateUpdated(ChannelUID channelUID, State state);

    /**
     * Informs about an updated status of a thing.
     * 
     * @param thing thing (must not be null)
     * @param thingStatus thing status (must not be null)
     */
    void statusUpdated(Thing thing, ThingStatus thingStatus);

    /**
     * Informs about an update of the whole thing.
     * 
     * @param thing thing that was updated (must not be null)
     */
    void thingUpdated(Thing thing);

}
