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
package org.eclipse.smarthome.binding.iota.internal;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;

/**
 * Implements a percentage value. Minimum and maximum are definable.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class PercentValue implements AbstractIotaThingValue {
    private final Boolean isFloat;

    private PercentType percentValue = null;

    public PercentValue(Boolean isFloat) {
        this.isFloat = isFloat == null ? true : isFloat;
    }

    @Override
    public State getValue() {
        return percentValue;
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        percentValue = PercentType.valueOf(updatedValue);
        return percentValue;
    }
}
