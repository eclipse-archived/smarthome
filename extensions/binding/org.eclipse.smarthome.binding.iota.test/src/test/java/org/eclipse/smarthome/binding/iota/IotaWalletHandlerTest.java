/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http:www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.iota;

import static org.junit.Assert.*;

import org.eclipse.smarthome.binding.iota.handler.ChannelConfig;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Before;
import org.junit.Test;

/**
 * The {@link IotaWalletHandlerTest} provides test cases for {@link IotaHandler}. The tests provide mocks for supporting
 * entities using Mockito.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaWalletHandlerTest extends JavaTest {

    private IotaTestMocks mocks;

    private Bridge bridge;
    private Thing thing;

    private ChannelConfig config;

    @Before
    public void setUp() {
        mocks = new IotaTestMocks();
        bridge = mocks.initializeBridge();
        thing = mocks.initializeThingWallet(true);
    }

    @Test
    public void thingShouldInitialize() {
        assertEquals(ThingStatus.ONLINE, thing.getStatus());
    }

    @Test
    public void bridgeShouldInitialize() {
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
    }

    @Test
    public void processingMessageShouldUpdateChannelValue() {
        config = mocks.getConfigWallet();
        assertNull(config.getValue().getValue());
        config.processMessage("1");
        assertTrue(config.getValue().getValue().toFullString().equals("1"));
    }

}
