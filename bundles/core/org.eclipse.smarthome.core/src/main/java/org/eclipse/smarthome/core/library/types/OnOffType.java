/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public enum OnOffType implements PrimitiveType, State, Command {
    ON,
    OFF;

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
            return this == ON ? new DecimalType(1) : DecimalType.ZERO;
        } else if (target == PercentType.class) {
            return this == ON ? PercentType.HUNDRED : PercentType.ZERO;
        } else if (target == HSBType.class) {
            return this == ON ? HSBType.WHITE : HSBType.BLACK;
        } else {
            return State.super.as(target);
        }
    }

}
