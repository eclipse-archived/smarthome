/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.core.thing.internal.profiles;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.SystemProfiles;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link RawRockerToDimmerProfile} transforms rocker switch channel events into dimmer commands.
 *
 * @author Jan Kemmler - Initial contribution
 */
@NonNullByDefault
public class RawRockerToDimmerProfile implements TriggerProfile {

    private final ProfileCallback callback;

    ProfileContext context;

    @Nullable
    private ScheduledFuture<?> dimmFuture;
    @Nullable
    private ScheduledFuture<?> timeoutFuture;

    private long pressedTime = 0;

    final class DimmerIncreaseDecreaseTask implements Runnable {
        private ProfileCallback callback;
        private Command command;

        DimmerIncreaseDecreaseTask(ProfileCallback callback, Command command) {
            this.callback = callback;
            this.command = command;
        }

        @Override
        public void run() {
            callback.sendCommand(command);
        }
    };

    @SuppressWarnings("null")
    RawRockerToDimmerProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return SystemProfiles.RAWROCKER_TO_DIMMER;
    }

    @Override
    public void onStateUpdateFromItem(State state) {

    }

    @Override
    public void onTriggerFromHandler(String event) {
        if (CommonTriggerEvents.DIR1_PRESSED.equals(event)) {
            buttonPressed(IncreaseDecreaseType.INCREASE);
        } else if (CommonTriggerEvents.DIR1_RELEASED.equals(event)) {
            buttonReleased(OnOffType.ON);
        } else if (CommonTriggerEvents.DIR2_PRESSED.equals(event)) {
            buttonPressed(IncreaseDecreaseType.DECREASE);
        } else if (CommonTriggerEvents.DIR2_RELEASED.equals(event)) {
            buttonReleased(OnOffType.OFF);
        }
    }

    private void buttonPressed(Command commandToSend) {
        if (null != timeoutFuture) {
            timeoutFuture.cancel(false);
        }
        if (null != dimmFuture) {
            dimmFuture.cancel(false);
        }

        dimmFuture = context.getExecutorService().scheduleWithFixedDelay(
                new DimmerIncreaseDecreaseTask(callback, commandToSend), 550, 200, TimeUnit.MILLISECONDS);
        timeoutFuture = context.getExecutorService().schedule(new Runnable() {
            @Override
            public void run() {
                if (null != dimmFuture) {
                    dimmFuture.cancel(false);
                }
            }
        }, 10000, TimeUnit.MILLISECONDS);
        pressedTime = System.currentTimeMillis();
    }

    private void buttonReleased(Command commandToSend) {
        if (null != timeoutFuture) {
            timeoutFuture.cancel(false);
        }
        if (null != dimmFuture) {
            dimmFuture.cancel(false);
        }

        if (System.currentTimeMillis() - pressedTime <= 500) {
            callback.sendCommand(commandToSend);
        }
    }

}
