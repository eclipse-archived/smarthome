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
import static org.eclipse.smarthome.core.library.unit.MetricPrefix.MILLI;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.Util;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DS2438} class defines an DS2438 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2438 extends AbstractOwDevice {
    private final Logger logger = LoggerFactory.getLogger(DS2438.class);

    public enum LightSensorType {
        ElabNetV1,
        ElabNetV2
    }

    private LightSensorType lightSensorType = LightSensorType.ElabNetV1;

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

    private static final OwDeviceParameterMap VOLTAGE_PARAMETER = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/VAD"));
        }
    };

    private static final OwDeviceParameterMap CURRENT_PARAMETER = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/vis"));
        }
    };

    private static final OwDeviceParameterMap SUPPLY_VOLTAGE_PARAMETER = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/VDD"));
        }
    };

    public DS2438(String sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() {
        Thing thing = callback.getThing();

        Channel humidityChannel = thing.getChannel(CHANNEL_HUMIDITY);
        if (humidityChannel != null) {
            Configuration channelConfiguration = humidityChannel.getConfiguration();
            if (channelConfiguration.get(CONFIG_HUMIDITY) != null) {
                HUMIDITY_PARAMETER.set(THING_TYPE_OWSERVER,
                        new OwserverDeviceParameter((String) channelConfiguration.get(CONFIG_HUMIDITY)));
            } else {
                HUMIDITY_PARAMETER.set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/humidity"));
            }
        }

        isConfigured = true;
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        if (isConfigured) {
            if (enabledChannels.contains(CHANNEL_TEMPERATURE) || enabledChannels.contains(CHANNEL_HUMIDITY)
                    || enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)
                    || enabledChannels.contains(CHANNEL_DEWPOINT)) {
                QuantityType<Temperature> temperature = new QuantityType<Temperature>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, TEMPERATURE_PARAMETER), SIUnits.CELSIUS);
                logger.trace("read temperature {} from {}", temperature, sensorId);

                if (enabledChannels.contains(CHANNEL_TEMPERATURE)) {
                    callback.postUpdate(CHANNEL_TEMPERATURE, temperature);
                }

                if (enabledChannels.contains(CHANNEL_HUMIDITY) || enabledChannels.contains(CHANNEL_ABSOLUTE_HUMIDITY)
                        || enabledChannels.contains(CHANNEL_DEWPOINT)) {
                    QuantityType<Dimensionless> humidity = new QuantityType<Dimensionless>(
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

            if (enabledChannels.contains(CHANNEL_VOLTAGE)) {
                State voltage = new QuantityType<ElectricPotential>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, VOLTAGE_PARAMETER), SmartHomeUnits.VOLT);
                logger.trace("read voltage {} from {}", voltage, sensorId);
                callback.postUpdate(CHANNEL_VOLTAGE, voltage);
            }

            if (enabledChannels.contains(CHANNEL_CURRENT)) {
                State current = new QuantityType<ElectricCurrent>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, CURRENT_PARAMETER),
                        MILLI(SmartHomeUnits.AMPERE));
                callback.postUpdate(CHANNEL_CURRENT, current);
            }

            if (enabledChannels.contains(CHANNEL_LIGHT)) {
                State light = bridgeHandler.readDecimalType(sensorId, CURRENT_PARAMETER);
                if (light instanceof DecimalType) {
                    if (lightSensorType == LightSensorType.ElabNetV2) {
                        light = new QuantityType<Illuminance>(
                                Math.round(Math.pow(10, ((DecimalType) light).doubleValue() / 47 * 1000)),
                                SmartHomeUnits.LUX);
                    } else {
                        light = new QuantityType<Illuminance>(Math.round(Math.exp(
                                1.059 * Math.log(1000000 * ((DecimalType) light).doubleValue() / (4096 * 390)) + 4.518)
                                * 20000), SmartHomeUnits.LUX);
                    }
                    callback.postUpdate(CHANNEL_LIGHT, light);
                }
            }

            if (enabledChannels.contains(CHANNEL_SUPPLYVOLTAGE)) {
                State supplyVoltage = new QuantityType<ElectricPotential>(
                        (DecimalType) bridgeHandler.readDecimalType(sensorId, SUPPLY_VOLTAGE_PARAMETER),
                        SmartHomeUnits.VOLT);

                callback.postUpdate(CHANNEL_SUPPLYVOLTAGE, supplyVoltage);
            }
        }
    }

    /**
     * set the type of the attached light sensor
     *
     * @param lightSensorType
     */
    public void setLightSensorType(LightSensorType lightSensorType) {
        this.lightSensorType = lightSensorType;
    }
}
