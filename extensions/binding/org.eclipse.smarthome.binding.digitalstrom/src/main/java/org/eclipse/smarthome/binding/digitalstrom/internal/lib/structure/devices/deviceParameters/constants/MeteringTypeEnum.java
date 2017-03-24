/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants;

import java.util.Arrays;
import java.util.List;

/**
 * The {@link MeteringTypeEnum} lists all available digitalSTROM metering types.
 *
 * @author Alexander Betker - initial contributer
 * @author Michael Ochel - add MeteringUnitEnum list
 * @author Matthias Siegele - add MeteringUnitEnum list
 */
public enum MeteringTypeEnum {
    ENERGY(Arrays.asList(MeteringUnitsEnum.WH, MeteringUnitsEnum.WS)),
    // currently null by request getLast
    // energyDelta(Lists.newArrayList(MeteringUnitsEnum.Wh, MeteringUnitsEnum.Ws)),
    CONSUMPTION(Arrays.asList(MeteringUnitsEnum.WH));

    private final List<MeteringUnitsEnum> meteringUnits;

    private MeteringTypeEnum(List<MeteringUnitsEnum> meteringUnits) {
        this.meteringUnits = meteringUnits;
    }

    /**
     * Returns the available units as {@link List} for this {@link MeteringTypeEnum}.
     *
     * @return units
     */
    public List<MeteringUnitsEnum> getMeteringUnitList() {
        return meteringUnits;
    }
}
