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
package org.eclipse.smarthome.binding.homematic.internal.discovery;

import static org.eclipse.smarthome.binding.homematic.test.util.BridgeHelper.createHomematicBridge;
import static org.eclipse.smarthome.binding.homematic.test.util.DimmerHelper.createDimmerHmDevice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.smarthome.binding.homematic.handler.HomematicBridgeHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.HomematicGateway;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.binding.homematic.internal.type.HomematicTypeGenerator;
import org.eclipse.smarthome.binding.homematic.test.util.SimpleDiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link HomematicDeviceDiscoveryServiceTest}.
 * 
 * @author Florian Stolte - Initial Contribution
 *
 */
public class HomematicDeviceDiscoveryServiceTest extends JavaTest {

    private HomematicDeviceDiscoveryService homematicDeviceDiscoveryService;
    private HomematicBridgeHandler homematicBridgeHandler;

    @Before
    public void setup() throws IOException {
        this.homematicBridgeHandler = mockHomematicBridgeHandler();
        this.homematicDeviceDiscoveryService = new HomematicDeviceDiscoveryService(homematicBridgeHandler);
    }

    private HomematicBridgeHandler mockHomematicBridgeHandler() throws IOException {
        HomematicBridgeHandler homematicBridgeHandler = mock(HomematicBridgeHandler.class);
        Bridge bridge = createHomematicBridge();
        HomematicGateway homematicGateway = mockHomematicGateway();
        HomematicTypeGenerator homematicTypeGenerator = mockHomematicTypeGenerator();

        when(homematicBridgeHandler.getThing()).thenReturn(bridge);
        when(homematicBridgeHandler.getGateway()).thenReturn(homematicGateway);
        when(homematicBridgeHandler.getTypeGenerator()).thenReturn(homematicTypeGenerator);

        return homematicBridgeHandler;
    }

    private HomematicGateway mockHomematicGateway() throws IOException {
        HomematicGateway homematicGateway = mock(HomematicGateway.class);

        when(homematicGateway.getInstallMode()).thenReturn(60);

        return homematicGateway;
    }

    private HomematicTypeGenerator mockHomematicTypeGenerator() {
        return mock(HomematicTypeGenerator.class);
    }

    @Test
    public void testDiscoveryResultIsReportedForNewDevice() {
        SimpleDiscoveryListener discoveryListener = new SimpleDiscoveryListener();
        homematicDeviceDiscoveryService.addDiscoveryListener(discoveryListener);

        HmDevice hmDevice = createDimmerHmDevice();
        homematicDeviceDiscoveryService.deviceDiscovered(hmDevice);

        assertThat(discoveryListener.discoveredResults.size(), is(1));
        discoveryResultMatchesHmDevice(discoveryListener.discoveredResults.element(), hmDevice);
    }

    @Test
    public void testDevicesAreLoadedFromBridgeDuringDiscovery() throws IOException {
        startScanAndWaitForLoadedDevices();

        verify(homematicBridgeHandler.getGateway()).loadAllDeviceMetadata();
    }

    @Test
    public void testInstallModeIsNotActiveDuringInitialDiscovery() throws IOException {
        startScanAndWaitForLoadedDevices();

        verify(homematicBridgeHandler.getGateway(), never()).setInstallMode(eq(true), anyInt());
    }

    @Test
    public void testInstallModeIsActiveDuringSubsequentDiscovery() throws IOException {
        homematicBridgeHandler.getThing()
                .setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));

        startScanAndWaitForLoadedDevices();

        verify(homematicBridgeHandler.getGateway()).setInstallMode(true, 60);
    }

    @Test
    public void testStoppingDiscoveryDisablesInstallMode() throws IOException {
        homematicBridgeHandler.getThing()
                .setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));
        homematicDeviceDiscoveryService.startScan();

        homematicDeviceDiscoveryService.stopScan();

        verify(homematicBridgeHandler.getGateway()).setInstallMode(false, 0);
    }

    private void startScanAndWaitForLoadedDevices() {
        homematicDeviceDiscoveryService.startScan();
        waitForAssert(() -> verify(homematicBridgeHandler).setOfflineStatus(), 1000, 50);
    }

    private void discoveryResultMatchesHmDevice(DiscoveryResult result, HmDevice device) {
        assertThat(result.getThingTypeUID().getId(), is(device.getType()));
        assertThat(result.getThingUID().getId(), is(device.getAddress()));
        assertThat(result.getLabel(), is(device.getName()));
    }
}
