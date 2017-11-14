/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

/**
 * The {@link DeviceParameterClassEnum} lists all digitalSTROM-device parameter classes.
 *
 * @author Alexander Betker
 * @version digitalSTROM-API 1.14.5
 */
public enum DeviceParameterClassEnum {

    /**
     * communication specific parameters
     */
    CLASS_0(0),

    /**
     * digitalSTROM device specific parameters
     */
    CLASS_1(1),

    /**
     * function specific parameters
     */
    CLASS_3(3),

    /**
     * sensor event table
     */
    CLASS_6(6),

    /**
     * output status
     *
     * possible OffsetParameters:
     * - READ_OUTPUT
     */
    CLASS_64(64),

    /**
     * read scene table
     * use index/offset 0-127
     */
    CLASS_128(128);

    private final int classIndex;

    DeviceParameterClassEnum(int index) {
        this.classIndex = index;
    }

    /**
     * Returns the index of the {@link DeviceParameterClassEnum}.
     *
     * @return index
     */
    public int getClassIndex() {
        return this.classIndex;
    }
}
