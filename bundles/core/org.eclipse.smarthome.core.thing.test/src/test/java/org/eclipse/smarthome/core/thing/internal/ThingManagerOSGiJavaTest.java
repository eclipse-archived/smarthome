/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.thing.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.SafeCaller;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.service.ReadyMarker;
import org.eclipse.smarthome.core.service.ReadyService;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class ThingManagerOSGiJavaTest extends JavaOSGiTest {

    private ManagedThingProvider managedThingProvider;
    private ItemRegistry itemRegistry;
    private ReadyService readyService;
    private ItemChannelLinkRegistry itemChannelLinkRegistry;

    private final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("binding:type");
    private final ThingUID THING_UID = new ThingUID(THING_TYPE_UID, "id");
    private final ChannelUID CHANNEL_UID = new ChannelUID(THING_UID, "channel");
    private Thing THING;

    @Before
    public void setUp() throws Exception {
        THING = ThingBuilder.create(THING_TYPE_UID, THING_UID)
                .withChannels(Collections.singletonList(new Channel(CHANNEL_UID, "Switch"))).build();
        registerVolatileStorageService();

        configureAutoLinking(false);

        managedThingProvider = getService(ManagedThingProvider.class);

        itemRegistry = getService(ItemRegistry.class);
        assertNotNull(itemRegistry);

        itemChannelLinkRegistry = getService(ItemChannelLinkRegistry.class);
        assertNotNull(itemChannelLinkRegistry);

        readyService = getService(ReadyService.class);
        assertNotNull(readyService);

        waitForAssert(() -> {
            try {
                assertThat(
                        bundleContext.getServiceReferences(ReadyMarker.class,
                                "(esh.xmlThingTypes=" + bundleContext.getBundle().getSymbolicName() + ")"),
                        is(notNullValue()));
            } catch (InvalidSyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        waitForAssert(() -> {
            try {
                assertThat(bundleContext.getServiceReferences(ChannelItemProvider.class, null), is(notNullValue()));
            } catch (InvalidSyntaxException e) {
                throw new RuntimeException(e);
            }
        });

    }

    @After
    public void teardown() throws Exception {
        managedThingProvider.getAll().forEach(it -> {
            managedThingProvider.remove(it.getUID());
        });
        configureAutoLinking(true);
    }

    @Test
    public void testInitializeCallsThingUpdated() throws Exception {
        registerThingTypeProvider();
        AtomicReference<ThingHandlerCallback> thc = new AtomicReference<>();
        AtomicReference<Boolean> initializeRunning = new AtomicReference<>(false);
        ThingHandlerFactory thingHandlerFactory = new BaseThingHandlerFactory() {
            @Override
            public boolean supportsThingType(@NonNull ThingTypeUID thingTypeUID) {
                return true;
            }

            @Override
            protected @Nullable ThingHandler createHandler(@NonNull Thing thing) {
                ThingHandler mockHandler = mock(ThingHandler.class);
                doAnswer(a -> {
                    thc.set((ThingHandlerCallback) a.getArguments()[0]);
                    return null;
                }).when(mockHandler).setCallback(Matchers.isA(ThingHandlerCallback.class));
                doAnswer(a -> {
                    initializeRunning.set(true);

                    // call thingUpdated() from within initialize()
                    thc.get().thingUpdated(THING);

                    // hang on a little to provoke a potential dead-lock
                    Thread.sleep(1000);

                    initializeRunning.set(false);
                    return null;
                }).when(mockHandler).initialize();
                when(mockHandler.getThing()).thenReturn(THING);
                return mockHandler;
            }
        };
        registerService(thingHandlerFactory, ThingHandlerFactory.class.getName());
        new Thread((Runnable) () -> managedThingProvider.add(THING)).start();

        waitForAssert(() -> {
            assertThat(THING.getStatus(), is(ThingStatus.INITIALIZING));
        });

        // ensure it didn't run into a dead-lock which gets resolved by the SafeCaller.
        waitForAssert(() -> {
            assertThat(initializeRunning.get(), is(false));
        }, SafeCaller.DEFAULT_TIMEOUT - 100, 50);
    }

    private void registerThingTypeProvider() throws Exception {
        URI configDescriptionUri = new URI("test:test");
        ThingType thingType = ThingTypeBuilder.instance(new ThingTypeUID("binding", "type"), "label")
                .withConfigDescriptionURI(configDescriptionUri).build();

        ThingTypeProvider mockThingTypeProvider = mock(ThingTypeProvider.class);
        when(mockThingTypeProvider.getThingType(Matchers.isA(ThingTypeUID.class), Matchers.isA(Locale.class)))
                .thenReturn(thingType);
        registerService(mockThingTypeProvider);

        ThingTypeRegistry mockThingTypeRegistry = mock(ThingTypeRegistry.class);
        when(mockThingTypeRegistry.getThingType(Matchers.isA(ThingTypeUID.class))).thenReturn(thingType);
        registerService(mockThingTypeRegistry);
    }

    private void configureAutoLinking(Boolean on) throws IOException {
        ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);
        Configuration config = configAdmin.getConfiguration("org.eclipse.smarthome.links", null);
        Dictionary<String, Object> properties = config.getProperties();
        if (properties == null) {
            properties = new Hashtable<>();
        }
        properties.put("autoLinks", on.toString());
        config.update(properties);
    }

}
