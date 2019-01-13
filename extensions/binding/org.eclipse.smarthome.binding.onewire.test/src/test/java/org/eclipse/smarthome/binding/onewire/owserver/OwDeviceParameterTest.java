/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.onewire.owserver;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.binding.onewire.internal.SensorId;
import org.eclipse.smarthome.binding.onewire.internal.device.OwDeviceParameter;
import org.junit.Test;

/**
 * Tests cases for {@link OwDeviceParamater}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class OwDeviceParameterTest {
    private final SensorId sensorId = new SensorId("/1F.0123456789ab/main/00.1234567890ab");

    @Test
    public void withoutPrefixTest() {
        OwDeviceParameter OwDeviceParameter = new OwDeviceParameter("/humidity");
        assertEquals("/1F.0123456789ab/main/00.1234567890ab/humidity", OwDeviceParameter.getPath(sensorId));

        OwDeviceParameter = new OwDeviceParameter("humidity");
        assertEquals("/1F.0123456789ab/main/00.1234567890ab/humidity", OwDeviceParameter.getPath(sensorId));
    }

    public void withPrefixTest() {
        OwDeviceParameter OwDeviceParameter = new OwDeviceParameter("uncached", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity", OwDeviceParameter.getPath(sensorId));

        OwDeviceParameter = new OwDeviceParameter("uncached", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity", OwDeviceParameter.getPath(sensorId));

        OwDeviceParameter = new OwDeviceParameter("/uncached", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity", OwDeviceParameter.getPath(sensorId));

        OwDeviceParameter = new OwDeviceParameter("/uncached/", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity", OwDeviceParameter.getPath(sensorId));

        OwDeviceParameter = new OwDeviceParameter("uncached/", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity", OwDeviceParameter.getPath(sensorId));

    }

}
