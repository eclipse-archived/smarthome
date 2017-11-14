/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants;

import java.util.HashMap;

/**
 * The {@link ApartmentSceneEnum} lists all group independent scenes from digitalSTROM which are callable at the
 * digitalSTROM web interface.
 *
 * @author Alexander Betker - Initial contribution
 * @see digitalSTROM wiki on http://redmine.digitalstrom.org/projects/dss/wiki/Scene_table
 *
 * @author Michael Ochel - add new scenes and deleted scenes which are show as zone scenes in the dss-web-interface
 * @author Mathias Siegele - add new scenes and deleted scenes which are show as zone scenes in the dss-web-interface
 * @see http://developer.digitalstrom.org/Architecture/ds-basics.pdf , Table 35: Group independent activities and scene
 *      command values, page 47 and dss-web-interface
 */
public enum ApartmentSceneEnum implements Scene {

    ENERGY_OVERLOAD((short) 66),
    ZONE_ACTIVE((short) 75),
    ALARM_SIGNAL((short) 74),
    AUTO_STANDBY((short) 64),
    ABSENT((short) 72),
    PRESENT((short) 71),
    DOOR_BELL((short) 73),
    PANIC((short) 65),
    FIRE((short) 76),
    ALARM_1((short) 74),
    ALARM_2((short) 83),
    ALARM_3((short) 84),
    ALARM_4((short) 85),
    WIND((short) 86),
    NO_WIND((short) 87),
    RAIN((short) 88),
    NO_RAIN((short) 89),
    HAIL((short) 90),
    NO_HAIL((short) 91);

    private final short sceneNumber;
    static final HashMap<Short, ApartmentSceneEnum> apartmentScenes = new HashMap<Short, ApartmentSceneEnum>();

    static {
        for (ApartmentSceneEnum as : ApartmentSceneEnum.values()) {
            apartmentScenes.put(as.getSceneNumber(), as);
        }
    }

    private ApartmentSceneEnum(Short sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    /**
     * Returns the apartment scene from the given scene number.
     *
     * @param sceneNumber
     * @return apartment scene
     */
    public static ApartmentSceneEnum getApartmentScene(short sceneNumber) {
        return apartmentScenes.get(sceneNumber);
    }

    /**
     * Returns true, if the given scene number contains in digitalSTROM apartment scenes, otherwise false.
     *
     * @param sceneNumber
     * @return true, if contains, otherwise false
     */
    public static boolean containsScene(Short sceneNumber) {
        return apartmentScenes.keySet().contains(sceneNumber);
    }

    @Override
    public short getSceneNumber() {
        return this.sceneNumber;
    }
}
