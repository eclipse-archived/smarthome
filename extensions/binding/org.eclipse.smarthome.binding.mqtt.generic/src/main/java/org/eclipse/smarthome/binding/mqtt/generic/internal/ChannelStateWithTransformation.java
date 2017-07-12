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
package org.eclipse.smarthome.binding.mqtt.generic.internal;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.GenericChannelConfig;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.AbstractMqttThingValue;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 * This object consists of an {@link AbstractMqttThingValue}, which is updated on the respective MQTT topic change.
 * If a transformation is configured in {@link GenericChannelConfig}, the transformation is applied before assigning
 * the result to the {@link AbstractMqttThingValue}. Updates to the value are propagated via the
 * {@link ChannelStateUpdateListener}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelStateWithTransformation extends ChannelState {
    public final TransformationServiceProvider transformationServiceProvider;
    protected @Nullable TransformationService transformationService;
    public final String transformationPattern;
    public final String transformationServiceName;

    /**
     * Creates a new MQTT topic subscriber.
     *
     * @param stateTopic The MQTT state topic
     * @param commandTopic The MQTT command topic
     * @param transformationPattern A transformation pattern, starting with the transformation service
     *            name,followed by a colon and the transformation itself. An Example:
     *            JSONPATH:$.device.status.temperature for a json {device: {status: {
     *            temperature: 23.2 }}}.
     * @param channelUID The channel UID, used in processMessage() to inform the respective channel
     *            of an update.
     * @param value A value object
     * @param transformationServiceProvider The transformation service provider
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
        final TransformationService transformationService = this.transformationService;
        if (transformationService != null) {
            @Nullable
            String temp = transformationService.transform(transformationPattern, value);
            return (temp != null) ? temp : value;
        }
        return value;
    }

    @Override
    public CompletableFuture<Boolean> start(MqttBrokerConnection connection,
            ChannelStateUpdateListener channelStateUpdateListener) throws IllegalArgumentException {
        CompletableFuture<Boolean> r = super.start(connection, channelStateUpdateListener);
        if (!transformationServiceName.isEmpty()) {
            transformationService = transformationServiceProvider.getTransformationService(transformationServiceName);
            if (transformationService == null) {
                throw new IllegalArgumentException(
                        "Transformation service " + transformationServiceName + " not found");
            }
        }
        return r;
    }
}
