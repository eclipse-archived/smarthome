package org.eclipse.smarthome.core.types.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.smarthome.core.types.Dimension;
import org.eclipse.smarthome.core.types.ESHUnits;
import org.eclipse.smarthome.core.types.UnitProvider;
import org.osgi.service.component.annotations.Component;

import tec.uom.se.unit.Units;

@Component(service = UnitProvider.class, name = "unitProvider")
public class DefaultUnitProvider implements UnitProvider {

    private Map<Dimension, Map<MeasurementSystem, Unit<?>>> dimensionMap;

    public DefaultUnitProvider() {
        initDefaultMap();
    }

    @Override
    public Unit<?> getUnit(Dimension dimension, Locale locale) {
        Map<MeasurementSystem, Unit<?>> unitMap = dimensionMap.get(dimension);
        
        MeasurementSystem system = getMeasurmenetSystem(locale);
        return unitMap.get(system);
    }
    
    private MeasurementSystem getMeasurmenetSystem(Locale locale) {
        // Only US and Liberia use the Imperial System.
        if (Locale.US.equals(locale) || Locale.forLanguageTag("en-LR").equals(locale)) {
            return MeasurementSystem.US;
        }
        return MeasurementSystem.SI;
    }

    private void initDefaultMap() {
        dimensionMap = new HashMap<>();

        Map<MeasurementSystem, Unit<?>> temperatureMap = new HashMap<>();
        temperatureMap.put(MeasurementSystem.SI, ESHUnits.CELSIUS);
        temperatureMap.put(MeasurementSystem.US, ESHUnits.FAHRENHEIT);
        dimensionMap.put(Dimension.TEMPERATURE, temperatureMap);

        Map<MeasurementSystem, Unit<?>> pressureMap = new HashMap<>();
        pressureMap.put(MeasurementSystem.SI, ESHUnits.HECTO_PASCAL);
        pressureMap.put(MeasurementSystem.US, ESHUnits.INCH_OF_MERCURY);
        dimensionMap.put(Dimension.PRESSURE, pressureMap);

        Map<MeasurementSystem, Unit<?>> speedMap = new HashMap<>();
        speedMap.put(MeasurementSystem.SI, Units.KILOMETRE_PER_HOUR);
        speedMap.put(MeasurementSystem.US, ESHUnits.MILES_PER_HOUR);
        dimensionMap.put(Dimension.SPEED, speedMap);

        Map<MeasurementSystem, Unit<?>> lengthMap = new HashMap<>();
        lengthMap.put(MeasurementSystem.SI, Units.METRE);
        lengthMap.put(MeasurementSystem.US, ESHUnits.INCH);
        dimensionMap.put(Dimension.LENGTH, lengthMap);

        Map<MeasurementSystem, Unit<?>> intensityMap = new HashMap<>();
        intensityMap.put(MeasurementSystem.SI, ESHUnits.IRRADIANCE);
        intensityMap.put(MeasurementSystem.US, ESHUnits.IRRADIANCE);
        dimensionMap.put(Dimension.INTENSITY, intensityMap);
    }

    private enum MeasurementSystem {
        SI, // metric measurement system
        US  // imperial measurement system
    }
}