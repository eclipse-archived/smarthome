/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.types;

/**
 * Due to the duality of some types (which can be states and commands at the
 * same time), we need to be able to differentiate what the meaning of a
 * message on the bus is - does "item ON" mean that its state has changed to
 * ON or that it should turn itself ON? To decide this, we send the event
 * type as an additional information on the event bus for each message.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public enum EventType {

    COMMAND("command"), UPDATE("update");

    private String name;

    private EventType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
