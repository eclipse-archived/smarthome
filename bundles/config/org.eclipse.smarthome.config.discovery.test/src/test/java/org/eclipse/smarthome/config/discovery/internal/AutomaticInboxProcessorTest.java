/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import static org.eclipse.smarthome.config.discovery.inbox.InboxPredicates.withFlag;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.events.ThingStatusInfoChangedEvent;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableMap;

/**
 * @author Andre Fuechsel - Initial contribution
 */
public class AutomaticInboxProcessorTest {

    private static final String DEVICE_ID = "deviceId";
    private static final String DEVICE_ID_KEY = "deviceIdKey";

    private static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("test", "test");
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_UID, "test");
    private static final ThingUID THING_UID2 = new ThingUID(THING_TYPE_UID, "test2");
    private static final ThingType THING_TYPE = new ThingType(THING_TYPE_UID, null, "label", null, true, DEVICE_ID_KEY,
            null, null, null, null);
    private final static Map<String, String> THING_PROPERTIES = new ImmutableMap.Builder<String, String>()
            .put(DEVICE_ID_KEY, DEVICE_ID).build();

    private AutomaticInboxProcessor inboxAutoIgnore;
    private PersistentInbox inbox;

    @Mock
    private ThingRegistry thingRegistry;

    @Mock
    private ThingTypeRegistry thingTypeRegistry;

    @Mock
    private Thing thing;

    @Mock
    private ThingStatusInfoChangedEvent thingStatusInfoChangedEvent;

    @Mock
    private ConfigDescriptionRegistry configDescriptionRegistry;

    @Mock
    private ThingHandlerFactory thingHandlerFactory;

    @Mock
    private ManagedThingProvider thingProvider;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(thing.getConfiguration()).thenReturn(new Configuration());
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_UID);
        when(thing.getProperties()).thenReturn(THING_PROPERTIES);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing.getUID()).thenReturn(THING_UID);
        when(thingRegistry.stream()).thenReturn(Stream.empty());
        when(thingTypeRegistry.getThingType(THING_TYPE_UID)).thenReturn(THING_TYPE);
        when(thingHandlerFactory.supportsThingType(eq(THING_TYPE_UID))).thenReturn(true);
        when(thingHandlerFactory.createThing(eq(THING_TYPE_UID), any(Configuration.class), eq(THING_UID),
                any(ThingUID.class)))
                        .then(invocation -> ThingBuilder.create(THING_TYPE_UID, "test")
                                .withConfiguration((Configuration) invocation.getArguments()[1]).build());

        inbox = new PersistentInbox();
        inbox.setThingRegistry(thingRegistry);
        inbox.setStorageService(new VolatileStorageService());
        inbox.setManagedThingProvider(thingProvider);
        inbox.setConfigDescriptionRegistry(configDescriptionRegistry);
        inbox.setThingTypeRegistry(thingTypeRegistry);
        inbox.addThingHandlerFactory(thingHandlerFactory);

        inbox.add(DiscoveryResultBuilder.create(THING_UID).withProperty(DEVICE_ID_KEY, DEVICE_ID)
                .withRepresentationProperty(DEVICE_ID_KEY).build());

        inboxAutoIgnore = new AutomaticInboxProcessor();
        inboxAutoIgnore.setThingRegistry(thingRegistry);
        inboxAutoIgnore.setThingTypeRegistry(thingTypeRegistry);
        inboxAutoIgnore.setInbox(inbox);
    }

    @Test
    public void testThingWentOnline() {
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW))
                .collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));

        when(thingRegistry.get(THING_UID)).thenReturn(thing);
        when(thingStatusInfoChangedEvent.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(thingStatusInfoChangedEvent.getThingUID()).thenReturn(THING_UID);
        inboxAutoIgnore.receive(thingStatusInfoChangedEvent);

        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW)).collect(Collectors.toList());
        assertThat(results.size(), is(0));
        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED)).collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));
    }

    @Test
    public void testInboxHasBeenChanged() {
        inbox.stream().map(DiscoveryResult::getThingUID).forEach(t -> inbox.remove(t));
        assertThat(inbox.getAll().size(), is(0));

        when(thingRegistry.get(THING_UID)).thenReturn(thing);
        when(thingRegistry.stream()).thenReturn(Stream.of(thing));

        inbox.add(DiscoveryResultBuilder.create(THING_UID2).withProperty(DEVICE_ID_KEY, DEVICE_ID)
                .withRepresentationProperty(DEVICE_ID_KEY).build());

        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW))
                .collect(Collectors.toList());
        assertThat(results.size(), is(0));
        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED)).collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID2)));
    }

    @Test
    public void testThingIsBeingRemoved() {
        inbox.setFlag(THING_UID, DiscoveryResultFlag.IGNORED);
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED))
                .collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));

        when(thingRegistry.get(THING_UID)).thenReturn(thing);
        when(thingStatusInfoChangedEvent.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.REMOVING, ThingStatusDetail.NONE, null));
        when(thingStatusInfoChangedEvent.getThingUID()).thenReturn(THING_UID);
        inboxAutoIgnore.receive(thingStatusInfoChangedEvent);

        results = inbox.getAll();
        assertThat(results.size(), is(0));
    }

}
