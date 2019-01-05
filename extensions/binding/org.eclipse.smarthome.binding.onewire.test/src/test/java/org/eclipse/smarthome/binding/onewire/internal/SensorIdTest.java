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
package org.eclipse.smarthome.binding.onewire.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests cases for {@link SensorId}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class SensorIdTest {

    @Test
    public void bareSensorIdConstructionTest() {
        SensorId sensorId = new SensorId("28.0123456789ab");
        assertEquals("/28.0123456789ab", sensorId.getFullPath());
        assertEquals("28.0123456789ab", sensorId.getId());
        assertEquals("28", sensorId.getFamilyId());

        sensorId = new SensorId("/28.0123456789ab");
        assertEquals("/28.0123456789ab", sensorId.getFullPath());
        assertEquals("28.0123456789ab", sensorId.getId());
        assertEquals("28", sensorId.getFamilyId());
    }

    @Test
    public void hubMainSensorIdConstructionTest() {
        SensorId sensorId = new SensorId("1F.0123456789ab/main/28.0123456789ab");
        assertEquals("/1F.0123456789ab/main/28.0123456789ab", sensorId.getFullPath());
        assertEquals("28.0123456789ab", sensorId.getId());
        assertEquals("28", sensorId.getFamilyId());

        sensorId = new SensorId("/1F.0123456789ab/main/28.0123456789ab");
        assertEquals("/1F.0123456789ab/main/28.0123456789ab", sensorId.getFullPath());
        assertEquals("28.0123456789ab", sensorId.getId());
        assertEquals("28", sensorId.getFamilyId());
    }

    @Test
    public void hubAuxSensorIdConstructionTest() {
        SensorId sensorId = new SensorId("1F.0123456789ab/aux/28.0123456789ab");
        assertEquals("/1F.0123456789ab/aux/28.0123456789ab", sensorId.getFullPath());
        assertEquals("28.0123456789ab", sensorId.getId());
        assertEquals("28", sensorId.getFamilyId());

        sensorId = new SensorId("/1F.0123456789ab/aux/28.0123456789ab");
        assertEquals("/1F.0123456789ab/aux/28.0123456789ab", sensorId.getFullPath());
        assertEquals("28.0123456789ab", sensorId.getId());
        assertEquals("28", sensorId.getFamilyId());
    }

}
