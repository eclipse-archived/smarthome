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
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.thing.profiles.SystemProfiles;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * This is the default implementation for a follow profile.
 *
 * In contrast to the {@link SystemDefaultProfile} it does not forward any state updates from the framework to the
 * ThingHandler. Instead, it only accepts {@link Command}s and then forwards those to the {@link ThingHandler}.
 * <p>
 * This allows devices to be operated in an "execution-only" mode, whereby they only take commands that are explicitly
 * given by the framework and ignore state updates that might come from the Item that is for example linked to other
 * Channels that are used to feed back a State
 * <p>
 * The ThingHandler may send commands to the framework, but no state updates are forwarded.
 *
 * @author Karel Goderis - initial contribution and API.
 *
 */
@NonNullByDefault
public class SystemCommandProfile implements StateProfile {

    private final ProfileCallback callback;

    public SystemCommandProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return SystemProfiles.COMMAND;
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
        callback.handleCommand(command);
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        // no-op
    }

}
