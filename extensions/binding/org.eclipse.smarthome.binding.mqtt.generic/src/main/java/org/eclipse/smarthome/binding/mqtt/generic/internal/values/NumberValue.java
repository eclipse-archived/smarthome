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
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements a number value.
 *
 * <p>
 * Accepts user updates and MQTT state updates from a DecimalType, IncreaseDecreaseType and UpDownType.
 * </p>
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NumberValue extends Value {
    private final @Nullable BigDecimal min;
    private final @Nullable BigDecimal max;
    private final BigDecimal step;

    public NumberValue(@Nullable BigDecimal min, @Nullable BigDecimal max, @Nullable BigDecimal step) {
        super(CoreItemFactory.NUMBER, Stream.of(DecimalType.class, IncreaseDecreaseType.class, UpDownType.class)
                .collect(Collectors.toList()));
        this.min = min;
        this.max = max;
        this.step = step == null ? new BigDecimal(1.0) : step;
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        DecimalType oldvalue = (state == UnDefType.UNDEF) ? new DecimalType() : (DecimalType) state;
        if (command instanceof DecimalType) {
            state = (DecimalType) command;
        } else if (command instanceof IncreaseDecreaseType || command instanceof UpDownType) {
            if (command == IncreaseDecreaseType.INCREASE || command == UpDownType.UP) {
                state = new DecimalType(oldvalue.toBigDecimal().add(step));
            } else {
                state = new DecimalType(oldvalue.toBigDecimal().subtract(step));
            }
        } else {
            state = DecimalType.valueOf(command.toString());
        }
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(min, max, step, "%s " + unit.replace("%", "%%"), readOnly, Collections.emptyList());
    }
}
