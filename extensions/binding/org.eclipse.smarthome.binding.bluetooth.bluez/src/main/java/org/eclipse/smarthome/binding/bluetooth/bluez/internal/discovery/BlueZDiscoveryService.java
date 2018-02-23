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
package org.eclipse.smarthome.binding.bluetooth.bluez.internal.discovery;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.bluetooth.bluez.BlueZAdapterConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tinyb.BluetoothAdapter;
import tinyb.BluetoothManager;

/**
 * This is a discovery service, which checks whether we are running on a Linux with a BlueZ stack.
 * If this is the case, we create a bridge handler that provides Bluetooth access through BlueZ.
 *
 * @author Kai Kreuzer - Initial Contribution and API
 *
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.bluetooth.bluez")
public class BlueZDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(BlueZDiscoveryService.class);

    private BluetoothManager manager;

    public BlueZDiscoveryService() {
        super(Collections.singleton(BlueZAdapterConstants.THING_TYPE_BLUEZ), 1, true);
    }

    @Override
    @Activate
    protected void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        modified(configProperties);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.modified(configProperties);
        if (isBackgroundDiscoveryEnabled()) {
            startScan();
        }
    }

    @Override
    @Deactivate
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        try {
            manager = BluetoothManager.getBluetoothManager();
            manager.getAdapters().stream().map(this::createDiscoveryResult).forEach(this::thingDiscovered);
        } catch (UnsatisfiedLinkError e) {
            // we cannot initialize the BlueZ stack
            return;
        } catch (RuntimeException e) {
            // we do not get anything more specific from TinyB here
            if (e.getMessage().contains("AccessDenied")) {
                logger.error(
                        "Cannot access BlueZ stack due to permission problems. Make sure that your OS user is part of the 'bluetooth' group of BlueZ.");
            } else {
                logger.error("Failed to scan for Bluetooth devices: {}", e.getMessage());
            }
        }
    }

    private DiscoveryResult createDiscoveryResult(BluetoothAdapter adapter) {
        return DiscoveryResultBuilder.create(new ThingUID(BlueZAdapterConstants.THING_TYPE_BLUEZ, getId(adapter)))
                .withLabel("Bluetooth Interface " + adapter.getName())
                .withProperty(BlueZAdapterConstants.PROPERTY_ADDRESS, adapter.getAddress())
                .withRepresentationProperty(BlueZAdapterConstants.PROPERTY_ADDRESS).build();
    }

    private String getId(BluetoothAdapter adapter) {
        return adapter.getInterfaceName().replaceAll("[^a-zA-Z0-9_]", "");
    }
}
