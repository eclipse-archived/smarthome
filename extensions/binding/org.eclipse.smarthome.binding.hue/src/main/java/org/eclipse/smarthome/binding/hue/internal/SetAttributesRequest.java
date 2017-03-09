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
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@SuppressWarnings("unused")
class SetAttributesRequest {
    private String name;
    private List<String> lights;

    public SetAttributesRequest(String name) {
        this(name, null);
    }

    public SetAttributesRequest(List<Light> lights) {
        this(null, lights);
    }

    public SetAttributesRequest(String name, List<Light> lights) {
        if (name != null && Util.stringSize(name) > 32) {
            throw new IllegalArgumentException("Name can be at most 32 characters long");
        } else if (lights != null && (lights.size() == 0 || lights.size() > 16)) {
            throw new IllegalArgumentException("Group cannot be empty and cannot have more than 16 lights");
        }

        this.name = name;
        if (lights != null) {
            this.lights = Util.lightsToIds(lights);
        }
    }
}
