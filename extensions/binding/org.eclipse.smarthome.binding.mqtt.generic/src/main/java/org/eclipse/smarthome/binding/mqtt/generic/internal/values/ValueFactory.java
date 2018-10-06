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
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.GenericChannelConfig;

/**
 * A factory t
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ValueFactory {
    /**
     * Creates a new channel state value.
     *
     * @param config The channel configuration
     * @param channelTypeID The channel type, for instance TEXT_CHANNEL.
     */
    public static AbstractMqttThingValue createValueState(GenericChannelConfig config, String channelTypeID) {
        AbstractMqttThingValue value;
        switch (channelTypeID) {
            case MqttBindingConstants.STRING:
                value = StringUtils.isBlank(config.allowedStates) ? new TextValue()
                        : new TextValue(config.allowedStates.split(","));
                break;
            case MqttBindingConstants.NUMBER:
                value = new NumberValue(config.isFloat, config.min, config.max, config.step, false);
                break;
            case MqttBindingConstants.DIMMER:
                value = new NumberValue(config.isFloat, config.min, config.max, config.step, true);
                break;
            case MqttBindingConstants.COLOR:
                value = new ColorValue(config.isRGB, null, null);
                break;
            case MqttBindingConstants.SWITCH:
                if (StringUtils.isBlank(config.allowedStates)) {
                    value = new OnOffValue(config.on, config.off, config.inverse);
                } else {
                    value = new OnOffValue(config.on, config.off, config.inverse);
                }
                break;
            case MqttBindingConstants.CONTACT:
                value = OnOffValue.createReceiveOnly(config.on, config.off, config.inverse);
                break;
            default:
                throw new IllegalArgumentException("ChannelTypeUID not recognised: " + channelTypeID);
        }
        return value;
    }

}
