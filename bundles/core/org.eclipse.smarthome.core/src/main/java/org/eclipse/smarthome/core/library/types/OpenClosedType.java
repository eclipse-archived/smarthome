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
package org.eclipse.smarthome.core.library.types;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public enum OpenClosedType implements PrimitiveType, State, Command {
    OPEN,
    CLOSED;

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
            return this == OPEN ? new DecimalType(1) : DecimalType.ZERO;
        } else if (target == PercentType.class) {
            return this == OPEN ? PercentType.HUNDRED : PercentType.ZERO;
        } else {
            return State.super.as(target);
        }
    }

}
