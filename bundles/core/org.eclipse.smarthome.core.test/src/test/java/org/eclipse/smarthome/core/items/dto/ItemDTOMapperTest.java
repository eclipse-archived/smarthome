/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * @author Stefan Triller - initial contribution
 */
package org.eclipse.smarthome.core.items.dto;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction;
import org.junit.Test;

public class ItemDTOMapperTest {

    @Test
    public void testMapFunctionWithNumberItemAndCountFunction() {

        // testing Group:Number:Count(".*hello.*")
        NumberItem item1 = new NumberItem("item1");

        GroupFunctionDTO gFuncDTO = new GroupFunctionDTO();
        gFuncDTO.name = "COUNT";
        gFuncDTO.params = new String[] { ".*hello.*" };

        GroupFunction gFunc = ItemDTOMapper.mapFunction(item1, gFuncDTO);

        assertThat(gFunc, instanceOf(ArithmeticGroupFunction.Count.class));
    }

}
