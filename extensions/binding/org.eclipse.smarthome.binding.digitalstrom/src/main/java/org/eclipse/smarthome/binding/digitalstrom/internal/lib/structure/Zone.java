/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;

/**
 * The {@link Zone} represents a digitalSTROM-Zone.
 *
 * @author Alexander Betker
 *
 * @author Michael Ochel - add java-doc
 * @author Matthias Siegele - add java-doc
 */
public interface Zone {

    /**
     * Returns the zone id of this {@link Zone}.
     *
     * @return zoneID
     */
    public int getZoneId();

    /**
     * Sets the zone id of this {@link Zone}.
     */
    public void setZoneId(int id);

    /**
     * Returns the zone name of this {@link Zone}.
     *
     * @return zone name
     */
    public String getName();

    /**
     * Sets the zone name of this {@link Zone}.
     */
    public void setName(String name);

    /**
     * Returns the {@link List} of all included groups as {@link DetailedGroupInfo}.
     *
     * @return list of all groups
     */
    public List<DetailedGroupInfo> getGroups();

    /**
     * Adds a group as {@link DetailedGroupInfo}.
     *
     * @param group
     */
    public void addGroup(DetailedGroupInfo group);

    /**
     * Returns a {@link List} of all included {@link Device}'s.
     *
     * @return device list
     */
    public List<Device> getDevices();

    /**
     * Adds a {@link Device} to this {@link Zone}.
     *
     * @param device
     */
    public void addDevice(Device device);
}
