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
package org.eclipse.smarthome.binding.hue.internal.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@NonNullByDefault
public class Util {
    // This is used to check what byte size strings have, because the bridge doesn't natively support UTF-8
    public static int stringSize(String str) {
        return str.getBytes(StandardCharsets.UTF_8).length;
    }

    // UTF-8 URL encode
    public static String enc(@Nullable String str) {
        try {
            if (str != null) {
                return URLEncoder.encode(str, "UTF-8");
            } else {
                return "";
            }
        } catch (UnsupportedEncodingException ignored) {
            // Will never happen. See also https://bugs.openjdk.java.net/browse/JDK-8178704.
            // From Java 10 on, a StandardCharsets.UTF_8 overload exists and this helper can be removed.
            return "";
        }
    }
}
