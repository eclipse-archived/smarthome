/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.type.BridgeType
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition
import org.eclipse.smarthome.core.thing.type.ChannelGroupType
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Test

/**
 * ThingFactoryTest is a test for the ThingFactory class.
 *
 * @author Dennis Nobel - Initial contribution, added test for different default types
 * @author Alex Tugarev - Adapted for constructor modification of ConfigDescriptionParameter
 * @author Thomas Höfer - Thing type constructor modified because of thing properties introduction
 */
class ThingFactoryTest extends OSGiTest{

    @Test
    void 'create simple Thing'() {

        def thingType = new ThingType("bindingId", "thingTypeId", "label")
        def configuration = new Configuration();

        def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

        assertThat thing.getUID().toString(), is(equalTo("bindingId:thingTypeId:thingId"))
        assertThat thing.getThingTypeUID().toString(), is(equalTo("bindingId:thingTypeId"))
        assertThat thing.getConfiguration(), is(not(null))
        assertThat thing.getProperties(), is(not(null))
    }

    @Test
    void 'create simple Bridge'() {

        def thingType = new BridgeType("bindingId", "thingTypeId", "label")
        def configuration = new Configuration();

        def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

        assertThat thing, is(instanceOf(Bridge))
        assertThat thing.getProperties(), is(not(null))
    }

    @Test
    void 'create Thing with Bridge'() {

        def bridgeUID = new ThingUID("binding:bridge:1")

        def thingType = new ThingType("bindingId", "thingTypeId", "label")
        def configuration = new Configuration();

        def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration, bridgeUID)

        assertThat thing.getBridgeUID(), is(equalTo(bridgeUID))
    }

    private List getChannelDefinitions(){
        List channelDefinitions = new ArrayList<ChannelDefinition>()
        def channelType1 = new ChannelType(new ChannelTypeUID("bindingId:cd1"), false, "itemType", "channelLabel", "description", "category", new HashSet<String>(), null, new URI("scheme", "channelType:cd1", null))
        def channelType2 = new ChannelType(new ChannelTypeUID("bindingId:cd2"), false, "itemType2", "label2", "description22222", "category", new HashSet<String>(), null, new URI("scheme", "channelType:cd2",null))

        registerChannelTypes([channelType1, channelType2], [])

        def cd1 = new ChannelDefinition("channel1", channelType1.UID)
        def cd2 = new ChannelDefinition("channel2", channelType2.UID)
        channelDefinitions.add(cd1)
        channelDefinitions.add(cd2)
        return channelDefinitions;
    }


    @Test
    void 'create Thing with Default values'(){
        def thingType = new ThingType(new ThingTypeUID("myThingType","myThing"), null, "label", "description", getChannelDefinitions(), null, null, new URI("scheme", "thingType", null))
        def configuration = new Configuration()

        def configDescriptionRegistry = new ConfigDescriptionRegistry() {
                    ConfigDescription getConfigDescription( URI uri) {
                        def parameters = [
                            new ConfigDescriptionParameter("testProperty",
                            ConfigDescriptionParameter.Type.TEXT, null, null, null, null, false, false, false, "context", "default", "label", "description", null, null, null, false, true, null)
                        ]
                        return new ConfigDescription(uri, parameters)
                    }
                }

        def Thing thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration, null, configDescriptionRegistry)
        assertThat thing.configuration, is(not(null))
        assertThat thing.configuration.get("testProperty"), is(not(null))
        assertThat thing.configuration.get("testProperty"), is(equalTo("default"))
        assertThat thing.channels.size, is(equalTo(2))
        assertThat thing.channels[0].configuration.get("testProperty"), is(equalTo("default"))
        assertThat thing.channels[1].configuration.get("testProperty"), is(equalTo("default"))
        assertThat thing.getProperties().size(), is(0)
    }

    @Test
    void 'create Thing with different default value types'(){
        def thingType = new ThingType(new ThingTypeUID("myThingType","myThing"), null, "label", "description", null, null, null, new URI("scheme", "thingType", null))
        def configuration = new Configuration()

        def configDescriptionRegistry = new ConfigDescriptionRegistry() {
                    ConfigDescription getConfigDescription( URI uri) {
                        def parameters = [
                            new ConfigDescriptionParameter("p1",
                            ConfigDescriptionParameter.Type.BOOLEAN, null, null, null, null, false, false, false, "context", "true", "label", "description", null, null, null, false, true, null),
                            new ConfigDescriptionParameter("p2",
                            ConfigDescriptionParameter.Type.INTEGER, null, null, null, null, false, false, false, "context", "5", "label", "description", null, null, null, false, true, null),
                            new ConfigDescriptionParameter("p3",
                            ConfigDescriptionParameter.Type.DECIMAL, null, null, null, null, false, false, false, "context", "2.3", "label", "description", null, null, null, false, true, null),
                            new ConfigDescriptionParameter("p4",
                            ConfigDescriptionParameter.Type.DECIMAL, null, null, null, null, false, false, false, "context", "invalid", "label", "description", null, null, null, false, true, null)
                        ]
                        return new ConfigDescription(uri, parameters)
                    }
                }

        def Thing thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration, null, configDescriptionRegistry)
        assertThat thing.configuration, is(not(null))
        assertThat thing.configuration.get("p1"), is(equalTo(true))
        assertThat thing.configuration.get("p2").compareTo(new BigDecimal("5")), is(0)
        assertThat thing.configuration.get("p3").compareTo(new BigDecimal("2.3")), is(0)
        assertThat thing.configuration.get("p4"), is(null)
        assertThat thing.getProperties().size(), is(0)
    }

    @Test
    void 'create Thing with Channels'() {

        ChannelType channelType1 = new ChannelType(new ChannelTypeUID("bindingId:channelTypeId1"), false, "Color", "label", "description", "category", new HashSet(["tag1", "tag2"]), null, null)
        ChannelType channelType2 = new ChannelType(new ChannelTypeUID("bindingId:channelTypeId2"), false, "Dimmer", "label", "description", "category", new HashSet(["tag3"]), null, null)

        registerChannelTypes([channelType1, channelType2], [])

        ChannelDefinition channelDef1 = new ChannelDefinition("ch1", channelType1.UID)
        ChannelDefinition channelDef2 = new ChannelDefinition("ch2", channelType2.UID)

        def thingType = new ThingType(new ThingTypeUID("bindingId:thingType"), [], "label", null, [channelDef1, channelDef2], null, null, null)
        def configuration = new Configuration();

        def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

        assertThat thing.getChannels().size, is(2)
        assertThat thing.getChannels().get(0).getUID().toString(), is(equalTo("bindingId:thingType:thingId:ch1"))
        assertThat thing.getChannels().get(0).getAcceptedItemType(), is(equalTo("Color"))
        assertThat thing.getChannels().get(0).getDefaultTags().contains("tag1"), is(true)
        assertThat thing.getChannels().get(0).getDefaultTags().contains("tag2"), is(true)
        assertThat thing.getChannels().get(0).getDefaultTags().contains("tag3"), is(false)
        assertThat thing.getChannels().get(1).getDefaultTags().contains("tag1"), is(false)
        assertThat thing.getChannels().get(1).getDefaultTags().contains("tag2"), is(false)
        assertThat thing.getChannels().get(1).getDefaultTags().contains("tag3"), is(true)
    }

    @Test
    void 'create Thing with channels groups'() {

        ChannelType channelType1 = new ChannelType(new ChannelTypeUID("bindingId:channelTypeId1"), false, "Color", "label", "description", "category", new HashSet(["tag1", "tag2"]), null, null)
        ChannelType channelType2 = new ChannelType(new ChannelTypeUID("bindingId:channelTypeId2"), false, "Dimmer", "label", "description", "category", new HashSet(["tag3"]), null, null)

        ChannelDefinition channelDef1 = new ChannelDefinition("ch1", channelType1.UID)
        ChannelDefinition channelDef2 = new ChannelDefinition("ch2", channelType2.UID)

        ChannelGroupType channelGroupType1 = new ChannelGroupType(new ChannelGroupTypeUID("bindingid:groupTypeId1"), false, "label", "description", [channelDef1, channelDef2])
        ChannelGroupType channelGroupType2 = new ChannelGroupType(new ChannelGroupTypeUID("bindingid:groupTypeId2"), false, "label", "description", [channelDef1])

        ChannelGroupDefinition channelGroupDef1 = new ChannelGroupDefinition("group1", channelGroupType1.UID)
        ChannelGroupDefinition channelGroupDef2 = new ChannelGroupDefinition("group2", channelGroupType2.UID)

        registerChannelTypes([channelType1, channelType2], [
            channelGroupType1,
            channelGroupType2
        ])

        def thingType = new ThingType(new ThingTypeUID("bindingId:thingType"), [], "label", null, null, [
            channelGroupDef1,
            channelGroupDef2
        ], null, null)
        def configuration = new Configuration();

        def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

        assertThat thing.getChannels().size, is(3)
        assertThat thing.getChannels().get(0).getUID().toString(), is(equalTo("bindingId:thingType:thingId:group1#ch1"))
        assertThat thing.getChannels().get(1).getUID().toString(), is(equalTo("bindingId:thingType:thingId:group1#ch2"))
        assertThat thing.getChannels().get(2).getUID().toString(), is(equalTo("bindingId:thingType:thingId:group2#ch1"))
    }

    @Test
    void 'create Thing with properties'() {
        def thingType = new ThingType(new ThingTypeUID("bindingId:thingType"), [], "label", null, null, null, ["key1":"value1", "key2":"value2"], null)
        def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), new Configuration())

        assertThat thing.getProperties().size(), is(2)
        assertThat thing.getProperties().get("key1"), is("value1")
        assertThat thing.getProperties().get("key2"), is("value2")
    }

    @Test
    void 'create Bridge with properties'() {
        def thingType = new BridgeType(new ThingTypeUID("bindingId", "thingTypeId"), null, "label", null, null, null, ["key1":"value1", "key2":"value2"], null);
        def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), new Configuration())

        assertThat thing.getProperties().size(), is(2)
        assertThat thing.getProperties().get("key1"), is("value1")
        assertThat thing.getProperties().get("key2"), is("value2")
    }

    private void registerChannelTypes(channelTypes, channelGroupTypes) {
        registerService([
            getChannelTypes: { channelTypes },
            getChannelType: { ChannelTypeUID uid, Locale locale ->
                channelTypes.find { it.UID == uid }
            },
            getChannelGroupTypes: { channelGroupTypes },
            getChannelGroupType: { ChannelGroupTypeUID uid, Locale locale ->
                channelGroupTypes.find { it.UID == uid }
            },
        ] as ChannelTypeProvider)
    }
}
