/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

/**
 * Kind of the channel.
 *
 * @author Moritz Kammerer - Initial contribution and API.
 */
public enum ChannelKind {
    /**
     * Channels which have a state.
     */
    STATE,
    /**
     * Channels which can be triggered.
     */
    TRIGGER;

    /**
     * Parses the input string into a {@link ChannelKind}.
     *
     * @param input the input string
     * @return the parsed ChannelKind.
     * @throws IllegalArgumentException if the input couldn't be parsed.
     */
    public static ChannelKind parse(String input) {
        if (input == null) {
            return STATE;
        }

        for (ChannelKind value : values()) {
            if (value.name().equalsIgnoreCase(input)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown channel kind: '" + input + "'");
    }
}
