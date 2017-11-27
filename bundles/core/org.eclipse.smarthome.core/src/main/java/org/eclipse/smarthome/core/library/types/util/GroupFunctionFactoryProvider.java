/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types.util;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.NumberItem;

/**
 *
 * @author Henning Treu - initial contribution
 *
 */
public class GroupFunctionFactoryProvider {

    public GroupFunctionFactory provideGroupFunctionFactory(Item baseItem) {
        if (baseItem instanceof NumberItem && ((NumberItem) baseItem).getDimension() != null) {
            return new DimensionGroupFunctionFactory(((NumberItem) baseItem).getDimension());
        }
        return new DefaultGroupFunctionFactory();
    }

}
