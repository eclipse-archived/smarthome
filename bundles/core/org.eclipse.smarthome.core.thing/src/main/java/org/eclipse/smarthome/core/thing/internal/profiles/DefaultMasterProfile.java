/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * This is the default profile for stateful channels.
 *
 * It forwards commands to the {@link ThingHandler}. In the other direction it posts events to the event bus
 * for state updates.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class DefaultMasterProfile implements StateProfile {

    private final ProfileCallback callback;

    public DefaultMasterProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCommand(Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void stateUpdated(State state) {
        callback.sendStateEvent(state);
    }

    @Override
    public void postCommand(Command command) {
        callback.sendCommandEvent(command);
    }

    @Override
    public void onUpdate(State state) {
        callback.handleUpdate(state);
    }

}
