/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.lib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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

    private static final QuantityType<Temperature> Q_LENGTH_1 = new QuantityType<Temperature>("1 m");
    private static final QuantityType<Temperature> Q_LENGTH_2 = new QuantityType<Temperature>("2 cm");

    @Test
    public void operatorPlus_Number_Number() {
        assertThat(NumberExtensions.operator_plus(DECIMAL1, DECIMAL2), is(BigDecimal.valueOf(3)));
    }

    @Test
    public void operatorPlus_Quantity_Quantity() {
        assertThat(NumberExtensions.operator_plus(Q_CELSIUS_1, Q_CELSIUS_2), is(QuantityType.valueOf("3 °C")));
    }

    @Test
    public void operatorMinus_Number() {
        assertThat(NumberExtensions.operator_minus(DECIMAL1), is(BigDecimal.valueOf(-1)));
    }

    @Test
    public void operatorMinus_Quantity() {
        assertThat(NumberExtensions.operator_minus(Q_CELSIUS_1), is(QuantityType.valueOf("-1 °C")));
    }

    @Test
    public void operatorMinus_Number_Number() {
        assertThat(NumberExtensions.operator_minus(DECIMAL2, DECIMAL1), is(BigDecimal.valueOf(1)));
    }

    @Test
    public void operatorMinus_Quantity_Quantity() {
        assertThat(NumberExtensions.operator_minus(Q_LENGTH_1, Q_LENGTH_2), is(QuantityType.valueOf("0.98 m")));
    }

    @Test
    public void operatorMultiply_Number_Quantity() {
        assertThat(NumberExtensions.operator_multiply(DECIMAL2, Q_LENGTH_2), is(QuantityType.valueOf("4 cm")));
    }

    @Test
    public void operatorMultiply_Quantity_Quantity() {
        assertThat(NumberExtensions.operator_multiply(Q_LENGTH_1, Q_LENGTH_2), is(QuantityType.valueOf("2 m·cm")));
    }

    @Test
    public void operatorDivide_Quantity_Number() {
        assertThat(NumberExtensions.operator_divide(Q_LENGTH_1, DECIMAL2), is(QuantityType.valueOf("0.5 m")));
    }

    @Test
    public void operatorDivide_Quantity_Quantity() {
        assertThat(NumberExtensions.operator_divide(Q_LENGTH_1, Q_LENGTH_2), is(QuantityType.valueOf("0.5 m/cm")));
    }

    @Test
    public void operatorDivide_Numer_Quantity() {
        assertThat(NumberExtensions.operator_divide(DECIMAL1, Q_LENGTH_2), is(QuantityType.valueOf("0.5 one/cm")));
    }

    @Test
    public void operatorEquals_Numer_Quantity() {
        assertFalse(NumberExtensions.operator_equals((Number) DECIMAL1, Q_LENGTH_2));
    }

    @Test
    public void operatorEquals_Quantity_Number() {
        assertFalse(NumberExtensions.operator_equals((Number) Q_LENGTH_2, DECIMAL1));
    }

}
