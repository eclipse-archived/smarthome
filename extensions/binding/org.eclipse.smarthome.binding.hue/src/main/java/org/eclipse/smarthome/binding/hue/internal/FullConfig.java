/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Container for all data on a bridge.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class FullConfig {
    private Map<String, FullLight> lights;
    private Map<String, FullGroup> groups;
    private Config config;

    /**
     * Returns detailed information about all lights known to the bridge.
     *
     * @return detailed lights list
     */
    public List<FullLight> getLights() {
        ArrayList<FullLight> lightsList = new ArrayList<>();

        for (String id : lights.keySet()) {
            FullLight light = lights.get(id);
            light.setId(id);
            lightsList.add(light);
        }

        return lightsList;
    }

    /**
     * Returns detailed information about all groups on the bridge.
     *
     * @return detailed groups list
     */
    public List<FullGroup> getGroups() {
        ArrayList<FullGroup> groupsList = new ArrayList<>();

        for (String id : groups.keySet()) {
            FullGroup group = groups.get(id);
            group.setId(id);
            groupsList.add(group);
        }

        return groupsList;
    }

    /**
     * Returns bridge configuration.
     * Use HueBridge.getConfig() if you only need this.
     *
     * @return bridge configuration
     */
    public Config getConfig() {
        return config;
    }
}
