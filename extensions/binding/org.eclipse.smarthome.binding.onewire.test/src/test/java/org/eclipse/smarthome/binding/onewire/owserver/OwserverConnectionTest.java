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
package org.eclipse.smarthome.binding.onewire.owserver;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.util.List;

import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.OwPageBuffer;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverConnection;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverConnectionState;
import org.eclipse.smarthome.binding.onewire.test.OwserverTestServer;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests cases for {@link OwserverConnection}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class OwserverConnectionTest extends JavaTest {

    private final String TEST_HOST = "127.0.0.1";
    private int TEST_PORT = 54304; // non standard port

    OwserverTestServer testServer;
    OwserverConnection owserverConnection;

    @Mock
    OwserverBridgeHandler bridgeHandler;

    @Before
    public void setup() {
        int error_counter = 0;
        MockitoAnnotations.initMocks(this);

        while (true) {
            try {
                testServer = new OwserverTestServer(TEST_PORT);
                testServer.startServer();
                break;
            } catch (IOException e) {
                error_counter++;
                TEST_PORT++;
                if (error_counter > 3) {
                    fail("could not start test server: " + e.getMessage());
                }
            }
        }

        owserverConnection = new OwserverConnection(bridgeHandler);
        owserverConnection.setHost(TEST_HOST);
        owserverConnection.setPort(TEST_PORT);
    }

    @After
    public void tearDown() {
        try {
            testServer.stopServer();
        } catch (IOException e) {
            fail("could not stop test server");
        }
    }

    @Test
    public void successfullConnectionReportedToBridgeHandler() {
        owserverConnection.start();

        Mockito.verify(bridgeHandler).reportConnectionState(OwserverConnectionState.OPENED);
    }

    @Test
    public void failedConnectionReportedToBridgeHandler() {
        owserverConnection.setPort(TEST_PORT + 1);

        owserverConnection.start();

        Mockito.verify(bridgeHandler).reportConnectionState(OwserverConnectionState.FAILED);
    }

    @Test
    public void testGetDirectory() {
        owserverConnection.start();
        try {
            List<String> presence = owserverConnection.getDirectory();

            assertEquals(3, presence.size());
            assertEquals("sensor0", presence.get(0));
            assertEquals("sensor1", presence.get(1));
            assertEquals("sensor2", presence.get(2));
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void testCheckPresence() {
        owserverConnection.start();
        try {
            State presence = owserverConnection.checkPresence("present");
            assertEquals(OnOffType.ON, presence);

            presence = owserverConnection.checkPresence("notpresent");
            assertEquals(OnOffType.OFF, presence);
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void testReadDecimalType() {
        owserverConnection.start();
        try {
            DecimalType number = (DecimalType) owserverConnection.readDecimalType("testsensor/decimal");

            assertEquals(17.4, number.doubleValue(), 0.01);
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void testGetPages() {
        owserverConnection.start();
        try {
            OwPageBuffer pageBuffer = owserverConnection.readPages("testsensor");

            assertEquals(31, pageBuffer.getByte(5, 7));
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void testWriteDecimalType() {
        owserverConnection.start();
        try {
            owserverConnection.writeDecimalType("testsensor/decimal", new DecimalType(2009));

            Mockito.verify(bridgeHandler, never()).reportConnectionState(OwserverConnectionState.FAILED);
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }
}
