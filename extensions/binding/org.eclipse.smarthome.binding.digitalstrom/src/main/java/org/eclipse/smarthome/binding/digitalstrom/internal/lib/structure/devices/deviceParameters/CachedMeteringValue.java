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
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

/**
 * The {@link CachedMeteringValue} saves the metering value of an digitalSTROM-Circuit.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - add missing java-doc
 * @author Matthias Siegele - add missing java-doc
 */
public interface CachedMeteringValue {

    /**
     * Returns the {@link DSID} of the digitalSTROM-Circuit.
     *
     * @return dSID of circuit
     */
    public DSID getDsid();

    /**
     * Returns the saved sensor value.
     *
     * @return sensor value
     */
    public double getValue();

    /**
     * Returns the time stamp when the sensor value was read out.
     *
     * @return read out time stamp
     */
    public String getDate();

}
