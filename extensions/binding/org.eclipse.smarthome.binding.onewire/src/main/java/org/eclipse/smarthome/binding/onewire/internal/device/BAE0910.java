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

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Frequency;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.SensorId;
import org.eclipse.smarthome.binding.onewire.internal.config.BAE091xAnalogConfiguration;
import org.eclipse.smarthome.binding.onewire.internal.config.BAE091xPIOConfiguration;
import org.eclipse.smarthome.binding.onewire.internal.config.BAE091xPWMConfiguration;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseThingHandler;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
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
    private static final int TPMC_PS2 = 2;
    private static final int TPMC_PS1 = 1;
    private static final int TPMC_PS0 = 0;

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
    private final OwDeviceParameterMap period1Parameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/period1"));
        }
    };
    private final OwDeviceParameterMap period2Parameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/period2"));
        }
    };
    private final OwDeviceParameterMap duty1Parameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/duty1"));
        }
    };
    private final OwDeviceParameterMap duty2Parameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/duty2"));
        }
    };
    private final OwDeviceParameterMap duty3Parameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/duty3"));
        }
    };
    private final OwDeviceParameterMap duty4Parameter = new OwDeviceParameterMap() {
        {
            set(THING_TYPE_OWSERVER, new OwserverDeviceParameter("/duty4"));
        }
    };

    private BitSet outcRegister = new BitSet(8);
    private BitSet piocRegister = new BitSet(8);
    private BitSet adccRegister = new BitSet(8);
    private BitSet tpm1cRegister = new BitSet(8);
    private BitSet tpm2cRegister = new BitSet(8);

    private double resolution1 = 8; // in µs
    private double resolution2 = 8; // in µs

    public BAE0910(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() {
    }

    public void configureChannels(OwBaseBridgeHandler bridgeHandler) throws OwException {
        outcRegister.clear();
        piocRegister.clear();
        adccRegister.clear();
        tpm1cRegister.clear();
        tpm2cRegister.clear();

        if (enabledChannels.contains(CHANNEL_PWM_FREQ1)) {
            Channel channel = callback.getThing().getChannel(CHANNEL_PWM_FREQ1);
            if (channel != null) {
                BAE091xPWMConfiguration channelConfig = channel.getConfiguration().as(BAE091xPWMConfiguration.class);
                tpm1cRegister.set(TPMC_POL, channelConfig.reversePolarity);
                tpm1cRegister.set(TPMC_PS2, (channelConfig.prescaler & 4) == 4);
                tpm1cRegister.set(TPMC_PS1, (channelConfig.prescaler & 2) == 2);
                tpm1cRegister.set(TPMC_PS0, (channelConfig.prescaler & 1) == 1);
                resolution1 = 0.0625 * (1 << channelConfig.prescaler);
            } else {
                throw new OwException("trying to configure pwm but frequency channel is missing");
            }
        }

        if (enabledChannels.contains(CHANNEL_PWM_FREQ2)) {
            Channel channel = callback.getThing().getChannel(CHANNEL_PWM_FREQ2);
            if (channel != null) {
                BAE091xPWMConfiguration channelConfig = channel.getConfiguration().as(BAE091xPWMConfiguration.class);
                tpm2cRegister.set(TPMC_POL, channelConfig.reversePolarity);
                tpm2cRegister.set(TPMC_PS2, (channelConfig.prescaler & 4) == 4);
                tpm2cRegister.set(TPMC_PS1, (channelConfig.prescaler & 2) == 2);
                tpm2cRegister.set(TPMC_PS0, (channelConfig.prescaler & 1) == 1);
                resolution2 = 0.0625 * (1 << channelConfig.prescaler);
            } else {
                throw new OwException("trying to configure pwm but frequency channel is missing");
            }
        }

        // Pin 2
        if (enabledChannels.contains(CHANNEL_DIGITAL2)) {
            outcRegister.set(OUTC_DS);
            outcRegister.set(OUTC_OUTEN);
        }

        // Pin 6
        if (enabledChannels.contains(CHANNEL_DIGITAL6)) {
            piocRegister.set(PIOC_PIOEN);
            piocRegister.set(PIOC_DS);
            Channel channel = callback.getThing().getChannel(CHANNEL_DIGITAL6);
            if (channel != null) {
                BAE091xPIOConfiguration channelConfig = channel.getConfiguration().as(BAE091xPIOConfiguration.class);
                piocRegister.set(PIOC_DD, channelConfig.mode.equals("output"));
                switch (channelConfig.pulldevice) {
                    case "pullup":
                        piocRegister.set(PIOC_PE);
                        piocRegister.clear(PIOC_PD);
                        break;
                    case "pulldown":
                        piocRegister.set(PIOC_PE);
                        piocRegister.set(PIOC_PD);
                        break;
                    default:
                }
            } else {
                throw new OwException("trying to configure pin 6 but channel is missing");
            }
        }

        // Pin 7
        if (enabledChannels.contains(CHANNEL_VOLTAGE)) {
            adccRegister.set(ADCC_ADCEN);
            Channel channel = callback.getThing().getChannel(CHANNEL_VOLTAGE);
            if (channel != null) {
                BAE091xAnalogConfiguration channelConfig = channel.getConfiguration()
                        .as(BAE091xAnalogConfiguration.class);
                adccRegister.set(ADCC_10BIT, channelConfig.hires);
            } else {
                throw new OwException("trying to configure pin 7 but channel is missing");
            }
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
                }
            } else {
                throw new OwException("trying to configure pin 8 but channel is missing");
            }
        }

        // write configuration
        bridgeHandler.writeBitSet(sensorId, outcParameter, outcRegister);
        bridgeHandler.writeBitSet(sensorId, piocParameter, piocRegister);
        bridgeHandler.writeBitSet(sensorId, adccParameter, adccRegister);
        bridgeHandler.writeBitSet(sensorId, tpm1cParameter, tpm1cRegister);
        bridgeHandler.writeBitSet(sensorId, tpm2cParameter, tpm2cRegister);

        isConfigured = true;
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        // Counter
        if (enabledChannels.contains(CHANNEL_COUNTER)) {
            State counterValue = bridgeHandler.readDecimalType(sensorId, pin1CounterParameter);
            callback.postUpdate(CHANNEL_COUNTER, counterValue);
        }

        // Digital Pins
        if (enabledChannels.contains(CHANNEL_DIGITAL2)) {
            BitSet value = bridgeHandler.readBitSet(sensorId, pin2OutParameter);
            callback.postUpdate(CHANNEL_DIGITAL2, OnOffType.from(value.get(0)));
        }
        if (enabledChannels.contains(CHANNEL_DIGITAL6)) {
            BitSet value = bridgeHandler.readBitSet(sensorId, pin6PIOParameter);
            callback.postUpdate(CHANNEL_DIGITAL6, OnOffType.from(value.get(0)));
        }
        if (enabledChannels.contains(CHANNEL_DIGITAL7)) {
            BitSet value = bridgeHandler.readBitSet(sensorId, tpm2cParameter);
            callback.postUpdate(CHANNEL_DIGITAL7, OnOffType.from(value.get(TPMC_POL)));
        }
        if (enabledChannels.contains(CHANNEL_DIGITAL8)) {
            BitSet value = bridgeHandler.readBitSet(sensorId, tpm1cParameter);
            callback.postUpdate(CHANNEL_DIGITAL8, OnOffType.from(value.get(TPMC_POL)));
        }

        // Analog
        if (enabledChannels.contains(CHANNEL_VOLTAGE)) {
            State analogValue = bridgeHandler.readDecimalType(sensorId, pin7AnalogParameter);
            callback.postUpdate(CHANNEL_VOLTAGE, analogValue);
        }

        // PWM
        int period1 = 0;
        int period2 = 0;
        if (enabledChannels.contains(CHANNEL_PWM_FREQ1)) {
            period1 = ((DecimalType) bridgeHandler.readDecimalType(sensorId, period1Parameter)).intValue();
            double frequency = (period1 > 0) ? 1 / (period1 * resolution1 * 1e-6) : 0;
            callback.postUpdate(CHANNEL_PWM_FREQ1, new QuantityType<Frequency>(frequency, SmartHomeUnits.HERTZ));
        }
        if (enabledChannels.contains(CHANNEL_PWM_FREQ2)) {
            period2 = ((DecimalType) bridgeHandler.readDecimalType(sensorId, period2Parameter)).intValue();
            double frequency = (period2 > 0) ? 1 / (period2 * resolution2 * 1e-6) : 0;
            callback.postUpdate(CHANNEL_PWM_FREQ2, new QuantityType<Frequency>(frequency, SmartHomeUnits.HERTZ));
        }
        if (enabledChannels.contains(CHANNEL_PWM_DUTY1)) {
            int dutyValue = ((DecimalType) bridgeHandler.readDecimalType(sensorId, duty1Parameter)).intValue();
            double duty = (period1 > 0 && dutyValue <= period1) ? 100 * dutyValue / period1 : 100;
            callback.postUpdate(CHANNEL_PWM_DUTY1, new QuantityType<Dimensionless>(duty, SmartHomeUnits.PERCENT));
        }
        if (enabledChannels.contains(CHANNEL_PWM_DUTY2)) {
            int dutyValue = ((DecimalType) bridgeHandler.readDecimalType(sensorId, duty2Parameter)).intValue();
            double duty = (period2 > 0 && dutyValue <= period2) ? 100 * dutyValue / period2 : 100;
            callback.postUpdate(CHANNEL_PWM_DUTY1, new QuantityType<Dimensionless>(duty, SmartHomeUnits.PERCENT));
        }
        if (enabledChannels.contains(CHANNEL_PWM_DUTY3)) {
            int dutyValue = ((DecimalType) bridgeHandler.readDecimalType(sensorId, duty3Parameter)).intValue();
            double duty = (period1 > 0 && dutyValue <= period1) ? 100 * dutyValue / period1 : 100;
            callback.postUpdate(CHANNEL_PWM_DUTY3, new QuantityType<Dimensionless>(duty, SmartHomeUnits.PERCENT));
        }
        if (enabledChannels.contains(CHANNEL_PWM_DUTY4)) {
            int dutyValue = ((DecimalType) bridgeHandler.readDecimalType(sensorId, duty4Parameter)).intValue();
            double duty = (period2 > 0 && dutyValue <= period2) ? 100 * dutyValue / period2 : 100;
            callback.postUpdate(CHANNEL_PWM_DUTY4, new QuantityType<Dimensionless>(duty, SmartHomeUnits.PERCENT));
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
                    value.set(1, ((OnOffType) command).equals(OnOffType.ON));
                    bridgeHandler.writeBitSet(sensorId, pin2OutParameter, value);
                    break;
                case CHANNEL_DIGITAL6:
                    // not input, pio
                    if (!piocRegister.get(PIOC_DD) || !piocRegister.get(PIOC_PIOEN)) {
                        return false;
                    }
                    value.set(1, ((OnOffType) command).equals(OnOffType.ON));
                    bridgeHandler.writeBitSet(sensorId, pin6PIOParameter, value);
                    break;
                case CHANNEL_DIGITAL7:
                    // not pwm, not analog
                    if (!tpm2cRegister.get(TPMC_PWMDIS) || adccRegister.get(ADCC_ADCEN)) {
                        return false;
                    }
                    tpm2cRegister.set(TPMC_POL, ((OnOffType) command).equals(OnOffType.ON));
                    bridgeHandler.writeBitSet(sensorId, tpm2cParameter, tpm2cRegister);
                    break;
                case CHANNEL_DIGITAL8:
                    // not input, not pwm
                    if (tpm1cRegister.get(TPMC_INENA) || !tpm1cRegister.get(TPMC_PWMDIS)) {
                        return false;
                    }
                    tpm1cRegister.set(TPMC_POL, ((OnOffType) command).equals(OnOffType.ON));
                    bridgeHandler.writeBitSet(sensorId, tpm1cParameter, tpm1cRegister);
                    break;
                // TODO: PWM channels
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
