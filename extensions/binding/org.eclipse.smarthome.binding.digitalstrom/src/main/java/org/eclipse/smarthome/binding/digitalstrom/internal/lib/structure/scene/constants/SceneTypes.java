/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants;

/**
 * The {@link SceneTypes} lists the difference scene types of this digitalSTROM-Library.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SceneTypes {
    /**
     * This scene type represents a scene with an user defined name.
     */
    public static final String NAMED_SCENE = "namedScene";
    /**
     * This scene type represents a scene, which will be call on a hole zone.
     */
    public static final String ZONE_SCENE = "zoneScene";
    /**
     * This scene type represents a scene, which will be call on the hole apartment.
     */
    public static final String APARTMENT_SCENE = "appScene";
    /**
     * This scene type represents a scene, which will be call on a group.
     */
    public static final String GROUP_SCENE = "groupScene";
}
