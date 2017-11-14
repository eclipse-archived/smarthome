/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.internal.item;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.items.dto.GroupItemDTO;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.io.rest.core.internal.item.ItemResource;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;

public class ItemResourceOSGiTest extends JavaOSGiTest {

    private static final @NonNull String ITEM_NAME1 = "Item1";
    private static final @NonNull String ITEM_NAME2 = "Item2";
    private static final @NonNull String ITEM_NAME3 = "Item3";

    private GenericItem item1;
    private GenericItem item2;
    private GenericItem item3;

    @Mock
    private ItemProvider itemProvider;

    private ItemResource itemResource;

    private ManagedItemProvider managedItemProvider;

    @Before
    public void setup() {
        initMocks(this);
        itemResource = getService(ItemResource.class);
        itemResource.uriInfo = mock(UriInfo.class);

        registerVolatileStorageService();
        managedItemProvider = getService(ManagedItemProvider.class);

        item1 = new SwitchItem(ITEM_NAME1);
        item2 = new SwitchItem(ITEM_NAME2);
        item3 = new DimmerItem(ITEM_NAME3);

        when(itemProvider.getAll()).thenReturn(Lists.newArrayList(item1, item2, item3));
        registerService(itemProvider);
    }

    @Test
    public void shouldFilterItemsByTag() throws Exception {
        item1.addTag("Tag1");
        item2.addTag("Tag1");
        item2.addTag("Tag2");
        item3.addTag("Tag2");

        Response response = itemResource.getItems(null, null, "Tag1", false);
        assertThat(readItemNamesFromResponse(response), hasItems(ITEM_NAME1, ITEM_NAME2));

        response = itemResource.getItems(null, null, "Tag2", false);
        assertThat(readItemNamesFromResponse(response), hasItems(ITEM_NAME2, ITEM_NAME3));

        response = itemResource.getItems(null, null, "NotExistingTag", false);
        assertThat(readItemNamesFromResponse(response), hasSize(0));
    }

    @Test
    public void shouldFilterItemsByType() throws Exception {
        Response response = itemResource.getItems(null, "Switch", null, false);
        assertThat(readItemNamesFromResponse(response), hasItems(ITEM_NAME1, ITEM_NAME2));

        response = itemResource.getItems(null, "Dimmer", null, false);
        assertThat(readItemNamesFromResponse(response), hasItems(ITEM_NAME3));

        response = itemResource.getItems(null, "Color", null, false);
        assertThat(readItemNamesFromResponse(response), hasSize(0));
    }

    @Test
    public void shouldAddAndRemoveTags() throws Exception {
        managedItemProvider.add(new SwitchItem("Switch"));

        Response response = itemResource.getItems(null, null, "MyTag", false);
        assertThat(readItemNamesFromResponse(response), hasSize(0));

        itemResource.addTag("Switch", "MyTag");
        response = itemResource.getItems(null, null, "MyTag", false);
        assertThat(readItemNamesFromResponse(response), hasSize(1));

        itemResource.removeTag("Switch", "MyTag");
        response = itemResource.getItems(null, null, "MyTag", false);
        assertThat(readItemNamesFromResponse(response), hasSize(0));
    }

    @Test
    public void shouldProvideReturnCodesForTagHandling() {
        Response response = itemResource.addTag("Switch", "MyTag");
        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

        response = itemResource.removeTag("Switch", "MyTag");
        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

        unregisterService(itemProvider);
        when(itemProvider.getAll()).thenReturn(Lists.newArrayList(new SwitchItem("UnmanagedItem")));
        registerService(itemProvider);

        response = itemResource.addTag("UnmanagedItem", "MyTag");
        assertThat(response.getStatus(), is(Status.METHOD_NOT_ALLOWED.getStatusCode()));
    }

    private List<String> readItemNamesFromResponse(Response response) throws IOException {
        String jsonResponse = IOUtils.toString((InputStream) response.getEntity());
        return JsonPath.read(jsonResponse, "$..name");
    }

    @Test
    public void addMultipleItems() throws IOException {

        List<GroupItemDTO> itemList = new ArrayList<>();
        GroupItemDTO[] items = new GroupItemDTO[] {};

        GroupItemDTO item1DTO = new GroupItemDTO();
        item1DTO.name = "item1";
        item1DTO.type = "Switch";
        item1DTO.label = "item1Label";
        itemList.add(item1DTO);

        GroupItemDTO item2DTO = new GroupItemDTO();
        item2DTO.name = "item2";
        item2DTO.type = "Rollershutter";
        item2DTO.label = "item2Label";
        itemList.add(item2DTO);

        items = itemList.toArray(items);
        Response response = itemResource.createOrUpdateItems(items);

        String jsonResponse = IOUtils.toString((InputStream) response.getEntity());
        List<String> statusCodes = JsonPath.read(jsonResponse, "$..status");

        // expect 2x created
        assertThat(statusCodes.size(), is(2));
        assertThat(statusCodes.get(0), is("created"));
        assertThat(statusCodes.get(1), is("created"));

        itemList.clear();

        item1DTO.label = "item1LabelNew";
        itemList.add(item1DTO);
        item2DTO.type = "WrongType";
        itemList.add(item2DTO);

        items = itemList.toArray(items);
        response = itemResource.createOrUpdateItems(items);

        jsonResponse = IOUtils.toString((InputStream) response.getEntity());
        statusCodes = JsonPath.read(jsonResponse, "$..status");

        // expect error and updated
        assertThat(statusCodes.size(), is(2));
        assertThat(statusCodes.get(0), is("error"));
        assertThat(statusCodes.get(1), is("updated"));
    }

}
