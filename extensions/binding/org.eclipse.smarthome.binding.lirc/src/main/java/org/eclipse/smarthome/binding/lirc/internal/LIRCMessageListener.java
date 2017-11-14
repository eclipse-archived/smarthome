/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc.internal;

import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCButtonEvent;
import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCResponse;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * Interface for listeners to receive messages from LIRC server
 *
 * @author Andrew Nagle
 */
public interface LIRCMessageListener {

    /**
     * This method is called whenever the message is received from the bridge.
     *
     * @param bridge
     *            The LIRC bridge where message is received.
     * @param message
     *            The message which received.
     */
    void onMessageReceived(ThingUID bridge, LIRCResponse message);

    /**
     * This method is called whenever a button is pressed on a remote.
     *
     * @param bridge
     *            The LIRC bridge where message is received.
     * @param buttonEvent
     *            Button event details
     */
    void onButtonPressed(ThingUID bridge, LIRCButtonEvent buttonEvent);
}
