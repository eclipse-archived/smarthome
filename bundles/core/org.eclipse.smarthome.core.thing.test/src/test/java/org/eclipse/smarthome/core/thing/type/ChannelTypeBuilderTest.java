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
package org.eclipse.smarthome.core.thing.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.EventOption;
import org.eclipse.smarthome.core.types.StateDescription;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link ChannelTypeBuilder}.
 *
 * @author Stefan Triller - initial contribution
 *
 */
public class ChannelTypeBuilderTest {

    private static final String DESCRIPTION = "description";
    private static final String ITEM_TYPE = "itemType";
    private static final String CATEGORY = "category";
    private static final String LABEL = "label";
    private static final String TAG = "tag";
    private static final List<String> TAGS = Arrays.asList("TAG1", "TAG2");
    private static URI CONFIGDESCRIPTION_URI;
    private static final String CHANNEL_TYPE_ID = "thingTypeId";
    private static final String BINDING_ID = "bindingId";
    private static final StateDescription STATE_DESCRIPTION = new StateDescription(BigDecimal.ZERO, new BigDecimal(100),
            BigDecimal.ONE, "%s", false, null);
    private static final EventDescription EVENT_DESCRIPTION = new EventDescription(
            Arrays.asList(new EventOption(CommonTriggerEvents.DIR1_PRESSED, null),
                    new EventOption(CommonTriggerEvents.DIR1_RELEASED, null)));

    private ChannelTypeBuilder builder;

    @Before
    public void setup() throws URISyntaxException {
        CONFIGDESCRIPTION_URI = new URI("config:dummy");
        // set up a valid basic ChannelTypeBuilder
        builder = new ChannelTypeBuilder(BINDING_ID, CHANNEL_TYPE_ID, LABEL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenChannelTypeIdAndBindingIdBlank_shouldFail() {
        new ChannelTypeBuilder("", "", LABEL).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenChannelTypeIdBlank_shouldFail() {
        new ChannelTypeBuilder(BINDING_ID, "", LABEL).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenBindingIdBlank_shouldFail() {
        new ChannelTypeBuilder("", CHANNEL_TYPE_ID, LABEL).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenLabelBlank_shouldFail() {
        new ChannelTypeBuilder(CHANNEL_TYPE_ID, BINDING_ID, "").build();
    }

    @Test
    public void withLabelAndChannelTypeUID_shouldCreateChannelType() {
        ChannelType channelType = new ChannelTypeBuilder(BINDING_ID, CHANNEL_TYPE_ID, LABEL).withItemType(ITEM_TYPE)
                .build();

        assertThat(channelType.getUID().getBindingId(), is(BINDING_ID));
        assertThat(channelType.getUID().getId(), is(CHANNEL_TYPE_ID));
        assertThat(channelType.getLabel(), is(LABEL));
    }

    @Test
    public void withLabelAndThingTypeIdAndBindingID_shouldCreateThingType() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).build();

        assertThat(channelType.getUID().getBindingId(), is(BINDING_ID));
        assertThat(channelType.getUID().getId(), is(CHANNEL_TYPE_ID));
        assertThat(channelType.getLabel(), is(LABEL));
    }

    @Test
    public void withdefaultAdvancedIsFalse() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).build();

        assertThat(channelType.isAdvanced(), is(false));
    }

    @Test
    public void withLabelAndChannelTypeIdAndBindingID_shouldSetAdvanced() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).isAdvanced(true).build();

        assertThat(channelType.isAdvanced(), is(true));
    }

    @Test
    public void withDescription_shouldSetDescription() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).withDescription(DESCRIPTION).build();

        assertThat(channelType.getDescription(), is(DESCRIPTION));
    }

    @Test
    public void withCategory_shouldSetCategory() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).withCategory(CATEGORY).build();

        assertThat(channelType.getCategory(), is(CATEGORY));
    }

    @Test
    public void withItemType_shouldSetItemType() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).build();

        assertThat(channelType.getItemType(), is(ITEM_TYPE));
    }

    @Test
    public void withdefaultChannelKindIsState() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).build();

        assertThat(channelType.getKind(), is(ChannelKind.STATE));
    }

    @Test
    public void withChannelKind_shouldSetChannelKind() {
        ChannelType channelType = builder.withKind(ChannelKind.TRIGGER).build();

        assertThat(channelType.getKind(), is(ChannelKind.TRIGGER));
    }

    @Test
    public void withConfigDescriptionURI_shouldSetConfigDescriptionURI() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).withConfigDescriptionURI(CONFIGDESCRIPTION_URI)
                .build();

        assertThat(channelType.getConfigDescriptionURI(), is(CONFIGDESCRIPTION_URI));
    }

    @Test
    public void withTags_shouldSetTag() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).withTag(TAG).build();

        assertThat(channelType.getTags(), is(hasSize(1)));
    }

    @Test
    public void withTags_shouldSetTags() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).withTags(TAGS).build();

        assertThat(channelType.getTags(), is(hasSize(2)));
    }

    @Test
    public void withStateDescription_shouldSetStateDescription() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).withStateDescription(STATE_DESCRIPTION).build();

        assertThat(channelType.getState(), is(STATE_DESCRIPTION));
    }

    @Test
    public void withEventDescription_shouldSetEventDescription() {
        ChannelType channelType = builder.withItemType(ITEM_TYPE).withEventDescription(EVENT_DESCRIPTION).build();

        assertThat(channelType.getEvent(), is(EVENT_DESCRIPTION));
    }
}
