/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.onewire.internal.device;

import org.eclipse.smarthome.binding.onewire.internal.SensorId;

/**
 * The {@link OwDeviceParameter} defines a member of the OwDeviceParameterMap
 *
 * @author Jan N. Klug - Initial contribution
 */
public class OwDeviceParameter {
    private String prefix = "";
    private String path = "";

    /**
     * device parameter for owserver bridge handler
     *
     * @param prefix path prefix (e.g. "uncached/")
     * @param path path without sensor id (e.g. "/humidity")
     */
    public OwDeviceParameter(String prefix, String path) {
        if (prefix.endsWith("/")) {
            this.prefix = prefix.substring(0, prefix.length() - 1);
        } else {
            this.prefix = prefix;
        }
        if (path.startsWith("/")) {
            this.path = path;
        } else {
            this.path = "/" + path;
        }
    }

    /**
     * device parameter for owserver bridge handler
     *
     * @param path path without sensor id (e.g. "/humidity")
     */
    public OwDeviceParameter(String path) {
        this("", path);
    }

    /**
     * get the full owfs path for a given sensor id
     *
     * @param sensorId
     */
    public String getPath(SensorId sensorId) {
        return prefix + sensorId.getFullPath() + path;
    }

    @Override
    public String toString() {
        return prefix + "/sensorId" + path;
    }
}
