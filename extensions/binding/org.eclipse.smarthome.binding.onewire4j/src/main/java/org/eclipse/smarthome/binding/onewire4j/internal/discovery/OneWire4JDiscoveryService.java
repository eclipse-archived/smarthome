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
package org.eclipse.smarthome.binding.onewire4j.internal.discovery;

import java.io.IOException;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.onewire4j.OneWire4JBindingConstants;
import org.eclipse.smarthome.binding.onewire4j.handler.SpswBridgeHandler;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.container.OneWireDevice;
import de.ibapl.onewire4j.container.TemperatureContainer;

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
@NonNullByDefault
public class OneWire4JDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(OneWire4JDiscoveryService.class);

    private final static int SEARCH_TIME = 60;

    private final SpswBridgeHandler spswBridgeHandler;

    public OneWire4JDiscoveryService(SpswBridgeHandler spswBridgeHandler) {
        super(SpswBridgeHandler.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.spswBridgeHandler = spswBridgeHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SpswBridgeHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        try {
            spswBridgeHandler.discover(container -> {
                addDevice(container.getAddress());
            }, true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        stopScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    private String devIdStr(@Nullable String devidStr) {
        if (devidStr == null) {
            throw new RuntimeException();
        } else {
            return devidStr;
        }
    }

    @Override
    public void deactivate() {
        stopScan();
    }

    private void addDevice(long deviceId) {
        final String deviceIdStr = OneWireContainer.addressToString(deviceId);
        final ThingUID thingUID = getThingUID(deviceIdStr);

        ThingTypeUID thingTypeUID = getThingTypeUID("unknown");
        try {
            OneWireDevice owd = OneWireDevice.fromAdress(deviceId, false);
            if (owd instanceof TemperatureContainer) {
                thingTypeUID = getThingTypeUID("temperature");

            }
        } catch (Exception e) {
            // Unknown device
        }

        ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperty("deviceId", deviceIdStr).withBridge(bridgeUID).withRepresentationProperty(deviceIdStr)
                .withLabel("Temp" + deviceIdStr).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID getThingUID(String deviceId) {
        ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = getThingTypeUID("temperature");

        return new ThingUID(thingTypeUID, bridgeUID, deviceId);
    }

    private ThingTypeUID getThingTypeUID(String oneWireBinding) {
        return new ThingTypeUID(OneWire4JBindingConstants.BINDING_ID, oneWireBinding);
    }

}
