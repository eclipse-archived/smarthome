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
package org.eclipse.smarthome.binding.sonos.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Dynamic provider of state options while leaving other state description fields as original.
 *
 * @author Laurent Garnier - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, SonosStateDescriptionOptionProvider.class })
@NonNullByDefault
public class SonosStateDescriptionOptionProvider implements DynamicStateDescriptionProvider {
    private final Map<ChannelUID, @Nullable List<StateOption>> channelOptionsMap = new ConcurrentHashMap<>();

    public @Nullable List<StateOption> getStateOptions(ChannelUID channelUID) {
        return channelOptionsMap.get(channelUID);
    }

    public void setStateOptions(ChannelUID channelUID, List<StateOption> options) {
        channelOptionsMap.put(channelUID, options);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        List<StateOption> options = channelOptionsMap.get(channel.getUID());
        if (options == null) {
            return null;
        }

        if (original != null) {
            return new StateDescription(original.getMinimum(), original.getMaximum(), original.getStep(),
                    original.getPattern(), original.isReadOnly(), options);
        }

        return new StateDescription(null, null, null, null, false, options);
    }

    @Deactivate
    public void deactivate() {
        channelOptionsMap.clear();
    }
}
