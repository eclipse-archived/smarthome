/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

import java.util.HashMap;

/**
 * The {@link OutputModeEnum} lists all available digitalSTROM-device output modes.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 * @see http://developer.digitalstrom.org/Architecture/ds-basics.pdf Table 36: Output Mode Register, page 51
 */
public enum OutputModeEnum {
    /*
     * | Output Mode | Description |
     * ------------------------------------------------------------------------------
     * | 0 | No output or output disabled |
     * | 16 | Switched |
     * | 17 | RMS (root mean square) dimmer |
     * | 18 | RMS dimmer with characteristic curve |
     * | 19 | Phase control dimmer |
     * | 20 | Phase control dimmer with characteristic curve |
     * | 21 | Reverse phase control dimmer |
     * | 22 | Reverse phase control dimmer with characteristic curve |
     * | 23 | PWM (pulse width modulation) |
     * | 24 | PWM with characteristic curve |
     * | 33 | Positioning control |
     * | 34 | combined 2 stage switch [Both relais switch combined in tow steps depending on the output value. Output >
     * 33% - relais 1 is on. Output > 66% - relais 1 and 2 are on.] | (from ds web configurator, it dosn't stand in the
     * ds-basic.pdf from 19.08.2015)
     * | 35 | single switch | (from ds web configurator, it dosn't stand in the ds-basic.pdf from 19.08.2015)
     * | 38 | combined 3 stage switch [Both relais switch combined in tow steps depending on the output value. Output >
     * 25% - relais 1 is on. Output > 50% - relais 1 is off and relais 2 is on. Output > 75% - relais 1 and 2 are on.] |
     * (from ds web configurator, it dosn't stand in the
     * ds-basic.pdf from 19.08.2015)
     * | 39 | Relay with switched mode scene table configuration |
     * | 40 | Relay with wiped mode scene table configuration |
     * | 41 | Relay with saving mode scene table configuration |
     * | 42 | Positioning control for uncalibrated shutter |
     * | 43 | combined switch | (from ds web configurator, it dosn't stand in the ds-basic.pdf from 19.08.2015)
     */
    DISABLED(0),
    SWITCHED(16),
    RMS_DIMMER(17),
    RMS_DIMMER_CC(18),
    PC_DIMMER(19),
    PC_DIMMER_CC(20),
    RPC_DIMMER(21),
    RPC_DIMMER_CC(22),
    PWM(23),
    PWM_CC(24),
    POSITION_CON(33),
    COMBINED_2_STAGE_SWITCH(34),
    SINGLE_SWITCH(35),
    COMBINED_3_STAGE_SWITCH(38),
    SWITCH(39),
    WIPE(40),
    POWERSAVE(41),
    POSITION_CON_US(42),
    COMBINED_SWITCH(43);

    private final int mode;

    static final HashMap<Integer, OutputModeEnum> outputModes = new HashMap<Integer, OutputModeEnum>();

    static {
        for (OutputModeEnum out : OutputModeEnum.values()) {
            outputModes.put(out.getMode(), out);
        }
    }

    /**
     * Returns true, if the output mode id contains in digitalSTROM, otherwise false.
     *
     * @param modeID
     * @return true if contains otherwise false
     */
    public static boolean containsMode(Integer modeID) {
        return outputModes.keySet().contains(modeID);
    }

    /**
     * Returns the {@link OutputModeEnum} for the given modeID, otherwise null.
     *
     * @param modeID
     * @return OutputModeEnum or null
     */
    public static OutputModeEnum getMode(Integer modeID) {
        return outputModes.get(modeID);
    }

    private OutputModeEnum(int outputMode) {
        this.mode = outputMode;
    }

    /**
     * Returns the id of this {@link OutputModeEnum} object.
     *
     * @return mode id
     */
    public int getMode() {
        return mode;
    }
}
