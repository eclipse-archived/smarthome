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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.iota.handler.ChannelConfig;
import org.eclipse.smarthome.binding.iota.handler.IotaBridgeHandler;
import org.eclipse.smarthome.binding.iota.handler.IotaTopicThingHandler;
import org.eclipse.smarthome.binding.iota.handler.IotaWalletThingHandler;
import org.eclipse.smarthome.binding.iota.handler.TransformationServiceProvider;
import org.eclipse.smarthome.binding.iota.internal.NumberValue;
import org.eclipse.smarthome.binding.iota.internal.TextValue;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.mockito.Mockito;

/**
 * The {@link IotaTestMocks} provides methods to facilitate testing of other classes
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaTestMocks {

    private IotaBridgeHandler iotaBridgeHandler;
    private IotaWalletThingHandler iotaWalletThingHandler;
    private IotaTopicThingHandler iotaTopicThingHandler;
    private Map<String, Object> bridgeProperties;
    private Map<String, Object> thingProperties;
    private Bridge bridge;
    private Thing thingWallet;
    private Thing thingTopic;
    private ChannelConfig configTopic;
    private ChannelConfig configWallet;

    public IotaTestMocks() {

    }

    protected Bridge initializeBridge() {
        bridgeProperties = new HashMap<>();
        bridge = BridgeBuilder.create(new ThingTypeUID("iota", "test-bridge"), "testbridge").withLabel("Test Bridge")
                .withConfiguration(new Configuration(bridgeProperties)).build();

        IotaUtilsImpl mockUtils = Mockito.mock(IotaUtilsImpl.class);
        when(mockUtils.checkAPI()).thenReturn(true);

        iotaBridgeHandler = new IotaBridgeHandler(bridge, mockUtils);
        bridge.setHandler(iotaBridgeHandler);
        ThingHandlerCallback bridgeHandlerCallback = mock(ThingHandlerCallback.class);
        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(bridgeHandlerCallback).statusUpdated(any(), any());
        iotaBridgeHandler.setCallback(bridgeHandlerCallback);
        iotaBridgeHandler.initialize();
        return bridge;
    }

    protected Thing initializeThingWallet(boolean isFloat) {
        thingProperties = new HashMap<>();

        Channel channel = ChannelBuilder
                .create(new ChannelUID("binding:thing-type:testthing:groupid"),
                        IotaBindingConstants.THING_TYPE_IOTA_WALLET.toString())
                .withKind(ChannelKind.STATE)
                .withType(new ChannelTypeUID(IotaBindingConstants.BINDING_ID, IotaBindingConstants.CHANNEL_BALANCE))
                .build();

        thingWallet = ThingBuilder.create(new ThingTypeUID("binding:thing-type:thing:groupid"), "testthing")
                .withLabel("Test Wallet Thing").withConfiguration(new Configuration(thingProperties))
                .withChannel(channel).build();

        iotaWalletThingHandler = new IotaWalletThingHandler(thingWallet);
        thingWallet.setHandler(iotaWalletThingHandler);
        ThingHandlerCallback thingHandlerCallback = mock(ThingHandlerCallback.class);
        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(thingHandlerCallback).statusUpdated(any(), any());
        iotaWalletThingHandler.setCallback(thingHandlerCallback);

        /**
         * Add the corresponding ChannelConfig configuration for this channel
         */
        configWallet = channel.getConfiguration().as(ChannelConfig.class);
        configWallet.setChannelUID(channel.getUID());
        configWallet.setChannelStateUpdateListener(iotaWalletThingHandler);

        /**
         * Set the current value of the channel
         */
        configWallet.setValue(new NumberValue(isFloat));
        iotaWalletThingHandler.addChannelDataByChannelUID(channel.getUID(), configWallet);

        iotaWalletThingHandler.initialize();

        return thingWallet;
    }

    protected Thing initializeThingTopic(String value, TransformationServiceProvider transformationServiceProvider,
            String transformationServiceName, String transformationPattern) {
        thingProperties = new HashMap<>();
        thingProperties.put("root", "");

        Channel channel = ChannelBuilder
                .create(new ChannelUID("binding:thing-type:testthing:groupid"),
                        IotaBindingConstants.THING_TYPE_IOTA_WALLET.toString())
                .withKind(ChannelKind.STATE)
                .withType(new ChannelTypeUID(IotaBindingConstants.BINDING_ID, IotaBindingConstants.TEXT_CHANNEL))
                .build();

        thingTopic = ThingBuilder.create(new ThingTypeUID("binding:thing-type:thing:groupid"), "testthing")
                .withLabel("Test Thing").withConfiguration(new Configuration(thingProperties))
                .withBridge(bridge.getBridgeUID()).withChannel(channel).build();

        iotaTopicThingHandler = new IotaTopicThingHandler(thingTopic);
        thingTopic.setHandler(iotaTopicThingHandler);
        ThingHandlerCallback thingHandlerCallback = mock(ThingHandlerCallback.class);
        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(thingHandlerCallback).statusUpdated(any(), any());
        iotaTopicThingHandler.setCallback(thingHandlerCallback);

        /**
         * Add the corresponding ChannelConfig configuration for this channel
         */
        configTopic = channel.getConfiguration().as(ChannelConfig.class);
        configTopic.setChannelUID(channel.getUID());
        configTopic.setChannelStateUpdateListener(iotaTopicThingHandler);

        /**
         * Set the current value of the channel
         */
        configTopic.setTransformationServiceProvider(transformationServiceProvider);
        configTopic.setTransformationServiceName(transformationServiceName);
        configTopic.setTransformationPattern(transformationPattern);
        configTopic.setValue(new TextValue(value));
        iotaTopicThingHandler.addChannelDataByChannelUID(channel.getUID(), configTopic);

        iotaTopicThingHandler.initialize();

        return thingTopic;
    }

    protected ChannelConfig getConfigTopic() {
        return configTopic;
    }

    protected ChannelConfig getConfigWallet() {
        return configWallet;
    }

    protected IotaBridgeHandler getIotaBridgeHandler() {
        return iotaBridgeHandler;
    }

    protected IotaTopicThingHandler getIotaTopicThingHandler() {
        return iotaTopicThingHandler;
    }

    class MockTransformationService implements TransformationService {
        private final String value;

        public MockTransformationService(String value) {
            this.value = value;
        }

        @Override
        public @Nullable String transform(String jsonPathExpression, String source) throws TransformationException {
            return this.value;
        }
    }

}
