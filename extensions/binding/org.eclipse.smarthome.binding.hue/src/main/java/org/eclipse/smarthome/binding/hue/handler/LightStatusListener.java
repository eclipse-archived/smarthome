/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.hue.internal.FullLight;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;

/**
 * The {@link LightStatusListener} is notified when a light status has changed or a light has been removed or added.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Denis Dudnik - switched to internally integrated source of Jue library, minor code cleanup
 *
 */
public interface LightStatusListener {

    /**
     * This method is called whenever the state of the given light has changed. The new state can be obtained by
     * {@link FullLight#getState()}.
     *
     * @param bridge The bridge the changed light is connected to.
     * @param light The light which received the state update.
     */
    void onLightStateChanged(HueBridge bridge, @NonNull FullLight light);

    /**
     * This method is called whenever a light is removed.
     *
     * @param bridge The bridge the removed light was connected to.
     * @param light The light which is removed.
     */
    void onLightRemoved(HueBridge bridge, FullLight light);

    /**
     * This method is called whenever a light is added.
     *
     * @param bridge The bridge the added light was connected to.
     * @param light The light which is added.
     */
    void onLightAdded(HueBridge bridge, FullLight light);
}
