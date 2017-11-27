/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types.util;

import java.util.List;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.library.types.QuantityTypeArithmeticGroupFunction;
import org.eclipse.smarthome.core.types.State;

/**
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public class DimensionGroupFunctionFactory extends DefaultGroupFunctionFactory {

    private final Class<? extends Quantity<?>> dimension;

    public DimensionGroupFunctionFactory(Class<? extends Quantity<?>> dimension) {
        this.dimension = dimension;
    }

    @Override
    public @Nullable GroupFunction createGroupFunction(String functionName, List<State> args) {
        switch (functionName.toUpperCase()) {
            case "AVG":
                return new QuantityTypeArithmeticGroupFunction.Avg(dimension);
            case "SUM":
                return new QuantityTypeArithmeticGroupFunction.Sum(dimension);
            case "MIN":
                return new QuantityTypeArithmeticGroupFunction.Min(dimension);
            case "MAX":
                return new QuantityTypeArithmeticGroupFunction.Max(dimension);
            default:
                return super.createGroupFunction(functionName, args);
        }
    }

}
