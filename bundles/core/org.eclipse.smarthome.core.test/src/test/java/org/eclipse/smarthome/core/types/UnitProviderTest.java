package org.eclipse.smarthome.core.types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import javax.measure.Quantity;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.junit.Test;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

public class UnitProviderTest {

    @Test
    public void testinHg2PascalConversion() {
        Quantity<Pressure> inHg = Quantities.getQuantity(BigDecimal.ONE, ESHUnits.INCH_OF_MERCURY);

        assertThat(inHg.to(Units.PASCAL), is(Quantities.getQuantity(new BigDecimal("3386.388"), Units.PASCAL)));
        assertThat(inHg.to(ESHUnits.HECTO_PASCAL),
                is(Quantities.getQuantity(new BigDecimal("33.86388"), ESHUnits.HECTO_PASCAL)));
    }
    
    @Test
    public void test_inHg_UnitSymbol() {
        assertThat(ESHUnits.INCH_OF_MERCURY.getSymbol(), is("inHg"));
        assertThat(ESHUnits.INCH_OF_MERCURY.toString(), is("inHg"));
    }

    @Test
    public void testPascal2inHgConversion() {
        Quantity<Pressure> pascal = Quantities.getQuantity(new BigDecimal("3386.388"), Units.PASCAL);

        assertThat(pascal.to(ESHUnits.INCH_OF_MERCURY),
                is(Quantities.getQuantity(new BigDecimal("1.000"), ESHUnits.INCH_OF_MERCURY)));
    }

    @Test
    public void testHectoPascal2Pascal() {
        Quantity<Pressure> pascal = Quantities.getQuantity(BigDecimal.valueOf(100), Units.PASCAL);

        assertThat(pascal.to(ESHUnits.HECTO_PASCAL),
                is(Quantities.getQuantity(BigDecimal.ONE, ESHUnits.HECTO_PASCAL)));
    }

    @Test
    public void test_hPa_UnitSymbol() {
        assertThat(ESHUnits.HECTO_PASCAL.toString(), is("hPa"));
    }
    
    @Test
    public void testKelvin2Fahrenheit() {
        Quantity<Temperature> kelvin = Quantities.getQuantity(BigDecimal.ZERO, ESHUnits.KELVIN);
        
        assertThat(kelvin.to(ESHUnits.FAHRENHEIT), is(Quantities.getQuantity(new BigDecimal("-459.67"), ESHUnits.FAHRENHEIT)));
    }

    @Test
    public void testKelvin2Fahrenheit2() {
        Quantity<Temperature> kelvin = Quantities.getQuantity(new BigDecimal(300), ESHUnits.KELVIN);
        
        assertThat(kelvin.to(ESHUnits.FAHRENHEIT), is(Quantities.getQuantity(new BigDecimal("80.33"), ESHUnits.FAHRENHEIT)));
    }
    
    @Test
    public void testFahrenheit2Kelvin() {
        Quantity<Temperature> fahrenheit = Quantities.getQuantity(new BigDecimal(100), ESHUnits.FAHRENHEIT);
        
        assertThat(fahrenheit.to(ESHUnits.KELVIN), is(Quantities.getQuantity(new BigDecimal("310.9277777777777777777777777777778"), ESHUnits.KELVIN)));
    }

    @Test
    public void test_fahrenheit_UnitSymbol() {
        assertThat(ESHUnits.FAHRENHEIT.getSymbol(), is("°F"));
        assertThat(ESHUnits.FAHRENHEIT.toString(), is("°F"));
    }

}
