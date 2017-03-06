/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.tests

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.events.AbstractThingRegistryEvent
import org.eclipse.smarthome.core.thing.events.ThingAddedEvent
import org.eclipse.smarthome.core.thing.events.ThingRemovedEvent
import org.eclipse.smarthome.core.thing.events.ThingUpdatedEvent
import org.eclipse.smarthome.core.thing.type.ChannelKind
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets

class GenericThingProviderTest extends OSGiTest {

    private final static String TESTMODEL_NAME = "testModel.things"

    ModelRepository modelRepository
    ThingRegistry thingRegistry

    @Before
    void setUp() {
        thingRegistry = getService ThingRegistry
        assertThat thingRegistry, is(notNullValue())
        modelRepository = getService ModelRepository
        assertThat modelRepository, is(notNullValue())
        modelRepository.removeModel(TESTMODEL_NAME)
    }

    @After
    void tearDown() {
        modelRepository.removeModel(TESTMODEL_NAME)
    }

    @Test
    void 'assert that things that are contained in things files are added to ThingRegistry'() {

        def things = thingRegistry.getAll()
        assertThat things.size(), is(0)

        String model =
                '''
			Bridge hue:bridge:myBridge @ "basement" [ ip = "1.2.3.4", username = "123" ] {
				LCT001 bulb1 [ lightId = "1" ] { Switch : notification }
				Bridge bridge myBridge2 [ ] {
					LCT001 bulb2 [ ]
				}
			}
            hue:TEST:bulb4 [ lightId = "5"]{
                Switch : notification [ duration = "5" ]
            }

			hue:LCT001:bulb3 @ "livingroom" [ lightId = "4" ] {
				Switch : notification [ duration = "5" ]
			}
			'''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(6)

        def bridge1 = actualThings.find {
            "hue:bridge:myBridge".equals(it.UID.toString())
        }

        assertThat bridge1, isA(Bridge)
        assertThat bridge1.channels.size(), is(0)
        //assertThat bridge.status, is(ThingStatus.ONLINE)
        assertThat bridge1.bridgeUID, is(nullValue())
        assertThat bridge1.configuration.values().size(), is(2)
        assertThat bridge1.configuration.get("ip"), is("1.2.3.4")
        assertThat bridge1.configuration.get("username"), is("123")
        assertThat bridge1.thingTypeUID.toString(), is("hue:bridge")
        assertThat bridge1.location, is("basement")

        def bridge2 = actualThings.find {
            "hue:bridge:myBridge:myBridge2".equals(it.UID.toString())
        }

        assertThat bridge2, isA(Bridge)
        assertThat bridge2.channels.size(), is(0)
        //assertThat bridge.status, is(ThingStatus.ONLINE)
        assertThat bridge2.bridgeUID, is(bridge1.UID)
        assertThat bridge2.configuration.values().size(), is(0)
        assertThat bridge2.thingTypeUID.toString(), is("hue:bridge")

        def bulb1 = actualThings.find {
            "hue:LCT001:myBridge:bulb1".equals(it.UID.toString())
        }

        assertThat bulb1, isA(Thing)
        // three channels should be defined, color and color_temperature from thingType and switch from dsl
        assertThat bulb1.channels.size(), is(3)
        bulb1.channels.each {
            assertThat it.getUID().toString(), anyOf(is("hue:LCT001:myBridge:bulb1:notification"), is("hue:LCT001:myBridge:bulb1:color"), is("hue:LCT001:myBridge:bulb1:color_temperature"), is("hue:TEST:bulb4"))
            assertThat it.acceptedItemType, anyOf(is("Switch"), is("Color"), is("Dimmer"))
        }
        //assertThat bridge.status, is(ThingStatus.ONLINE)
        assertThat bulb1.bridgeUID, is(bridge1.UID)
        assertThat bulb1.configuration.values().size(), is(1)
        assertThat bulb1.configuration.get("lightId"), is("1")
        assertThat bulb1.thingTypeUID.toString(), is("hue:LCT001")

        def bulb2 = actualThings.find {
            "hue:LCT001:myBridge:myBridge2:bulb2".equals(it.UID.toString())
        }

        assertThat bulb2, isA(Thing)
        assertThat bulb2.channels.size(), is(2)
        //assertThat bridge.status, is(ThingStatus.ONLINE)
        assertThat bulb2.bridgeUID, is(bridge2.UID)
        assertThat bulb2.configuration.values().size(), is(0)
        assertThat bulb2.thingTypeUID.toString(), is("hue:LCT001")

        def bulb3 = actualThings.find {
            "hue:LCT001:bulb3".equals(it.UID.toString())
        }

        assertThat bulb3, isA(Thing)
        assertThat bulb3.channels.size(), is(3)
        Channel firstChannel
        bulb3.channels.each {
            if (it.uid.toString().equals("hue:LCT001:bulb3:notification")) {
                firstChannel=it
            }
        }
        assertThat firstChannel.uid.toString(), is("hue:LCT001:bulb3:notification")
        assertThat firstChannel.acceptedItemType, is("Switch")
        assertThat firstChannel.configuration.values().size(), is(1)
        assertThat firstChannel.configuration.get("duration"), is("5")
        //assertThat bridge.status, is(ThingStatus.ONLINE)
        assertThat bulb3.bridgeUID, is(nullValue())
        assertThat bulb3.configuration.values().size(), is(1)
        assertThat bulb3.configuration.get("lightId"), is("4")
        assertThat bulb3.thingTypeUID.toString(), is("hue:LCT001")
        assertThat bulb3.location, is("livingroom")
    }

    @Test
    void 'assert that the things in an updated things file is registered in the ThingRegistry'() {
        ThingRegistry thingRegistry = getService ThingRegistry
        assertThat thingRegistry, is(notNullValue())
        def things = thingRegistry.getAll()
        assertThat things.size(), is(0)
        ModelRepository modelRepository = getService ModelRepository
        assertThat modelRepository, is(notNullValue())
        String model =
                '''
			Bridge hue:bridge:myBridge [ ip = "1.2.3.4", username = "123" ]  {
				LCT001 bulb1 [ lightId = "1" ] { Switch : notification }
				Bridge bridge myBridge2 [ ] {
					LCT001 bulb2 [ ]
				}
			}

			hue:LCT001:bulb3 [ lightId = "4" ] {
				Switch : notification [ duration = "5" ]
			}
			'''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        String newModel =
                '''
			Bridge hue:bridge:myBridge [ ip = "5.6.7.8", secret = "123" ] {
				LCT001 bulb1 [ ]
			}

			hue:LCT001:bulb2 [ lightId = "2" ] {
				Color : color
			}
			'''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(newModel.bytes))
        def actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(3)

        def bridge1 = actualThings.find {
            "hue:bridge:myBridge".equals(it.UID.toString())
        }

        assertThat bridge1, isA(Bridge)
        assertThat bridge1.channels.size(), is(0)
        //assertThat bridge.status, is(ThingStatus.ONLINE)
        assertThat bridge1.bridgeUID, is(nullValue())
        assertThat bridge1.configuration.values().size(), is(2)
        assertThat bridge1.configuration.get("ip"), is("5.6.7.8")
        assertThat bridge1.configuration.get("secret"), is("123")
        assertThat bridge1.thingTypeUID.toString(), is("hue:bridge")

        def bulb1 = actualThings.find {
            "hue:LCT001:myBridge:bulb1".equals(it.UID.toString())
        }

        assertThat bulb1, isA(Thing)
        //there should be color and color_temperature from thingType definition
        assertThat bulb1.channels.size(), is(2)
        //assertThat bridge.status, is(ThingStatus.ONLINE)
        assertThat bulb1.bridgeUID, is(bridge1.UID)
        assertThat bulb1.configuration.values().size(), is(0)
        assertThat bulb1.thingTypeUID.toString(), is("hue:LCT001")

        def bulb2 = actualThings.find {
            "hue:LCT001:bulb2".equals(it.UID.toString())
        }

        assertThat bulb2, isA(Thing)
        //channels should be Color as defined in dsl and color_temperature from thingType
        assertThat bulb2.channels.size(), is(2)
        Channel firstChannel = bulb2.channels.first()
        assertThat firstChannel.uid.toString(), is("hue:LCT001:bulb2:color")
        assertThat firstChannel.acceptedItemType, is("Color")
        //assertThat bridge.status, is(ThingStatus.ONLINE)
        assertThat bulb2.bridgeUID, is(nullValue())
        assertThat bulb2.configuration.values().size(), is(1)
        assertThat bulb2.configuration.get("lightId"), is("2")
        assertThat bulb2.thingTypeUID.toString(), is("hue:LCT001")

    }

    @Test
    void 'assert that thingid can contain all characters allowed in config-description XSD'() {

        def things = thingRegistry.getAll()
        assertThat things.size(), is(0)

        String model =
                '''
            hue:1-thing-id-with-5-dashes_and_3_underscores:thing1 [ lightId = "1"]{
                Switch : notification [ duration = "5" ]
            }
			'''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(1)


        def thing1 = actualThings.find {
            "hue:1-thing-id-with-5-dashes_and_3_underscores:thing1".equals(it.UID.toString())
        }

        assertThat thing1, isA(Thing)
        assertThat thing1.bridgeUID, is(nullValue())
        assertThat thing1.configuration.values().size(), is(1)
        assertThat thing1.configuration.get("lightId"), is("1")
        assertThat thing1.thingTypeUID.toString(), is("hue:1-thing-id-with-5-dashes_and_3_underscores")
    }

    @Test
    void 'assert that bridge UID can be set'() {

        def things = thingRegistry.getAll()
        assertThat things.size(), is(0)

        String model =
                '''
            hue:bridge:bridge1 []
            hue:LCT001:bridge1:bulb (hue:bridge:bridge1) []
            '''

        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(2)

        Thing thing = actualThings.find { !(it instanceof Bridge) }
        Bridge bridge = actualThings.find { it instanceof Bridge }

        assertThat thing.bridgeUID.toString(), is("hue:bridge:bridge1")
        assertThat bridge.things.contains(thing), is(true)
    }

    @Test
    void 'assert that channel definitions can be referenced'() {
        def things = thingRegistry.getAll()
        assertThat things.size(), is(0)

        String model =
                '''
                    Bridge hue:bridge:bridge1 [] {
                        LCT001 bulb_default []
                        LCT001 bulb_custom [] {
                            Channels:
                                Type color : manual []
                                Type color : manualWithLabel "With Label" []
                        }
                        LCT001 bulb_broken [] {
                            Channels:
                                Type broken : manual []
                                Type broken : manualWithLabel "With Label" []
                        }
                    }

                '''

        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def List<Thing> actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(4)

        Thing thingDefault = actualThings.find { it.getUID().getId().equals("bulb_default") }
        assertThat thingDefault.getChannels().size(), is(2)

        Thing thingCustom = actualThings.find { it.getUID().getId().equals("bulb_custom") }
        assertThat thingCustom.getChannels().size(), is(4)
        assertThat thingCustom.getChannel("manual").getChannelTypeUID(), is(equalTo(new ChannelTypeUID("hue", "color")))
        assertThat thingCustom.getChannel("manual").getLabel(), is("colorLabel") // default from thing type
        assertThat thingCustom.getChannel("manualWithLabel").getLabel(), is("With Label") // manual overrides default

        Thing thingBroken = actualThings.find { it.getUID().getId().equals("bulb_broken") }
        assertThat thingBroken.getChannels().size(), is(4)
        assertThat thingBroken.getChannel("manual").getChannelTypeUID(), is(equalTo(new ChannelTypeUID("hue", "broken")))
        assertThat thingBroken.getChannel("manual").getKind(), is(ChannelKind.STATE)
        assertThat thingBroken.getChannel("manual").getAcceptedItemType(), is(nullValue())
        assertThat thingBroken.getChannel("manual").getLabel(), is(nullValue())
        assertThat thingBroken.getChannel("manualWithLabel").getLabel(), is("With Label")

    }

    @Test
    void 'assert that things can be embedded within bridges in short notation'() {
        assertThat thingRegistry.getAll().size(), is(0)

        String model =
                '''
            Bridge hue:bridge:myBridge @ "basement" [ ip = "1.2.3.4", username = "123" ] {
                LCT001 bulb1 [ lightId = "1" ] { Switch : notification }
            }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def List<Thing> actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(2)

        assertThat actualThings.find {"hue:bridge:myBridge".equals(it.UID.toString())}, is(notNullValue())
        assertThat actualThings.find {"hue:LCT001:myBridge:bulb1".equals(it.UID.toString())}, is(notNullValue())
        assertThat actualThings.find {"hue:LCT001:myBridge:bulb1".equals(it.UID.toString())}.getBridgeUID().toString(), is(equalTo("hue:bridge:myBridge"))
    }

    @Test
    void 'assert that things can be embedded within bridges in long notation'() {
        assertThat thingRegistry.getAll().size(), is(0)

        String model =
                '''
            Bridge hue:bridge:myBridge @ "basement" [ ip = "1.2.3.4", username = "123" ] {
                hue:LCT001:bulb1 [ lightId = "1" ] { Switch : notification }
            }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def List<Thing> actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(2)

        assertThat actualThings.find {"hue:bridge:myBridge".equals(it.UID.toString())}, is(notNullValue())
        assertThat actualThings.find {"hue:LCT001:bulb1".equals(it.UID.toString())}, is(notNullValue())
        assertThat actualThings.find {"hue:LCT001:bulb1".equals(it.UID.toString())}.getBridgeUID().toString(), is(equalTo("hue:bridge:myBridge"))
    }

    @Test
    void 'assert that standalone things can have bridges in long notation'() {
        assertThat thingRegistry.getAll().size(), is(0)

        String model =
                '''
            Bridge hue:bridge:myBridge @ "basement" [ ip = "1.2.3.4", username = "123" ]
            hue:LCT001:bulb1 (hue:bridge:myBridge) [ lightId = "1" ] { Switch : notification }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def List<Thing> actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(2)

        assertThat actualThings.find {"hue:bridge:myBridge".equals(it.UID.toString())}, is(notNullValue())
        assertThat actualThings.find {"hue:LCT001:bulb1".equals(it.UID.toString())}, is(notNullValue())
        assertThat actualThings.find {"hue:LCT001:bulb1".equals(it.UID.toString())}.getBridgeUID().toString(), is(equalTo("hue:bridge:myBridge"))
    }

    @Test
    void 'assert that standalone thing with a bridge works in short notation'() {
        assertThat thingRegistry.getAll().size(), is(0)

        String model =
                '''
        Bridge hue:bridge:myBridge @ "basement" [ ip = "1.2.3.4", username = "123" ]
        LCT001 bulb1 (hue:bridge:myBridge) [ lightId = "1" ] { Switch : notification }
        '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def List<Thing> actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(2)

        assertThat actualThings.find {"hue:bridge:myBridge".equals(it.UID.toString())}, is(notNullValue())
        assertThat actualThings.find {"hue:LCT001:bulb1".equals(it.UID.toString())}, is(notNullValue())
        assertThat actualThings.find {"hue:LCT001:bulb1".equals(it.UID.toString())}.getBridgeUID().toString(), is(equalTo("hue:bridge:myBridge"))
    }

    @Test
    void 'assert that standalone thing without a bridge does not work in short notation'() {
        assertThat thingRegistry.getAll().size(), is(0)

        String model =
                '''
        LCT001 bulb1 [ lightId = "1" ] { Switch : notification }
        hue:LCT001:bulb2 (hue:bridge:myBridge) [ lightId = "2" ] { Switch : notification }
        '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def List<Thing> actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(1)
        assertThat actualThings.find {"hue:LCT001:bulb2".equals(it.UID.toString())}, is(notNullValue())
    }

    @Test
    void 'assert that updating an embedded Thing causes the right behavior wrt adding and updating and no removed events are sent'() {
        List<AbstractThingRegistryEvent> receivedEvents = new ArrayList<>()
        def thingEventSubscriber = [
            receive: { event ->
                receivedEvents.add(event as AbstractThingRegistryEvent)
            },
            getSubscribedEventTypes: {
                Sets.newHashSet(ThingUpdatedEvent.TYPE, ThingRemovedEvent.TYPE, ThingAddedEvent.TYPE) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService(thingEventSubscriber)

        assertThat thingRegistry.getAll().size(), is(0)

        String model =
                '''
            Bridge hue:bridge:myBridge [ ip = "1.2.3.4", username = "123" ]  {
                LCT001 bulb1 [ lightId = "1" ] { Switch : notification }
            }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        assertThat thingRegistry.getAll().size(), is(2)
        waitForAssert {
            assertThat receivedEvents.size(), is(equalTo(2))
            receivedEvents.each { assertThat it, isA(ThingAddedEvent) }
        }
        receivedEvents.clear()

        String newModel =
                '''
            Bridge hue:bridge:myBridge [ ip = "1.2.3.4", username = "123" ]  {
                LCT001 bulb1 [ lightId = "2" ] { Switch : notification }
            }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(newModel.bytes))
        assertThat thingRegistry.getAll().size(), is(2)

        waitForAssert {
            assertThat receivedEvents.size(), is(equalTo(2))
            receivedEvents.each { assertThat it, isA(ThingUpdatedEvent) }
        }

    }

    @Test
    void 'assert that an empty model does not cause any harm'() {
        assertThat thingRegistry.getAll().size(), is(0)

        String model = ''''''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def List<Thing> actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(0)
    }

    @Test
    void 'assert that default configurations are applied to channels'() {
        assertThat thingRegistry.getAll().size(), is(0)

        String model =
                '''
            Bridge hue:bridge:myBridge @ "basement" [ ] {
                LCT001 myBulb [ lightId = "1" ] {
                    Type color : myChannel []
                }
            }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def List<Thing> actualThings = thingRegistry.getAll()

        // ensure a standard channel has its default values
        assertThat actualThings.find {"hue:LCT001:myBridge:myBulb".equals(it.UID.toString())}.getChannel("color").getConfiguration().get("defaultConfig"), is(equalTo("defaultValue"))

        // ensure a user-defined channel has its default values
        assertThat actualThings.find {"hue:LCT001:myBridge:myBulb".equals(it.UID.toString())}.getChannel("myChannel").getConfiguration().get("defaultConfig"), is(equalTo("defaultValue"))
    }

    @Test
    void 'assert that channels within channel groups can be overridden'() {
        assertThat thingRegistry.getAll().size(), is(0)

        String model =
                '''
                Bridge hue:bridge:myBridge [] {
                    Thing grouped myGroupedThing [] {
                        Type color : group#bar [
                            myProp="successful",
                            customConfig="yes"
                        ]
                    }
                }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def List<Thing> actualThings = thingRegistry.getAll()

        assertThat actualThings.size(), is(equalTo(2))

        // ensure the non-declared channel is there and has its default properties
        assertThat actualThings.find {"hue:grouped:myBridge:myGroupedThing".equals(it.UID.toString())}.getChannel("group#foo"), is(notNullValue())
        assertThat actualThings.find {"hue:grouped:myBridge:myGroupedThing".equals(it.UID.toString())}.getChannel("group#foo").getConfiguration().get("defaultConfig"), is(equalTo("defaultValue"))
        assertThat actualThings.find {"hue:grouped:myBridge:myGroupedThing".equals(it.UID.toString())}.getChannel("group#foo").getConfiguration().get("customConfig"), is(equalTo("none"))

        assertThat actualThings.find {"hue:grouped:myBridge:myGroupedThing".equals(it.UID.toString())}.getChannel("group#bar"), is(notNullValue())
        // ensure the non-declared default property is there
        assertThat actualThings.find {"hue:grouped:myBridge:myGroupedThing".equals(it.UID.toString())}.getChannel("group#bar").getConfiguration().get("defaultConfig"), is(equalTo("defaultValue"))
        // ensure overriding a default property worked
        assertThat actualThings.find {"hue:grouped:myBridge:myGroupedThing".equals(it.UID.toString())}.getChannel("group#bar").getConfiguration().get("customConfig"), is(equalTo("yes"))
        // ensure an additional property can be set
        assertThat actualThings.find {"hue:grouped:myBridge:myGroupedThing".equals(it.UID.toString())}.getChannel("group#bar").getConfiguration().get("myProp"), is(equalTo("successful"))
    }


}
