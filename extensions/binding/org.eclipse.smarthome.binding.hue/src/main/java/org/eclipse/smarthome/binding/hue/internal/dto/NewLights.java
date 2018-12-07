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
package org.eclipse.smarthome.binding.hue.internal.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * REST API endpoint: /api/{username}/lights/new
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 * @author David Graeff - Rewritten
 */
@NonNullByDefault
public class NewLights {
    public String lastscan = "none";

    public @Nullable Date getLastScan() {
        switch (lastscan) {
            case "none":
                return null;
            case "active":
                return new Date();
            default:
                try {
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(lastscan);
                } catch (ParseException e) {
                    return null;
                }
        }
    }
}
