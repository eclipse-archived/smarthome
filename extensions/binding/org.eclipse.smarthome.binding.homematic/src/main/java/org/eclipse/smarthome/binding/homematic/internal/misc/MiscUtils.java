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
package org.eclipse.smarthome.binding.homematic.internal.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global utility class with helper methods.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class MiscUtils {
    private static final Logger logger = LoggerFactory.getLogger(MiscUtils.class);

    /**
     * Replaces invalid characters of the text to fit into a openHAB UID.
     */
    public static String validateCharacters(String text, String textType, String replaceChar) {
        if (text == null) {
            return "EMPTY";
        }
        String cleanedText = text.replaceAll("[^A-Za-z0-9_-]", replaceChar);
        if (!text.equals(cleanedText)) {
            logger.info("{} '{}' contains invalid characters, new {} '{}'", textType, text, textType, cleanedText);
        }
        return cleanedText;
    }

    /**
     * Returns true, if the value is not null and true.
     */
    public static boolean isTrueValue(Object value) {
        return value != null && value == Boolean.TRUE;
    }

    /**
     * Returns true, if the value is not null and false.
     */
    public static boolean isFalseValue(Object value) {
        return value != null && value == Boolean.FALSE;
    }
}
