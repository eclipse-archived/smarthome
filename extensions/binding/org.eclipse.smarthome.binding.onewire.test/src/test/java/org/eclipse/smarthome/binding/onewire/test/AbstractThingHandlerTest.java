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
package org.eclipse.smarthome.binding.onewire.test;

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.THING_TYPE_OWSERVER;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwBaseThingHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Assert;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Tests cases for {@link DigitalIOThingeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public abstract class AbstractThingHandlerTest extends JavaTest {

    protected Map<String, Object> bridgeProperties = new HashMap<>();
    protected Map<String, String> thingProperties = new HashMap<>();
    protected Map<String, Object> thingConfiguration = new HashMap<>();
    protected Map<String, Object> channelProperties = new HashMap<>();

    @Mock
    protected ThingHandlerCallback thingHandlerCallback;

    @Mock
    protected OwDynamicStateDescriptionProvider stateProvider;

    @Mock
    protected ThingHandlerCallback bridgeHandlerCallback;

    @Mock
    protected OwBaseBridgeHandler bridgeHandler;

    protected List<Channel> channels = new ArrayList<Channel>();

    protected Bridge bridge;
    protected Thing thing;
    protected OwBaseThingHandler thingHandler;

    protected InOrder inOrder;

    @After
    public void tearDown() {
        thingHandler.dispose();
    }

    protected void initializeHandlerMocks() {
        thingHandler.getThing().setHandler(thingHandler);
        thingHandler.setCallback(thingHandlerCallback);

        Mockito.doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(thingHandlerCallback).statusUpdated(any(), any());

        inOrder = Mockito.inOrder(bridgeHandler);
    }

    public void initializeBridge() {
        bridgeProperties = new HashMap<>();
        bridge = BridgeBuilder.create(THING_TYPE_OWSERVER, "testbridge").withLabel("Test Bridge")
                .withConfiguration(new Configuration(bridgeProperties)).build();

        bridge.setHandler(bridgeHandler);

        Mockito.doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(bridgeHandlerCallback).statusUpdated(any(), any());

        try {
            Mockito.doAnswer(answer -> {
                return OnOffType.ON;
            }).when(bridgeHandler).checkPresence(any());

            Mockito.doAnswer(answer -> {
                return new DecimalType(10);
            }).when(bridgeHandler).readDecimalType(any(), any());

        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

}
