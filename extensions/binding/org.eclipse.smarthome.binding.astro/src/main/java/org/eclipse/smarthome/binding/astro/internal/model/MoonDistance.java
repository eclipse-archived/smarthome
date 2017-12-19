/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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

import java.util.Calendar;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.smarthome.binding.astro.internal.util.DateTimeUtils;

/**
 * Holds a distance informations.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class MoonDistance {
    private static final double KM_TO_MILES = 0.621371192;

    private Calendar date;
    private double kilometer;

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
    public double getKilometer() {
        return kilometer;
    }

    /**
     * Sets the distance in kilometers.
     */
    public void setKilometer(double kilometer) {
        this.kilometer = kilometer;
    }

    /**
     * Returns the distance in miles.
     */
    public double getMiles() {
        return kilometer * KM_TO_MILES;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("date", DateTimeUtils.getDate(date))
                .append("kilometer", kilometer).append("miles", getMiles()).toString();
    }

}
