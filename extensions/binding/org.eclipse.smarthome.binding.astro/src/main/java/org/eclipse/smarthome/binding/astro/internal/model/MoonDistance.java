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
package org.eclipse.smarthome.binding.astro.internal.model;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.KILO;
import static org.eclipse.smarthome.core.library.unit.SIUnits.METRE;

import java.util.Calendar;

import javax.measure.quantity.Length;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.smarthome.binding.astro.internal.util.DateTimeUtils;
import org.eclipse.smarthome.core.library.types.QuantityType;

/**
 * Holds a distance informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
public class MoonDistance {

    private Calendar date;
    private double distance;

    /**
     * Returns the date of the calculated distance.
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Sets the date of the calculated distance.
     */
    public void setDate(Calendar date) {
        this.date = date;
    }

    /**
     * Returns the distance in kilometers.
     */
    public QuantityType<Length> getDistance() {
        return new QuantityType<Length>(distance, KILO(METRE));
    }

    /**
     * Sets the distance in kilometers.
     */
    public void setDistance(double kilometer) {
        this.distance = kilometer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("date", DateTimeUtils.getDate(date))
                .append("distance", distance).toString();
    }
}
