/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.hue.internal.profiles;

import static org.eclipse.smarthome.binding.hue.internal.profiles.HueProfileFactory.HUE_TOGGLE_SWITCH_PROFILE_TYPE_UID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link HueToggleSwitchTriggerProfile} class implements the toggle ON/OFF behavior when being linked to a Switch
 * item.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HueToggleSwitchTriggerProfile extends AbstractHueTriggerProfile {

    private @Nullable State previousState;

    HueToggleSwitchTriggerProfile(ProfileCallback callback, ProfileContext context) {
        super(callback, context);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return HUE_TOGGLE_SWITCH_PROFILE_TYPE_UID;
    }

    @Override
    public void onTriggerFromHandler(String payload) {
        if (payload.equals(event)) {
            OnOffType state = OnOffType.ON.equals(previousState) ? OnOffType.OFF : OnOffType.ON;
            callback.sendCommand(state);
            previousState = state;
        }
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        previousState = state.as(OnOffType.class);
    }
}
