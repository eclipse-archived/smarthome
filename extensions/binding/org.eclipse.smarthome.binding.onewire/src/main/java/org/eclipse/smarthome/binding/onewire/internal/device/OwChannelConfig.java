/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.onewire.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link OwChannelConfig} class defines a map entry
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwChannelConfig {
    public String channelId;
    public ChannelTypeUID channelTypeUID;
    public @Nullable String label;

    public OwChannelConfig(String channelId, ChannelTypeUID channelTypeUID, @Nullable String label) {
        this.channelId = channelId;
        this.channelTypeUID = channelTypeUID;
        this.label = label;
    }

    public OwChannelConfig(String channelId, ChannelTypeUID channelTypeUID) {
        this(channelId, channelTypeUID, null);
    }
}
