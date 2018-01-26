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
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants;

/**
 * The {@link ControlStates} contains all digitalSTROM heating control states.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum ControlStates {

    INTERNAL((short) 0, "internal"),
    EXTERNAL((short) 1, "external"),
    EXBACKUP((short) 2, "exbackup"),
    EMERGENCY((short) 3, "emergency");

    private final Short id;
    private final String key;
    private static final ControlStates[] CONTROL_STATES = new ControlStates[ControlStates.values().length];

    static {
        for (ControlStates controlState : ControlStates.values()) {
            CONTROL_STATES[controlState.id] = controlState;
        }
    }

    private ControlStates(short id, String key) {
        this.id = id;
        this.key = key;
    }

    /**
     * Returns the key of the operation mode.
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the ID of the operation mode.
     *
     * @return ID
     */
    public Short getID() {
        return id;
    }

    /**
     * Returns the {@link ControlStates} of the given control state id.
     *
     * @param id of the control state
     * @return control state
     */
    public static ControlStates getControlState(short id) {
        try {
            return CONTROL_STATES[id];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
