/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.model;

import java.util.Calendar;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.smarthome.binding.astro.internal.util.DateTimeUtils;

/**
 * Range class which holds a start and a end calendar object.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Range {
    private Calendar start;
    private Calendar end;

    public Range() {
    }

    public Range(Calendar start, Calendar end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the start of the range.
     */
    public Calendar getStart() {
        return start;
    }

    /**
     * Returns the end of the range.
     */
    public Calendar getEnd() {
        return end;
    }

    /**
     * Returns the duration in minutes.
     */
    public long getDuration() {
        if (start == null || end == null) {
            return -1;
        }
        if (start.after(end)) {
            return 0;
        }
        long diff = end.getTimeInMillis() - start.getTimeInMillis();
        return diff / 60000;
    }

    /**
     * Returns true, if the given calendar matches into the range.
     */
    public boolean matches(Calendar cal) {
        if (start == null && end == null) {
            return false;
        }
        long matchStart = start != null ? start.getTimeInMillis()
                : DateTimeUtils.truncateToMidnight(cal).getTimeInMillis();
        long matchEnd = end != null ? end.getTimeInMillis() : DateTimeUtils.endOfDayDate(cal).getTimeInMillis();
        return cal.getTimeInMillis() >= matchStart && cal.getTimeInMillis() < matchEnd;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("start", DateTimeUtils.getDate(start))
                .append("end", DateTimeUtils.getDate(end)).toString();
    }
}
