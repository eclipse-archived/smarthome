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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Holds the calculated direct, diffuse and total
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Radiation {
    private double direct;
    private double diffuse;
    private double total;

    public Radiation() {
    }

    public Radiation(double direct, double diffuse, double total) {
        this.direct = direct;
        this.diffuse = diffuse;
        this.total = total;
    }

    /**
     * Sets the direct radiation.
     */
    public void setDirect(double direct) {
        this.direct = direct;
    }

    /**
     * Sets the diffuse radiation.
     */
    public void setDiffuse(double diffuse) {
        this.diffuse = diffuse;
    }

    /**
     * Sets the total radiation.
     */
    public void setTotal(double total) {
        this.total = total;
    }

    /**
     * Returns the total radiation.
     */
    public double getTotal() {
        return total;
    }

    /**
     * Returns the direct radiation.
     */
    public double getDirect() {
        return direct;
    }

    /**
     * Returns the diffuse radiation.
     */
    public double getDiffuse() {
        return diffuse;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("direct", direct)
                .append("diffuse", diffuse).append("total", total).toString();
    }
}
