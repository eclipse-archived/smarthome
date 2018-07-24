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
package org.eclipse.smarthome.core.thing;

import static org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider.SYSTEM_OUTDOOR_TEMPERATURE;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing {@link Channel} properties.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class ChannelPropertiesTest extends JavaOSGiTest {

    private static final String NULL_STRING = null; // trick the null-annotation tooling
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";
    private final Map<String, String> properties = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put(KEY1, VALUE1);
            put(KEY2, VALUE2);
        }
    };
    private Channel channel;

    @Before
    public void setup() {
        ThingType thingType = ThingTypeBuilder.instance(new ThingTypeUID("bindingId", "thingTypeId"), "thingLabel")
                .build();
        ChannelUID channelUID = new ChannelUID(new ThingUID(thingType.getUID(), "thingId"), "temperature");
        channel = ChannelBuilder.create(channelUID, SYSTEM_OUTDOOR_TEMPERATURE.getItemType())
                .withType(SYSTEM_OUTDOOR_TEMPERATURE.getUID()).withProperties(properties).build();
    }

    @Test
    public void testGetProperties() {
        assertEquals(2, channel.getProperties().size());
        assertEquals(VALUE1, channel.getProperties().get(KEY1));
        assertEquals(VALUE2, channel.getProperties().get(KEY2));
    }

    @Test
    public void testSetPropertyNewKey() {
        channel.setProperty(KEY3, VALUE3);

        assertEquals(3, channel.getProperties().size());
        assertEquals(VALUE1, channel.getProperties().get(KEY1));
        assertEquals(VALUE2, channel.getProperties().get(KEY2));
        assertEquals(VALUE3, channel.getProperties().get(KEY3));
    }

    @Test
    public void testSetPropertyNewValue() {
        String value = channel.setProperty(KEY2, VALUE3);

        assertEquals(VALUE2, value);
        assertEquals(2, channel.getProperties().size());
        assertEquals(VALUE1, channel.getProperties().get(KEY1));
        assertEquals(VALUE3, channel.getProperties().get(KEY2));
    }

    @Test
    public void testSetProperties() {
        Map<String, String> newProperties = new HashMap<>();
        newProperties.put(KEY3, VALUE3);

        channel.setProperties(newProperties);

        assertEquals(1, channel.getProperties().size());
        assertEquals(VALUE3, channel.getProperties().get(KEY3));
    }

    @Test
    public void testRemoveProperty() {
        String value = channel.setProperty(KEY1, null);

        assertEquals(VALUE1, value);
        assertEquals(1, channel.getProperties().size());
        assertEquals(VALUE2, channel.getProperties().get(KEY2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPropertyNullKey() {
        channel.setProperty(NULL_STRING, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPropertyEmptyName() {
        channel.setProperty("", "");
    }
}
