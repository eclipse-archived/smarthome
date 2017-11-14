/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public enum UpDownType implements PrimitiveType, State, Command {
    UP,
    DOWN;

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toString());
    }

    @Override
    public String toString() {
        return toFullString();
    }

    @Override
    public String toFullString() {
        return super.toString();
    }

    @Override
    public State as(Class<? extends State> target) {
        if (target == DecimalType.class) {
            return equals(UP) ? DecimalType.ZERO : new DecimalType(new BigDecimal("1.0"));
        } else if (target == PercentType.class) {
            return equals(UP) ? PercentType.ZERO : PercentType.HUNDRED;
        } else {
            return State.super.as(target);
        }
    }

}
