/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
