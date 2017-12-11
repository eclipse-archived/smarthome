package org.eclipse.smarthome.core.types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

import org.junit.Test;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class ESHUnitsTest {

    private static final double DEFAULT_ERROR = 0.0000000000000001d;

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

        assertThat(pascal.to(ESHUnits.HECTO_PASCAL), is(Quantities.getQuantity(BigDecimal.ONE, ESHUnits.HECTO_PASCAL)));
    }

    @Test
    public void test_hPa_UnitSymbol() {
        assertThat(ESHUnits.HECTO_PASCAL.toString(), is("hPa"));
    }

    @Test
    public void testKelvin2Fahrenheit() {
        Quantity<Temperature> kelvin = Quantities.getQuantity(BigDecimal.ZERO, Units.KELVIN);

        assertThat(kelvin.to(ESHUnits.FAHRENHEIT),
                is(Quantities.getQuantity(new BigDecimal("-459.67"), ESHUnits.FAHRENHEIT)));
    }

    @Test
    public void testKelvin2Fahrenheit2() {
        Quantity<Temperature> kelvin = Quantities.getQuantity(new BigDecimal(300), Units.KELVIN);

        assertThat(kelvin.to(ESHUnits.FAHRENHEIT),
                is(Quantities.getQuantity(new BigDecimal("80.33"), ESHUnits.FAHRENHEIT)));
    }

    @Test
    public void testFahrenheit2Kelvin() {
        Quantity<Temperature> fahrenheit = Quantities.getQuantity(new BigDecimal(100), ESHUnits.FAHRENHEIT);

        Quantity<Temperature> kelvin = fahrenheit.to(Units.KELVIN);
        assertThat(kelvin.getUnit(), is(Units.KELVIN));
        assertThat(kelvin.getValue().doubleValue(), is(closeTo(310.92777777777777778d, DEFAULT_ERROR)));
    }

    @Test
    public void testKmh2Mih() {
        Quantity<Speed> kmh = Quantities.getQuantity(new BigDecimal(10), Units.KILOMETRE_PER_HOUR);

        Quantity<Speed> mph = kmh.to(ESHUnits.MILES_PER_HOUR);
        assertThat(mph.getUnit(), is(ESHUnits.MILES_PER_HOUR));
        assertThat(mph.getValue().doubleValue(), is(closeTo(6.21371192237333935d, DEFAULT_ERROR)));
    }

    @Test
    public void testCm2In() {
        Quantity<Length> cm = Quantities.getQuantity(new BigDecimal(10), MetricPrefix.CENTI(Units.METRE));

        Quantity<Length> in = cm.to(ESHUnits.INCH);
        assertThat(in.getUnit(), is(ESHUnits.INCH));
        assertThat(in.getValue().doubleValue(), is(closeTo(3.93700787401574803d, DEFAULT_ERROR)));
    }

    @Test
    public void testM2Ml() {
        Quantity<Length> km = Quantities.getQuantity(new BigDecimal(10), MetricPrefix.KILO(Units.METRE));

        Quantity<Length> mile = km.to(ESHUnits.MILE);
        assertThat(mile.getUnit(), is(ESHUnits.MILE));
        assertThat(mile.getValue().doubleValue(), is(closeTo(6.2137119223733395d, DEFAULT_ERROR)));
    }

    @Test
    public void test_fahrenheit_UnitSymbol() {
        assertThat(ESHUnits.FAHRENHEIT.getSymbol(), is("°F"));
        assertThat(ESHUnits.FAHRENHEIT.toString(), is("°F"));
    }

    @Test
    public void test_inch_UnitSymbol() {
        assertThat(ESHUnits.INCH.getSymbol(), is("in"));
        assertThat(ESHUnits.INCH.toString(), is("in"));
    }

    @Test
    public void test_mile_UnitSymbol() {
        assertThat(ESHUnits.MILE.getSymbol(), is("mi"));
        assertThat(ESHUnits.MILE.toString(), is("mi"));
    }

}
