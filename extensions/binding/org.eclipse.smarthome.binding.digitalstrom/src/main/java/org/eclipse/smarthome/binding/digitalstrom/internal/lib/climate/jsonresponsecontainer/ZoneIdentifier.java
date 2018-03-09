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
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer;

/**
 * The {@link ZoneIdentifier} can be implement to identify a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface ZoneIdentifier {

    /**
     * Returns the zoneID of this zone.
     *
     * @return the zoneID
     */
    Integer getZoneID();

    /**
     * Returns the zoneName of this zone.
     *
     * @return the zoneName
     */
    String getZoneName();
}
