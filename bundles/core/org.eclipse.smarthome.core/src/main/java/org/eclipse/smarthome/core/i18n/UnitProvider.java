/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.i18n;

import java.awt.Dimension;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.spi.SystemOfUnits;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Provides {@link Unit}s and the current {@link MeasurementSystem}.
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public interface UnitProvider {

    /**
     * Retrieves the default {@link Unit} for the given {@link Dimension} according to the current
     * {@link MeasurementSystem}.
     *
     * @param dimension The {@link Dimension} defines the base unit for the retrieved unit.
     * @return The {@link Unit} matching the given {@link Dimension}, {@code null} otherwise.
     */
    <T extends Quantity<T>> @Nullable Unit<T> getUnit(@Nullable Class<T> dimension);

    /**
     * Returns the {@link MeasurementSystem} which is currently set, must not be null.
     *
     * @return the {@link MeasurementSystem} which is currently set, must not be null.
     */
    SystemOfUnits getMeasurementSystem();

}
