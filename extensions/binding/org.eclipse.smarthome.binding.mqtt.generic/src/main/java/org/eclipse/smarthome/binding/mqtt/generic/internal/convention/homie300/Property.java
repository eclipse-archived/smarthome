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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelStateUpdateListener;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.PropertyAttributes.DataTypeEnum;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttAttributeClass;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.AbstractMqttThingValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.ColorValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.NumberValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.OnOffValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.TextValue;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A homie Property (which translates into an ESH channel).
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Property implements MqttAttributeClass {
    private final Logger logger = LoggerFactory.getLogger(Property.class);
    // Homie data
    public final PropertyAttributes attributes = new PropertyAttributes();
    public final Node parentNode;
    public final String propertyID;
    // Runtime state
    private @Nullable ChannelState channelState;
    // ESH
    public final ThingUID thingUID;
    public final ChannelUID channelUID;
    public final ChannelTypeUID channelTypeUID;
    private @Nullable ChannelType type;

    /**
     * Creates a Homie Property.
     *
     * @param node The parent Homie Node.
     * @param propertyID The unique property ID (among all properties on this Node).
     * @param thingUID The Thing UID
     */
    public Property(Node node, String propertyID, ThingUID thingUID) {
        this.parentNode = node;
        this.propertyID = propertyID;
        this.thingUID = thingUID;
        channelUID = new ChannelUID(thingUID, parentNode.nodeID, propertyID);
        channelTypeUID = new ChannelTypeUID(MqttBindingConstants.BINDING_ID,
                thingUID.getId() + "_" + parentNode.nodeID + "_" + propertyID);
    }

    /**
     * Subscribe to property attributes. This will not subscribe
     * to the property value though. Call {@link Device#startChannels(MqttBrokerConnection)} to do that.
     *
     * @return Returns a future that completes as soon as all attribute values have been received or requests have timed
     *         out.
     */
    @Override
    public CompletableFuture<Void> subscribe(MqttTopicClassMapper topicMapper, int timeout) {
        return topicMapper.subscribe("homie/" + thingUID.getId() + "/" + parentNode.nodeID + "/" + propertyID,
                attributes, null, timeout).thenRun(this::attributesReceived);
    }

    private @Nullable BigDecimal convertFromString(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ignore) {
            logger.debug("Cannot convert {} to a number", value);
            return null;
        }
    }

    /**
     * As soon as subscribing succeeded and corresponding MQTT values have been received, the ChannelType and
     * ChannelState are determined.
     *
     * Public for testing only.
     */
    public void attributesReceived() {
        final String commandTopic = !attributes.settable ? ""
                : "homie/" + thingUID.getId() + "/" + parentNode.nodeID + "/" + propertyID + "/set";
        final String stateTopic = "homie/" + thingUID.getId() + "/" + parentNode.nodeID + "/" + propertyID;

        AbstractMqttThingValue value;
        switch (attributes.datatype) {
            case boolean_:
                if (attributes.settable) {
                    value = new OnOffValue("true", "false", false);
                } else {
                    value = OnOffValue.createReceiveOnly("true", "false", false);
                }
                break;
            case color_:
                value = new ColorValue(attributes.format.contains("rgb"), null, null);
                break;
            case enum_:
                String enumValues[] = attributes.format.split(",");
                value = new TextValue(enumValues);
                break;
            case float_:
            case integer_:
                boolean isFloat = attributes.datatype == DataTypeEnum.float_;
                String s[] = attributes.format.split("\\:");
                BigDecimal min = s.length == 2 ? convertFromString(s[0]) : null;
                BigDecimal max = s.length == 2 ? convertFromString(s[1]) : null;
                BigDecimal step = (min != null && max != null)
                        ? max.subtract(min).divide(new BigDecimal(100.0), new MathContext(isFloat ? 2 : 0))
                        : null;
                if (step != null && !isFloat && step.intValue() <= 0) {
                    step = new BigDecimal(1);
                }

                value = new NumberValue(isFloat, min, max, step, false);
                break;
            case string_:
            case unknown:
            default:
                value = new TextValue();
                break;
        }

        final ChannelState channelState = new ChannelState(stateTopic, commandTopic, channelUID, value);
        this.channelState = channelState;

        if (StringUtils.isBlank(stateTopic)) {
            type = ChannelTypeBuilder.trigger(channelTypeUID, attributes.name).build();
        } else {
            type = ChannelTypeBuilder.state(channelTypeUID, attributes.name, channelState.getItemType())
                    .withStateDescription(value.createStateDescription(attributes.unit, !attributes.settable)).build();
        }
    }

    /**
     * Unsubscribe from all property attributes
     *
     * @param topicMapper The topic mapper object, where the subscribe has been performed before
     * @return Returns a future that completes as soon as all unsubscriptions have been performed.
     */
    @Override
    public CompletableFuture<Void> unsubscribe(MqttTopicClassMapper topicMapper) {
        return topicMapper.unsubscribe(attributes);
    }

    /**
     * @return Returns the channelState. You should have called {@link Property#subscribe(MqttTopicClassMapper, int)}
     *         and waited for the future to complete before calling this Getter.
     */
    public @Nullable ChannelState getChannelState() {
        return channelState;
    }

    /**
     * Subscribes to the state topic on the given connection and informs about updates on the given listener.
     *
     * @param connection A broker connection
     * @param channelStateUpdateListener An update listener
     * @return A future that completes with true if the subscribing worked and false and/or exceptionally otherwise.
     */
    public CompletableFuture<Boolean> startChannel(MqttBrokerConnection connection,
            ChannelStateUpdateListener channelStateUpdateListener) {
        final ChannelState channelState = this.channelState;
        if (channelState != null) {
            return channelState.start(connection, channelStateUpdateListener);
        }
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Removes the subscription to the state topic.
     *
     * @return A future that completes with true if the unsubscribing worked and false and/or exceptionally otherwise.
     */
    public CompletableFuture<Boolean> stopChannel() {
        final ChannelState channelState = this.channelState;
        if (channelState != null) {
            return channelState.stop();
        }
        return CompletableFuture.completedFuture(true);
    }

    /**
     * @return Returns the channel type of this property.
     */
    public @Nullable ChannelType getType() {
        return type;
    }
}
