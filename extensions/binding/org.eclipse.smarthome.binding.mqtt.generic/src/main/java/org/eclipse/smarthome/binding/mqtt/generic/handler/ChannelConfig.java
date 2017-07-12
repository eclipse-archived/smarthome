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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The channel configuration
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelConfig {
    public String stateTopic = "";
    public String commandTopic = "";
    public String transformationPattern = "";
    public String allowedStates = "";

    public @Nullable BigDecimal min;
    public @Nullable BigDecimal max;
    public @Nullable BigDecimal step;
    public boolean isFloat = false;
    public boolean inverse = false;
    public boolean isRGB = false;

    public @Nullable String on;
    public @Nullable String off;
}