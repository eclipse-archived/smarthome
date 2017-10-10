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
     * Provides access to the time zone property.
     *
     * @return the time zone set in the user interface and if there isn't one, the default time zone of the operating system
     */
    ZoneId getTimeZone();
}
