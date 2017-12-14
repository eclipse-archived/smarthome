/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.i18n;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.types.MeasurementSystem;

/**
 * Provide {@link Unit}s and the current {@link MeasurementSystem}.
 *
 * @author Henning Treu - initial contribution
 *
 */
public interface UnitProvider {

    /**
     * Retrieve the default {@link Unit} for the given {@link Dimension} according to the current
     * {@link MeasurementSystem}.
     *
     * @param dimension The {@link Dimension} defines the base unit for the retrieved unit.
     * @return The {@link Unit} matching the given {@link Dimension}.
     */
    @NonNull
    <T extends Quantity<T>> Unit<T> getUnit(@NonNull Class<? extends T> dimension);

    /**
     *
     * @return
     */
    @NonNull
    MeasurementSystem getMeasurementSystem();

}
