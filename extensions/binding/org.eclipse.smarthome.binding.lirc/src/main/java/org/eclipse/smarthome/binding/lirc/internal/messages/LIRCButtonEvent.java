/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc.internal.messages;

/**
 * Represents a button event that was received from the LIRC server
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCButtonEvent {

    private final String code;
    private final int repeats;
    private final String button;
    private final String remote;

    public LIRCButtonEvent(String remote, String button, int repeats, String code) {
        this.code = code;
        this.repeats = repeats;
        this.button = button;
        this.remote = remote;
    }

    /**
     * Gets the number of times this event was repeated.
     *
     * @return number of repeats
     */
    public int getRepeats() {
        return repeats;
    }

    /**
     * Gets the name of the button that was pressed
     *
     * @return the name of the button
     */
    public String getButton() {
        return button;
    }

    /**
     * Gets the name of the remote that generated this event
     *
     * @return the name of the remote
     */
    public String getRemote() {
        return remote;
    }

    /**
     * Gets the raw hex code of the button pressed
     *
     * @return the hex code
     */
    public String getCode() {
        return code;
    }
}
