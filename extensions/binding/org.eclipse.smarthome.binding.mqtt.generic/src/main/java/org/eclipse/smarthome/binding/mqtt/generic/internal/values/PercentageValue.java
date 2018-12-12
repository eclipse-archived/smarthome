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
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements a percentage value. Minimum and maximum are definable.
 *
 * <p>
 * Accepts user updates from a DecimalType, IncreaseDecreaseType and UpDownType.
 * If this is a percent value, PercentType
 * </p>
 * Accepts MQTT state updates as DecimalType, IncreaseDecreaseType and UpDownType
 * StringType with comma separated HSB ("h,s,b"), RGB ("r,g,b") and on, off strings.
 * On, Off strings can be customized.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class PercentageValue extends Value {
    private final double min;
    private final double max;
    private final double step;

    public PercentageValue(@Nullable BigDecimal min, @Nullable BigDecimal max, @Nullable BigDecimal step) {
        super(CoreItemFactory.DIMMER, Stream.of(DecimalType.class, IncreaseDecreaseType.class, UpDownType.class)
                .collect(Collectors.toList()));
        this.min = min == null ? 0.0 : min.doubleValue();
        this.max = max == null ? 100.0 : max.doubleValue();
        if (this.min >= this.max) {
            throw new IllegalArgumentException("Min need to be smaller than max!");
        }
        this.step = step == null ? 1.0 : step.doubleValue();
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        PercentType oldvalue = (state == UnDefType.UNDEF) ? new PercentType() : (PercentType) state;
        if (command instanceof PercentType) {
            state = (PercentType) command;
        } else if (command instanceof DecimalType) {
            double v = ((DecimalType) command).doubleValue();
            v = (v - min) * 100.0 / (max - min);
            state = new PercentType(new BigDecimal(v));
        } else if (command instanceof IncreaseDecreaseType) {
            if (((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE) {
                final double v = oldvalue.doubleValue() + step;
                state = new PercentType(new BigDecimal(v <= max ? v : max));
            } else {
                double v = oldvalue.doubleValue() - step;
                state = new PercentType(new BigDecimal(v >= min ? v : min));
            }
        } else if (command instanceof UpDownType) {
            if (((UpDownType) command) == UpDownType.UP) {
                final double v = oldvalue.doubleValue() + step;
                state = new PercentType(new BigDecimal(v <= max ? v : max));
            } else {
                final double v = oldvalue.doubleValue() - step;
                state = new PercentType(new BigDecimal(v >= min ? v : min));
            }
        } else {
            state = PercentType.valueOf(command.toString());
        }
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(new BigDecimal(min), new BigDecimal(max), new BigDecimal(step),
                "%s " + unit.replace("%", "%%"), readOnly, Collections.emptyList());
    }
}
