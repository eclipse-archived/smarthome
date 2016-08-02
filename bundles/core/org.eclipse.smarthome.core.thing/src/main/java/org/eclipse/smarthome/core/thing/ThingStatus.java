/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

/**
 * {@link ThingStatus} defines possible statuses of a {@link ThingStatusInfo}.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public enum ThingStatus {
    UNINITIALIZED(0),
    INITIALIZING(1),
    ONLINE(2),
    OFFLINE(3),
    REMOVING(4),
    REMOVED(5);

    private final int value;

    private ThingStatus(final int newValue) {
        value = newValue;
    }

    /**
     * Gets the value of a thing status.
     * 
     * @return the value
     */
    public int getValue() {
        return value;
    }
}
