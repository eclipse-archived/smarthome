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

import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.SensorId;
import org.eclipse.smarthome.binding.onewire.internal.config.BAE091xAnalogConfiguration;
import org.eclipse.smarthome.binding.onewire.internal.config.BAE091xPIOConfiguration;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BAE0910} class defines an BAE0910 device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BAE0910 extends AbstractOwDevice {
    private static final int OUTC_OUTEN = 4;
    private static final int OUTC_DS = 3;

    private static final int PIOC_PIOEN = 4;
    private static final int PIOC_DS = 3;
    private static final int PIOC_PD = 2;
    private static final int PIOC_PE = 1;
    private static final int PIOC_DD = 0;

    private static final int ADCC_ADCEN = 4;
    private static final int ADCC_10BIT = 3;
    private static final int ADCC_OFS = 2;
    private static final int ADCC_GRP = 1;
    private static final int ADCC_STP = 0;

    private static final int TPMC_POL = 7;
    private static final int TPMC_INENA = 5;
    private static final int TPMC_PWMDIS = 4;
    private static final int TPMC_PS3 = 2;
    private static final int TPMC_PS2 = 1;
    private static final int TPMC_PS1 = 0;

    private final Logger logger = LoggerFactory.getLogger(BAE0910.class);
    private final OwDeviceParameterMap pin1CounterParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/counter"));
        }
    };
    private final OwDeviceParameterMap pin2OutParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/out"));
        }
    };
    private final OwDeviceParameterMap pin6PIOParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/pio"));
        }
    };
    private final OwDeviceParameterMap pin7AnalogParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/adc"));
        }
    };

    private final OwDeviceParameterMap outcParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/outc"));
        }
    };
    private final OwDeviceParameterMap piocParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/pioc"));
        }
    };
    private final OwDeviceParameterMap adccParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/adcc"));
        }
    };
    private final OwDeviceParameterMap tpm1cParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/tpm1c"));
        }
    };
    private final OwDeviceParameterMap tpm2cParameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/tpm2c"));
        }
    };

    private BitSet outcRegister = new BitSet(8);
    private BitSet piocRegister = new BitSet(8);
    private BitSet adccRegister = new BitSet(8);
    private BitSet tpm1cRegister = new BitSet(8);
    private BitSet tpm2cRegister = new BitSet(8);

    public BAE0910(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() {
    }

    public void configureChannels(OwBaseBridgeHandler bridgeHandler) throws OwException {
        // Pin 2
        if (enabledChannels.contains(CHANNEL_DIGITAL2)) {
            outcRegister.set(OUTC_DS);
            outcRegister.set(OUTC_OUTEN);
        } else {
            outcRegister.clear(OUTC_DS);
            outcRegister.clear(OUTC_OUTEN);
        }

        // Pin 6
        if (enabledChannels.contains(CHANNEL_DIGITAL6)) {
            piocRegister.set(PIOC_PIOEN);
            piocRegister.set(PIOC_DS);
            Channel channel6 = callback.getThing().getChannel(CHANNEL_DIGITAL6);
            if (channel6 != null) {
                BAE091xPIOConfiguration channelConfig = channel6.getConfiguration().as(BAE091xPIOConfiguration.class);
                if (channelConfig.mode.equals("output")) {
                    piocRegister.set(PIOC_DD);
                } else {
                    piocRegister.clear(PIOC_DD);
                }
                switch (channelConfig.pulldevice) {
                    case "pullup":
                        piocRegister.set(PIOC_PE);
                        piocRegister.clear(PIOC_PD);
                        break;
                    case "pulldown":
                        piocRegister.set(PIOC_PE);
                        piocRegister.clear(PIOC_PD);
                        break;
                    default:
                        piocRegister.clear(PIOC_PE);
                }
            } else {
                throw new OwException("trying to configure pin 6 but channel is missing");
            }
        } else {
            piocRegister.clear(PIOC_PIOEN);
            piocRegister.clear(PIOC_DS);
        }

        // Pin 7
        if (enabledChannels.contains(CHANNEL_VOLTAGE)) {
            adccRegister.set(ADCC_ADCEN);
            Channel channel7 = callback.getThing().getChannel(CHANNEL_VOLTAGE);
            if (channel7 != null) {
                BAE091xAnalogConfiguration channelConfig = channel7.getConfiguration()
                        .as(BAE091xAnalogConfiguration.class);
                if (channelConfig.hires) {
                    adccRegister.set(ADCC_10BIT);
                } else {
                    adccRegister.clear(ADCC_10BIT);
                }
            } else {
                throw new OwException("trying to configure pin 7 but channel is missing");
            }
        } else {
            adccRegister.clear(ADCC_ADCEN);
        }
        if (enabledChannels.contains(CHANNEL_DIGITAL7)) {
            tpm2cRegister.set(TPMC_PWMDIS);
        }

        // Pin 8
        if (enabledChannels.contains(CHANNEL_DIGITAL8)) {
            tpm1cRegister.set(TPMC_PWMDIS);
            Channel channel = callback.getThing().getChannel(CHANNEL_DIGITAL8);
            if (channel != null) {
                if (!(new ChannelTypeUID(BINDING_ID, "bae-in")).equals(channel.getChannelTypeUID())) {
                    tpm1cRegister.set(TPMC_INENA);
                } else {
                    tpm1cRegister.clear(TPMC_INENA);
                }
            } else {
                throw new OwException("trying to configure pin 8 but channel is missing");
            }
        }

        bridgeHandler.writeBitSet(sensorId, outcParameter, outcRegister);
        bridgeHandler.writeBitSet(sensorId, piocParameter, piocRegister);
        bridgeHandler.writeBitSet(sensorId, adccParameter, adccRegister);
        bridgeHandler.writeBitSet(sensorId, tpm1cParameter, tpm1cRegister);
        bridgeHandler.writeBitSet(sensorId, tpm2cParameter, tpm2cRegister);
        isConfigured = true;
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        // Pin1
        if (enabledChannels.contains(CHANNEL_COUNTER)) {
            State counterValue = bridgeHandler.readDecimalType(sensorId, pin1CounterParameter);
            logger.trace("read {} from {}", counterValue, sensorId);
            callback.postUpdate(CHANNEL_COUNTER, counterValue);
        }

        // Pin 2
        if (enabledChannels.contains(CHANNEL_DIGITAL2)) {
            BitSet value = bridgeHandler.readBitSet(sensorId, pin2OutParameter);
            logger.trace("read {} from {}", value, sensorId);
            callback.postUpdate(CHANNEL_DIGITAL2, OnOffType.from(value.get(0)));
        }

        // Pin 6
        if (enabledChannels.contains(CHANNEL_DIGITAL6)) {
            BitSet value = bridgeHandler.readBitSet(sensorId, pin6PIOParameter);
            logger.trace("read {} from {}", value, sensorId);
            callback.postUpdate(CHANNEL_DIGITAL6, OnOffType.from(value.get(0)));
        }

        // Pin 7
        if (enabledChannels.contains(CHANNEL_DIGITAL7)) {
            BitSet value = bridgeHandler.readBitSet(sensorId, tpm2cParameter);
            logger.trace("read {} from {}", value, sensorId);
            callback.postUpdate(CHANNEL_DIGITAL7, OnOffType.from(value.get(TPMC_POL)));
        }
        if (enabledChannels.contains(CHANNEL_VOLTAGE)) {
            State analogValue = bridgeHandler.readDecimalType(sensorId, pin7AnalogParameter);
            logger.trace("read {} from {}", analogValue, sensorId);
            callback.postUpdate(CHANNEL_VOLTAGE, analogValue);
        }

        // Pin 8
        if (enabledChannels.contains(CHANNEL_DIGITAL8)) {
            BitSet value = bridgeHandler.readBitSet(sensorId, tpm1cParameter);
            logger.trace("read {} from {}", value, sensorId);
            callback.postUpdate(CHANNEL_DIGITAL8, OnOffType.from(value.get(TPMC_POL)));
        }
    }

    public boolean writeChannel(OwBaseBridgeHandler bridgeHandler, String channelId, Command command) {
        try {
            BitSet value = new BitSet(8);

            switch (channelId) {
                case CHANNEL_DIGITAL2:
                    // output
                    if (!outcRegister.get(OUTC_OUTEN)) {
                        return false;
                    }
                    if (((OnOffType) command).equals(OnOffType.ON)) {
                        value.set(1);
                    }
                    bridgeHandler.writeBitSet(sensorId, pin2OutParameter, value);
                    break;
                case CHANNEL_DIGITAL6:
                    // not input, pio
                    if (!piocRegister.get(PIOC_DD) || !piocRegister.get(PIOC_PIOEN)) {
                        return false;
                    }
                    if (((OnOffType) command).equals(OnOffType.ON)) {
                        value.set(1);
                    }
                    bridgeHandler.writeBitSet(sensorId, pin6PIOParameter, value);
                    break;
                case CHANNEL_DIGITAL7:
                    // not pwm, not analog
                    if (!tpm1cRegister.get(TPMC_PWMDIS) || adccRegister.get(ADCC_ADCEN)) {
                        return false;
                    }
                    if (((OnOffType) command).equals(OnOffType.ON)) {
                        tpm2cRegister.set(TPMC_POL);
                    } else {
                        tpm2cRegister.clear(TPMC_POL);
                    }
                    bridgeHandler.writeBitSet(sensorId, tpm2cParameter, tpm2cRegister);
                    break;
                case CHANNEL_DIGITAL8:
                    // not input, not pwm
                    if (tpm1cRegister.get(TPMC_INENA) || !tpm1cRegister.get(TPMC_PWMDIS)) {
                        return false;
                    }
                    if (((OnOffType) command).equals(OnOffType.ON)) {
                        tpm1cRegister.set(TPMC_POL);
                    } else {
                        tpm1cRegister.clear(TPMC_POL);
                    }
                    bridgeHandler.writeBitSet(sensorId, tpm1cParameter, tpm1cRegister);
                    break;
                default:
                    throw new OwException("unknown or invalid channel");
            }
            return true;
        } catch (OwException e) {
            logger.info("could not write {} to {}: {}", command, channelId, e.getMessage());
            return false;
        }
    }

    public static OwSensorType getDeviceSubType(OwBaseBridgeHandler bridgeHandler, SensorId sensorId)
            throws OwException {
        OwDeviceParameterMap deviceTypeParameter = new OwDeviceParameterMap() {
            {
                set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/device_type"));
            }
        };

        String subDeviceType = bridgeHandler.readString(sensorId, deviceTypeParameter);
        switch (subDeviceType) {
            case "2":
                return OwSensorType.BAE0910;
            case "3":
                return OwSensorType.BAE0911;
            default:
                return OwSensorType.UNKNOWN;
        }
    }
}
