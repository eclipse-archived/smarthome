package org.eclipse.smarthome.core.types;

import java.util.Locale;

import javax.measure.Unit;

public interface UnitProvider {

    /**
     * Retrieve the {@link Unit} matching the given {@link Dimension} and {@link Locale}.
     *
     * @param dimension The {@link Dimension} defines the base unit for the retrieved unit.
     * @param locale The {@link Locale} defines the concrete unit for the given {@link Dimension}.
     * @return The {@link Unit} matching the given {@link Dimension} and {@link Locale}.
     */
    Unit<?> getUnit(Dimension dimension, Locale locale);

}
