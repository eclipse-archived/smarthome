/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.item.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class GenericItemProvider2Test extends JavaOSGiTest {

    private static final String TESTMODEL_NAME = "testModel.items";
    private static final String TESTMODEL_NAME2 = "testModel2.items";

    private ModelRepository modelRepository;
    private ItemRegistry itemRegistry;

    @Before
    public void setUp() {
        itemRegistry = getService(ItemRegistry.class);
        assertThat(itemRegistry, is(notNullValue()));
        modelRepository = getService(ModelRepository.class);
        assertThat(modelRepository, is(notNullValue()));
        modelRepository.removeModel(TESTMODEL_NAME);
        modelRepository.removeModel(TESTMODEL_NAME2);
    }

    @After
    public void tearDown() {
        modelRepository.removeModel(TESTMODEL_NAME);
        modelRepository.removeModel(TESTMODEL_NAME2);
    }

    @Test
    public void testStableOrder() {
        assertThat(itemRegistry.getAll().size(), is(0));

        String model = "Group testGroup " + //
                "Number number1 (testGroup) " + //
                "Number number2 (testGroup) " + //
                "Number number3 (testGroup) " + //
                "Number number4 (testGroup) " + //
                "Number number5 (testGroup) " + //
                "Number number6 (testGroup) " + //
                "Number number7 (testGroup) " + //
                "Number number8 (testGroup) " + //
                "Number number9 (testGroup) ";

        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.getBytes()));
        GroupItem groupItem = (GroupItem) itemRegistry.get("testGroup");
        assertNotNull(groupItem);

        int number = 0;
        Iterator<Item> it = groupItem.getMembers().iterator();
        while (it.hasNext()) {
            Item item = it.next();
            assertEquals("number" + (++number), item.getName());
        }
    }

    @Test
    public void testStableReloadOrder() {
        assertThat(itemRegistry.getAll().size(), is(0));

        String model = "Group testGroup " + //
                "Number number1 (testGroup) " + //
                "Number number2 (testGroup) " + //
                "Number number3 (testGroup) " + //
                "Number number4 (testGroup) " + //
                "Number number5 (testGroup) " + //
                "Number number6 (testGroup) " + //
                "Number number7 (testGroup) " + //
                "Number number8 (testGroup) " + //
                "Number number9 (testGroup) ";

        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.getBytes()));
        assertThat(itemRegistry.getAll().size(), is(10));

        model = "Group testGroup " + //
                "Number number1 (testGroup) " + //
                "Number number2 (testGroup) " + //
                "Number number3 (testGroup) " + //
                "Number number4 (testGroup) " + //
                "Number number5 (testGroup) " + //
                "Number number6 (testGroup) " + //
                "Number number7 \"Number Seven\" (testGroup) " + //
                "Number number8 (testGroup) " + //
                "Number number9 (testGroup) ";

        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.getBytes()));
        GroupItem groupItem = (GroupItem) itemRegistry.get("testGroup");
        assertNotNull(groupItem);

        int number = 0;
        Iterator<Item> it = groupItem.getMembers().iterator();
        while (it.hasNext()) {
            Item item = it.next();
            assertEquals("number" + (++number), item.getName());
            if (number == 7) {
                assertEquals("Number Seven", item.getLabel());
            }
        }
    }

    @Test
    public void testGroupAssignmentsAreConsidered() {
        assertThat(itemRegistry.getAll().size(), is(0));

        String model = "Group testGroup " + //
                "Number number1 (testGroup) " + //
                "Number number2 ";

        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.getBytes()));

        model = "Group testGroup " + //
                "Number number1 (testGroup) " + //
                "Number number2 (testGroup)";

        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.getBytes()));

        GenericItem item = (GenericItem) itemRegistry.get("number2");
        assertTrue(item.getGroupNames().contains("testGroup"));
        GroupItem groupItem = (GroupItem) itemRegistry.get("testGroup");
        assertTrue(groupItem.getAllMembers().contains(item));
    }

}
