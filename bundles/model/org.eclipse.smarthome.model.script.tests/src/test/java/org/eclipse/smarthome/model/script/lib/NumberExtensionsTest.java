/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.lib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.junit.Test;

/**
 *
 * @author Henning Treu - initial contribution
 *
 */
public class NumberExtensionsTest {

    private static final DecimalType DECIMAL1 = new DecimalType(1);
    private static final DecimalType DECIMAL2 = new DecimalType(2);

    private static final QuantityType<Temperature> Q_CELSIUS_1 = new QuantityType<Temperature>("1 °C");
    private static final QuantityType<Temperature> Q_CELSIUS_2 = new QuantityType<Temperature>("2 °C");

    @Test
    public void operatorPlus_Number_Number() {
        assertThat(NumberExtensions.operator_plus(DECIMAL1, DECIMAL2), is(BigDecimal.valueOf(3)));
    }

    @Test
    public void operatorPlus_Quantity_Quantity() {
        assertThat(NumberExtensions.operator_plus(Q_CELSIUS_1, Q_CELSIUS_2), is(QuantityType.valueOf("3 °C")));
    }

}
