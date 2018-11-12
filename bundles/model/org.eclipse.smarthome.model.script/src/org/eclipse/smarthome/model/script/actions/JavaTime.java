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

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * <p>
 * The static methods of this class are made available as functions in the scripts.
 * Previously, when we used joda-time, we made all static methods of org.joda.time.DateTime
 * available to scripts.
 * </p>
 *
 * <p>
 * The {@link java.time.ZonedDateTime} class has 13 static methods, so rather than pollute
 * the script API with all of them, we make this class available instead which has
 * a cut-down set.
 * </p>
 *
 * @author Jon Evans - Initial contribution and API
 *
 */
public class JavaTime {

    /**
     * Get the current date & time in the system time zone.
     *
     * @return the current date & time
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    /**
     * Get the current date & time in the specified time zone.
     *
     * @param zoneId the time zone ID to use
     * @return the current date & time
     */
    public static ZonedDateTime now(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }
}
