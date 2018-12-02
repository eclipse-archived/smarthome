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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A user can add custom channels to a generic MQTT Thing.
 * This class contains the channel configuration.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelConfig {
    /** This is either a state topic or a trigger topic, depending on {@link #trigger}. */
    public String stateTopic = "";
    public String commandTopic = "";

    /**
     * If true, the channel state is not updated on a new message.
     * Instead a postCommand() call is performed.
     */
    public boolean postCommand = false;
    /** If true publishes messages as retained messages */
    public boolean retained = false;
    /** If true, the state topic will not update a state, but trigger a channel instead. */
    public boolean trigger = false;
    public String unit = "";

    public String transformationPattern = "";
    public String formatBeforePublish = "%s";
    public String allowedStates = "";

    public @Nullable BigDecimal min;
    public @Nullable BigDecimal max;
    public @Nullable BigDecimal step;
    public boolean isDecimal = false;
    public @Nullable String on;
    public @Nullable String off;
}
