/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import static org.eclipse.smarthome.config.discovery.inbox.InboxPredicates.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

import java.util.List;
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
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.events.ThingStatusInfoChangedEvent;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Andre Fuechsel - Initial contribution
 */
public class AutomaticInboxProcessorTest {

    private static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("test", "test");
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_UID, "test");
    private static final ThingType THING_TYPE = new ThingType(THING_TYPE_UID, null, "label", null, null, null, null,
            null);
    private static final String DEVICE_ID = "deviceId";

    private AutomaticInboxProcessor inboxAutoIgnore;
    private PersistentInbox inbox;

    @Mock
    private ThingRegistry thingRegistry;

    @Mock
    private ThingTypeRegistry thingTypeRegistry;

    @Mock
    private Thing thing;

    @Mock
    private ThingHandler thingHandler;

    @Mock
    private ThingStatusInfoChangedEvent event;

    @Mock
    private ConfigDescriptionRegistry configDescriptionRegistry;

    @Mock
    private ThingHandlerFactory thingHandlerFactory;

    @Mock
    private ManagedThingProvider thingProvider;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(thingHandler.getUniqueIdentifier()).thenReturn(DEVICE_ID);
        when(thing.getHandler()).thenReturn(thingHandler);
        when(thing.getConfiguration()).thenReturn(new Configuration());
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

        inbox.add(DiscoveryResultBuilder.create(THING_UID).withProperty("deviceId", DEVICE_ID)
                .withRepresentationProperty("deviceId").build());

        inboxAutoIgnore = new AutomaticInboxProcessor();
        inboxAutoIgnore.setThingRegistry(thingRegistry);
        inboxAutoIgnore.setInbox(inbox);
    }

    @Test
    public void testThingWentOnline() {
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW))
                .collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));

        when(thingRegistry.get(THING_UID)).thenReturn(thing);
        when(event.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(event.getThingUID()).thenReturn(THING_UID);
        inboxAutoIgnore.receiveTypedEvent(event);

        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW)).collect(Collectors.toList());
        assertThat(results.size(), is(0));
        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED)).collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));
    }

    @Test
    public void testThingIsBeingRemoved() {
        inbox.setFlag(THING_UID, DiscoveryResultFlag.IGNORED);
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED))
                .collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));

        when(thingRegistry.get(THING_UID)).thenReturn(thing);
        when(event.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.REMOVING, ThingStatusDetail.NONE, null));
        when(event.getThingUID()).thenReturn(THING_UID);
        inboxAutoIgnore.receiveTypedEvent(event);

        results = inbox.getAll();
        assertThat(results.size(), is(0));
    }

}
