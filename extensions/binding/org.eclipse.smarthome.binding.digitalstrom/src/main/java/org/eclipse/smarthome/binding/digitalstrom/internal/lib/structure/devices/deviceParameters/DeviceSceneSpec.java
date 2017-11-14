/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;

/**
 * The {@link DeviceSceneSpec} saves a digitalSTROM-Device scene mode.
 *
 * @author Alexander Betker
 * @author Michael Ochel - add missing java-doc
 * @author Matthias Siegele - add missing java-doc
 */
public interface DeviceSceneSpec {

    /**
     * Returns the sceneID.
     *
     * @return sceneID
     */
    public Scene getScene();

    /**
     * Returns true, if the don't care flag is set, otherwise false.
     *
     * @return true, if dont't care is set, otherwise false
     */
    public boolean isDontCare();

    /**
     * Sets the don't care flag.
     *
     * @param dontcare
     */
    public void setDontcare(boolean dontcare);

    /**
     * Returns true, if the local priority flag is set, otherwise false.
     *
     * @return true, if local priority is, set otherwise false
     */
    public boolean isLocalPrio();

    /**
     * Sets the local priority flag.
     *
     * @param localPrio
     */
    public void setLocalPrio(boolean localPrio);

    /**
     * Returns true, if the special mode flag is set, otherwise false.
     *
     * @return true, if special mode is set, otherwise false
     */
    public boolean isSpecialMode();

    /**
     * Sets the special mode flag.
     *
     * @param specialMode
     */
    public void setSpecialMode(boolean specialMode);

    /**
     * Returns true, if the flash mode flag is set, otherwise false.
     *
     * @return true, if flash mode is set, otherwise false
     */
    public boolean isFlashMode();

    /**
     * Sets the flash mode flag.
     *
     * @param flashMode
     */
    public void setFlashMode(boolean flashMode);
}
