/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.inbox

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria
import org.eclipse.smarthome.config.discovery.inbox.InboxListener
import org.eclipse.smarthome.config.discovery.inbox.events.InboxAddedEvent
import org.eclipse.smarthome.config.discovery.inbox.events.InboxRemovedEvent
import org.eclipse.smarthome.config.discovery.inbox.events.InboxUpdatedEvent
import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl
import org.eclipse.smarthome.config.discovery.internal.PersistentInbox
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext

import com.google.common.collect.Sets

class InboxOSGITest extends OSGiTest {

    class DiscoveryService1 extends AbstractDiscoveryService {
        public DiscoveryService1() {
            super(5)
        }

        @Override
        protected void startScan() {
        }
    }
    class DiscoveryService2 extends AbstractDiscoveryService {
        public DiscoveryService2() {
            super(5)
        }

        @Override
        protected void startScan() {
        }
    }

    def discoveryService1 = [] as DiscoveryService1
    def discoveryService2 = [] as DiscoveryService2

    def DEFAULT_TTL = 60
    def BRIDGE_ID = new ThingUID("bindingId:bridge:bridgeId")

    DiscoveryResult BRIDGE = new DiscoveryResultImpl(BRIDGE_ID, null, null,"Bridge", "bridge", DEFAULT_TTL)
    DiscoveryResult THING1_WITH_BRIDGE = new DiscoveryResultImpl(new ThingUID("bindingId:thing:id1"), BRIDGE_ID, null,"Thing1", "thing1", DEFAULT_TTL)
    DiscoveryResult THING2_WITH_BRIDGE = new DiscoveryResultImpl(new ThingUID("bindingId:thing:id2"), BRIDGE_ID, null,"Thing2", "thing2", DEFAULT_TTL)
    DiscoveryResult THING_WITHOUT_BRIDGE = new DiscoveryResultImpl(new ThingUID("bindingId:thing:id3"), null, null,"Thing3", "thing3", DEFAULT_TTL)
    DiscoveryResult THING_WITH_OTHER_BRIDGE = new DiscoveryResultImpl(new ThingUID("bindingId:thing:id4"), new ThingUID("bindingId:thing:id5"),null,"Thing4", "thing4", DEFAULT_TTL)

    final List<DiscoveryResult> inboxContent = []
    final URI testURI = new URI("http:dummy")
    final String testThingLabel = "dummy_thing"
    final ThingUID testUID = new ThingUID("binding:type:id")
    final ThingTypeUID testTypeUID = new ThingTypeUID("binding:type")
    final Thing testThing = ThingBuilder.create(testUID).build()
    final String discoveryResultLabel = "MyLabel"
    final Map<String, Object> discoveryResultProperties =
    ["ip":"192.168.3.99",
        "pnr": 1234455,
        "snr":12345,
        "manufacturer":"huawei",
        "manufactured":new Date(12344)
    ]
    final DiscoveryResult testDiscoveryResult = DiscoveryResultBuilder.create(testThing.getUID()).withProperties(discoveryResultProperties).withLabel(discoveryResultLabel).build()
    final ThingType testThingType = ThingTypeBuilder.instance(testTypeUID, "label").withConfigDescriptionURI(testURI).build();
    final ConfigDescriptionParameter[] configDescriptionParameter = [
        [
            discoveryResultProperties.keySet().getAt(0),
            Type.TEXT
        ],
        [
            discoveryResultProperties.keySet().getAt(1),
            Type.INTEGER
        ]
    ]
    final ConfigDescription testConfigDescription  = new ConfigDescription(testURI, Arrays.asList(configDescriptionParameter))
    final String[] keysInConfigDescription = [
        discoveryResultProperties.keySet().getAt(0),
        discoveryResultProperties.keySet().getAt(1)
    ]
    final String[] keysNotInConfigDescription = [
        discoveryResultProperties.keySet().getAt(2),
        discoveryResultProperties.keySet().getAt(3),
        discoveryResultProperties.keySet().getAt(4)
    ]
    final Map<ThingUID, DiscoveryResult> discoveryResults = [:]
    final List<InboxListener> inboxListeners = new ArrayList<>()

    Inbox inbox
    DiscoveryServiceRegistry discoveryServiceRegistry
    ManagedThingProvider managedThingProvider
    ThingRegistry registry
    ThingTypeRegistry typeRegistry;
    ConfigDescriptionRegistry descriptionRegistry;
    ThingTypeRegistry thingTypeRegistry = new ThingTypeRegistry()
    ConfigDescriptionRegistry configDescRegistry

    @Before
    void setUp() {
        registerVolatileStorageService()
        discoveryResults.clear()
        inboxListeners.clear()
        inbox = getService Inbox
        discoveryServiceRegistry = getService DiscoveryServiceRegistry
        managedThingProvider = getService ManagedThingProvider
        registry = getService ThingRegistry
        typeRegistry = getService ThingTypeRegistry
        descriptionRegistry = getService ConfigDescriptionRegistry
        def componentContextMock = [
            getBundleContext: { getBundleContext() }
        ] as ComponentContext
        ((PersistentInbox)inbox).addThingHandlerFactory(new DummyThingHandlerFactory(componentContextMock))
    }

    @After
    void cleanUp() {
        discoveryResults.each {
            inbox.remove(it.key)
        }
        inboxListeners.each { inbox.removeInboxListener(it) }
        discoveryResults.clear()
        inboxListeners.clear()
        registry.remove(BRIDGE_ID)
        managedThingProvider.all.each {
            managedThingProvider.remove(it.getUID())
        }
    }

    private boolean addDiscoveryResult(DiscoveryResult discoveryResult) {
        boolean result = inbox.add(discoveryResult)
        if (result) {
            discoveryResults.put(discoveryResult.thingUID, discoveryResult)
        }
        result
    }

    private boolean removeDiscoveryResult(ThingUID thingUID) {
        boolean result = inbox.remove(thingUID)
        if (result) {
            discoveryResults.remove(thingUID)
        }
        result
    }

    private void addInboxListener(InboxListener inboxListener) {
        inbox.addInboxListener(inboxListener)
        // TODO: the test fails if this line is used
        //        inboxListeners.add(inboxListener)
    }

    private void removeInboxListener(InboxListener inboxListener) {
        inbox.removeInboxListener(inboxListener)
        // TODO: the test fails if this line is used
        //        inboxListeners.remove(inboxListener)
    }

    @Test
    void 'assert that getAll includes previously added DiscoveryResult'() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "thingId")

        List<DiscoveryResult> allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)

        Map<String, Object> props = new HashMap<>()
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property1", "DummyLabel1", DEFAULT_TTL)

        assertTrue addDiscoveryResult(discoveryResult)

        allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(1)
        DiscoveryResult actualDiscoveryResult = allDiscoveryResults.first()
        actualDiscoveryResult.with {
            assertThat it.thingUID, is(thingUID)
            assertThat it.thingTypeUID, is (thingTypeUID)
            assertThat bindingId, is ("dummyBindingId")
            assertThat flag, is(DiscoveryResultFlag.NEW)
            assertThat label, is ("DummyLabel1")
            assertThat properties.size(), is(2)
            assertThat properties.get("property1"), is("property1value1")
            assertThat properties.get("property2"), is("property2value1")
            assertThat representationProperty, is("property1")
        }
    }

    @Test
    void 'assert that getAll includes previously updated DiscoveryResult'() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        List<DiscoveryResult> allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)

        Map<String, Object> props = new HashMap<>()
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property1", "DummyLabel1", DEFAULT_TTL)
        assertTrue addDiscoveryResult(discoveryResult)

        props.clear()
        props.put("property2", "property2value2")
        props.put("property3", "property3value1")

        discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property3", "DummyLabel2", DEFAULT_TTL)

        assertTrue addDiscoveryResult(discoveryResult)

        allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(1)
        DiscoveryResult actualDiscoveryResult = allDiscoveryResults.first()
        actualDiscoveryResult.with {
            assertThat it.thingUID, is(thingUID)
            assertThat it.thingTypeUID, is (thingTypeUID)
            assertThat bindingId, is ("dummyBindingId")
            assertThat flag, is(DiscoveryResultFlag.NEW)
            assertThat label, is ("DummyLabel2")
            assertThat properties.size(), is(2)
            assertThat properties.get("property2"), is("property2value2")
            assertThat properties.get("property3"), is("property3value1")
            assertThat representationProperty, is("property3")
        }
    }

    @Test
    void 'assert that getAll includes two previously added DiscoveryResults'() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        List<DiscoveryResult> allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, null, null, "DummyLabel1", DEFAULT_TTL)

        assertTrue addDiscoveryResult(discoveryResult)

        ThingUID thingUID2 = new ThingUID(thingTypeUID, "dummyThingId2")
        discoveryResult = new DiscoveryResultImpl(thingUID2, null, null, null, "DummyLabel2", DEFAULT_TTL)

        addDiscoveryResult(discoveryResult)

        allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(2)
    }

    @Test
    void 'assert that getAll not includes removed DiscoveryResult'() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        List<DiscoveryResult> allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)

        Map<String, Object> props = new HashMap<>()
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property1", "DummyLabel1", DEFAULT_TTL)
        assertTrue addDiscoveryResult(discoveryResult)

        allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(1)

        assertTrue removeDiscoveryResult(thingUID)

        allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)
    }

    @Test
    void 'assert that getAll includes removed updated DiscoveryResult'() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        List<DiscoveryResult> allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)

        Map<String, Object> props = new HashMap<>()
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property1", "DummyLabel1", DEFAULT_TTL)
        assertTrue addDiscoveryResult(discoveryResult)

        allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(1)

        props.clear()
        props.put("property2", "property2value2")
        props.put("property3", "property3value1")

        DiscoveryResult discoveryResultUpdate = new DiscoveryResultImpl(thingUID, null, props, "property3", "DummyLabel2", DEFAULT_TTL)

        assertTrue addDiscoveryResult(discoveryResultUpdate)

        allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(1)

        assertTrue removeDiscoveryResult(thingUID)

        allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)
    }

    @Test
    void 'assert that get with InboxFilterCriteria returns correct results'() {
        List<DiscoveryResult> allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)

        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        DiscoveryResult discoveryResult1 = new DiscoveryResultImpl(thingUID, null, null, null, "DummyLabel1", DEFAULT_TTL)
        assertTrue addDiscoveryResult(discoveryResult1)

        def thingUID2 = new ThingUID(thingTypeUID, "dummyThingId2")
        DiscoveryResult discoveryResult2 = new DiscoveryResultImpl(thingUID2, null, null, null, "DummyLabel2", DEFAULT_TTL)
        assertTrue addDiscoveryResult(discoveryResult2)

        inbox.setFlag(thingUID2, DiscoveryResultFlag.IGNORED)

        def thingTypeUID3 = new ThingTypeUID("dummyBindingId", "dummyThingType3")
        DiscoveryResult discoveryResult3 = new DiscoveryResultImpl(new ThingUID(thingTypeUID3, "dummyThingId3"), null, null, null, "DummyLabel3", DEFAULT_TTL)
        assertTrue addDiscoveryResult(discoveryResult3)

        DiscoveryResult discoveryResult4 = new DiscoveryResultImpl(new ThingUID(thingTypeUID, "dummyThingId4"), null, null, null, "DummyLabel4", DEFAULT_TTL)

        assertTrue addDiscoveryResult(discoveryResult4)


        allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(4)

        List<DiscoveryResult> discoveryResults = inbox.get(null)
        assertIncludesAll([
            discoveryResult1,
            discoveryResult2,
            discoveryResult3,
            discoveryResult4
        ], discoveryResults)

        // Filter by nothing
        discoveryResults = inbox.get(new InboxFilterCriteria(null, null))
        assertIncludesAll([
            discoveryResult1,
            discoveryResult2,
            discoveryResult3,
            discoveryResult4
        ], discoveryResults)

        // Filter by thingType
        discoveryResults = inbox.get(new InboxFilterCriteria(thingTypeUID, null))
        assertIncludesAll([
            discoveryResult1,
            discoveryResult2,
            discoveryResult4
        ], discoveryResults)

        // Filter by bindingId
        discoveryResults = inbox.get(new InboxFilterCriteria("dummyBindingId", null))
        assertIncludesAll([
            discoveryResult1,
            discoveryResult2,
            discoveryResult3,
            discoveryResult4
        ], discoveryResults)

        // Filter by DiscoveryResultFlag
        discoveryResults = inbox.get(new InboxFilterCriteria((String)null, DiscoveryResultFlag.NEW))
        assertIncludesAll([
            discoveryResult1,
            discoveryResult3,
            discoveryResult4
        ], discoveryResults)

        // Filter by thingId
        discoveryResults = inbox.get(new InboxFilterCriteria(new ThingUID(thingTypeUID, "dummyThingId4"), null))
        assertIncludesAll([discoveryResult4], discoveryResults)

        // Filter by thingType and DiscoveryResultFlag
        discoveryResults = inbox.get(new InboxFilterCriteria(thingTypeUID, DiscoveryResultFlag.IGNORED))
        assertIncludesAll([discoveryResult2], discoveryResults)

        // Filter by bindingId and DiscoveryResultFlag
        discoveryResults = inbox.get(new InboxFilterCriteria("dummyBindingId", DiscoveryResultFlag.NEW))
        assertIncludesAll([
            discoveryResult1,
            discoveryResult3,
            discoveryResult4
        ], discoveryResults)
    }

    @Test
    void 'assert that InboxListener is notified about previously added DiscoveryResult'() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        List<DiscoveryResult> allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)

        Map<String, Object> props = new HashMap<>()
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property1", "DummyLabel1", DEFAULT_TTL)

        AsyncResultWrapper<DiscoveryResult> addedDiscoveryResultWrapper = new AsyncResultWrapper<DiscoveryResult>()
        AsyncResultWrapper<DiscoveryResult> updatedDiscoveryResultWrapper = new AsyncResultWrapper<DiscoveryResult>()
        AsyncResultWrapper<DiscoveryResult> removedDiscoveryResultWrapper = new AsyncResultWrapper<DiscoveryResult>()

        addInboxListener( [
            'thingAdded' : { Inbox source, DiscoveryResult result ->
                if (source == inbox) {
                    addedDiscoveryResultWrapper.set(result)
                }
            },
            'thingUpdated' : { Inbox source, DiscoveryResult result ->
                if (source == inbox) {
                    updatedDiscoveryResultWrapper.set(result)
                }
            },
            'thingRemoved' : { Inbox source, DiscoveryResult result ->
                if (source == inbox) {
                    removedDiscoveryResultWrapper.set(result)
                }
            }
        ] as InboxListener)

        assertTrue addDiscoveryResult(discoveryResult)

        waitForAssert{ assertTrue addedDiscoveryResultWrapper.isSet }

        assertFalse updatedDiscoveryResultWrapper.isSet
        assertFalse removedDiscoveryResultWrapper.isSet

        DiscoveryResult actualDiscoveryResult = addedDiscoveryResultWrapper.wrappedObject

        actualDiscoveryResult.with {
            assertThat it.thingUID, is(thingUID)
            assertThat it.thingTypeUID, is (thingTypeUID)
            assertThat bindingId, is ("dummyBindingId")
            assertThat flag, is(DiscoveryResultFlag.NEW)
            assertThat label, is ("DummyLabel1")
            assertThat properties.size(), is(2)
            assertThat properties.get("property1"), is("property1value1")
            assertThat properties.get("property2"), is("property2value1")
            assertThat representationProperty, is("property1")
        }
    }

    @Test
    void 'assert that InboxListener is notified about previously updated DiscoveryResult'() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        List<DiscoveryResult> allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)

        Map<String, Object> props = new HashMap<>();
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property1", "DummyLabel1", DEFAULT_TTL)
        assertTrue addDiscoveryResult(discoveryResult)

        props.clear()
        props.put("property2", "property2value2")
        props.put("property3", "property3value1")

        discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property3", "DummyLabel2", DEFAULT_TTL)

        AsyncResultWrapper<DiscoveryResult> addedDiscoveryResultWrapper = new AsyncResultWrapper<DiscoveryResult>()
        AsyncResultWrapper<DiscoveryResult> updatedDiscoveryResultWrapper = new AsyncResultWrapper<DiscoveryResult>()
        AsyncResultWrapper<DiscoveryResult> removedDiscoveryResultWrapper = new AsyncResultWrapper<DiscoveryResult>()

        addInboxListener( [
            'thingAdded' : { Inbox source, DiscoveryResult result ->
                if (source == inbox) {
                    addedDiscoveryResultWrapper.set(result)
                }
            },
            'thingUpdated' : { Inbox source, DiscoveryResult result ->
                if (source == inbox) {
                    updatedDiscoveryResultWrapper.set(result)
                }
            },
            'thingRemoved' : { Inbox source, DiscoveryResult result ->
                if (source == inbox) {
                    removedDiscoveryResultWrapper.set(result)
                }
            }
        ] as InboxListener)

        assertTrue addDiscoveryResult(discoveryResult)
        waitForAssert{ assertTrue updatedDiscoveryResultWrapper.isSet }

        assertFalse addedDiscoveryResultWrapper.isSet
        assertFalse removedDiscoveryResultWrapper.isSet

        DiscoveryResult actualDiscoveryResult = updatedDiscoveryResultWrapper.wrappedObject

        actualDiscoveryResult.with {
            assertThat it.thingUID, is(thingUID)
            assertThat it.thingTypeUID, is (thingTypeUID)
            assertThat bindingId, is ("dummyBindingId")
            assertThat flag, is(DiscoveryResultFlag.NEW)
            assertThat label, is ("DummyLabel2")
            assertThat properties.size(), is(2)
            assertThat properties.get("property2"), is("property2value2")
            assertThat properties.get("property3"), is("property3value1")
            assertThat representationProperty, is("property3")
        }
    }


    @Test
    void 'assert that InboxListener is notified about previously removed DiscoveryResult'() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        List<DiscoveryResult> allDiscoveryResults = inbox.all
        assertThat allDiscoveryResults.size(), is(0)

        Map<String, Object> props = new HashMap<>()
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property1", "DummyLabel1", DEFAULT_TTL)
        assertTrue addDiscoveryResult(discoveryResult)

        AsyncResultWrapper<DiscoveryResult> addedDiscoveryResultWrapper = new AsyncResultWrapper<DiscoveryResult>()
        AsyncResultWrapper<DiscoveryResult> updatedDiscoveryResultWrapper = new AsyncResultWrapper<DiscoveryResult>()
        AsyncResultWrapper<DiscoveryResult> removedDiscoveryResultWrapper = new AsyncResultWrapper<DiscoveryResult>()

        addInboxListener( [
            'thingAdded' : { Inbox source, DiscoveryResult result ->
                if (source == inbox) {
                    addedDiscoveryResultWrapper.set(result)
                }
            },
            'thingUpdated' : { Inbox source, DiscoveryResult result ->
                if (source == inbox) {
                    updatedDiscoveryResultWrapper.set(result)
                }
            },
            'thingRemoved' : { Inbox source, DiscoveryResult result ->
                if (source == inbox) {
                    removedDiscoveryResultWrapper.set(result)
                }
            }
        ] as InboxListener)

        assertTrue removeDiscoveryResult(thingUID)

        waitForAssert{ assertTrue removedDiscoveryResultWrapper.isSet }

        assertFalse updatedDiscoveryResultWrapper.isSet
        assertFalse addedDiscoveryResultWrapper.isSet

        DiscoveryResult actualDiscoveryResult = removedDiscoveryResultWrapper.wrappedObject

        actualDiscoveryResult.with {
            assertThat it.thingUID, is(thingUID)
            assertThat it.thingTypeUID, is (thingTypeUID)
            assertThat bindingId, is ("dummyBindingId")
            assertThat flag, is(DiscoveryResultFlag.NEW)
            assertThat label, is ("DummyLabel1")
            assertThat properties.size(), is(2)
            assertThat properties.get("property1"), is("property1value1")
            assertThat properties.get("property2"), is("property2value1")
            assertThat representationProperty, is("property1")
        }
    }

    @Test
    void 'assert that DiscoveryResult is removed when thing is added to ThingRegistry'() {
        assertThat inbox.getAll().size(), is(0)

        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        Map<String, Object> props = new HashMap<>()
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, props, "property1", "DummyLabel1", DEFAULT_TTL)

        inbox.add discoveryResult

        assertThat inbox.getAll().size(), is(1)

        managedThingProvider.add ThingBuilder.create(thingTypeUID, "dummyThingId").build()

        assertThat inbox.getAll().size(), is(0)
    }

    @Test
    void 'assert that DiscoveryResult is not added to Inbox when thing with same UID exists'() {
        assertThat inbox.getAll().size(), is(0)

        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        managedThingProvider.add ThingBuilder.create(thingTypeUID, "dummyThingId").build()

        Map<String, Object> props = new HashMap<>()
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, null, null, "DummyLabel1", DEFAULT_TTL)

        inbox.add discoveryResult

        assertThat inbox.getAll().size(), is(0)
    }

    @Test
    void 'assert that DiscoveryResult is added to Inbox when thing with different UID exists'() {
        assertThat inbox.getAll().size(), is(0)

        ThingTypeUID thingTypeUID = new ThingTypeUID("dummyBindingId2", "dummyThingType")
        ThingUID thingUID = new ThingUID(thingTypeUID, "dummyThingId")

        managedThingProvider.add ThingBuilder.create(thingTypeUID, "dummyThingId").build()

        Map<String, Object> props = new HashMap<>()
        props.put("property1", "property1value1")
        props.put("property2", "property2value1")

        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, null, null, "DummyLabel1", DEFAULT_TTL)

        inbox.add discoveryResult

        assertThat inbox.getAll().size(), is(0)
    }

    void assertIncludesAll(List<DiscoveryResult> expectedList, List<DiscoveryResult> actualList) {
        assertThat actualList.size(), is (expectedList.size())
        expectedList.each {
            assertTrue actualList.contains(it)
        }
    }

    @Test
    void 'assert that InboxEventSubscribers receive events about discovery result changes'() {
        def thingUID = new ThingUID("some:thing:uid")
        def receivedEvent = null
        def inboxEventSubscriber = [
            receive: { event -> receivedEvent = event },
            getSubscribedEventTypes: { Sets.newHashSet(InboxAddedEvent.TYPE, InboxRemovedEvent.TYPE, InboxUpdatedEvent.TYPE) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService inboxEventSubscriber

        // add discovery result
        DiscoveryResult discoveryResult = new DiscoveryResultImpl(thingUID, null, null, null, null, DEFAULT_TTL)
        addDiscoveryResult(discoveryResult)
        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent, is(instanceOf(InboxAddedEvent))
        receivedEvent = null

        // update discovery result
        discoveryResult = new DiscoveryResultImpl(thingUID, null, null, null, null, DEFAULT_TTL)
        addDiscoveryResult(discoveryResult)
        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent, is(instanceOf(InboxUpdatedEvent))
        receivedEvent = null

        // remove discovery result
        removeDiscoveryResult(thingUID)
        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent, is(instanceOf(InboxRemovedEvent))
    }

    @Test
    void 'assert that remove removes associated DiscoveryResults from Inbox when Bridge is removed'() {
        def receivedEvents = new ArrayList()
        def inboxEventSubscriber = [
            receive: { event -> receivedEvents.add(event) },
            getSubscribedEventTypes: { Sets.newHashSet(InboxAddedEvent.TYPE, InboxRemovedEvent.TYPE, InboxUpdatedEvent.TYPE) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService inboxEventSubscriber
        inbox.add(BRIDGE)
        inbox.add(THING1_WITH_BRIDGE)
        inbox.add(THING2_WITH_BRIDGE)
        inbox.add(THING_WITHOUT_BRIDGE)
        inbox.add(THING_WITH_OTHER_BRIDGE)
        waitForAssert {
            assertThat receivedEvents.size(), is(5)
        }
        receivedEvents.clear()

        assertTrue inbox.remove(BRIDGE.thingUID)
        assertTrue inbox.get(new InboxFilterCriteria(BRIDGE.thingUID, DiscoveryResultFlag.NEW)).isEmpty()
        assertTrue inbox.get(new InboxFilterCriteria(THING1_WITH_BRIDGE.thingUID, DiscoveryResultFlag.NEW)).isEmpty()
        assertTrue inbox.get(new InboxFilterCriteria(THING2_WITH_BRIDGE.thingUID, DiscoveryResultFlag.NEW)).isEmpty()
        assertThat inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.NEW)), hasItems(THING_WITHOUT_BRIDGE,THING_WITH_OTHER_BRIDGE)
        waitForAssert {
            assertThat receivedEvents.size(), is(3)
            receivedEvents.each{
                assertThat it, is(instanceOf(InboxRemovedEvent))
            }
        }
    }

    @Test
    void 'assert that remove leaves associated DiscoveryResults in Inbox when Bridge is added to ThingRegistry'() {
        def receivedEvents = new ArrayList()
        def inboxEventSubscriber = [
            receive: { event -> receivedEvents.add(event) },
            getSubscribedEventTypes: { Sets.newHashSet(InboxAddedEvent.TYPE, InboxRemovedEvent.TYPE, InboxUpdatedEvent.TYPE) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService inboxEventSubscriber
        inbox.add(BRIDGE)
        inbox.add(THING1_WITH_BRIDGE)
        inbox.add(THING2_WITH_BRIDGE)
        inbox.add(THING_WITHOUT_BRIDGE)
        waitForAssert {
            assertThat receivedEvents.size(), is(4)
        }
        receivedEvents.clear()

        registry.add(BridgeBuilder.create(BRIDGE.thingUID).build())
        assertTrue inbox.get(new InboxFilterCriteria(BRIDGE.thingUID, DiscoveryResultFlag.NEW)).isEmpty()
        assertThat inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.NEW)), hasItems(THING1_WITH_BRIDGE,THING2_WITH_BRIDGE,THING_WITHOUT_BRIDGE)
        waitForAssert {
            assertThat receivedEvents.size(), is(1)
            receivedEvents.each{
                assertThat it, is(instanceOf(InboxRemovedEvent))
            }
        }
    }

    @Test
    void 'assert that removing a bridge Thing from the registry removes its discovered child Things from the inbox'() {
        def receivedEvents = new ArrayList()
        def inboxEventSubscriber = [
            receive: { event -> receivedEvents.add(event) },
            getSubscribedEventTypes: { Sets.newHashSet(InboxAddedEvent.TYPE, InboxRemovedEvent.TYPE, InboxUpdatedEvent.TYPE) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService inboxEventSubscriber

        registry.add(BridgeBuilder.create(BRIDGE.thingUID).build())

        inbox.add(THING1_WITH_BRIDGE)
        inbox.add(THING2_WITH_BRIDGE)
        inbox.add(THING_WITHOUT_BRIDGE)
        inbox.add(THING_WITH_OTHER_BRIDGE)
        assertThat inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.NEW)), hasItems(THING1_WITH_BRIDGE,THING2_WITH_BRIDGE,THING_WITHOUT_BRIDGE, THING_WITH_OTHER_BRIDGE)


        registry.forceRemove(BRIDGE.thingUID)

        waitForAssert({
            assertTrue inbox.get(new InboxFilterCriteria(THING1_WITH_BRIDGE.thingUID, DiscoveryResultFlag.NEW)).isEmpty()
            assertTrue inbox.get(new InboxFilterCriteria(THING2_WITH_BRIDGE.thingUID, DiscoveryResultFlag.NEW)).isEmpty()
            assertThat inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.NEW)), hasItems(THING_WITHOUT_BRIDGE,THING_WITH_OTHER_BRIDGE)
        })
    }

    @Test(expected=IllegalArgumentException.class)
    void 'assert that approve throw IllegalArgumentException if thingUID is null'(){
        inbox.approve(null, "label")
    }

    @Test(expected=IllegalArgumentException.class)
    void 'assert that approve throw IllegalArgumentException if no DiscoveryResult for given thingUID is available'(){
        inbox.approve(new ThingUID("1234"), "label")
    }

    @Test
    void 'assert that approve adds all properties of DiscoveryResult to Thing properties if no ConfigDescriptionParameters for the ThingType are available' () {
        inbox.add(testDiscoveryResult)
        Thing approvedThing = inbox.approve(testThing.getUID(), testThingLabel)
        Thing addedThing = registry.get(testThing.getUID())

        assertFalse addedThing == null
        assertFalse approvedThing == null
        assertTrue approvedThing.equals(addedThing)
        discoveryResultProperties.keySet().each{
            String thingProperty = addedThing.getProperties().get(it)
            String descResultParam = String.valueOf(discoveryResultProperties.get(it))
            assertFalse thingProperty == null
            assertFalse descResultParam == null
            assertTrue thingProperty.equals(descResultParam)
        }
    }

    @Test
    void 'assert that approve sets the explicitly given label' () {
        inbox.add(testDiscoveryResult)
        Thing approvedThing = inbox.approve(testThing.getUID(), testThingLabel)
        Thing addedThing = registry.get(testThing.getUID())

        assertThat approvedThing.getLabel(), is(testThingLabel)
        assertThat addedThing.getLabel(), is(testThingLabel)
    }

    @Test
    void 'assert that approve sets the discovered label if no other is given' () {
        inbox.add(testDiscoveryResult)
        Thing approvedThing = inbox.approve(testThing.getUID(), null)
        Thing addedThing = registry.get(testThing.getUID())

        assertThat approvedThing.getLabel(), is(discoveryResultLabel)
        assertThat addedThing.getLabel(), is(discoveryResultLabel)
    }

    @Test
    void 'assert that approve adds properties of DiscoveryResult which are ConfigDescriptionParameters as Thing Configuration properties and properties which are no ConfigDescriptionParameters as Thing properties'() {
        inbox.add(testDiscoveryResult)
        thingTypeRegistry = new ThingTypeRegistry() {
                    @Override
                    public ThingType getThingType(ThingTypeUID thingTypeUID) {
                        return testThingType
                    }
                }
        ((PersistentInbox)inbox).setThingTypeRegistry(thingTypeRegistry)
        configDescRegistry = new ConfigDescriptionRegistry() {
                    @Override
                    public ConfigDescription getConfigDescription(URI uri) {
                        return testConfigDescription
                    }
                }
        ((PersistentInbox)inbox).setConfigDescriptionRegistry(configDescRegistry)
        Thing approvedThing = inbox.approve(testThing.getUID(), testThingLabel)
        Thing addedThing = registry.get(testThing.getUID())
        assertTrue approvedThing.equals(addedThing)
        assertFalse addedThing == null
        keysInConfigDescription.each {
            Object thingConfItem = addedThing.getConfiguration().get(it)
            Object descResultParam = discoveryResultProperties.get(it)
            if(descResultParam instanceof Number){
                descResultParam = new BigDecimal(descResultParam.toString())
            }
            assertFalse thingConfItem == null
            assertFalse descResultParam == null
            assertTrue thingConfItem.equals(descResultParam)
        }
        keysNotInConfigDescription.each {
            String thingProperty = addedThing.getProperties().get(it)
            String descResultParam = String.valueOf(discoveryResultProperties.get(it))
            assertFalse thingProperty == null
            assertFalse descResultParam == null
            assertTrue thingProperty.equals(descResultParam)
        }
    }

    @Test
    void 'assert that removeOlderResults only removes results from the same discovery service'() {
        inbox.thingDiscovered discoveryService1, testDiscoveryResult
        long now = new Date().getTime() + 1
        assertThat inbox.getAll().size(), is(1)

        // should not remove a result
        inbox.removeOlderResults(discoveryService2, now, [testThingType.getUID()])
        assertThat inbox.getAll().size(), is(1)

        // should remove a result
        inbox.removeOlderResults(discoveryService1, now, [testThingType.getUID()])
        assertThat inbox.getAll().size(), is(0)
    }

    @Test
    void 'assert that removeOlderResults removes results without a source'() {
        inbox.add testDiscoveryResult
        long now = new Date().getTime() + 1
        assertThat inbox.getAll().size(), is(1)

        // should remove a result
        inbox.removeOlderResults(discoveryService2, now, [testThingType.getUID()])
        assertThat inbox.getAll().size(), is(0)
    }

    class DummyThingHandlerFactory extends BaseThingHandlerFactory {

        public DummyThingHandlerFactory(ComponentContext context) {
            super.activate(context);
        }

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            return true;
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return null;
        }

        @Override
        public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID, ThingUID bridgeUID) {
            return ThingBuilder.create(thingUID).withBridge(bridgeUID).withConfiguration(configuration).build()
        }
    }


}