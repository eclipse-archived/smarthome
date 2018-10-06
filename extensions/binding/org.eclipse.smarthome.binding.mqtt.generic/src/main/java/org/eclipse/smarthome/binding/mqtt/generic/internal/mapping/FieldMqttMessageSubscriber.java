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
package org.eclipse.smarthome.binding.mqtt.generic.internal.mapping;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used by {@link MqttTopicClassMapper}. Represents an internal MessageSubscriber object for a specific field.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class FieldMqttMessageSubscriber implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(FieldMqttMessageSubscriber.class);
    protected final CompletableFuture<Boolean> future = new CompletableFuture<>();
    public final Field field;
    public final Object objectWithFields;
    public final String topic;
    public final @Nullable FieldChanged changeCallback;
    private final ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> scheduledFuture;

    FieldMqttMessageSubscriber(ScheduledExecutorService scheduler, Field field, Object objectWithFields, String topic,
            @Nullable FieldChanged changeCallback) {
        this.scheduler = scheduler;
        this.field = field;
        this.objectWithFields = objectWithFields;
        this.topic = topic;
        this.changeCallback = changeCallback;
    }

    @SuppressWarnings("unchecked")
    static Object numberConvert(Object value, Class<?> type) throws IllegalArgumentException, NumberFormatException {
        Object result = value;
        // Handle the conversion case of BigDecimal to Float,Double,Long,Integer and the respective
        // primitive types
        String typeName = type.getSimpleName();
        if (value instanceof BigDecimal && !type.equals(BigDecimal.class)) {
            BigDecimal bdValue = (BigDecimal) value;
            if (type.equals(Float.class) || typeName.equals("float")) {
                result = bdValue.floatValue();
            } else if (type.equals(Double.class) || typeName.equals("double")) {
                result = bdValue.doubleValue();
            } else if (type.equals(Long.class) || typeName.equals("long")) {
                result = bdValue.longValue();
            } else if (type.equals(Integer.class) || typeName.equals("int")) {
                result = bdValue.intValue();
            }
        } else
        // Handle the conversion case of String to Float,Double,Long,Integer,BigDecimal and the respective
        // primitive types
        if (value instanceof String && !type.equals(String.class)) {
            String bdValue = (String) value;
            if (type.equals(Float.class) || typeName.equals("float")) {
                result = Float.valueOf(bdValue);
            } else if (type.equals(Double.class) || typeName.equals("double")) {
                result = Double.valueOf(bdValue);
            } else if (type.equals(Long.class) || typeName.equals("long")) {
                result = Long.valueOf(bdValue);
            } else if (type.equals(BigDecimal.class)) {
                result = new BigDecimal(bdValue);
            } else if (type.equals(Integer.class) || typeName.equals("int")) {
                result = Integer.valueOf(bdValue);
            } else if (type.equals(Boolean.class) || typeName.equals("boolean")) {
                result = Boolean.valueOf(bdValue);
            } else
            // Handle enums
            if (type.isEnum()) {
                @SuppressWarnings({ "rawtypes" })
                final Class<? extends Enum> enumType = (Class<? extends Enum>) type;
                return Enum.valueOf(enumType, value.toString());
            }
        }
        return result;
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public void processMessage(@NonNull String topic, byte @NonNull [] payload) {
        final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) { // Cancel timeout
            scheduledFuture.cancel(false);
            this.scheduledFuture = null;
        }
        String valueStr = new String(payload, StandardCharsets.UTF_8);

        try {
            // Check if there is a manipulation annotation attached to the field
            final MapToField mapToField = field.getAnnotation(MapToField.class);
            Object value;
            if (mapToField != null) {
                // Add a prefix/suffix to the value
                valueStr = mapToField.prefix() + valueStr + mapToField.suffix();
                // Split the value if the field is an array. Convert numbers/enums if necessary.
                value = field.getType().isArray() ? valueStr.split(mapToField.splitCharacter())
                        : numberConvert(valueStr, field.getType());
            } else if (field.getType().isArray()) {
                throw new IllegalArgumentException("No split character defined!");
            } else {
                // Convert numbers/enums if necessary
                value = numberConvert(valueStr, field.getType());
            }
            // Assign to field, invoke callback
            field.set(objectWithFields, value);
            if (changeCallback != null) {
                changeCallback.fieldChanged(future, field.getName(), value);
            } else {
                future.complete(true);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.warn("Could not assign value {} to field {}", valueStr, field, e);
            future.complete(true);
        }
    }

    void timeoutReached() {
        future.complete(false);
    }

    /**
     * Subscribe to the MQTT topic now.
     *
     * @param connection An MQTT connection.
     * @param timeout Timeout in milliseconds. The returned future completes after this time even if no message has
     *            been
     *            received for the MQTT topic.
     * @return Returns a future that completes if either a value is received for the topic or a timeout happens.
     * @throws MqttException If an MQTT IO exception happens this exception is thrown.
     */
    public CompletableFuture<Boolean> start(MqttBrokerConnection connection, int timeout) {
        connection.subscribe(topic, this).exceptionally(e -> {
            logger.debug("Failed to subscribe to topic {}", topic, e);
            final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
            if (scheduledFuture != null) { // Cancel timeout
                scheduledFuture.cancel(false);
                this.scheduledFuture = null;
            }
            future.complete(false);
            return false;
        });
        this.scheduledFuture = scheduler.schedule(this::timeoutReached, timeout, TimeUnit.MILLISECONDS);
        return future;
    }
}
