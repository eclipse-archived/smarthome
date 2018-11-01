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
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.OwPageBuffer;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverConnection;
import org.eclipse.smarthome.binding.onewire.internal.owserver.OwserverConnectionState;
import org.eclipse.smarthome.binding.onewire.test.OwserverTestServer;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.TestPortUtil;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Tests cases for {@link OwserverConnection}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class OwserverConnectionTest extends JavaTest {

    private final String TEST_HOST = "127.0.0.1";

    OwserverTestServer testServer;
    OwserverConnection owserverConnection;

    @Mock
    OwserverBridgeHandler bridgeHandler;

    private int testPort;

    @Before
    public void setup() throws Exception {
        initMocks(this);

        CompletableFuture<Boolean> serverStarted = new CompletableFuture<>();
        testPort = TestPortUtil.findFreePort();
        try {
            testServer = new OwserverTestServer(testPort);
            testServer.startServer(serverStarted);
        } catch (IOException e) {
            fail("could not start test server");
        }

        owserverConnection = new OwserverConnection(bridgeHandler);
        owserverConnection.setHost(TEST_HOST);
        owserverConnection.setPort(testPort);

        serverStarted.get(); // wait for the server thread to start
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
        owserverConnection.setPort(1);

        owserverConnection.start();

        Mockito.verify(bridgeHandler, timeout(100)).reportConnectionState(OwserverConnectionState.FAILED);
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
    public void testReadDecimalTypeArray() {
        owserverConnection.start();
        try {
            List<State> numbers = owserverConnection.readDecimalTypeArray("testsensor/decimalarray");

            assertEquals(3834, ((DecimalType) numbers.get(0)).intValue());
            assertEquals(0, ((DecimalType) numbers.get(1)).intValue());
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
