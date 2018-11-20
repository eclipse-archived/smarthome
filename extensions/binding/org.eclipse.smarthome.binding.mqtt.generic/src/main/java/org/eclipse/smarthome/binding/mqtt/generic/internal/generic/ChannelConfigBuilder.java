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
package org.eclipse.smarthome.binding.mqtt.generic.internal.generic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link ChannelConfig} is required for the {@link ChannelState} object.
 * For easily creating a configuration, use this builder.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelConfigBuilder {
    private final ChannelConfig config = new ChannelConfig();

    private ChannelConfigBuilder() {
    }

    public static ChannelConfigBuilder create() {
        return new ChannelConfigBuilder();
    }

    public static ChannelConfigBuilder create(@Nullable String stateTopic, @Nullable String commandTopic) {
        return new ChannelConfigBuilder().withStateTopic(stateTopic).withCommandTopic(commandTopic);
    }

    public ChannelConfig build() {
        return config;
    }

    public ChannelConfigBuilder withStateTopic(@Nullable String topic) {
        if (topic != null) {
            config.stateTopic = topic;
        }
        return this;
    }

    public ChannelConfigBuilder withCommandTopic(@Nullable String topic) {
        if (topic != null) {
            config.commandTopic = topic;
        }
        return this;
    }

    public ChannelConfigBuilder withRetain(boolean retain) {
        config.retained = retain;
        return this;
    }

    public ChannelConfigBuilder makeTrigger(boolean trigger) {
        config.trigger = trigger;
        return this;
    }
}
