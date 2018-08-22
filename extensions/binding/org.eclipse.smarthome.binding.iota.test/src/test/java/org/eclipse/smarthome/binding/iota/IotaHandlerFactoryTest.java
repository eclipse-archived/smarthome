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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.eclipse.smarthome.binding.iota.handler.IotaBridgeHandler;
import org.eclipse.smarthome.binding.iota.handler.IotaTopicThingHandler;
import org.eclipse.smarthome.binding.iota.handler.IotaWalletThingHandler;
import org.eclipse.smarthome.binding.iota.internal.IotaHandlerFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Before;
import org.junit.Test;

/**
 * The {@link IotaHandlerFactoryTest} provides test cases for {@link IotaHandlerFactory}. The tests provide mocks for
 * supporting entities using Mockito.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaHandlerFactoryTest {

    private IotaHandlerFactory factory;

    @Before
    public void setup() {
        factory = new IotaHandlerFactory();
    }

    @Test
    public void shouldReturnNullForUnknownThingTypeUID() {
        Thing thing = mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID("anyBinding:someThingType"));
        assertEquals(factory.createHandler(thing), null);
    }

    @Test
    public void shouldReturnIotaTopicThingHandler() {
        Thing thing = mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(IotaBindingConstants.THING_TYPE_IOTA_TOPIC);
        assertThat(factory.createHandler(thing), is(instanceOf(IotaTopicThingHandler.class)));
    }

    @Test
    public void shouldReturnIotaWalletThingHandler() {
        Thing thing = mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(IotaBindingConstants.THING_TYPE_IOTA_WALLET);
        assertThat(factory.createHandler(thing), is(instanceOf(IotaWalletThingHandler.class)));
    }

    @Test
    public void shouldReturnIotaBridgeHandler() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getThingTypeUID()).thenReturn(IotaBindingConstants.THING_TYPE_BRIDGE);
        assertThat(factory.createHandler(bridge), is(instanceOf(IotaBridgeHandler.class)));
    }

}
