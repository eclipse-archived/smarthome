/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.test.item

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import javax.ws.rs.core.Response.Status

import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ManagedItemProvider
import org.eclipse.smarthome.core.library.items.DimmerItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.io.rest.core.item.ItemResource
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * ItemResourceOSGiTest tests the ItemResource REST resource on the OSGi level.
 *
 * @author Dennis Nobel - Initial contribution
 */
class ItemResourceOSGiTest extends OSGiTest {

    ItemResource itemResource
    ManagedItemProvider managedItemProvider

    @Before
    void setUp() {
        registerVolatileStorageService()
        managedItemProvider = getService ManagedItemProvider
        itemResource = getService ItemResource
        itemResource.uriInfo = [
            getPath: { return "path" },
            getBaseUri: { return new URI("uri")}
        ] as UriInfo
    }

    @After
    void cleanUp() {
        managedItemProvider.getAll().each {
            managedItemProvider.remove(it.name)
        }
    }


    @Test
    void 'assert getItems with tag filter works'() {

        def item1 = new SwitchItem("Item1")
        def item2 = new SwitchItem("Item2")
        def item3 = new SwitchItem("Item3")

        item1.addTag("Tag1")
        item2.addTag("Tag2")
        item2.addTag("Tag1")
        item3.addTag("Tag2")

        def itemProvider = [
            getAll: {
                return [item1, item2, item3]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
        ] as ItemProvider
        registerService itemProvider

        assertThat containsItems(itemResource.getItems(null, null, "Tag1", false).getEntity(), ["Item1", "Item2"]), is(true)
        assertThat containsItems(itemResource.getItems(null, null, "Tag2", false).getEntity(), ["Item2", "Item3"]), is(true)
        assertThat itemResource.getItems(null, null, "NotExistingTag", false).getEntity().size(), is(0)
    }

    @Test
    void 'assert getItems with type filter works'() {

        def item1 = new SwitchItem("Item1")
        def item2 = new SwitchItem("Item2")
        def item3 = new DimmerItem("Item3")

        def itemProvider = [
            getAll: {
                return [item1, item2, item3]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
        ] as ItemProvider
        registerService itemProvider

        assertThat containsItems(itemResource.getItems(null, "Switch", null, false).getEntity(), ["Item1", "Item2"]), is(true)
        assertThat containsItems(itemResource.getItems(null, "Dimmer", null, false).getEntity(), ["Item3"]), is(true)
        assertThat itemResource.getItems(null, null, "Color", false).getEntity().size(), is(0)
    }

    @Test
    void 'assert addTag and removeTag works'() {
        managedItemProvider.add(new SwitchItem("Switch"))
        assertThat itemResource.getItems(null, null, "MyTag", false).getEntity().size(), is(0)
        itemResource.addTag("Switch", "MyTag")
        assertThat itemResource.getItems(null, null, "MyTag", false).getEntity().size(), is(1)
        itemResource.removeTag("Switch", "MyTag")
        assertThat itemResource.getItems(null, null, "MyTag", false).getEntity().size(), is(0)
    }

    @Test
    void 'assert expected status codes for addTag and removeTag are returned'() {
        Response response = itemResource.addTag("Switch", "MyTag")
        assertThat response.status, is(Status.NOT_FOUND.code)

        response = itemResource.removeTag("Switch", "MyTag")
        assertThat response.status, is(Status.NOT_FOUND.code)

        def itemProvider = [
            getAll: {
                return [
                    new SwitchItem("UnmanagedItem")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
        ] as ItemProvider
        registerService itemProvider

        response = itemResource.addTag("UnmanagedItem", "MyTag")
        assertThat response.status, is(Status.METHOD_NOT_ALLOWED.code)
    }

    private containsItems(Object entity, List<String> itemNames) {
        def allFound = true
        itemNames.each { itemName ->
            if(entity.find { itemBean -> itemBean.name == itemName} == null) {
                allFound = false
            }
        }
        return allFound
    }
}
