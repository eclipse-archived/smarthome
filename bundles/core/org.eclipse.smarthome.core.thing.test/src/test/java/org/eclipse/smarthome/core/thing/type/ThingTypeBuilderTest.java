/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link ThingTypeBuilder}.
 *
 * @author Henning Treu - initial contribution
 *
 */
public class ThingTypeBuilderTest {

    private static final String CONF_URI = "conf:uri";
    private static final String REPRESENTATION_PROPERTY = "representationProperty";
    private static final String DESCRIPTION = "description";
    private static final String CATEGORY = "category";
    private static final String LABEL = "label";
    private static final String THING_TYPE_ID = "thingTypeId";
    private static final String BINDING_ID = "bindingId";

    private ThingTypeBuilder builder;

    @Before
    public void setup() {
        // set up a valid basic ThingTypeBuilder
        builder = new ThingTypeBuilder().withThingTypeId(THING_TYPE_ID).withBindingId(BINDING_ID).withLabel(LABEL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenThingTypeIdAndBindingIdMissing_shouldFail() {
        new ThingTypeBuilder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenThingTypeIdMissing_shouldFail() {
        new ThingTypeBuilder().withBindingId(BINDING_ID).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenBindingIdMissing_shouldFail() {
        new ThingTypeBuilder().withThingTypeId(THING_TYPE_ID).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenLabelMissing_shouldFail() {
        new ThingTypeBuilder().withThingTypeId(THING_TYPE_ID).withBindingId(BINDING_ID).build();
    }

    @Test
    public void withLabelAndThingTypeUID_shouldCreateThingType() {
        ThingType thingType = new ThingTypeBuilder().withThingTypeUID(new ThingTypeUID(BINDING_ID, THING_TYPE_ID))
                .withLabel(LABEL).build();

        assertThat(thingType.getBindingId(), is(BINDING_ID));
        assertThat(thingType.getUID().getBindingId(), is(BINDING_ID));
        assertThat(thingType.getUID().getId(), is(THING_TYPE_ID));
        assertThat(thingType.getLabel(), is(LABEL));
    }

    @Test
    public void withLabelAndThingTypeIdAndBindingID_shouldCreateThingType() {
        ThingType thingType = builder.build();

        assertThat(thingType.getBindingId(), is(BINDING_ID));
        assertThat(thingType.getUID().getBindingId(), is(BINDING_ID));
        assertThat(thingType.getUID().getId(), is(THING_TYPE_ID));
        assertThat(thingType.getLabel(), is(LABEL));
    }

    @Test
    public void withLabelAndThingTypeIdAndBindingID_shouldSetListed() {
        ThingType thingType = builder.build();

        assertThat(thingType.isListed(), is(true));
    }

    @Test
    public void whithDescription_shouldSetDescription() {
        ThingType thingType = builder.withDescription(DESCRIPTION).build();

        assertThat(thingType.getDescription(), is(DESCRIPTION));
    }

    @Test
    public void whithCategory_shouldSetCategory() {
        ThingType thingType = builder.withCategory(CATEGORY).build();

        assertThat(thingType.getCategory(), is(CATEGORY));
    }

    @Test
    public void whithListed_shouldBeListed() {
        ThingType thingType = builder.isListed(false).build();

        assertThat(thingType.isListed(), is(false));
    }

    @Test
    public void whithRepresentationProperty_shouldSetRepresentationProperty() {
        ThingType thingType = builder.withRepresentationProperty(REPRESENTATION_PROPERTY).build();

        assertThat(thingType.getRepresentationProperty(), is(REPRESENTATION_PROPERTY));
    }

    @Test
    public void whithChannelDefinitions_shouldSetUnmodifiableChannelDefinitions() {
        ThingType thingType = builder.withChannelDefinitions(mockList(ChannelDefinition.class, 2)).build();

        assertThat(thingType.getChannelDefinitions(), is(hasSize(2)));
        try {
            thingType.getChannelDefinitions().add(mock(ChannelDefinition.class));
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void whithChannelGroupDefinitions_shouldSetUnmodifiableChannelGroupDefinitions() {
        ThingType thingType = builder.withChannelGroupDefinitions(mockList(ChannelGroupDefinition.class, 2)).build();

        assertThat(thingType.getChannelGroupDefinitions(), is(hasSize(2)));
        try {
            thingType.getChannelGroupDefinitions().add(mock(ChannelGroupDefinition.class));
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void withProperties_shouldSetUnmodifiableProperties() {
        ThingType thingType = builder.withProperties(mockProperties()).build();

        assertThat(thingType.getProperties().entrySet(), is(hasSize(2)));
        try {
            thingType.getProperties().put("should", "fail");
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void whithConfigDescriptionURI_shouldSetConfigDescriptionURI() throws Exception {
        ThingType thingType = builder.withConfigDescriptionURI(new URI(CONF_URI)).build();

        assertThat(thingType.getConfigDescriptionURI(), is(new URI(CONF_URI)));
    }

    @Test
    public void withExtensibleChannelTypeIds_shouldSetUnmodifiableExtensibleChannelTypeIds() {
        @SuppressWarnings("null")
        ThingType thingType = builder
                .withExtensibleChannelTypeIds(Arrays.asList(new String[] { "channelTypeId1", "channelTypeId2" }))
                .build();

        assertThat(thingType.getExtensibleChannelTypeIds(), is(hasSize(2)));
        try {
            thingType.getExtensibleChannelTypeIds().add("channelTypeId");
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void withSupportedBridgeTypeUIDs_shouldSetUnmodifiableSupportedBridgeTypeUIDs() {
        ThingType thingType = builder.withSupportedBridgeTypeUIDs(Arrays.asList(new String[] { "bridgeTypeUID1" }))
                .build();

        assertThat(thingType.getSupportedBridgeTypeUIDs(), is(hasSize(1)));
        try {
            thingType.getSupportedBridgeTypeUIDs().add("bridgeTypeUID");
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void shoulsBuildBridgeType() {
        BridgeType bridgeType = builder.buildBridge();

        assertThat(bridgeType.getBindingId(), is(BINDING_ID));
        assertThat(bridgeType.getUID().getBindingId(), is(BINDING_ID));
        assertThat(bridgeType.getUID().getId(), is(THING_TYPE_ID));
        assertThat(bridgeType.getLabel(), is(LABEL));
    }

    private Map<@NonNull String, String> mockProperties() {
        Map<@NonNull String, String> result = new HashMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");

        return result;
    }

    private <T> List<T> mockList(Class<T> entityClass, int size) {
        List<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(mock(entityClass));
        }

        return result;
    }

}
