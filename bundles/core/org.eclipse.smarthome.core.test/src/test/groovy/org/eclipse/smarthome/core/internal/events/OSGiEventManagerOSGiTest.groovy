package org.eclipse.smarthome.core.internal.events;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventFactory
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.events.TopicEventFilter
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.ServiceRegistration

import com.google.common.collect.Sets


/**
 * The {@link OSGiEventManagerOSGiTest} runs inside an OSGi container and tests the {@link OSGiEventManager}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
class OSGiEventManagerOSGiTest extends OSGiTest {

    Map<String, ServiceRegistration<Object>> serviceRegistrations =  new HashMap<String, ServiceRegistration<Object>>()

    EventPublisher eventPublisher

    String EVENT_TYPE_A = "EVENT_TYPE_A"

    String EVENT_TYPE_B = "EVENT_TYPE_B"

    String EVENT_TYPE_C = "EVENT_TYPE_C"

    Event receivedEvent_TypeBasedSubscriber1

    Event receivedEvent_TypeBasedSubscriber2

    Event receivedEvent_TopicBasedSubscriber3

    Event receivedEvent_allEventTypesSubscriber4

    @Before
    public void setUp() {
        resetReceivedEvents()
        eventPublisher = getService(EventPublisher)

        def eventTypeFactoryAB = [
            createEvent: { eventType, topic, payload, source ->
                [ getType: {eventType}, getTopic: {topic}, getPayload: {payload}, getSource: {source} ] as Event
            },
            getSupportedEventTypes: { Sets.newHashSet(EVENT_TYPE_A, EVENT_TYPE_B) }
        ] as EventFactory
        registerService("EVENT_TYPE_FACTORY_A_B", EventFactory, eventTypeFactoryAB)

        def eventTypeFactoryC = [
            createEvent: { eventType, topic, payload, source ->
                [ getType: {eventType}, getTopic: {topic}, getPayload: {payload}, getSource: {source} ] as Event
            },
            getSupportedEventTypes: { Sets.newHashSet(EVENT_TYPE_C) }
        ] as EventFactory
        registerService("EVENT_TYPE_FACTORY_C", EventFactory, eventTypeFactoryC)

        def typeBasedSubscriber1 = [
            receive: { event -> receivedEvent_TypeBasedSubscriber1 = event },
            getSubscribedEventTypes: { Sets.newHashSet(EVENT_TYPE_A) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService("TYPE_BASED_SUBSCRIBER_1", EventSubscriber, typeBasedSubscriber1)

        def typeBasedSubscriber2 = [
            receive: { event -> receivedEvent_TypeBasedSubscriber2 = event },
            getSubscribedEventTypes: { Sets.newHashSet(EVENT_TYPE_A) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService("TYPE_BASED_SUBSCRIBER_2", EventSubscriber, typeBasedSubscriber2)

        def topicBasedSubscriber3 = [
            receive: { event -> receivedEvent_TopicBasedSubscriber3 = event },
            getSubscribedEventTypes: { Sets.newHashSet(EVENT_TYPE_B, EVENT_TYPE_C) },
            getEventFilter: { new TopicEventFilter("smarthome/some/topic") },
        ] as EventSubscriber
        registerService("TOPIC_BASED_SUBSCRIBER_3", EventSubscriber, topicBasedSubscriber3)

        def allEventTypesSubscriber4 = [
            receive: { event -> receivedEvent_allEventTypesSubscriber4 = event },
            getSubscribedEventTypes: { Sets.newHashSet(EventSubscriber.ALL_EVENT_TYPES) },
            getEventFilter: { new TopicEventFilter("smarthome/some/topic") },
        ] as EventSubscriber
        registerService("ALL_EVENT_TYPES_SUBSCRIBER_4", EventSubscriber, allEventTypesSubscriber4)
    }

    @After
    public void cleanUp() {
        serviceRegistrations.each() { name, service ->
            service.unregister()
        }
        serviceRegistrations.clear()
    }

    @Test
    void 'OSGiEventManager dispatches event data correctly'() {
        Event typeAEvent = createEvent(EVENT_TYPE_A)
        eventPublisher.post(typeAEvent)

        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber1, not(null)}
        assertThat receivedEvent_TypeBasedSubscriber1.getType(), is(typeAEvent.getType())
        assertThat receivedEvent_TypeBasedSubscriber1.getPayload(), is(typeAEvent.getPayload())
        assertThat receivedEvent_TypeBasedSubscriber1.getTopic(), is(typeAEvent.getTopic())

        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber2, not(null)}
        assertThat receivedEvent_TypeBasedSubscriber2.getType(), is(typeAEvent.getType())
        assertThat receivedEvent_TypeBasedSubscriber2.getPayload(), is(typeAEvent.getPayload())
        assertThat receivedEvent_TypeBasedSubscriber2.getTopic(), is(typeAEvent.getTopic())
    }

    @Test
    void 'OSGiEventManager dispatches diffent event types to corresponding subscribers correctly'() {
        Event typeAEvent = createEvent(EVENT_TYPE_A)
        Event typeBEvent = createEvent(EVENT_TYPE_B)
        Event typeCEvent = createEvent(EVENT_TYPE_C)

        eventPublisher.post(typeAEvent)
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber1, not(null)}
        assertThat receivedEvent_TypeBasedSubscriber1.getType(), is(EVENT_TYPE_A)
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber2, not(null)}
        assertThat receivedEvent_TypeBasedSubscriber2.getType(), is(EVENT_TYPE_A)
        waitForAssert {assertThat receivedEvent_TopicBasedSubscriber3, is(null)}
        waitForAssert {assertThat receivedEvent_allEventTypesSubscriber4, not(null)}
        assertThat receivedEvent_allEventTypesSubscriber4.getType(), is(EVENT_TYPE_A)
        resetReceivedEvents()

        eventPublisher.post(typeBEvent)
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber1, is(null)}
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber2, is(null)}
        waitForAssert {assertThat receivedEvent_TopicBasedSubscriber3, not(null)}
        assertThat receivedEvent_TopicBasedSubscriber3.getType(), is(EVENT_TYPE_B)
        waitForAssert {assertThat receivedEvent_allEventTypesSubscriber4, not(null)}
        assertThat receivedEvent_allEventTypesSubscriber4.getType(), is(EVENT_TYPE_B)
        resetReceivedEvents()

        eventPublisher.post(typeCEvent)
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber1, is(null)}
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber2, is(null)}
        waitForAssert {assertThat receivedEvent_TopicBasedSubscriber3, not(null)}
        assertThat receivedEvent_TopicBasedSubscriber3.getType(), is(EVENT_TYPE_C)
        waitForAssert {assertThat receivedEvent_allEventTypesSubscriber4, not(null)}
        assertThat receivedEvent_allEventTypesSubscriber4.getType(), is(EVENT_TYPE_C)
    }

    @Test
    void 'OSGiEventManager dispatches no event after subscriber unregistration'() {
        eventPublisher.post(createEvent(EVENT_TYPE_A))
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber1, not(null)}
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber2, not(null)}
        resetReceivedEvents()

        unregisterService("TYPE_BASED_SUBSCRIBER_1")
        eventPublisher.post(createEvent(EVENT_TYPE_A))
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber1, is(null)}
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber2, not(null)}
        resetReceivedEvents()

        unregisterService("TYPE_BASED_SUBSCRIBER_2")
        eventPublisher.post(createEvent(EVENT_TYPE_A))
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber1, is(null)}
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber2, is(null)}
    }

    @Test
    void 'OSGiEventManager dispatches no event after factory unregistration'() {
        eventPublisher.post(createEvent(EVENT_TYPE_A))
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber1, not(null)}
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber2, not(null)}
        unregisterService("EVENT_TYPE_FACTORY_A_B")
        resetReceivedEvents()

        eventPublisher.post(createEvent(EVENT_TYPE_A))
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber1, is(null)}
        waitForAssert {assertThat receivedEvent_TypeBasedSubscriber2, is(null)}
    }
    
    @Test
    public void 'OSGiEventManager validates events before posted'() {
        try {
            eventPublisher.post(null)
            fail("IllegalArgumentException expected!")
        } catch(IllegalArgumentException e) {
            assertThat e.getMessage(), is("Argument 'event' must not be null.")
        }

        Event event = createEvent(null, "{a: 'A', b: 'B'}", "smarthome/some/topic")
        try {
            eventPublisher.post(event)
            fail("IllegalArgumentException expected!")
        } catch(IllegalArgumentException e) {
            assertThat e.getMessage(), is("The type of the 'event' argument must not be null or empty.")
        }

        event = createEvent(EVENT_TYPE_A, null, "smarthome/some/topic")
        try {
            eventPublisher.post(event)
            fail("IllegalArgumentException expected!")
        } catch(IllegalArgumentException e) {
            assertThat e.getMessage(), is("The payload of the 'event' argument must not be null or empty.")
        }

        event = createEvent(EVENT_TYPE_A, "{a: 'A', b: 'B'}", null)
        try {
            eventPublisher.post(event)
            fail("IllegalArgumentException expected!")
        } catch(IllegalArgumentException e) {
            assertThat e.getMessage(), is("The topic of the 'event' argument must not be null or empty.")
        }
    }

    private Event createEvent(String eventType) {
        createEvent(eventType, "{a: 'A', b: 'B'}", "smarthome/some/topic")
    }
    
    private Event createEvent(String eventType, String payload, String topic) {
        [ getType: { eventType }, getPayload: { payload }, getTopic: { topic }, getSource: { null } ] as Event
    }

    private void registerService(String key, Class clazz, Object serviceObject) {
        serviceRegistrations.put(key, bundleContext.registerService(clazz, serviceObject, null))
    }

    private void unregisterService(String key) {
        serviceRegistrations.get(key)?.unregister()
        serviceRegistrations.remove(key)
    }

    private void resetReceivedEvents() {
        receivedEvent_TypeBasedSubscriber1 = null
        receivedEvent_TypeBasedSubscriber2 = null
        receivedEvent_TopicBasedSubscriber3 = null
        receivedEvent_allEventTypesSubscriber4 = null
    }
}
