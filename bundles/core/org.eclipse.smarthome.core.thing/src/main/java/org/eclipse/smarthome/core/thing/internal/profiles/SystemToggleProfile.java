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
package org.eclipse.smarthome.core.thing.internal.profiles;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.SystemProfiles;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * This extends the {@link SystemDefaultProfile} by a "toggle" functionality.
 * <p>
 * In addition to what the {@link SystemDefaultProfile} does, this class also implements {@link TriggerProfile} so that
 * it can handle trigger events. Whenever it receives a "TOGGLE" event, it inverts the last known state (if any) and
 * sends a corresponding command to the linked item.
 * <p>
 * This works for all items which accept any of
 * <ul>
 * <li>{@link OnOffType}
 * <li>{@link UpDownType}
 * <li>{@link PlayPauseType}
 * </ul>
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class SystemToggleProfile extends SystemDefaultProfile implements TriggerProfile {

    public static final Set<String> SUPPORTED_ITEM_TYPES = Stream.of(//
            CoreItemFactory.SWITCH, //
            CoreItemFactory.DIMMER, //
            CoreItemFactory.COLOR, //
            CoreItemFactory.ROLLERSHUTTER, //
            CoreItemFactory.PLAYER //
    ).collect(toSet());

    private State previousState = UnDefType.NULL;
    private final ProfileCallback callback;

    public SystemToggleProfile(ProfileCallback callback) {
        super(callback);
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return SystemProfiles.TOGGLE;
    }

    @Override
    public void onStateUpdateFromItem(@NonNull State state) {
        super.onStateUpdateFromItem(state);
        this.previousState = state;
    }

    @Override
    public void onStateUpdateFromHandler(@NonNull State state) {
        super.onStateUpdateFromHandler(state);
        this.previousState = state;
    }

    @Override
    public void onTriggerFromHandler(@NonNull String event) {
        if ("TOGGLE".equalsIgnoreCase(event)) {
            if (OnOffType.ON.equals(previousState.as(OnOffType.class))) {
                callback.sendCommand(OnOffType.OFF);
            } else if (OnOffType.OFF.equals(previousState.as(OnOffType.class))) {
                callback.sendCommand(OnOffType.ON);
            } else if (UpDownType.UP.equals(previousState.as(UpDownType.class))) {
                callback.sendCommand(UpDownType.DOWN);
            } else if (UpDownType.DOWN.equals(previousState.as(UpDownType.class))) {
                callback.sendCommand(UpDownType.UP);
            } else if (PlayPauseType.PAUSE.equals(previousState.as(PlayPauseType.class))) {
                callback.sendCommand(PlayPauseType.PLAY);
            } else if (PlayPauseType.PLAY.equals(previousState.as(PlayPauseType.class))) {
                callback.sendCommand(PlayPauseType.PAUSE);
            }
        }
    }

}
