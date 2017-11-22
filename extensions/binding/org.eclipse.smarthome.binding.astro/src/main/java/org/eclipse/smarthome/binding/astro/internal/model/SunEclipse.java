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
 * Extends the eclipse object with the ring-like eclipse information.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SunEclipse extends Eclipse {
    private Calendar ring;

    /**
     * Returns the date of the next ring-like eclipse.
     */
    public Calendar getRing() {
        return ring;
    }

    /**
     * Sets the date of the next ring-like eclipse.
     */
    public void setRing(Calendar ring) {
        this.ring = ring;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("total", DateTimeUtils.getDate(getTotal()))
                .append("partial", DateTimeUtils.getDate(getPartial())).append("ring", DateTimeUtils.getDate(ring))
                .toString();
    }

}
