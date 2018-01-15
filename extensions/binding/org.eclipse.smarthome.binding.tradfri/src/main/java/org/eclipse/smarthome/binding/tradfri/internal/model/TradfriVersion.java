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
package org.eclipse.smarthome.binding.tradfri.internal.model;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link TradfriVersion} class is a default implementation of comparing TRÅDFRI versions.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class TradfriVersion implements Comparable<TradfriVersion> {
    private static final String VERSION_PATTERN = "[0-9]+(\\.[0-9]+)*";
    private static final String VERSION_DELIMITER = "\\.";
    private final String version;

    /**
     * Create a new instance.
     *
     * @param version the version string
     */
    public TradfriVersion(@NonNull String version) {
        if (version == null)
            throw new IllegalArgumentException("TradfriVersion cannot be created as version is null.");
        if (!version.matches(VERSION_PATTERN))
            throw new IllegalArgumentException("TradfriVersion cannot be created as version has invalid format.");
        this.version = version;
    }

    /**
     * Returns the version string.
     */
    public String get() {
        return version;
    }

    @Override
    public int compareTo(@NonNull TradfriVersion other) {
        if (other == null) {
            return 1;
        }
        String[] thisParts = get().split(VERSION_DELIMITER);
        String[] otherParts = other.get().split(VERSION_DELIMITER);
        int length = Math.max(thisParts.length, otherParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
            int otherPart = i < otherParts.length ? Integer.parseInt(otherParts[i]) : 0;
            if (thisPart < otherPart) {
                return -1;
            }
            if (thisPart > otherPart) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((TradfriVersion) obj) == 0;
    }
}
