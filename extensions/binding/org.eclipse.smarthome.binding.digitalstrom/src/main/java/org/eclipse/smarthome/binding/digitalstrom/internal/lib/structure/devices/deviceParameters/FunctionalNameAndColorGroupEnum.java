/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

import java.util.HashMap;

/**
 * The {@link FunctionalNameAndColorGroupEnum} contains all digitalSTROM functional group names and links to their
 * {@link FunctionalColorGroupEnum}.
 *
 * @see http://developer.digitalstrom.org/Architecture/ds-basics.pdf,
 *      "Table 1: digitalSTROM functional groups and their colors", page 9
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum FunctionalNameAndColorGroupEnum {
    /*
     * | Number | Name | Color | Function |
     * --------------------------------------------------------------------------------------
     * | 1 | Lights | Yellow | Room lights |
     * | 2 | Blinds | Gray | Blinds or shades outside |
     * | 12 | Curtains | Gray | Curtains and blinds inside |
     * | 3 | Heating | Blue | Heating |
     * | 9 | Cooling | Blue | Cooling |
     * | 10 | Ventilation | Blue | Ventilation |
     * | 11 | Window | Blue | Window |
     * | 48 | Temperature Control | Blue | Single room temperature control |
     * | 4 | Audio | Cyan | Playing music or radio |
     * | 5 | Video | Magenta | TV, Video |
     * | 8 | Joker | Black | Configurable behaviour |
     * | n/a | Single Device | White | Various, individual per device |
     * | n/a | Security | Red | Security related functions, Alarms |
     * | n/a | Access | Green | Access related functions, door bell |
     *
     */
    LIGHTS(1, FunctionalColorGroupEnum.getColorGroup(1)),
    BLINDS(2, FunctionalColorGroupEnum.getColorGroup(2)),
    CURTAINS(12, FunctionalColorGroupEnum.getColorGroup(12)),
    HEATING(3, FunctionalColorGroupEnum.getColorGroup(3)),
    COOLING(9, FunctionalColorGroupEnum.getColorGroup(9)),
    VENTILATION(10, FunctionalColorGroupEnum.getColorGroup(10)),
    WINDOW(11, FunctionalColorGroupEnum.getColorGroup(11)),
    TEMPERATION_CONTROL(48, FunctionalColorGroupEnum.getColorGroup(48)),
    AUDIO(4, FunctionalColorGroupEnum.getColorGroup(4)),
    VIDEO(5, FunctionalColorGroupEnum.getColorGroup(5)),
    JOKER(8, FunctionalColorGroupEnum.getColorGroup(8)),
    SINGLE_DEVICE(-1, FunctionalColorGroupEnum.getColorGroup(-1)),
    SECURITY(-2, FunctionalColorGroupEnum.getColorGroup(-2)),
    ACCESS(-3, FunctionalColorGroupEnum.getColorGroup(-3));

    private final int colorGroup;
    private final FunctionalColorGroupEnum color;

    static final HashMap<Integer, FunctionalNameAndColorGroupEnum> colorGroups = new HashMap<Integer, FunctionalNameAndColorGroupEnum>();

    static {
        for (FunctionalNameAndColorGroupEnum colorGroup : FunctionalNameAndColorGroupEnum.values()) {
            colorGroups.put(colorGroup.getFunctionalColorGroup(), colorGroup);
        }
    }

    /**
     * Returns true, if contains the given output mode id in DigitalSTROM, otherwise false.
     *
     * @param functionalNameGroupID
     * @return true, if contains
     */
    public static boolean containsColorGroup(Integer functionalNameGroupID) {
        return colorGroups.keySet().contains(functionalNameGroupID);
    }

    /**
     * Returns the {@link FunctionalNameAndColorGroupEnum} of the given functional name group id.
     *
     * @param functionalNameGroupID
     * @return FunctionalNameAndColorGroupEnum
     */
    public static FunctionalNameAndColorGroupEnum getMode(Integer functionalNameGroupID) {
        return colorGroups.get(functionalNameGroupID);
    }

    private FunctionalNameAndColorGroupEnum(int functionalColorGroupID, FunctionalColorGroupEnum functionalColorGroup) {
        this.colorGroup = functionalColorGroupID;
        this.color = functionalColorGroup;
    }

    /**
     * Returns the functional name group id form this Object.
     *
     * @return functional name group id
     */
    public int getFunctionalColorGroup() {
        return colorGroup;
    }

    /**
     * Returns the {@link FunctionalColorGroupEnum} form this Object.
     *
     * @return FunctionalColorGroupEnum
     */
    public FunctionalColorGroupEnum getFunctionalColor() {
        return color;
    }
}
