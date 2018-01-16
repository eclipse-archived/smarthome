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
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TradfriVersion} class is a default implementation of comparing TRÅDFRI versions.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class TradfriVersion implements Comparable<TradfriVersion> {
    private static final String VERSION_PATTERN = "[0-9]+(\\.[0-9]+)*";
    private static final String VERSION_DELIMITER = "\\.";
    private final String[] parts;

    /**
     * Create a new instance.
     *
     * @param version the version string
     */
    public TradfriVersion(@NonNull String version) {
        if (!version.matches(VERSION_PATTERN)) {
            throw new IllegalArgumentException("TradfriVersion cannot be created as version has invalid format.");
        }
        this.parts = version.split(VERSION_DELIMITER);
    }

    /**
     * Returns an array of strings containing the version parts
     */
    public String[] get() {
        return parts;
    }

    @Override
    public int compareTo(@NonNull TradfriVersion other) {
        String[] otherParts = other.get();
        int length = Math.max(parts.length, otherParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < parts.length ? Integer.parseInt(parts[i]) : 0;
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
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((TradfriVersion) obj) == 0;
    }

    @Override
    public String toString() {
        return String.join(VERSION_DELIMITER, parts);
    }
}
