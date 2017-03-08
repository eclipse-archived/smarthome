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
 * @author Andre Fuechsel - search for lights with given serial number added
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@SuppressWarnings("unused")
class SearchForLightsRequest {
    private List<String> deviceid;

    public SearchForLightsRequest(List<String> deviceid) {
        if (deviceid != null && (deviceid.size() == 0 || deviceid.size() > 16)) {
            throw new IllegalArgumentException("Group cannot be empty and cannot have more than 16 lights");
        }
        if (deviceid != null) {
            this.deviceid = deviceid;
        }
    }
}
