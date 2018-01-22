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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TradfriVersion} class is a default implementation for comparing TRÅDFRI versions.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class TradfriVersion implements Comparable<TradfriVersion> {
    private static final String VERSION_PATTERN = "[0-9]+(\\.[0-9]+)*";
    private static final String VERSION_DELIMITER = "\\.";
    private final List<Integer> parts;

    /**
     * Create a new instance.
     *
     * @param version the version string
     */
    public TradfriVersion(final String version) {
        if (!version.matches(VERSION_PATTERN)) {
            throw new IllegalArgumentException("TradfriVersion cannot be created as version has invalid format.");
        }
        parts = Arrays.stream(version.split(VERSION_DELIMITER)).map(part -> Integer.parseInt(part))
                .collect(Collectors.toList());
    }

    /**
     * Returns an array of strings containing the version parts
     */
    public List<Integer> getParts() {
        return parts;
    }

    @Override
    public int compareTo(final TradfriVersion other) {
        List<Integer> otherParts = other.getParts();
        int minSize = Math.min(parts.size(), otherParts.size());
        for (int i = 0; i < minSize; ++i) {
            int diff = parts.get(i) - otherParts.get(i);
            if (diff == 0) {
                continue;
            } else if (diff < 0) {
                return -1;
            } else {
                return 1;
            }
        }
        for (int i = minSize; i < parts.size(); ++i) {
            if (parts.get(i) != 0) {
                return 1;
            }
        }
        for (int i = minSize; i < otherParts.size(); ++i) {
            if (otherParts.get(i) != 0) {
                return -1;
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
        return Arrays.toString(parts.toArray());
    }
}
