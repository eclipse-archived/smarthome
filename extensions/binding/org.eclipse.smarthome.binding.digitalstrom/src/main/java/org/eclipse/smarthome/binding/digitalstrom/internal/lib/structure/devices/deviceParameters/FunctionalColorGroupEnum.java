/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The {@link FunctionalColorGroupEnum} contains all digitalSTROM functional color groups.
 *
 * @see http://developer.digitalstrom.org/Architecture/ds-basics.pdf,
 *      "Table 1: digitalSTROM functional groups and their colors", page 9 [04.09.2015]
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum FunctionalColorGroupEnum {
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
    YELLOW(Lists.newArrayList(1)),
    GREY(Lists.newArrayList(2, 12)),
    BLUE(Lists.newArrayList(3, 9, 10, 11, 48)),
    CYAN(Lists.newArrayList(4)),
    MAGENTA(Lists.newArrayList(5)),
    BLACK(Lists.newArrayList(8)),
    WHITE(Lists.newArrayList(-1)),
    RED(Lists.newArrayList(-2)),
    GREEN(Lists.newArrayList(-3));

    private final List<Integer> colorGroup;

    static final HashMap<Integer, FunctionalColorGroupEnum> colorGroups = new HashMap<Integer, FunctionalColorGroupEnum>();

    static {
        for (FunctionalColorGroupEnum colorGroup : FunctionalColorGroupEnum.values()) {
            for (Integer colorGroupID : colorGroup.getFunctionalColorGroup()) {
                colorGroups.put(colorGroupID, colorGroup);
            }
        }
    }

    /**
     * Returns true, if contains the given functional color group id in digitalSTROM exits, otherwise false.
     *
     * @param functionalColorGroupID
     * @return true, if contains
     */
    public static boolean containsColorGroup(Integer functionalColorGroupID) {
        return colorGroups.keySet().contains(functionalColorGroupID);
    }

    /**
     * Returns the {@link FunctionalColorGroupEnum} of the given color id.
     *
     * @param modeID
     * @return mode
     */
    public static FunctionalColorGroupEnum getColorGroup(Integer functionalColorGroupID) {
        return colorGroups.get(functionalColorGroupID);
    }

    private FunctionalColorGroupEnum(List<Integer> functionalColorGroupID) {
        this.colorGroup = Lists.newArrayList(functionalColorGroupID);
    }

    /**
     * Returns the functional color group id form this Object.
     *
     * @return functional color group id
     */
    public List<Integer> getFunctionalColorGroup() {
        return colorGroup;
    }

}
