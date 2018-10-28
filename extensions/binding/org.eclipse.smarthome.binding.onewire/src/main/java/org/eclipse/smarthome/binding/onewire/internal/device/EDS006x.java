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

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.*;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.Util;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EDS006x} class defines an EDS006x device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class EDS006x extends AbstractOwDevice {
    private final Logger logger = LoggerFactory.getLogger(EDS006x.class);

    private static final OwDeviceParameterMap TEMPERATURE_PARAMETER = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/temperature"));
        }
    };

    private static final OwDeviceParameterMap HUMIDITY_PARAMETER = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/humidity"));
        }
    };

    private static final OwDeviceParameterMap PRESSURE_PARAMETER = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/pressure"));
        }
    };

    private static final OwDeviceParameterMap LIGHT_PARAMETER = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/light"));
        }
    };

    public EDS006x(String sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() {
        isConfigured = false;
    }

    /**
     * configure channels for EDS sensors
     *
     * @param sensorType an OwSensorType
     */
    public void configureChannels(OwSensorType sensorType) {
        TEMPERATURE_PARAMETER.set(THING_TYPE_OWSERVER,
                new OwserverDeviceParameter("/" + sensorType.name() + "/temperature"));
        HUMIDITY_PARAMETER.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/" + sensorType.name() + "/humidity"));
        PRESSURE_PARAMETER.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/" + sensorType.name() + "/pressure"));
        LIGHT_PARAMETER.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/" + sensorType.name() + "/light"));

        isConfigured = true;
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        if (isConfigured) {
            if (enabledChannels.contains(CHANNEL_TEMPERATURE) || enabledChannels.contains(CHANNEL_HUMIDITY)
                    || enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)
                    || enabledChannels.contains(CHANNEL_DEWPOINT)) {
                QuantityType<Temperature> temperature = new QuantityType<>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, TEMPERATURE_PARAMETER), SIUnits.CELSIUS);
                logger.trace("read temperature {} from {}", temperature, sensorId);

                if (enabledChannels.contains(CHANNEL_TEMPERATURE)) {
                    callback.postUpdate(CHANNEL_TEMPERATURE, temperature);
                }

                if (enabledChannels.contains(CHANNEL_HUMIDITY) || enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)
                        || enabledChannels.contains(CHANNEL_DEWPOINT)) {
                    QuantityType<Dimensionless> humidity = new QuantityType<>(
                            (DecimalType) bridgeHandler.readDecimalType(sensorId, HUMIDITY_PARAMETER),
                            SmartHomeUnits.PERCENT);
                    logger.trace("read humidity {} from {}", humidity, sensorId);

                    if (enabledChannels.contains(CHANNEL_HUMIDITY)) {
                        callback.postUpdate(CHANNEL_HUMIDITY, humidity);
                    }

                    if (enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)) {
                        callback.postUpdate(CHANNEL_ABSOLUTE_HUMIDITY,
                                Util.calculateAbsoluteHumidity(temperature, humidity));
                    }

                    if (enabledChannels.contains(CHANNEL_DEWPOINT)) {
                        callback.postUpdate(CHANNEL_DEWPOINT, Util.calculateDewpoint(temperature, humidity));
                    }
                }
            }

            if (enabledChannels.contains(CHANNEL_LIGHT)) {
                QuantityType<Illuminance> light = new QuantityType<>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, LIGHT_PARAMETER), SmartHomeUnits.LUX);
                logger.trace("read light {} from {}", light, sensorId);
                callback.postUpdate(CHANNEL_LIGHT, light);
            }

            if (enabledChannels.contains(CHANNEL_PRESSURE)) {
                QuantityType<Pressure> pressure = new QuantityType<>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, PRESSURE_PARAMETER),
                        MetricPrefix.HECTO(SIUnits.PASCAL));
                logger.trace("read pressure {} from {}", pressure, sensorId);
                callback.postUpdate(CHANNEL_PRESSURE, pressure);
            }
        }
    }
}
