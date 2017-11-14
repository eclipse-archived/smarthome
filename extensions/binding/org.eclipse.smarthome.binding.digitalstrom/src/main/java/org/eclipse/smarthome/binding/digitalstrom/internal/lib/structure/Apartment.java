/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure;

import java.util.Map;

/**
 * The {@link Apartment} represents a digitalSTROM-Apartment.
 *
 * @author Alexander Betker
 */
public interface Apartment {

    /**
     * Returns the {@link Map} of all digitalSTROM-Zones with the zone id as key and the {@link Zone} as value.
     *
     * @return map of all zones
     */
    public Map<Integer, Zone> getZoneMap();
}
