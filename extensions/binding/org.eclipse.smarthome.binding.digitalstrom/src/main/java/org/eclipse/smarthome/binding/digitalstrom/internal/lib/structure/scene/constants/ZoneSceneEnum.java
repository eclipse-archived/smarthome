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
 * The {@link ZoneSceneEnum} lists all zone scenes which are available on the dSS-web-interface.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum ZoneSceneEnum implements Scene {

    DEEP_OFF((short) 68),
    STANDBY((short) 67),
    SLEEPING((short) 69),
    WAKEUP((short) 70);

    private final short sceneNumber;
    static final HashMap<Short, ZoneSceneEnum> zoneScenes = new HashMap<Short, ZoneSceneEnum>();

    static {
        for (ZoneSceneEnum zs : ZoneSceneEnum.values()) {
            zoneScenes.put(zs.getSceneNumber(), zs);
        }
    }

    private ZoneSceneEnum(short sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    /**
     * Returns the {@link ZoneSceneEnum} of the given scene number.
     *
     * @param sceneNumber
     * @return ZoneSceneEnum
     */
    public static ZoneSceneEnum getZoneScene(short sceneNumber) {
        return zoneScenes.get(sceneNumber);
    }

    /**
     * Returns true, if the given scene number contains in digitalSTROM zone scenes, otherwise false.
     *
     * @param sceneNumber
     * @return true, if contains, otherwise false
     */
    public static boolean containsScene(Short sceneNumber) {
        return zoneScenes.keySet().contains(sceneNumber);
    }

    @Override
    public short getSceneNumber() {
        return this.sceneNumber;
    }
}
