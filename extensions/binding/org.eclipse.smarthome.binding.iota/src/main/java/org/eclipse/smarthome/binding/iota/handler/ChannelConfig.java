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
package org.eclipse.smarthome.binding.iota.handler;

import org.eclipse.smarthome.binding.iota.internal.AbstractIotaThingValue;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChannelConfig} contains parsed channel configuration and runtime fields like the channel value.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class ChannelConfig {

    private final Logger logger = LoggerFactory.getLogger(ChannelConfig.class);

    String address;
    String transformationPattern;
    Boolean isFloat = false;
    Boolean inverse = false;
    String on;
    String off;
    String transformationServiceName;

    AbstractIotaThingValue value;
    ChannelUID channelUID;
    TransformationServiceProvider transformationServiceProvider;
    ChannelStateUpdateListener channelStateUpdateListener = null;

    public void processMessage(String payload) {
        try {
            if (channelStateUpdateListener != null) {
                channelStateUpdateListener.channelStateUpdated(channelUID, value.update(payload));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Incoming payload '{}' not supported", value);
        }
    }

    void dispose() {
        channelStateUpdateListener = null;
    }

    public void setValue(AbstractIotaThingValue value) {
        this.value = value;
    }

    public AbstractIotaThingValue getValue() {
        return value;
    }

    public void setChannelUID(ChannelUID channelUID) {
        this.channelUID = channelUID;
    }

    public void setChannelStateUpdateListener(ChannelStateUpdateListener channelStateUpdateListener) {
        this.channelStateUpdateListener = channelStateUpdateListener;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTransformationPattern(String transformationPattern) {
        this.transformationPattern = transformationPattern;
    }

    public void setTransformationServiceName(String transformationServiceName) {
        this.transformationServiceName = transformationServiceName;
    }

    public void setTransformationServiceProvider(TransformationServiceProvider transformationServiceProvider) {
        this.transformationServiceProvider = transformationServiceProvider;
    }

}
