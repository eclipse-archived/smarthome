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

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.binding.iota.handler.IotaBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Before;
import org.junit.Test;

/**
 * The {@link IotaBridgeHandlerTest} provides test cases for the {@link IotaBridgeHandler}. The tests provide mocks for
 * supporting entities using Mockito.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaBridgeHandlerTest extends JavaTest {

    private IotaTestMocks mocks;
    private Bridge bridge;

    @Before
    public void setUp() {
        mocks = new IotaTestMocks();
        bridge = mocks.initializeBridge();
    }

    @Test
    public void bridgeShouldInitialize() {
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
    }

}
