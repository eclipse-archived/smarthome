/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc.internal.connector;

import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCButtonEvent;
import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCResponse;

/**
 * Defines an interface to receive messages from the LIRC server
 *
 * @author Andrew Nagle
 */
public interface LIRCEventListener {

    /**
     * Procedure to receive messages from the LIRC server
     *
     * @param reponse
     *            Message received
     */
    void messageReceived(LIRCResponse message);

    /**
     * Procedure for receiving notification of button presses
     *
     * @param buttonEvent
     *            Button press event details
     */
    void buttonPressed(LIRCButtonEvent buttonEvent);

    /**
     * Procedure for receiving information about fatal errors.
     *
     * @param error
     *            Error occured.
     */
    void errorOccured(String error);

}
