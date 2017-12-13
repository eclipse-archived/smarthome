/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import static org.junit.Assert.assertTrue;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.types.DimensionClassParser;
import org.junit.Test;

public class DimensionClassParserTest {

    @Test
    public void whenValidDimensionIsGiven_shouldCreateQuantityClass() {
        Class<? extends Quantity<?>> temperature = DimensionClassParser.parseDimension("Temperature");

        assertTrue(Temperature.class.isAssignableFrom(temperature));
    }

}
