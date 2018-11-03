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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * A MQTT camera, following the https://www.home-assistant.io/components/camera.mqtt/ specification.
 *
 * At the moment this only notifies the user that this feature is not yet supported.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentCamera extends AbstractComponent {
    public ComponentCamera(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener) {
        super(thing, haID, configJSON);
        throw new UnsupportedOperationException("Component:Camera not supported yet");
    }

    @Override
    public @NonNull String name() {
        return "Camera";
    }
}
