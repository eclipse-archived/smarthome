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

/**
 * @author Samuel Leisering - Added API utils to perform actions more efficient on newer Versions
 *
 */
public class ApiVersionUtils {

    private static ApiVersion fullLights = new ApiVersion(1, 11, 0);

    /**
     * starting from version 1.11, <code>GET</code>ing the Lights always returns {@link FullLight FullLights} instead of
     * {@link HueObject Lights}
     *
     * @return
     */
    public static boolean supportsFullLights(ApiVersion version) {
        return fullLights.compare(version) <= 0;
    }
}
