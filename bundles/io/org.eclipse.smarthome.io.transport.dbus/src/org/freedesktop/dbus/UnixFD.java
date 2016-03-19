/*
   D-Bus Java Implementation
   Copyright (c) 2005-2006 Matthew Johnson

   This program is free software; you can redistribute it and/or modify it
   under the terms of either the GNU Lesser General Public License Version 2 or the
   Academic Free Licence Version 2.1.

   Full licence texts are included in the COPYING file with this program.
*/
package org.freedesktop.dbus;

/**
 * Class to represent a Unix File Handle over DBus
 */
@SuppressWarnings("serial")
public class UnixFD implements Comparable<UnixFD> {
    private Integer value;

    /**
     * Create a Handle from a long.
     *
     * @param value Must be a valid integer
     */
    public UnixFD(int value) {
        this.value = value;
    }

    /** Test two Handles for equality. */
    @Override
    public boolean equals(Object o) {
        return o instanceof UnixFD && this.value.equals(((UnixFD) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Compare two Handles.
     *
     * @return 0 if equal, -ve or +ve if they are different.
     */
    @Override
    public int compareTo(UnixFD other) {
        return this.value.compareTo(other.value);
    }

    /** The value of this as a string. */
    @Override
    public String toString() {
        return value.toString();
    }

    public int getValue() {
        return value;
    }
}
