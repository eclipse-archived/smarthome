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
package org.eclipse.smarthome.config.discovery.internal;

import static org.eclipse.smarthome.config.discovery.inbox.InboxPredicates.withFlag;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
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
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
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
    private static final String OTHER_KEY = "otherKey";
    private static final String OTHER_VALUE = "deviceId";
    private static final String CONFIG_KEY = "configKey";
    private static final String CONFIG_VALUE = "configValue";

    private static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("test", "test");
    private static final ThingTypeUID THING_TYPE_UID2 = new ThingTypeUID("test2", "test2");
    private static final ThingTypeUID THING_TYPE_UID3 = new ThingTypeUID("test3", "test3");

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_UID, "test");
    private static final ThingUID THING_UID2 = new ThingUID(THING_TYPE_UID, "test2");
    private static final ThingUID THING_UID3 = new ThingUID(THING_TYPE_UID3, "test3");

    private static final ThingType THING_TYPE = ThingTypeBuilder.instance(THING_TYPE_UID, "label").isListed(true)
            .withRepresentationProperty(DEVICE_ID_KEY).build();
    private static final ThingType THING_TYPE2 = ThingTypeBuilder.instance(THING_TYPE_UID2, "label").isListed(true)
            .withRepresentationProperty(CONFIG_KEY).build();
    private static final ThingType THING_TYPE3 = ThingTypeBuilder.instance(THING_TYPE_UID3, "label").isListed(true)
            .withRepresentationProperty(OTHER_KEY).build();

    private final static Map<String, String> THING_PROPERTIES = new ImmutableMap.Builder<String, String>()
            .put(DEVICE_ID_KEY, DEVICE_ID).build();
    private final static Map<String, String> OTHER_THING_PROPERTIES = new ImmutableMap.Builder<String, String>()
            .put(OTHER_KEY, OTHER_VALUE).build();

    private final static Configuration CONFIG = new Configuration(
            new ImmutableMap.Builder<String, Object>().put(CONFIG_KEY, CONFIG_VALUE).build());

    private AutomaticInboxProcessor inboxAutoIgnore;
    private PersistentInbox inbox;

    @Mock
    private ThingRegistry thingRegistry;

    @Mock
    private ThingTypeRegistry thingTypeRegistry;

    @Mock
    private Thing thing;

    @Mock
    private Thing thing2;

    @Mock
    private Thing thing3;

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

        when(thing.getConfiguration()).thenReturn(CONFIG);
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_UID);
        when(thing.getProperties()).thenReturn(THING_PROPERTIES);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing.getUID()).thenReturn(THING_UID);

        when(thing2.getConfiguration()).thenReturn(CONFIG);
        when(thing2.getThingTypeUID()).thenReturn(THING_TYPE_UID2);
        when(thing2.getProperties()).thenReturn(THING_PROPERTIES);
        when(thing2.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing2.getUID()).thenReturn(THING_UID2);

        when(thing3.getConfiguration()).thenReturn(CONFIG);
        when(thing3.getThingTypeUID()).thenReturn(THING_TYPE_UID3);
        when(thing3.getProperties()).thenReturn(OTHER_THING_PROPERTIES);
        when(thing3.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing3.getUID()).thenReturn(THING_UID3);

        when(thingRegistry.stream()).thenReturn(Stream.empty());

        when(thingTypeRegistry.getThingType(THING_TYPE_UID)).thenReturn(THING_TYPE);
        when(thingTypeRegistry.getThingType(THING_TYPE_UID2)).thenReturn(THING_TYPE2);
        when(thingTypeRegistry.getThingType(THING_TYPE_UID3)).thenReturn(THING_TYPE3);

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

        inboxAutoIgnore = new AutomaticInboxProcessor();
        inboxAutoIgnore.setThingRegistry(thingRegistry);
        inboxAutoIgnore.setThingTypeRegistry(thingTypeRegistry);
        inboxAutoIgnore.setInbox(inbox);
    }

    /**
     * This test is just like the test testThingWentOnline in the AutomaticInboxProcessorTest, but in contrast to the
     * above test (where a thing with the same binding ID and the same representation property value went online) here a
     * thing with another binding ID and the same representation property value goes online.
     * <p/>
     * In this case, the discovery result should not be ignored, since it has a different thing type.
     */
    @Test
    public void testThingWithOtherBindingIDButSameRepresentationPropertyWentOnline() {
        // Add discovery result with thing type THING_TYPE_UID and representation property value DEVICE_ID
        inbox.add(DiscoveryResultBuilder.create(THING_UID).withProperty(DEVICE_ID_KEY, DEVICE_ID)
                .withRepresentationProperty(DEVICE_ID_KEY).build());

        // Then there is a discovery result which is NEW
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW))
                .collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));

        // Now a thing with thing type THING_TYPE_UID3 goes online, with representation property value being also the
        // device id
        when(thingRegistry.get(THING_UID3)).thenReturn(thing3);
        when(thingStatusInfoChangedEvent.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(thingStatusInfoChangedEvent.getThingUID()).thenReturn(THING_UID3);
        inboxAutoIgnore.receive(thingStatusInfoChangedEvent);

        // Then there should still be the NEW discovery result, but no IGNORED discovery result
        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW)).collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));
        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED)).collect(Collectors.toList());
        assertThat(results.size(), is(0));
    }

    @Test
    public void testThingWithOtherBindingIDButSameRepresentationPropertyIsDiscovered() {
        // insert thing with thing type THING_TYPE_UID3 and representation property value DEVICE_ID in registry
        when(thingRegistry.get(THING_UID)).thenReturn(thing);
        when(thingRegistry.stream()).thenReturn(Stream.of(thing));

        // Add discovery result with thing type THING_TYPE_UID3 and representation property value DEVICE_ID
        inbox.add(DiscoveryResultBuilder.create(THING_UID3).withProperty(DEVICE_ID_KEY, DEVICE_ID)
                .withRepresentationProperty(DEVICE_ID_KEY).build());

        // Do NOT ignore this discovery result because it has a different binding ID
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED))
                .collect(Collectors.toList());
        assertThat(results.size(), is(0));

        // Then there is a discovery result which is NEW
        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW)).collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID3)));
    }

    @Test
    public void testThingWentOnline() {
        inbox.add(DiscoveryResultBuilder.create(THING_UID).withProperty(DEVICE_ID_KEY, DEVICE_ID)
                .withRepresentationProperty(DEVICE_ID_KEY).build());

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
    public void testNoDiscoveryResultIfNoRepresentationPropertySet() {
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW))
                .collect(Collectors.toList());
        assertThat(results.size(), is(0));
    }

    @Test
    public void testThingWhenNoRepresentationPropertySet() {
        inbox.add(DiscoveryResultBuilder.create(THING_UID).withProperty(DEVICE_ID_KEY, DEVICE_ID).build());
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW))
                .collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));

        when(thing.getProperties()).thenReturn(Collections.emptyMap());
        when(thingStatusInfoChangedEvent.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(thingStatusInfoChangedEvent.getThingUID()).thenReturn(THING_UID);
        inboxAutoIgnore.receive(thingStatusInfoChangedEvent);

        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED)).collect(Collectors.toList());
        assertThat(results.size(), is(0));
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
        inbox.add(DiscoveryResultBuilder.create(THING_UID).withProperty(DEVICE_ID_KEY, DEVICE_ID)
                .withRepresentationProperty(DEVICE_ID_KEY).build());

        inbox.setFlag(THING_UID, DiscoveryResultFlag.IGNORED);
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED))
                .collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));

        inboxAutoIgnore.removed(thing);

        results = inbox.getAll();
        assertThat(results.size(), is(0));
    }

    @Test
    public void testOneThingOutOfTwoWithSameRepresentationPropertyButDifferentBindingIdIsBeingRemoved() {
        inbox.add(DiscoveryResultBuilder.create(THING_UID).withProperty(DEVICE_ID_KEY, DEVICE_ID)
                .withRepresentationProperty(DEVICE_ID_KEY).build());
        inbox.setFlag(THING_UID, DiscoveryResultFlag.IGNORED);

        inbox.add(DiscoveryResultBuilder.create(THING_UID3).withProperty(DEVICE_ID_KEY, DEVICE_ID)
                .withRepresentationProperty(DEVICE_ID_KEY).build());
        inbox.setFlag(THING_UID3, DiscoveryResultFlag.IGNORED);

        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED))
                .collect(Collectors.toList());
        assertThat(results.size(), is(2));

        inboxAutoIgnore.removed(thing);

        results = inbox.getAll();
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID3)));
    }

    @Test
    public void testThingWithConfigWentOnline() {
        inbox.add(DiscoveryResultBuilder.create(THING_UID2).withProperty(CONFIG_KEY, CONFIG_VALUE)
                .withRepresentationProperty(CONFIG_KEY).build());

        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW))
                .collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID2)));

        when(thingRegistry.get(THING_UID2)).thenReturn(thing2);
        when(thingStatusInfoChangedEvent.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(thingStatusInfoChangedEvent.getThingUID()).thenReturn(THING_UID2);
        inboxAutoIgnore.receive(thingStatusInfoChangedEvent);

        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW)).collect(Collectors.toList());
        assertThat(results.size(), is(0));
        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED)).collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID2)));
    }

    @Test
    public void testInboxWithConfigHasBeenChanged() {
        inbox.stream().map(DiscoveryResult::getThingUID).forEach(t -> inbox.remove(t));
        assertThat(inbox.getAll().size(), is(0));

        when(thingRegistry.get(THING_UID2)).thenReturn(thing2);
        when(thingRegistry.stream()).thenReturn(Stream.of(thing2));

        inbox.add(DiscoveryResultBuilder.create(THING_UID).withProperty(CONFIG_KEY, CONFIG_VALUE)
                .withRepresentationProperty(CONFIG_KEY).build());

        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.NEW))
                .collect(Collectors.toList());
        assertThat(results.size(), is(0));
        results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED)).collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID)));
    }

    @Test
    public void testThingWithConfigIsBeingRemoved() {
        inbox.add(DiscoveryResultBuilder.create(THING_UID2).withProperty(CONFIG_KEY, CONFIG_VALUE)
                .withRepresentationProperty(CONFIG_KEY).build());

        inbox.setFlag(THING_UID2, DiscoveryResultFlag.IGNORED);
        List<DiscoveryResult> results = inbox.stream().filter(withFlag(DiscoveryResultFlag.IGNORED))
                .collect(Collectors.toList());
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getThingUID(), is(equalTo(THING_UID2)));

        inboxAutoIgnore.removed(thing2);

        results = inbox.getAll();
        assertThat(results.size(), is(0));
    }

}
