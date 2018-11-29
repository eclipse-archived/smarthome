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
package org.eclipse.smarthome.binding.bluetooth.blukii.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Blukii megnometer data.
 *
 * @author Markus Rathgeb - Initial contribution (migrated from handler)
 */
@NonNullByDefault
public class Magnetometer {
    public final int x;
    public final int y;
    public final int z;

    public Magnetometer(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "Magnetometer [x=" + x + ", y=" + y + ", z=" + z + "]";
    }

}
