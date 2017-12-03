/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.thing.profiles.SystemProfiles;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * This is the default implementation for a passive profile.
 *
 * In contrast to the {@link SystemDefaultProfile} it does not forward any commands or updates to the ThingHandler.
 * Instead, it takes
 * {@link State} and {@link Command}s received from the {@link ThingHandler} to send to the {@link Item}.
 * <p>
 * This allows devices to be operated as "passive informers" for information they receive from Things
 * <p>
 * The ThingHandler may send commands and updates to the framework, but nothing is forwarded from the framework to the
 * ThingHandler.
 *
 * @author Karel Goderis - initial contribution and API.
 *
 */
@NonNullByDefault
public class SystemPassiveProfile implements StateProfile {

    private final ProfileCallback callback;

    public SystemPassiveProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return SystemProfiles.PASSIVE;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // no-op
    }

    @Override
    public void onCommandFromHandler(Command command) {
        callback.sendCommand(command);
    }

    @Override
    public void onCommandFromItem(Command command) {
        // no-op
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate(state);
    }

}
