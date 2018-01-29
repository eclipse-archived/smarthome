/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
