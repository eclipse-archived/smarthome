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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;

/**
 * Implements a number value.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class NumberValue implements AbstractIotaThingValue {
    private DecimalType numberValue = null;
    private final boolean isFloat;

    public NumberValue(Boolean isFloat) {
        this.isFloat = isFloat == null ? true : isFloat;
    }

    @Override
    public State getValue() {
        return numberValue;
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        numberValue = DecimalType.valueOf(updatedValue);
        return numberValue;
    }
}
