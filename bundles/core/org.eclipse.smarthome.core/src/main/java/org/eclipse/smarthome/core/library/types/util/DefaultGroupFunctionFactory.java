/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types.util;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.items.dto.GroupFunctionDTO;
import org.eclipse.smarthome.core.items.dto.ItemDTOMapper;
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public class DefaultGroupFunctionFactory implements GroupFunctionFactory {

    @Override
    public @Nullable GroupFunction createGroupFunction(GroupFunctionDTO function, List<State> args) {
        String functionName = function.name;
        switch (functionName.toUpperCase()) {
            case "AND":
                if (args.size() == 2) {
                    return new ArithmeticGroupFunction.And(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(GroupFunction.class)
                            .error("Group function 'AND' requires two arguments. Using Equality instead.");
                }
                break;
            case "OR":
                if (args.size() == 2) {
                    return new ArithmeticGroupFunction.Or(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(GroupFunction.class)
                            .error("Group function 'OR' requires two arguments. Using Equality instead.");
                }
                break;
            case "NAND":
                if (args.size() == 2) {
                    return new ArithmeticGroupFunction.NAnd(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(GroupFunction.class)
                            .error("Group function 'NOT AND' requires two arguments. Using Equality instead.");
                }
                break;
            case "NOR":
                if (args.size() == 2) {
                    return new ArithmeticGroupFunction.NOr(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(GroupFunction.class)
                            .error("Group function 'NOT OR' requires two arguments. Using Equality instead.");
                }
                break;
            case "COUNT":
                if (function.params != null && function.params.length == 1) {
                    State countParam = new StringType(function.params[0]);
                    return new ArithmeticGroupFunction.Count(countParam);
                } else {
                    LoggerFactory.getLogger(ItemDTOMapper.class)
                            .error("Group function 'COUNT' requires one argument. Using Equality instead.");
                }
                break;
            case "AVG":
                return new ArithmeticGroupFunction.Avg();
            case "SUM":
                return new ArithmeticGroupFunction.Sum();
            case "MIN":
                return new ArithmeticGroupFunction.Min();
            case "MAX":
                return new ArithmeticGroupFunction.Max();
            case "EQUAL":
                return new GroupFunction.Equality();
            default:
                LoggerFactory.getLogger(GroupFunction.class)
                        .error("Unknown group function '{}'. Using Equality instead.", functionName);
        }

        return new GroupFunction.Equality();
    }

}
