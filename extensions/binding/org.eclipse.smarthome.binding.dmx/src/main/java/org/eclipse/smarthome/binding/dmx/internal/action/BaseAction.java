/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.internal.action;

import org.eclipse.smarthome.binding.dmx.internal.multiverse.Channel;

/**
 * The {@link BaseAction} is the base class for Actions like faders, chasers, etc..
 *
 * @author Davy Vanherbergen
 * @author Jan N. Klug
 */
public abstract class BaseAction {

    protected boolean completed = false;
    protected long startTime = 0;

    /**
     * Calculate the new output value of the channel.
     *
     * @param channel
     * @param currentTime UNIX timestamp to use as current time
     * @return value as float between 0 - 65535
     */
    public abstract int getNewValue(Channel channel, long currentTime);

    /**
     * @return true if the action was completed.
     */
    public final boolean isCompleted() {
        return completed;
    }

    /**
     * Reset the action to start from the beginning.
     */
    public void reset() {
        startTime = 0;
        completed = false;
    }

}
