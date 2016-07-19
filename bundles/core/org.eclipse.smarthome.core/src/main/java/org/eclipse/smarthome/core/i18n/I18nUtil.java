/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.i18n;

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
