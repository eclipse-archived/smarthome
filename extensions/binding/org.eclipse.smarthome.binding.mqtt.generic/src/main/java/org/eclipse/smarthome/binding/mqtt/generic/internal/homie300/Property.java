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
package org.eclipse.smarthome.binding.mqtt.generic.internal.homie300;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.generic.handler.ChannelState;
import org.eclipse.smarthome.binding.mqtt.generic.handler.ChannelStateUpdateListener;
import org.eclipse.smarthome.binding.mqtt.generic.internal.AbstractMqttThingValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ColorValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.NumberValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.OnOffValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.TextValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.homie300.PropertyAttributes.DataTypeEnum;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A homie Property (which translates into an ESH channel).
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Property implements Subscribable {
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
     *         out
     * @throws MqttException
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

    public void attributesReceived() {
        final String commandTopic = !attributes.settable ? ""
                : "homie/" + thingUID.getId() + "/" + parentNode.nodeID + "/" + propertyID + "/set";
        final String stateTopic = "homie/" + thingUID.getId() + "/" + parentNode.nodeID + "/" + propertyID;
        final String description = "";
        final URI configDescriptionURI = null;
        final ChannelKind kind = StringUtils.isBlank(stateTopic) ? ChannelKind.TRIGGER : ChannelKind.STATE;
        final String statePattern = "%s " + attributes.unit;
        final Set<String> tags = Collections.emptySet();
        final boolean advanced = false;
        final String category = "";
        final EventDescription event = null;
        StateDescription stateDescription;

        AbstractMqttThingValue value;
        switch (attributes.datatype) {
            case boolean_:
                value = new OnOffValue("true", "false", false, attributes.settable);
                stateDescription = new StateDescription(null, null, null, statePattern, !attributes.settable,
                        Collections.emptyList());
                break;
            case color_:
                value = new ColorValue(attributes.format.contains("rgb"));
                stateDescription = new StateDescription(null, null, null, statePattern, !attributes.settable,
                        Collections.emptyList());
                break;
            case enum_:
                String enumValues[] = attributes.format.split(",");
                value = new TextValue(enumValues);
                List<StateOption> stateOptions = new ArrayList<>();
                for (String state : enumValues) {
                    stateOptions.add(new StateOption(state, state));
                }
                stateDescription = new StateDescription(null, null, null, statePattern, !attributes.settable,
                        stateOptions);
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
                stateDescription = new StateDescription(min, max, step, statePattern, !attributes.settable,
                        Collections.emptyList());
                break;
            case string_:
            case unknown:
            default:
                value = new TextValue();
                stateDescription = new StateDescription(null, null, null, statePattern, !attributes.settable,
                        Collections.emptyList());
                break;
        }

        channelState = new ChannelState(stateTopic, commandTopic, channelUID, value);

        type = new ChannelType(channelTypeUID, advanced, channelState.getItemType(), kind, attributes.name, description,
                category, tags, stateDescription, event, configDescriptionURI);
    }

    @Override
    public void unsubscribe(MqttTopicClassMapper topicMapper) {
        topicMapper.unsubscribe(attributes);
    }

    /**
     * @return the channelState
     */
    public @Nullable ChannelState getChannelState() {
        return channelState;
    }

    public CompletableFuture<Boolean> startChannel(MqttBrokerConnection connection,
            ChannelStateUpdateListener channelStateUpdateListener) throws MqttException {
        if (channelState != null) {
            return channelState.start(connection, channelStateUpdateListener);
        }
        return CompletableFuture.completedFuture(false);
    }

    public void stopChannel() {
        if (channelState != null) {
            channelState.stop();
        }
    }

    public @Nullable ChannelType getType() {
        return type;
    }
}
