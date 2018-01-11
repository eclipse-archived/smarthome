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
package org.eclipse.smarthome.core.thing;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.GenericUID;

/**
 * Base class for binding related unique identifiers within the SmartHome framework.
 * A UID must always start with a binding ID.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Added possibility to define UIDs with variable amount of segments
 * @author Jochen Hiller - Bugfix 455434: added default constructor, object is now mutable
 * @author Markus Rathgeb - Moved base non binding / thing related UID stuff to a base class
 */
@NonNullByDefault
public abstract class UID extends GenericUID {

    /**
     * For reflection only.
     * Constructor must be public, otherwise it can not be called by subclasses from another package.
     */
    public UID() {
        super();
    }

    /**
     * Parses a UID for a given string. The UID must be in the format
     * 'bindingId:segment:segment:...'.
     *
     * @param uid uid in form a string (must not be null)
     */
    public UID(String uid) {
        super(uid);
    }

    /**
     * Creates a UID for list of segments.
     *
     * @param segments segments (must not be null)
     */
    public UID(String... segments) {
        super(segments);
    }

    /**
     * Returns the binding id.
     *
     * @return binding id
     */
    public String getBindingId() {
        return getSegment(0);
    }

    protected String[] getSegments() {
        final List<String> segments = super.getAllSegments();
        return segments.toArray(new String[segments.size()]);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return super.equals(other);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getAsString() {
        return super.getAsString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
