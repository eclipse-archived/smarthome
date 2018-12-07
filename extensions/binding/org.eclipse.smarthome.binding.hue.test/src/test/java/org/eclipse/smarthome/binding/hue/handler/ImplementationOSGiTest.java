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
package org.eclipse.smarthome.binding.hue.handler;

import static org.junit.Assert.assertNotNull;

import org.eclipse.smarthome.binding.hue.internal.utils.AsyncHttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;

/**
 * This is a full implementation test (except discovery), with an emulated Hue bridge http servlet.
 * The implementation is tested for correct unauthenticated access, api key generation,
 * access to the bridge configuration, to the lights and to perform a light state update.
 *
 * @author David Graeff - Initial contribution
 */
public class ImplementationOSGiTest extends JavaOSGiTest {

    private final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge");
    private final ThingUID BRIDGE_THING_UID = new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge");

    private static final String TEST_USER_NAME = "eshTestUser";
    private static final String DUMMY_HOST = "1.2.3.4";

    private ThingRegistry thingRegistry;
    private AsyncHttpClient asyncHttpClient;

    protected HueBridgeHandler hueBridgeHandler;
    protected Bridge bridge;

    @Before
    public void setUp() {
        registerVolatileStorageService();
        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        HttpClientFactory httpClientFactory = getService(HttpClientFactory.class, HttpClientFactory.class);
        assertNotNull(httpClientFactory);
        asyncHttpClient = new AsyncHttpClient(httpClientFactory.getCommonHttpClient(), 5000);
        assertNotNull(thingRegistry);
    }

    @After
    public void cleanUp() {
        thingRegistry.remove(BRIDGE_THING_UID);
    }
}
