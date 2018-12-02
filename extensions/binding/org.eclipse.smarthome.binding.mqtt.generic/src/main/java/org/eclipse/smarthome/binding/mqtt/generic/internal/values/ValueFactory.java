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
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.ChannelConfig;

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
    public static Value createValueState(ChannelConfig config, String channelTypeID) throws IllegalArgumentException {
        Value value;
        switch (channelTypeID) {
            case MqttBindingConstants.STRING:
                value = StringUtils.isBlank(config.allowedStates) ? new TextValue()
                        : new TextValue(config.allowedStates.split(","));
                break;
            case MqttBindingConstants.NUMBER:
                value = new NumberValue(config.isDecimal, config.min, config.max, config.step, false);
                break;
            case MqttBindingConstants.DIMMER:
                value = new NumberValue(config.isDecimal, config.min, config.max, config.step, true);
                break;
            case MqttBindingConstants.COLOR_RGB:
                value = new ColorValue(true, config.on, config.off);
                break;
            case MqttBindingConstants.COLOR_HSB:
                value = new ColorValue(false, config.on, config.off);
                break;
            case MqttBindingConstants.SWITCH:
                value = new OnOffValue(config.on, config.off);
                break;
            case MqttBindingConstants.CONTACT:
                value = new OpenCloseValue(config.on, config.off);
                break;
            default:
                throw new IllegalArgumentException("ChannelTypeUID not recognised: " + channelTypeID);
        }
        return value;
    }

}
