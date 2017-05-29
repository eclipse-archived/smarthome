/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.yeelight.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.yeelight.YeelightBindingConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothAdapter;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothClass;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothDevice;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothManager;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothManufacturer;
import org.eclipse.smarthome.io.transport.bluetooth.le.BluetoothLeScanner;
import org.eclipse.smarthome.io.transport.bluetooth.le.ScanCallback;
import org.eclipse.smarthome.io.transport.bluetooth.le.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YeelightDiscoveryService} handles searching for new Bluetooth adapters
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class YeelightDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(YeelightDiscoveryService.class);

    private final static int SEARCH_TIME = 15;

    private BluetoothAdapter adapter = null;
    private BluetoothLeScanner scanner = null;
    private DiscoveryScanCallback callback = null;

    public YeelightDiscoveryService() {
        super(SEARCH_TIME);

        logger.debug("Creating Yeelight adapter discovery service");
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Collections.singleton(YeelightBindingConstants.THING_TYPE_YEELIGHT_BLUE);
    }

    @Override
    public void startScan() {
        if (adapter != null) {
            // logger.debug("Adapter not null - scan already in progress");
            // return;
        }

        logger.debug("Start Bluetooth adapter LE scan");
        if (adapter == null) {
            adapter = BluetoothManager.getDefaultAdapter();
        }
        if (adapter == null) {
            logger.info("No default bluetooth adapter found");
            return;
        }

        if (adapter.isEnabled() == false) {
            logger.info("Bluetooth adapter is disabled");
            return;
        }

        // If the adapter is enabled and supports BLE then start a scan
        if (adapter.isEnabled() == false || adapter.isLeReady() == false) {
            logger.info("Bluetooth adapter is disabled or not BLE capable");
            return;
        }

        callback = new DiscoveryScanCallback();
        scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) {
            logger.error("Bluetooth adapter did not return scanner!");
            return;
        }

        scanner.startScan(callback);
    }

    @Override
    public void stopScan() {
        if (adapter == null | scanner == null) {
            return;
        }

        // Stop the scan
        if (scanner != null) {
            scanner.stopScan(callback);
        }
    }

    void addDiscoveryResultThing(String thingType, String address, String label, String manufacturer) {
        // Sanatise the address
        String thingId = address.toLowerCase().replaceAll("[^a-z0-9_/]", "");

        // Create the new thing
        ThingTypeUID thingTypeUID = new ThingTypeUID(thingType);
        logger.info("Creating new Bluetooth thing {} {}", thingTypeUID, thingId);
        ThingUID thingUID = new ThingUID(thingTypeUID, thingId);

        Map<String, Object> properties = new HashMap<>(2);
        properties.put(YeelightBindingConstants.PROPERTY_ADDRESS, address);
        if (manufacturer != null) {
            properties.put(YeelightBindingConstants.PROPERTY_MANUFACTURER, manufacturer);
        }
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withLabel(label).build();

        thingDiscovered(discoveryResult);
    }

    class DiscoveryScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            BluetoothClass clazz = device.getBluetoothClass();

            // Use the name to detect if this is a Yeelight.
            // Could also use the UUID?
            if (!YeelightBindingConstants.YEELIGHT_NAME.equals(device.getName())) {
                // Not a Yeelight Blue II
                return;
            }

            String thingTypeUID = null;
            String label = "Yeelight";
            if (clazz != null) {
                switch (clazz.getMajorDeviceClass()) {
                }
            }

            // if (device.getAddress().equals("54:4A:16:20:4B:AE")) {
            thingTypeUID = YeelightBindingConstants.THING_TYPE_YEELIGHT_BLUE.getAsString();
            // }

            String manufacturerName = null;
            BluetoothManufacturer manufacturer = BluetoothManufacturer.getManufacturer(device.getManufacturer());
            if (manufacturer != null) {
                label += " by " + manufacturer.getLabel();
                manufacturerName = manufacturer.getLabel();
            }

            if (device.getName() != null) {
                label += " (" + device.getName() + ")";
            }
            addDiscoveryResultThing(thingTypeUID, device.getAddress(), label, manufacturerName);
        }
    }
}
