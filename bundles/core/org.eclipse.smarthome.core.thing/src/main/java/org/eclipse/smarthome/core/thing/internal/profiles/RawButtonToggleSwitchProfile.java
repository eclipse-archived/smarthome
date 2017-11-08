/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.State;

/**
 * This profile allows a channel of the "system:rawbutton" type to be bound to an item.
 *
 * It reads the triggered events and uses the item's current state and toggles it once it detects that the
 * button was pressed.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class RawButtonToggleSwitchProfile implements TriggerProfile {

    private final ProfileCallback callback;

    private State previousState;

    public RawButtonToggleSwitchProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onTrigger(String event) {
        if (CommonTriggerEvents.PRESSED.equals(event)) {
            OnOffType newState = OnOffType.ON.equals(previousState) ? OnOffType.OFF : OnOffType.ON;
            callback.sendCommandEvent(newState);
            previousState = newState;
        }
    }

    @Override
    public void onUpdate(State state) {
        this.previousState = state;
    }

}
