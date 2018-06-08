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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.smarthome.core.items.ActiveItem;
import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class ItemBuilderTest {

    private ItemRegistryImpl itemRegistry;
    private @Mock ItemFactory mockFactory;
    private @Mock ActiveItem mockItem;
    private @Mock Item originalItem;

    @Before
    public void setup() {
        initMocks(this);
        itemRegistry = new ItemRegistryImpl();
        itemRegistry.addItemFactory(mockFactory);
    }

    @Test
    public void testMinimal() {
        when(mockFactory.createItem(anyString(), anyString())).thenReturn(mockItem);

        Item res = itemRegistry.newItemBuilder("String", "test").build();

        assertSame(mockItem, res);
        verify(mockFactory).createItem(eq("String"), eq("test"));
        verify(mockItem).setLabel(isNull());
        verify(mockItem).setCategory(isNull());
        verify(mockItem).addGroupNames(eq(Collections.emptyList()));
    }

    @Test
    public void testMinimalGroupItem() {
        Item resItem = itemRegistry.newItemBuilder("Group", "test").build();

        assertEquals(GroupItem.class, resItem.getClass());
        GroupItem res = (GroupItem) resItem;
        verifyNoMoreInteractions(mockFactory);
        assertNull(res.getCategory());
        assertEquals(Collections.emptyList(), res.getGroupNames());
        assertNull(res.getLabel());
        assertNull(res.getFunction());
        assertNull(res.getBaseItem());
    }

    @Test
    public void testFull() {
        when(mockFactory.createItem(anyString(), anyString())).thenReturn(mockItem);

        Item res = itemRegistry.newItemBuilder("String", "test") //
                .withCategory("category") //
                .withGroups(Arrays.asList("a", "b")) //
                .withLabel("label") //
                .build();

        assertSame(mockItem, res);
        verify(mockFactory).createItem(eq("String"), eq("test"));
        verify(mockItem).setCategory(eq("category"));
        verify(mockItem).addGroupNames(eq(Arrays.asList("a", "b")));
        verify(mockItem).setLabel(eq("label"));
    }

    @Test
    public void testFullGroupItem() {
        Item baseItem = mock(Item.class);
        GroupFunction mockFunction = mock(GroupFunction.class);

        Item resItem = itemRegistry.newItemBuilder("Group", "test") //
                .withCategory("category") //
                .withGroups(Arrays.asList("a", "b")) //
                .withLabel("label") //
                .withBaseItem(baseItem)//
                .withGroupFunction(mockFunction) //
                .build();

        assertEquals(GroupItem.class, resItem.getClass());
        GroupItem res = (GroupItem) resItem;
        verifyNoMoreInteractions(mockFactory);
        assertEquals("category", res.getCategory());
        assertEquals(Arrays.asList("a", "b"), res.getGroupNames());
        assertEquals("label", res.getLabel());
        assertSame(mockFunction, res.getFunction());
        assertSame(baseItem, res.getBaseItem());
    }

    @Test
    public void testClone() {
        when(originalItem.getType()).thenReturn("type");
        when(originalItem.getName()).thenReturn("name");
        when(originalItem.getLabel()).thenReturn("label");
        when(originalItem.getCategory()).thenReturn("category");
        when(originalItem.getGroupNames()).thenReturn(Arrays.asList("a", "b"));

        when(mockFactory.createItem(anyString(), anyString())).thenReturn(mockItem);

        Item res = itemRegistry.newItemBuilder(originalItem).build();

        assertSame(mockItem, res);
        verify(mockFactory).createItem(eq("type"), eq("name"));
        verify(mockItem).setCategory(eq("category"));
        verify(mockItem).addGroupNames(eq(Arrays.asList("a", "b")));
        verify(mockItem).setLabel(eq("label"));
    }

    @Test
    public void testCloneGroupItem() {
        Item baseItem = mock(Item.class);
        GroupFunction mockFunction = mock(GroupFunction.class);
        GroupItem originalItem = new GroupItem("name", baseItem, mockFunction);
        originalItem.setCategory("category");
        originalItem.setLabel("label");
        originalItem.addGroupNames("a", "b");

        Item resItem = itemRegistry.newItemBuilder(originalItem).build();

        assertEquals(GroupItem.class, resItem.getClass());
        GroupItem res = (GroupItem) resItem;
        verifyNoMoreInteractions(mockFactory);
        assertEquals("category", res.getCategory());
        assertEquals(Arrays.asList("a", "b"), res.getGroupNames());
        assertEquals("label", res.getLabel());
        assertSame(mockFunction, res.getFunction());
        assertSame(baseItem, res.getBaseItem());
    }

    @Test(expected = IllegalStateException.class)
    public void testNoFactory() {
        when(mockFactory.createItem(anyString(), anyString())).thenReturn(null);
        itemRegistry.newItemBuilder("String", "test").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFunctionOnNonGroupItem() {
        GroupFunction mockFunction = mock(GroupFunction.class);
        itemRegistry.newItemBuilder("String", "test").withGroupFunction(mockFunction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseItemOnNonGroupItem() {
        Item mockItem = mock(Item.class);
        itemRegistry.newItemBuilder("String", "test").withBaseItem(mockItem);
    }

}
