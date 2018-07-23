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
package org.eclipse.smarthome.model.script.actions;

import java.net.MalformedURLException;

import org.eclipse.smarthome.model.script.engine.action.ActionDoc;
import org.eclipse.smarthome.model.script.internal.engine.action.EphemerisActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The static methods of this class are made available as functions in the scripts.
 * This allows a script to use ephemeris features.
 *
 * @author Gaël L'hopital - Initial contribution and API
 */
public class Ephemeris {
    private final static Logger logger = LoggerFactory.getLogger(Ephemeris.class);

    @ActionDoc(text = "checks if today is a week-end day")
    public static boolean isWeekEnd() {
        return isWeekEnd(0);
    }

    @ActionDoc(text = "checks if today plus or minus a given offset is a week-end day")
    public static boolean isWeekEnd(int offset) {
        return EphemerisActionService.ephemerisManager.isWeekEnd(offset);
    }

    @ActionDoc(text = "checks if today is defined in a given dayset")
    public static boolean isInDayset(String daysetName) {
        return isInDayset(daysetName, 0);
    }

    @ActionDoc(text = "checks if today plus or minus a given offset is defined in a given dayset")
    public static boolean isInDayset(String daysetName, int offset) {
        return EphemerisActionService.ephemerisManager.isInDayset(daysetName, offset);
    }

    @ActionDoc(text = "checks if today is bank holiday")
    public static boolean isBankHoliday() {
        return isBankHoliday(0);
    }

    @ActionDoc(text = "checks if today plus or minus a given offset is bank holiday")
    public static boolean isBankHoliday(int offset) {
        return EphemerisActionService.ephemerisManager.isBankHoliday(offset);
    }

    @ActionDoc(text = "get todays bank holiday name")
    public static String getBankHolidayName() {
        return getBankHolidayName(0);
    }

    @ActionDoc(text = "get bank holiday name for today plus or minus a given offset")
    public static String getBankHolidayName(int offset) {
        return EphemerisActionService.ephemerisManager.getBankHolidayName(offset);
    }

    @ActionDoc(text = "get holiday for today from a given userfile")
    public static String getHolidayUserFile(String filename) {
        return getHolidayUserFile(0, filename);
    }

    @ActionDoc(text = "get holiday for today plus or minus an offset from a given userfile")
    public static String getHolidayUserFile(int offset, String filename) {
        try {
            return EphemerisActionService.ephemerisManager.getHolidayUserFile(offset, filename);
        } catch (MalformedURLException e) {
            logger.error("Error accessing holiday user file {} : {}", filename, e.getMessage());
            return null;
        }
    }

}
