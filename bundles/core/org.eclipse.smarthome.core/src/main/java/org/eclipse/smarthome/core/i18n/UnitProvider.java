/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.i18n;

import static tec.uom.se.unit.MetricPrefix.HECTO;

import javax.measure.Unit;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import tec.uom.se.unit.Units;

/**
 * Unit Provider.
 *
 * @author Gaël L'hopital - Initial contribution .
 */
public class UnitProvider extends Units {

    public static final Unit<Pressure> INCH_OF_MERCURY = PASCAL.multiply(3386.388); // .alternate("inHG");
    // .asType(Pressure.class);
    public static final Unit<Pressure> HECTO_PASCAL = HECTO(PASCAL); // .alternate("hPa");
    public static final Unit<Temperature> FAHRENHEIT = CELSIUS.multiply(1.8).shift(-32); // .alternate("°F");

}
