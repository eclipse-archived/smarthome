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
package org.eclipse.smarthome.binding.onewire.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.DigitalIoConfig;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.SensorId;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseThingHandler;

/**
 * The {@link DS2406_DS2413} class defines an DS2406 or DS2413 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2406_DS2413 extends AbstractDigitalOwDevice {

    public DS2406_DS2413(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        ioConfig.clear();

        ioConfig.add(new DigitalIoConfig(callback.getThing(), 0, new OwDeviceParameter("uncached/", "/sensed.A"),
                new OwDeviceParameter("/PIO.A")));
        ioConfig.add(new DigitalIoConfig(callback.getThing(), 1, new OwDeviceParameter("/sensed.B"),
                new OwDeviceParameter("/PIO.B")));

        fullInParam = new OwDeviceParameter("uncached/", "/sensed.BYTE");
        fullOutParam = new OwDeviceParameter("/PIO.BYTE");

        super.configureChannels();
    }

}
