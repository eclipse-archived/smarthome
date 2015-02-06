/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.EventListener;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;

/**
 * {@link ThingListener} can be registered at a {@link Thing} object.
 *
 * @see Thing#addThingListener(ThingListener)
 * @author Dennis Nobel - Initial contribution
 */
public interface ThingListener extends EventListener {

    /**
     * Channel updated is called when the state of a channel was updated.
     *
     * @param channelUID
     *            unique identifier of a channel
     * @param state
     *            state
     */
    void channelUpdated(ChannelUID channelUID, State state);

}
