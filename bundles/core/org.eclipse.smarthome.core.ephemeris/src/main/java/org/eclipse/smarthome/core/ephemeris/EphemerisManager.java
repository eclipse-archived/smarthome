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
package org.eclipse.smarthome.core.ephemeris;

import java.net.MalformedURLException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This service provides functionality around days of the year and is the central
 * service to be used directly by others.
 *
 * @author Gaël L'hopital - Initial contribution and API
 */
@NonNullByDefault
public interface EphemerisManager {

    /**
     * Tests given day (related to today) status against configured week-end days
     *
     * @param offset Today +/- offset days (+1 = tomorrow, -1 = yesterday)
     * @return whether the day is on week-end
     */
    boolean isWeekEnd(int offset);

    /**
     * Tests given day (related to today) status against configured dayset
     *
     * @param daysetName name of the requested dayset, witout prefix
     * @param offset Today +/- offset days (+1 = tomorrow, -1 = yesterday)
     * @return whether the day is on week-end
     */
    boolean isInDayset(String daysetName, int offset);

    /**
     * Tests given day status against official bank holidays
     *
     * @param offset Today +/- offset days (+1 = tomorrow, -1 = yesterday)
     * @return whether the day is bank holiday or not
     */
    boolean isBankHoliday(int offset);

    /**
     * Get given day bank holiday name
     *
     * @param offset Today +/- offset days (+1 = tomorrow, -1 = yesterday)
     * @return name of the bank holiday or null if no bank holiday
     */
    @Nullable
    String getBankHolidayName(int offset);

    /**
     * Get given day name from given userfile
     *
     * @param filename Absolute path to the file on local file system
     * @param offset Today +/- offset days (+1 = tomorrow, -1 = yesterday)
     * @return name of the day or null if no corresponding entry
     */
    @Nullable
    String getHolidayUserFile(int offset, String filename) throws MalformedURLException;

}
