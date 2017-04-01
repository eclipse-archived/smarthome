/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.util.List;

/**
 * Detailed group information.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class FullGroup extends Group {
    private State action;
    private List<String> lights;

    FullGroup() {
    }

    /**
     * Returns the last sent state update to the group.
     * This does not have to reflect the current state of the group.
     *
     * @return last state update
     */
    public State getAction() {
        return action;
    }

    /**
     * Returns a list of the lights in the group.
     *
     * @return lights in the group
     */
    public List<Light> getLights() {
        return Util.idsToLights(lights);
    }
}
