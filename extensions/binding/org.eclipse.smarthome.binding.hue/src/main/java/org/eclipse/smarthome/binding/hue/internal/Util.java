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
package org.eclipse.smarthome.binding.hue.internal;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@NonNullByDefault
class Util {
    private Util() {
    }

    // This is used to check what byte size strings have, because the bridge doesn't natively support UTF-8
    public static int stringSize(String str) {
        try {
            return str.getBytes("utf-8").length;
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException("UTF-8 not supported");
        }
    }

    public static List<Light> idsToLights(List<String> ids) {
        List<Light> lights = new ArrayList<>();

        for (String id : ids) {
            Light light = new Light();
            light.setId(id);
            lights.add(light);
        }

        return lights;
    }

    public static List<String> lightsToIds(List<Light> lights) {
        List<String> ids = new ArrayList<>();

        for (Light light : lights) {
            ids.add(light.getId());
        }

        return ids;
    }

    public static @Nullable String quickMatch(String needle, String haystack) {
        Matcher m = Pattern.compile(needle).matcher(haystack);
        m.find();
        return m.group(1);
    }
}
