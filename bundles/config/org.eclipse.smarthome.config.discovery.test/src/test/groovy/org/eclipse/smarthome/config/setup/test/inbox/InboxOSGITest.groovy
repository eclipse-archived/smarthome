/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.inbox

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria
import org.eclipse.smarthome.config.discovery.inbox.InboxListener
import org.eclipse.smarthome.config.discovery.inbox.events.InboxAddedEvent
import org.eclipse.smarthome.config.discovery.inbox.events.InboxRemovedEvent
import org.eclipse.smarthome.config.discovery.inbox.events.InboxUpdatedEvent
import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets


class InboxOSGITest extends OSGiTest {

    def DEFAULT_TTL = 60

    Inbox inbox
    DiscoveryServiceRegistry discoveryServiceRegistry
    ManagedThingProvider managedThingProvider

    Map<ThingUID, DiscoveryResult> discoveryResults = [:]
    List<InboxListener> inboxListeners = new ArrayList<>()


    @Before
    void setUp() {
        registerVolatileStorageService()

        discoveryResults.clear()
        inboxListeners.clear()

        inbox = getService Inbox
        discoveryServiceRegistry = getService DiscoveryServiceRegistry
        managedThingProvider = getService ManagedThingProvider
    }

    @After
    void cleanUp() {
        discoveryResults.each {
            inbox.remove(it.key)
        }
        inboxListeners.each { inbox.removeInboxListener(it) }
        discoveryResults.clear()
        inboxListeners.clear()
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
        assertIncludesAll([
            discoveryResult4
        ], discoveryResults)

        // Filter by thingType and DiscoveryResultFlag
        discoveryResults = inbox.get(new InboxFilterCriteria(thingTypeUID, DiscoveryResultFlag.IGNORED))
        assertIncludesAll([
            discoveryResult2
        ], discoveryResults)

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
}
