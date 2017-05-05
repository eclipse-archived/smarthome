/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.fields;

/**
 * @author Wouter Born - Add Thing properties
 */
public class Version {

    private long major;
    private long minor;

    public Version(long major, long minor) {
        this.major = major;
        this.minor = minor;
    }

    public long getMajor() {
        return major;
    }

    public long getMinor() {
        return minor;
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

}
