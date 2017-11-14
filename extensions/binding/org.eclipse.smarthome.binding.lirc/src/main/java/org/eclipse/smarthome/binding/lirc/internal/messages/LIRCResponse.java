/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc.internal.messages;

/**
 * Represents a response received from the LIRC server
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCResponse {

    private final String command;
    private final boolean success;
    private final String[] data;

    public LIRCResponse(String command, boolean success, String[] data) {
        super();
        this.command = command;
        this.success = success;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public boolean isSuccess() {
        return success;
    }

    public String[] getData() {
        return data;
    }

}
