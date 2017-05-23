/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.items.GenericItem;
import org.junit.Before;
import org.junit.Test;

public class CoreItemFactoryTest {

    private CoreItemFactory coreItemFactory;

    private List<String> itemTypeNames;

    @Before
    public void setup() {
        coreItemFactory = new CoreItemFactory();
        itemTypeNames = Arrays.asList(coreItemFactory.getSupportedItemTypes());
    }

    @Test
    public void shouldCreateItems() {
        for (String itemTypeName : itemTypeNames) {
            GenericItem item = coreItemFactory.createItem(itemTypeName, itemTypeName.toLowerCase());

            assertThat(item.getType(), is(itemTypeName));
            assertThat(item.getName(), is(itemTypeName.toLowerCase()));
        }
    }

    @Test
    public void shouldReturnNullForUnsupportedItemTypeName() {
        GenericItem item = coreItemFactory.createItem("NoValidItemTypeName", "IWantMyItem");

        assertThat(item, is(nullValue()));
    }

}
