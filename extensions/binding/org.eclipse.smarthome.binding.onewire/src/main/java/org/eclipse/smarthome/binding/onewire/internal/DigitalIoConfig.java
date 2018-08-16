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

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.CHANNEL_DIGITAL;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.device.OwDeviceParameter;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link DigitalIoConfig} class provides the configuration of a digital IO channel
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DigitalIoConfig {
    private final String channelID;
    private final ChannelUID channelUID;
    private final OwDeviceParameter inParam;
    private final OwDeviceParameter outParam;
    private DigitalIoMode ioMode = DigitalIoMode.INPUT;
    private DigitalIoLogic ioLogic = DigitalIoLogic.NORMAL;

    public DigitalIoConfig(Thing thing, Integer channelIndex, OwDeviceParameter inParam, OwDeviceParameter outParam) {
        this.channelUID = new ChannelUID(thing.getUID(), String.format("%s%d", CHANNEL_DIGITAL, channelIndex));
        this.channelID = String.format("%s%d", CHANNEL_DIGITAL, channelIndex);
        this.inParam = inParam;
        this.outParam = outParam;
    }

    public void setIoMode(String ioMode) {
        this.ioMode = DigitalIoMode.valueOf(ioMode.toUpperCase());
    }

    public void setIoLogic(String ioLogic) {
        this.ioLogic = DigitalIoLogic.valueOf(ioLogic.toUpperCase());
    }

    public Boolean isInverted() {
        return (ioLogic == DigitalIoLogic.INVERTED);
    }

    public ChannelUID getChannelUID() {
        return channelUID;
    }

    public String getChannelId() {
        return channelID;
    }

    public OwDeviceParameter getParameter() {
        return (ioMode == DigitalIoMode.INPUT) ? inParam : outParam;
    }

    public Boolean isInput() {
        return (ioMode == DigitalIoMode.INPUT);
    }

    public Boolean isOutput() {
        return (ioMode == DigitalIoMode.OUTPUT);
    }

    public DigitalIoMode getIoDirection() {
        return ioMode;
    }

    public State convertState(Boolean rawValue) {
        if (ioLogic == DigitalIoLogic.NORMAL) {
            return rawValue ? OnOffType.ON : OnOffType.OFF;
        } else {
            return rawValue ? OnOffType.OFF : OnOffType.ON;
        }
    }

    @Override
    public String toString() {
        return String.format("path=%s, mode=%s, logic=%s", Arrays.asList(getParameter()), ioMode, ioLogic);
    }
}
