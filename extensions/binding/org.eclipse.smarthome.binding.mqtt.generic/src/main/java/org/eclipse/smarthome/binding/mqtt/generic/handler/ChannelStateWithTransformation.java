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
package org.eclipse.smarthome.binding.mqtt.generic.handler;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.AbstractMqttThingValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.TransformationServiceProvider;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;

/**
 * This object consists of an {@link AbstractMqttThingValue}, which is updated on the respective MQTT topic change.
 * If a transformation is configured in {@link ChannelConfig}, the transformation is applied before assigning
 * the result to the {@link AbstractMqttThingValue}. Updates to the value are propagated via the
 * {@link ChannelStateUpdateListener}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelStateWithTransformation extends ChannelState {
    protected final TransformationServiceProvider transformationServiceProvider;
    protected @Nullable TransformationService transformationService;
    protected final String transformationPattern;
    protected final String transformationServiceName;

    /**
     * Creates a new MQTT topic subscriber. The given configuration and channel type
     *
     * @param config
     * @param channelUID
     * @param channelTypeUID
     * @param transformationServiceProvider
     */
    public ChannelStateWithTransformation(String stateTopic, String commandTopic, String transformationPattern,
            ChannelUID channelUID, AbstractMqttThingValue value,
            TransformationServiceProvider transformationServiceProvider) {
        super(stateTopic, commandTopic, channelUID, value);
        this.transformationServiceProvider = transformationServiceProvider;
        if (StringUtils.isNotBlank(transformationPattern)) {
            int index = transformationPattern.indexOf(':');
            if (index == -1) {
                throw new IllegalArgumentException(
                        "The transformation pattern must consist of the type and the pattern separated by a colon");
            }
            String type = transformationPattern.substring(0, index).toUpperCase();
            this.transformationPattern = transformationPattern.substring(index + 1);
            this.transformationServiceName = type;
        } else {
            this.transformationPattern = "";
            this.transformationServiceName = "";
        }

    }

    @Override
    protected String postProcessIncomingValue(String value) throws TransformationException {
        if (transformationService != null) {
            @Nullable String temp = transformationService.transform(transformationPattern, value);
            return (temp!=null) ? temp : value;
        }
        return value;
    }

    @Override
    public CompletableFuture<Boolean> start(MqttBrokerConnection connection,
            ChannelStateUpdateListener channelStateUpdateListener) throws MqttException, IllegalArgumentException {
        CompletableFuture<Boolean> r = super.start(connection, channelStateUpdateListener);
        if (transformationServiceName != null) {
            transformationService = transformationServiceProvider.getTransformationService(transformationServiceName);
            if (transformationService == null) {
                throw new IllegalArgumentException(
                        "Transformation service " + transformationServiceName + " not found");
            }
        }
        return r;
    }
}
