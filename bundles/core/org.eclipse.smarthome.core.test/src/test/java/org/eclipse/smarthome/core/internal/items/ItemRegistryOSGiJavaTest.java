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
package org.eclipse.smarthome.core.internal.items;

import static org.eclipse.smarthome.core.internal.items.ItemRegistryImpl.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class ItemRegistryOSGiJavaTest extends JavaOSGiTest {

    private static final String ITEM_NAME = "test";

    private ItemRegistry itemRegistry;
    private MetadataRegistry metadataRegistry;
    private ManagedItemProvider managedItemProvider;
    private @Mock RegistryChangeListener<Item> mockListener;

    @Before
    public void setup() {
        initMocks(this);
        registerVolatileStorageService();

        managedItemProvider = getService(ManagedItemProvider.class);
        assertNotNull(managedItemProvider);

        metadataRegistry = getService(MetadataRegistry.class);
        assertNotNull(metadataRegistry);

        itemRegistry = getService(ItemRegistry.class);
        assertNotNull(itemRegistry);

        itemRegistry.removeRegistryChangeListener(mockListener);
    }

    @Test
    public void testGetTagsFromLegacy() throws Exception {
        prepareItem(ITEM_NAME, "hello");

        assertTagsInItem(ITEM_NAME, "hello");
    }

    @Test
    public void testGetTagsFromExistingMetadata() throws Exception {
        prepareMetadata(ITEM_NAME, "hello");
        prepareItem(ITEM_NAME);

        assertTagsInItem(ITEM_NAME, "hello");
    }

    @Test
    public void testGetTagsFromNewMetadata() throws Exception {
        prepareItem(ITEM_NAME);
        prepareMetadata(ITEM_NAME, "hello");

        assertTagsInItem(ITEM_NAME, "hello");
    }

    @Test
    public void testGetTagsFromBoth() throws Exception {
        prepareMetadata(ITEM_NAME, "foo");
        prepareItem(ITEM_NAME, "bar");

        assertTagsInItem(ITEM_NAME, "foo", "bar");
    }

    @Test
    public void testWriteTagsToMetadataViaAdd() throws Exception {
        StringItem item = new StringItem(ITEM_NAME);
        item.addTag("hello");
        itemRegistry.add(item);

        assertTagsInMetadata(ITEM_NAME, "hello");
        assertNoTagsPersisted(ITEM_NAME);
    }

    @Test
    public void testWriteTagsToMetadataViaUpdate() throws Exception {
        prepareItem(ITEM_NAME);

        StringItem item = new StringItem(ITEM_NAME);
        item.addTag("hello");
        itemRegistry.update(item);

        assertTagsInMetadata(ITEM_NAME, "hello");
        assertNoTagsPersisted(ITEM_NAME);
    }

    @Test
    public void testUpdateTagsInMetadataViaAdd() throws Exception {
        prepareMetadata(ITEM_NAME, "foo");

        StringItem item = new StringItem(ITEM_NAME);
        item.addTag("bar");
        itemRegistry.add(item);

        assertTagsInMetadata(ITEM_NAME, "bar");
        assertNoTagsPersisted(ITEM_NAME);
    }

    @Test
    public void testUpdateTagsInMetadataViaUpdate() throws Exception {
        prepareItem(ITEM_NAME);

        prepareMetadata(ITEM_NAME, "foo");

        StringItem item = new StringItem(ITEM_NAME);
        item.addTag("bar");
        itemRegistry.update(item);

        assertTagsInMetadata(ITEM_NAME, "bar");
        assertNoTagsPersisted(ITEM_NAME);
    }

    @Test
    public void testRemoveTagsInMetadataOnItemDeletion() throws Exception {
        prepareItem(ITEM_NAME);
        prepareMetadata(ITEM_NAME, "foo");

        itemRegistry.remove(ITEM_NAME);

        assertTagsInMetadata(ITEM_NAME);
    }

    @Test
    public void testAddTagWithPreviousLegacy() throws Exception {
        prepareItem(ITEM_NAME, "foo");

        itemRegistry.addTag(ITEM_NAME, "bar");

        assertTagsInMetadata(ITEM_NAME, "foo", "bar");
        assertTagsInItem(ITEM_NAME, "foo", "bar");
    }

    @Test
    public void testAddTagWithPreviousMetadata() throws Exception {
        prepareMetadata(ITEM_NAME, "foo");
        prepareItem(ITEM_NAME);

        itemRegistry.addTag(ITEM_NAME, "bar");

        assertTagsInMetadata(ITEM_NAME, "foo", "bar");
        assertTagsInItem(ITEM_NAME, "foo", "bar");
    }

    @Test
    public void testAddTagToNonExistingItem() throws Exception {
        itemRegistry.addTag(ITEM_NAME, "hello");
        assertTagsInMetadata(ITEM_NAME, "hello");
    }

    @Test
    public void testRemoveTag() throws Exception {
        prepareItem(ITEM_NAME, "foo", "bar");

        itemRegistry.removeTag(ITEM_NAME, "bar");

        assertTagsInMetadata(ITEM_NAME, "foo");
        assertTagsInItem(ITEM_NAME, "foo");
    }

    @Test
    public void testRemoveNonExistingTag() throws Exception {
        prepareItem(ITEM_NAME, "foo");

        itemRegistry.removeTag(ITEM_NAME, "bar");

        assertTagsInMetadata(ITEM_NAME, "foo");
        assertTagsInItem(ITEM_NAME, "foo");
    }

    @Test
    public void testRemoveTagFromNonExistingItem() throws Exception {
        prepareMetadata(ITEM_NAME, "hello");

        itemRegistry.removeTag(ITEM_NAME, "hello");

        assertTagsInMetadata(ITEM_NAME);
    }

    @Test
    public void testAddedNotificationsContainTagsFromMetadata() {
        itemRegistry.addRegistryChangeListener(mockListener);

        prepareMetadata(ITEM_NAME, "hello");
        prepareItem(ITEM_NAME);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(mockListener).added(captor.capture());
        Item res = captor.getValue();
        assertEquals(1, res.getTags().size());
        assertEquals("hello", res.getTags().iterator().next());

        StringItem item = new StringItem(ITEM_NAME);
        managedItemProvider.update(item);
    }

    @Test
    public void testUpdatedNotificationsContainTagsFromMetadata() {
        prepareMetadata(ITEM_NAME, "hello");
        prepareItem(ITEM_NAME);

        itemRegistry.addRegistryChangeListener(mockListener);

        StringItem item = new StringItem(ITEM_NAME);
        managedItemProvider.update(item);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(mockListener).updated(any(Item.class), captor.capture());
        Item res = captor.getValue();
        assertEquals(1, res.getTags().size());
        assertEquals("hello", res.getTags().iterator().next());
    }

    @Test
    public void testRemovedNotificationsContainTagsFromMetadata() {
        prepareMetadata(ITEM_NAME, "hello");
        prepareItem(ITEM_NAME);

        itemRegistry.addRegistryChangeListener(mockListener);
        managedItemProvider.remove(ITEM_NAME);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(mockListener).removed(captor.capture());
        Item res = captor.getValue();
        assertEquals(1, res.getTags().size());
        assertEquals("hello", res.getTags().iterator().next());
    }

    private void prepareItem(String itemName, String... tags) {
        StringItem item = new StringItem(itemName);
        item.addTags(tags);
        managedItemProvider.add(item);
    }

    private void prepareMetadata(String itemName, String... tags) {
        MetadataKey key = new MetadataKey(TAG_NAMESPACE, itemName);
        Metadata metadata = new Metadata(key, String.join(TAG_SEPARATOR, tags), null);
        metadataRegistry.add(metadata);
    }

    private void assertTagsInMetadata(String itemName, String... tags) {
        MetadataKey key = new MetadataKey(TAG_NAMESPACE, itemName);
        Metadata metadata = metadataRegistry.get(key);
        if (tags == null || tags.length == 0) {
            assertNull(metadata);
        } else {
            assertNotNull(metadata);
            List<String> res = Arrays.asList(metadata.getValue().split(TAG_SPLIT_REGEX));
            assertEquals(tags.length, res.size());
            for (String tag : tags) {
                assertTrue(tag, res.contains(tag));
            }
        }
    }

    private void assertTagsInItem(String itemName, String... tags) throws ItemNotFoundException {
        Item item = itemRegistry.getItem(itemName);
        assertEquals(tags.length, item.getTags().size());
        Set<@NonNull String> res = item.getTags();
        for (String tag : tags) {
            assertTrue(tag, res.contains(tag));
        }
    }

    private void assertNoTagsPersisted(String itemName) throws ItemNotFoundException {
        Item item = managedItemProvider.get(itemName);
        assertEquals(0, item.getTags().size());
    }
}
