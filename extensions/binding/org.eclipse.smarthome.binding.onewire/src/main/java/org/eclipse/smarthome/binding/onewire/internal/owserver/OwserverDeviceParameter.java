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
package org.eclipse.smarthome.binding.onewire.internal.owserver;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OwserverDeviceParameter} device parameter definition for owserver bridge handler
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class OwserverDeviceParameter {
    private String prefix = "";
    private String path = "";

    /**
     * device parameter for owserver bridge handler
     *
     * @param prefix path prefix (e.g. "uncached/")
     * @param path path without sensor id (e.g. "/humidity")
     */
    public OwserverDeviceParameter(String prefix, String path) {
        if (prefix.endsWith("/")) {
            this.prefix = prefix;
        } else {
            this.prefix = prefix + "/";
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
    public OwserverDeviceParameter(String path) {
        this("", path);
    }

    /**
     * get the full owfs path for a given sensor id
     *
     * @param sensorId
     */
    public String getPath(String sensorId) {
        return prefix + sensorId + path;
    }

    @Override
    public String toString() {
        return getPath("sensorId");
    }
}
