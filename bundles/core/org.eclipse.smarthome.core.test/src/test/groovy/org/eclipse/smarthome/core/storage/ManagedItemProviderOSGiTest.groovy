/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.storage

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.ManagedItemProvider
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * The {@link ManagedItemProviderOSGiTest} runs inside an
 * OSGi container and tests the {@link ManagedItemProvider}.
 *
 * @author Thomas Eichstaedt-Engelen - Initial contribution
 * @author Kai Kreuzer - added tests for repeated addition and removal
 * @author Andre Fuechsel - added tests for tags
 */
class ManagedItemProviderOSGiTest extends OSGiTest {

    ManagedItemProvider itemProvider
    ItemRegistry itemRegistry

    @Before
    void setUp() {
        registerVolatileStorageService()
        itemProvider = getService(ManagedItemProvider)
        itemRegistry = getService(ItemRegistry)
    }

    @After
    void tearDown() {
        itemProvider.getAll().each {
            itemProvider.remove(it.name)
        }
        unregisterService(itemProvider)
    }

    @Test
    void 'assert getItems returns item from registered ManagedItemProvider'() {

        assertThat itemProvider.getAll().size(), is(0)

        itemProvider.add new SwitchItem('SwitchItem')
        itemProvider.add new StringItem('StringItem')

        def items = itemProvider.getAll()
        assertThat items.size(), is(2)

        itemProvider.remove 'StringItem'
        itemProvider.remove 'SwitchItem'

        assertThat itemProvider.getAll().size(), is(0)
    }

    @Test
    void 'updating existing item returns old value'() {

        assertThat itemProvider.getAll().size(), is(0)

        itemProvider.add new StringItem('Item')
        def result = itemProvider.update new SwitchItem('Item')

        assertThat result.type, is("String")

        itemProvider.remove 'Item'

        assertThat itemProvider.getAll().size(), is(0)
    }

    @Test
    void 'assert removal returns old value'() {

        assertThat itemProvider.getAll().size(), is(0)

        itemProvider.add new StringItem('Item')
        def result = itemProvider.remove 'Unknown'

        assertNull result

        result = itemProvider.remove 'Item'

        assertThat result.name, is('Item')

        assertThat itemProvider.getAll().size(), is(0)
    }

    @Test(expected=IllegalArgumentException.class)
    void 'assert two items with same name can not be added'() {

        assertThat itemProvider.getAll().size(), is(0)

        itemProvider.add new StringItem('Item')
        itemProvider.add new StringItem('Item')
    }

    @Test
    void 'assert tags are stored and retrieve as well'() {

        assertThat itemProvider.getAll().size(), is(0)

        def item1 = new SwitchItem('SwitchItem1')
        def item2 = new SwitchItem('SwitchItem2')
        item1.addTag('tag1')
        item1.addTag('tag2')
        item2.addTag('tag3')

        itemProvider.add item1
        itemProvider.add item2

        def items = itemProvider.getAll()
        assertThat items.size(), is(2)

        def result1 = itemProvider.remove 'SwitchItem1'
        def result2 = itemProvider.remove 'SwitchItem2'

        assertThat result1.name, is('SwitchItem1')
        assertThat result1.getTags().size(), is(2)
        assertThat result1.hasTag('tag1'), is(true)
        assertThat result1.hasTag('tag2'), is(true)
        assertThat result1.hasTag('tag3'), is(false)

        assertThat result2.name, is('SwitchItem2')
        assertThat result2.getTags().size(), is(1)
        assertThat result2.hasTag('tag1'), is(false)
        assertThat result2.hasTag('tag2'), is(false)
        assertThat result2.hasTag('tag3'), is(true)

        assertThat itemProvider.getAll().size(), is(0)
    }

    @Test
    void 'assert remove recursively works'() {

        assertThat itemProvider.getAll().size(), is(0)

        def group = new GroupItem("group")

        def item1 = new SwitchItem('SwitchItem1')
        item1.addGroupName(group.name)
        def item2 = new SwitchItem('SwitchItem2')
        item2.addGroupName(group.name)

        itemProvider.add group
        itemProvider.add item1
        itemProvider.add item2

        assertThat itemProvider.getAll().size(), is(3)

        def oldItem = itemProvider.remove(group.name, true)

        assertThat oldItem, is(group)
        assertThat itemProvider.getAll().size(), is(0)
    }
}
