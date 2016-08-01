/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Arrays;

import com.google.common.base.Joiner;

/**
 * {@link UID} is the base class for unique identifiers within the SmartHome
 * framework. A UID must always start with a binding ID.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Added possibility to define UIDs with variable amount of segments
 * @author Jochen Hiller - Bugfix 455434: added default constructor, object is now mutable
 */
public abstract class UID {

    public static final String SEGMENT_PATTERN = "[A-Za-z0-9_-]*";
    public static final String SEPARATOR = ":";
    private String[] segments;

    /**
     * Constructor must be public, otherwise it can not be called by subclasses from another package.
     */
    public UID() {
        this.segments = null;
    }

    /**
     * Parses a UID for a given string. The UID must be in the format
     * 'bindingId:segment:segment:...'.
     *
     * @param uid
     *            uid in form a string (must not be null)
     */
    public UID(String uid) {
        this(splitToSegments(uid));
    }

    private static String[] splitToSegments(String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("Given uid must not be null.");
        }
        return uid.split(SEPARATOR);
    }

    /**
     * Creates a UID for list of segments.
     *
     * @param segments
     *            segments (must not be null)
     */
    public UID(String... segments) {
        if (segments == null) {
            throw new IllegalArgumentException("Given segments argument must not be null.");
        }
        int numberOfSegments = getMinimalNumberOfSegments();
        if (segments.length < numberOfSegments) {
            throw new IllegalArgumentException("UID must have at least " + numberOfSegments + " segments.");
        }
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            validateSegment(segment, i, segments.length);
        }
        this.segments = segments;
    }

    /**
     * Specifies how many segments the UID has to have at least.
     *
     * @return
     */
    protected abstract int getMinimalNumberOfSegments();

    protected String[] getSegments() {
        return this.segments;
    }

    protected String getSegment(int segment) {
        return this.segments[segment];
    }

    protected void validateSegment(String segment, int index, int length) {
        if (!segment.matches(SEGMENT_PATTERN)) {
            throw new IllegalArgumentException("UID segment '" + segment
                    + "' contains invalid characters. Each segment of the UID must match the pattern [A-Za-z0-9_-]*.");
        }
    }

    /**
     * Returns the binding id.
     *
     * @return binding id
     */
    public String getBindingId() {
        return segments[0];
    }

    @Override
    public String toString() {
        return getAsString();
    }

    public String getAsString() {
        return Joiner.on(SEPARATOR).join(segments);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(segments);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UID other = (UID) obj;
        if (!Arrays.equals(segments, other.segments))
            return false;
        return true;
    }

}
