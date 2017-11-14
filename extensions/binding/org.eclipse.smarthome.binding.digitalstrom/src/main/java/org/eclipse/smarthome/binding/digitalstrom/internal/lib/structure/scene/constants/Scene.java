/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants;

/**
 * The {@link Scene} represents digitalSTROM-Scene.
 *
 * @author Alexander Betker - Initial contribution
 */
public interface Scene {

    /**
     * Returns the scene number of this {@link Scene}.
     *
     * @return scene number
     */
    public short getSceneNumber();
}
