/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.i18n;

import java.time.ZoneId;

/**
 * This interface describes a provider for time zone.
 *
 * @author Erdoan Hadzhiyusein - Initial contribution and API
 */
public interface TimeZoneProvider {

    /**
     * Gets the configured time zone as {@link ZoneId} or the system default time zone if not configured properly.
     *
     * @return the configured time zone as {@link ZoneId} or the system default time zone if not configured properly.
     */
    ZoneId getTimeZone();
}
