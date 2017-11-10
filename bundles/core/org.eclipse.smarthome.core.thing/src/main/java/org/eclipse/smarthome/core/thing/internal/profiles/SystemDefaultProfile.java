/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.thing.profiles.SystemProfiles;
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
@NonNullByDefault
public class SystemDefaultProfile implements StateProfile {

    private final ProfileCallback callback;

    public SystemDefaultProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return SystemProfiles.DEFAULT;
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate(state);
    }

    @Override
    public void onCommandFromHandler(Command command) {
        callback.sendCommand(command);
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        callback.handleUpdate(state);
    }

}
