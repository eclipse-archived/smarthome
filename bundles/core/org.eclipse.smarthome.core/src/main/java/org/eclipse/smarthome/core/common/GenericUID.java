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
package org.eclipse.smarthome.core.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A non specific base class for unique identifiers within the SmartHome framework.
 *
 * @author Markus Rathgeb - Splitted from the Thing's UID class
 */
@NonNullByDefault
public abstract class GenericUID {

    public static final String SEGMENT_PATTERN = "[A-Za-z0-9_-]*";
    public static final String SEPARATOR = ":";
    private final List<String> segments;

    /**
     * For reflection only.
     */
    protected GenericUID() {
        segments = Collections.emptyList();
    }

    /**
     * Parses an UID for a given string.
     *
     * @param uid id in form a string
     */
    public GenericUID(final String uid) {
        this(splitToSegments(uid));
    }

    /**
     * Creates a UID for list of segments.
     *
     * @param segments the id segments
     */
    public GenericUID(final String... segments) {
        this(Arrays.asList(segments));
    }

    /**
     * Creates a UID for list of segments.
     *
     * @param segments the id segments
     */
    public GenericUID(final List<String> segments) {
        final int minNumberOfSegments = getMinimalNumberOfSegments();
        final int numberOfSegments = segments.size();

        if (numberOfSegments < minNumberOfSegments) {
            throw new IllegalArgumentException(
                    String.format("UID must have at least %d segments.", minNumberOfSegments));
        }

        for (int i = 0; i < numberOfSegments; i++) {
            final String segment = segments.get(i);
            validateSegment(segment, i, numberOfSegments);
        }

        this.segments = segments;
    }

    /**
     * Specifies how many segments the UID has to have at least.
     *
     * @return the minimum nuber of segments
     */
    protected abstract int getMinimalNumberOfSegments();

    protected List<String> getAllSegments() {
        return segments;
    }

    protected String getSegment(final int segment) {
        return segments.get(segment);
    }

    protected void validateSegment(final String segment, final int index, final int length) {
        if (!segment.matches(SEGMENT_PATTERN)) {
            throw new IllegalArgumentException(String.format(
                    "ID segment '%s' contains invalid characters. Each segment of the ID must match the pattern %s.",
                    segment, SEGMENT_PATTERN));
        }
    }

    @Override
    public String toString() {
        return getAsString();
    }

    /**
     * Return the ID as string.
     *
     * @return the ID as string
     */
    public String getAsString() {
        return String.join(SEPARATOR, segments);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + segments.hashCode();
        return result;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GenericUID other = (GenericUID) obj;
        if (!segments.equals(other.segments)) {
            return false;
        }
        return true;
    }

    private static List<String> splitToSegments(final String id) {
        return Arrays.asList(id.split(SEPARATOR));
    }

}
