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
package org.eclipse.smarthome.binding.onewire.internal.device;

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.THING_TYPE_OWSERVER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.DigitalIoConfig;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverDeviceParameter;

/**
 * The {@link DS2406_DS2413} class defines an DS2406 or DS2413 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2406_DS2413 extends AbstractDigitalOwDevice {

    public DS2406_DS2413(String sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        ioConfig.clear();

        OwDeviceParameterMap inParam = new OwDeviceParameterMap();
        OwDeviceParameterMap outParam = new OwDeviceParameterMap();

        inParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/sensed.A"));
        outParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/PIO.A"));
        ioConfig.add(new DigitalIoConfig(callback.getThing(), 0, inParam, outParam));

        inParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/sensed.B"));
        outParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/PIO.B"));
        ioConfig.add(new DigitalIoConfig(callback.getThing(), 1, inParam, outParam));

        fullInParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/sensed.BYTE"));
        fullOutParam.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/PIO.BYTE"));

        super.configureChannels();
    }

}
