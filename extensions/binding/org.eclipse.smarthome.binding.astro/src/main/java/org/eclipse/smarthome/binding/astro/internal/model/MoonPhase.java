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

import java.util.Calendar;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.smarthome.binding.astro.internal.util.DateTimeUtils;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

/**
 * Holds the calculates moon phase informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
public class MoonPhase {
    private Calendar firstQuarter;
    private Calendar full;
    private Calendar thirdQuarter;
    private Calendar _new;
    private int age;
    private double illumination;
    private double agePercent;
    private double ageDegree;

    private MoonPhaseName name;

    /**
     * Returns the date at which the moon is in the first quarter.
     */
    public Calendar getFirstQuarter() {
        return firstQuarter;
    }

    /**
     * Sets the date at which the moon is in the first quarter.
     */
    public void setFirstQuarter(Calendar firstQuarter) {
        this.firstQuarter = firstQuarter;
    }

    /**
     * Returns the date of the full moon.
     */
    public Calendar getFull() {
        return full;
    }

    /**
     * Sets the date of the full moon.
     */
    public void setFull(Calendar full) {
        this.full = full;
    }

    /**
     * Returns the date at which the moon is in the third quarter.
     */
    public Calendar getThirdQuarter() {
        return thirdQuarter;
    }

    /**
     * Sets the date at which the moon is in the third quarter.
     */
    public void setThirdQuarter(Calendar thirdQuarter) {
        this.thirdQuarter = thirdQuarter;
    }

    /**
     * Returns the date of the new moon.
     */
    public Calendar getNew() {
        return _new;
    }

    /**
     * Sets the date of the new moon.
     */
    public void setNew(Calendar _new) {
        this._new = _new;
    }

    /**
     * Returns the age in days.
     */
    public QuantityType<Time> getAge() {
        return new QuantityType<Time>(age, SmartHomeUnits.DAY);
    }

    /**
     * Sets the age in days.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Returns the illumination.
     */
    public QuantityType<Dimensionless> getIllumination() {
        return new QuantityType<Dimensionless>(illumination, SmartHomeUnits.PERCENT);
    }

    /**
     * Sets the illumination.
     */
    public void setIllumination(double illumination) {
        this.illumination = illumination;
    }

    /**
     * Returns the phase name.
     */
    public MoonPhaseName getName() {
        return name;
    }

    /**
     * Sets the phase name.
     */
    public void setName(MoonPhaseName name) {
        this.name = name;
    }

    /**
     * Returns the age in degree.
     */
    public QuantityType<Angle> getAgeDegree() {
        return new QuantityType<Angle>(ageDegree, SmartHomeUnits.DEGREE_ANGLE);
    }

    /**
     * Sets the age in degree.
     */
    public void setAgeDegree(double ageDegree) {
        this.ageDegree = ageDegree;
    }

    /**
     * Returns the age in percent.
     */
    public QuantityType<Dimensionless> getAgePercent() {
        return new QuantityType<Dimensionless>(agePercent, SmartHomeUnits.PERCENT);
    }

    /**
     * Sets the age in percent.
     */
    public void setAgePercent(double agePercent) {
        this.agePercent = agePercent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("firstQuarter", DateTimeUtils.getDate(firstQuarter)).append("full", DateTimeUtils.getDate(full))
                .append("thirdQuarter", DateTimeUtils.getDate(thirdQuarter)).append("new", DateTimeUtils.getDate(_new))
                .append("age", age).append("ageDegree", ageDegree).append("agePercent", agePercent)
                .append("illumination", illumination).append("name", name).toString();
    }
}
