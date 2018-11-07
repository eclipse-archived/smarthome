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
package org.eclipse.smarthome.binding.mqtt.generic.internal.generic;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.DiscoverComponents.ComponentDiscovered;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.TextValue;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * Tests the {@link ChannelState} class.
 *
 * @author David Graeff - Initial contribution
 */
public class ChannelStateTests extends JavaOSGiTest {
    @Mock
    MqttBrokerConnection connection;

    @Mock
    ComponentDiscovered discovered;

    @Mock
    ChannelStateUpdateListener channelStateUpdateListener;

    @Mock
    ChannelUID channelUID;

    @Spy
    TextValue textValue;

    ScheduledExecutorService scheduler;

    @Before
    public void setUp() {
        initMocks(this);
        CompletableFuture<Void> voidFutureComplete = new CompletableFuture<Void>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any(), anyInt(),
                anyBoolean());

        scheduler = new ScheduledThreadPoolExecutor(1);
    }

    @After
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    public void noInteractionTimeoutTest() throws InterruptedException, ExecutionException, TimeoutException {

        ChannelState c = spy(new ChannelState(ChannelConfigBuilder.create("state", "command").build(), channelUID,
                textValue, channelStateUpdateListener));
        c.start(connection, scheduler, 50).get(100, TimeUnit.MILLISECONDS);
        verify(connection).subscribe(eq("state"), eq(c));
        c.stop().get();
        verify(connection).unsubscribe(eq("state"), eq(c));
    }

    @Test
    public void publishTest() throws InterruptedException, ExecutionException, TimeoutException {
        ChannelState c = spy(new ChannelState(ChannelConfigBuilder.create("state", "command").build(), channelUID,
                textValue, channelStateUpdateListener));

        c.start(connection, scheduler, 0).get(50, TimeUnit.MILLISECONDS);
        verify(connection).subscribe(eq("state"), eq(c));

        c.setValue(new StringType("UPDATE")).get();
        verify(connection).publish(eq("command"), argThat(p -> Arrays.equals(p, "UPDATE".getBytes())), anyInt(),
                eq(false));

        c.config.formatBeforePublish = "prefix%s";
        c.setValue(new StringType("UPDATE")).get();
        verify(connection).publish(eq("command"), argThat(p -> Arrays.equals(p, "prefixUPDATE".getBytes())), anyInt(),
                eq(false));

        c.config.formatBeforePublish = "%1$s-%1$s";
        c.setValue(new StringType("UPDATE")).get();
        verify(connection).publish(eq("command"), argThat(p -> Arrays.equals(p, "UPDATE-UPDATE".getBytes())), anyInt(),
                eq(false));

        c.config.formatBeforePublish = "%s";
        c.config.retained = true;
        c.setValue(new StringType("UPDATE")).get();
        verify(connection).publish(eq("command"), any(), anyInt(), eq(true));

        c.stop().get();
        verify(connection).unsubscribe(eq("state"), eq(c));
    }

    @Test
    public void receiveTest() throws InterruptedException, ExecutionException, TimeoutException {
        ChannelState c = spy(new ChannelState(ChannelConfigBuilder.create("state", "command").build(), channelUID,
                textValue, channelStateUpdateListener));

        CompletableFuture<@Nullable Void> future = c.start(connection, scheduler, 100);
        c.processMessage("state", "A TEST".getBytes());
        future.get(300, TimeUnit.MILLISECONDS);

        assertThat(textValue.getValue().toString(), is("A TEST"));
        verify(channelStateUpdateListener).updateChannelState(eq(channelUID), any());
    }

    @Test
    public void receiveWildcardTest() throws InterruptedException, ExecutionException, TimeoutException {
        ChannelState c = spy(new ChannelState(ChannelConfigBuilder.create("state/+/topic", "command").build(),
                channelUID, textValue, channelStateUpdateListener));

        CompletableFuture<@Nullable Void> future = c.start(connection, scheduler, 100);
        c.processMessage("state/bla/topic", "A TEST".getBytes());
        future.get(300, TimeUnit.MILLISECONDS);

        assertThat(textValue.getValue().toString(), is("A TEST"));
        verify(channelStateUpdateListener).updateChannelState(eq(channelUID), any());
    }
}
