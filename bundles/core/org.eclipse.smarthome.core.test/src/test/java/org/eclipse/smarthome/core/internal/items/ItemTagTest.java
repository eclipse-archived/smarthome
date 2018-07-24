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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.junit.Test;

/**
 * Test Item Tag handling.
 *
 * @author Andre Fuechsel - Initial contribution
 */
public class ItemTagTest {

    private static final String TAG1 = "tag1";
    private static final String TAG2 = "tag2";
    private static final String TAG3 = "tag3";
    private static final String TAG1_UPPERCASE = "TAG1";
    private static final String TAG2_MIXED_CASE = "tAg2";

    private static final String ITEM1 = "item1";

    @Test
    public void assertTagsAreAddedAndRemovedCorrectly() {
        SwitchItem item = new SwitchItem(ITEM1);
        assertThat(item.getTags().size(), is(0));

        item.addTag(TAG1);
        item.addTag(TAG2);

        assertThat(item.getTags().size(), is(2));
        assertThat(item.hasTag(TAG1), is(true));
        assertThat(item.hasTag(TAG2), is(true));
        assertThat(item.hasTag(TAG3), is(false));

        item.removeTag(TAG2);
        assertThat(item.getTags().size(), is(1));
        assertThat(item.hasTag(TAG1), is(true));
        assertThat(item.hasTag(TAG2), is(false));
        assertThat(item.hasTag(TAG3), is(false));

        item.removeAllTags();
        assertThat(item.getTags().size(), is(0));
        assertThat(item.hasTag(TAG1), is(false));
        assertThat(item.hasTag(TAG2), is(false));
        assertThat(item.hasTag(TAG3), is(false));
    }

    @Test
    public void testThatTagsAreHandledCaseInsensitive() {
        SwitchItem item = new SwitchItem(ITEM1);
        assertThat(item.getTags().size(), is(0));

        item.addTag(TAG1);
        item.addTag(TAG2_MIXED_CASE);

        assertThat(item.getTags().size(), is(2));
        assertThat(item.hasTag(TAG1), is(true));
        assertThat(item.hasTag(TAG1_UPPERCASE), is(true));
        assertThat(item.hasTag(TAG2), is(true));
        assertThat(item.hasTag(TAG2_MIXED_CASE), is(true));
    }

    @Test
    public void testThatItemsToBeRemovedAreFoundCaseInsensitive() {
        SwitchItem item = new SwitchItem(ITEM1);
        assertThat(item.getTags().size(), is(0));

        item.addTag(TAG1);
        assertThat(item.getTags().size(), is(1));
        assertThat(item.hasTag(TAG1), is(true));

        item.removeTag(TAG1_UPPERCASE);
        assertThat(item.getTags().size(), is(0));
    }

    @Test
    public void testThatAddTagsIsWorkingCaseInsensitive() {
        SwitchItem item = new SwitchItem(ITEM1);
        assertThat(item.getTags().size(), is(0));

        item.addTags(TAG1_UPPERCASE, TAG2_MIXED_CASE, TAG3);
        assertThat(item.getTags().size(), is(3));
        assertThat(item.hasTag(TAG1), is(true));
        assertThat(item.hasTag(TAG2), is(true));
        assertThat(item.hasTag(TAG3), is(true));
    }

    @Test
    public void testThatAddTagsFromListIsWorkingCaseInsensitive() {
        SwitchItem item = new SwitchItem(ITEM1);
        assertThat(item.getTags().size(), is(0));

        item.addTags(Arrays.asList(TAG1_UPPERCASE, TAG2_MIXED_CASE, TAG3));
        assertThat(item.getTags().size(), is(3));
        assertThat(item.hasTag(TAG1), is(true));
        assertThat(item.hasTag(TAG2), is(true));
        assertThat(item.hasTag(TAG3), is(true));
    }

    @Test
    public void testThatGetTagsReturnsTagsAllLowercase() {
        SwitchItem item = new SwitchItem(ITEM1);
        assertThat(item.getTags().size(), is(0));

        item.addTags(TAG1_UPPERCASE, TAG2_MIXED_CASE, TAG3);
        assertThat(item.getTags().size(), is(3));
        assertThat(item.hasTag(TAG1), is(true));
        assertThat(item.hasTag(TAG2), is(true));
        assertThat(item.hasTag(TAG3), is(true));

        assertThat(item.getTags().containsAll(Arrays.asList(TAG1, TAG2, TAG3)), is(true));
    }
}
