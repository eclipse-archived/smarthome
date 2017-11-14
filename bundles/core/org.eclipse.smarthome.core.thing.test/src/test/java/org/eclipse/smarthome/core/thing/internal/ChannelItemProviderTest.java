/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class ChannelItemProviderTest {

    private static final String ITEM_NAME = "test";
    private static final ChannelUID CHANNEL_UID = new ChannelUID("test:test:test:test");
    private static final NumberItem ITEM = new NumberItem(ITEM_NAME);

    @Mock
    private ItemRegistry itemRegistry;
    @Mock
    private ThingRegistry thingRegistry;
    @Mock
    private ItemFactory itemFactory;
    @Mock
    private ProviderChangeListener<Item> listener;
    @Mock
    private LocaleProvider localeProvider;
    @Mock
    private ItemChannelLinkRegistry linkRegistry;

    private ChannelItemProvider provider;

    @Before
    public void setup() throws Exception {
        initMocks(this);

        provider = new ChannelItemProvider();
        provider.setItemRegistry(itemRegistry);
        provider.setThingRegistry(thingRegistry);
        provider.setItemChannelLinkRegistry(linkRegistry);
        provider.addItemFactory(itemFactory);
        provider.setLocaleProvider(localeProvider);
        provider.addProviderChangeListener(listener);

        Map<String, Object> props = new HashMap<>();
        props.put("enable", "true");
        props.put("initialDelay", "false");
        provider.activate(props);

        when(thingRegistry.getChannel(same(CHANNEL_UID))).thenReturn(new Channel(CHANNEL_UID, "Number"));
        when(itemFactory.createItem("Number", ITEM_NAME)).thenReturn(ITEM);
        when(localeProvider.getLocale()).thenReturn(Locale.ENGLISH);
    }

    @Test
    public void testItemCreation_notThere() throws Exception {
        provider.linkRegistryListener.added(new ItemChannelLink(ITEM_NAME, CHANNEL_UID));
        verify(listener, only()).added(same(provider), same(ITEM));
    }

    @Test
    public void testItemCreation_alreadyExists() throws Exception {
        when(itemRegistry.get(eq(ITEM_NAME))).thenReturn(ITEM);

        provider.linkRegistryListener.added(new ItemChannelLink(ITEM_NAME, CHANNEL_UID));
        verify(listener, never()).added(same(provider), same(ITEM));
    }

    @Test
    public void testItemRemoval_linkRemoved() throws Exception {
        provider.linkRegistryListener.added(new ItemChannelLink(ITEM_NAME, CHANNEL_UID));

        resetAndPrepareListener();

        provider.linkRegistryListener.removed(new ItemChannelLink(ITEM_NAME, CHANNEL_UID));
        verify(listener, never()).added(same(provider), same(ITEM));
        verify(listener, only()).removed(same(provider), same(ITEM));
    }

    @Test
    public void testItemRemoval_itemFromOtherProvider() throws Exception {
        provider.linkRegistryListener.added(new ItemChannelLink(ITEM_NAME, CHANNEL_UID));

        resetAndPrepareListener();

        provider.itemRegistryListener.added(new NumberItem(ITEM_NAME));
        verify(listener, only()).removed(same(provider), same(ITEM));
        verify(listener, never()).added(same(provider), same(ITEM));
    }

    @SuppressWarnings("unchecked")
    private void resetAndPrepareListener() {
        reset(listener);
        doAnswer(invocation -> {
            // this is crucial as it mimicks the real ItemRegistry's behavior
            provider.itemRegistryListener.removed((Item) invocation.getArguments()[1]);
            return null;
        }).when(listener).removed(same(provider), any(Item.class));
        doAnswer(invocation -> {
            // this is crucial as it mimicks the real ItemRegistry's behavior
            provider.itemRegistryListener.added((Item) invocation.getArguments()[1]);
            return null;
        }).when(listener).added(same(provider), any(Item.class));
        when(linkRegistry.getBoundChannels(eq(ITEM_NAME))).thenReturn(Collections.singleton(CHANNEL_UID));
        when(linkRegistry.getLinks(eq(CHANNEL_UID)))
                .thenReturn(Collections.singleton(new ItemChannelLink(ITEM_NAME, CHANNEL_UID)));
    }

}
