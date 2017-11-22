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
package org.eclipse.smarthome.core.i18n;

/**
 *
 * @author Denis Nobel - Initial contribution
 */
public class I18nUtil {

    /** The 'text' pattern (prefix) which marks constants. */
    private static final String CONSTANT_PATTERN = "@text/";

    public static boolean isConstant(String key) {
        return key != null && key.startsWith(CONSTANT_PATTERN);
    }

    public static String stripConstant(String key) {
        return key.replace(CONSTANT_PATTERN, "");
    }

}
