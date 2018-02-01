/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.bluetooth.yeelightblue.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.binding.bluetooth.BluetoothDevice;
import org.eclipse.smarthome.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.eclipse.smarthome.binding.bluetooth.yeelightblue.YeelightBlueBindingConstants;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * This discovery participant is able to recognize Yeelight devices and create discovery results for them.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(immediate = true)
public class YeelightDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    @Override
    public @NonNull Set<@NonNull ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(YeelightBlueBindingConstants.THING_TYPE_BLUE2);
    }

    @Override
    public @Nullable ThingUID getThingUID(@NonNull BluetoothDevice device) {
        if (YeelightBlueBindingConstants.YEELIGHT_NAME.equals(device.getName())) {
            return new ThingUID(YeelightBlueBindingConstants.THING_TYPE_BLUE2, device.getAdapter().getUID(),
                    device.getAddress().toString().toLowerCase().replace(":", ""));
        } else {
            return null;
        }
    }

    @Override
    public DiscoveryResult createResult(@NonNull BluetoothDevice device) {
        ThingUID thingUID = getThingUID(device);

        if (thingUID != null) {
            String label = device.getName();

            Map<String, Object> properties = new HashMap<>();
            properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
            properties.put(Thing.PROPERTY_VENDOR, "Yeelight");
            Integer txPower = device.getTxPower();
            if (txPower != null) {
                properties.put(BluetoothBindingConstants.PROPERTY_TXPOWER, Integer.toString(txPower));
            }

            // Create the discovery result and add to the inbox
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(device.getAdapter().getUID()).withLabel(label).build();
        } else {
            return null;
        }
    }

}
