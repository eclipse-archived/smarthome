/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

/**
 * The {@link DeviceStateUpdateImpl} is the implementation of the {@link DeviceStateUpdate}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DeviceStateUpdateImpl implements DeviceStateUpdate {

    private final String UPDATE_TYPE;
    private final int VALUE;

    public DeviceStateUpdateImpl(String updateType, int value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    @Override
    public int getValue() {
        return VALUE;
    }

    @Override
    public String getType() {
        return UPDATE_TYPE;
    }
}
