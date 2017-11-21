/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.events;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFactory;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.events.TopicEventFilter;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Sets;

/**
 * The {@link OSGiEventManagerOSGiTest} runs inside an OSGi container and tests the {@link OSGiEventManager}.
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Simon Kaufmann - migrated to Java
 */

public class OSGiEventManagerOSGiTest extends JavaOSGiTest {

    private static final int DEFAULT_SLEEP = 100;
    private static final String EVENT_TYPE_A = "EVENT_TYPE_A";
    private static final String EVENT_TYPE_B = "EVENT_TYPE_B";
    private static final String EVENT_TYPE_C = "EVENT_TYPE_C";

    private EventPublisher eventPublisher;

    @Mock
    private EventSubscriber typeBasedSubscriber1;

    @Mock
    private EventSubscriber typeBasedSubscriber2;

    @Mock
    private EventSubscriber typeBasedSubscriber3;

    @Mock
    private EventSubscriber allEventTypesSubscriber4;

    @Mock
    private EventFactory eventTypeFactoryAB;

    @Mock
    private EventFactory eventTypeFactoryC;
    private ServiceRegistration<?> regFactoryAB;
    private ServiceRegistration<?> regSubscriber1;
    private ServiceRegistration<?> regSubscriber2;

    private static class GenericEvent extends AbstractEvent {

        private final String eventType;

        public GenericEvent(String eventType, String topic, String payload, String source) {
            super(topic, payload, source);
            this.eventType = eventType;
        }

        public GenericEvent(Object[] args) {
            this((String) args[0], (String) args[1], (String) args[2], (String) args[3]);
        }

        @Override
        public String getType() {
            return eventType;
        }
    }

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        eventPublisher = getService(EventPublisher.class);

        doAnswer(answer -> {
            return new GenericEvent(answer.getArguments());
        }).when(eventTypeFactoryAB).createEvent(isA(String.class), isA(String.class), isA(String.class), any());
        when(eventTypeFactoryAB.getSupportedEventTypes()).thenReturn(Sets.newHashSet(EVENT_TYPE_A, EVENT_TYPE_B));
        regFactoryAB = registerService(eventTypeFactoryAB);

        doAnswer(answer -> {
            return new GenericEvent(answer.getArguments());
        }).when(eventTypeFactoryC).createEvent(isA(String.class), isA(String.class), isA(String.class), any());
        when(eventTypeFactoryC.getSupportedEventTypes()).thenReturn(Sets.newHashSet(EVENT_TYPE_C));
        registerService(eventTypeFactoryC);

        when(typeBasedSubscriber1.getSubscribedEventTypes()).thenReturn(Sets.newHashSet(EVENT_TYPE_A));
        regSubscriber1 = registerService(typeBasedSubscriber1);

        when(typeBasedSubscriber2.getSubscribedEventTypes()).thenReturn(Sets.newHashSet(EVENT_TYPE_A));
        regSubscriber2 = registerService(typeBasedSubscriber2);

        when(typeBasedSubscriber3.getSubscribedEventTypes()).thenReturn(Sets.newHashSet(EVENT_TYPE_B, EVENT_TYPE_C));
        when(typeBasedSubscriber3.getEventFilter()).thenReturn(new TopicEventFilter("smarthome/some/topic"));
        registerService(typeBasedSubscriber3);

        when(allEventTypesSubscriber4.getSubscribedEventTypes())
                .thenReturn(Sets.newHashSet(EventSubscriber.ALL_EVENT_TYPES));
        when(allEventTypesSubscriber4.getEventFilter()).thenReturn(new TopicEventFilter("smarthome/some/topic"));
        registerService(allEventTypesSubscriber4);
    }

    @Test
    public void testDispatching() throws Exception {
        Event event = createEvent(EVENT_TYPE_A);
        eventPublisher.post(event);

        ArgumentCaptor<Event> captor1 = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(typeBasedSubscriber1, times(1)).receive(captor1.capture());
        });
        Event event1 = captor1.getValue();
        assertThat(event1.getType(), is(event.getType()));
        assertThat(event1.getPayload(), is(event.getPayload()));
        assertThat(event1.getTopic(), is(event.getTopic()));

        ArgumentCaptor<Event> captor2 = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(typeBasedSubscriber2, times(1)).receive(captor2.capture());
        });
        Event event2 = captor2.getValue();
        assertThat(event2.getType(), is(event.getType()));
        assertThat(event2.getPayload(), is(event.getPayload()));
        assertThat(event2.getTopic(), is(event.getTopic()));
    }

    @Test
    public void testDispatchMulti_typeA() {
        Event event = createEvent(EVENT_TYPE_A);

        eventPublisher.post(event);

        ArgumentCaptor<Event> captor1 = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(typeBasedSubscriber1, times(1)).receive(captor1.capture());
        });
        assertThat(captor1.getValue().getType(), is(EVENT_TYPE_A));

        ArgumentCaptor<Event> captor2 = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(typeBasedSubscriber2, times(1)).receive(captor2.capture());
        });
        assertThat(captor2.getValue().getType(), is(EVENT_TYPE_A));

        ArgumentCaptor<Event> captor4 = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(allEventTypesSubscriber4, times(1)).receive(captor4.capture());
        });
        assertThat(captor4.getValue().getType(), is(EVENT_TYPE_A));

        verify(typeBasedSubscriber3, times(0)).receive(isA(Event.class));
    }

    @Test
    public void testDispatchMulti_typeB() {
        Event event = createEvent(EVENT_TYPE_B);

        eventPublisher.post(event);

        ArgumentCaptor<Event> captor3 = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(typeBasedSubscriber3, times(1)).receive(captor3.capture());
        });
        assertThat(captor3.getValue().getType(), is(EVENT_TYPE_B));

        ArgumentCaptor<Event> captor4 = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(allEventTypesSubscriber4, times(1)).receive(captor4.capture());
        });
        assertThat(captor4.getValue().getType(), is(EVENT_TYPE_B));

        verify(typeBasedSubscriber1, times(0)).receive(isA(Event.class));
        verify(typeBasedSubscriber2, times(0)).receive(isA(Event.class));
    }

    @Test
    public void testDispatchMulti_typeC() {
        Event event = createEvent(EVENT_TYPE_C);

        eventPublisher.post(event);

        ArgumentCaptor<Event> captor3 = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(typeBasedSubscriber3, times(1)).receive(captor3.capture());
        });
        assertThat(captor3.getValue().getType(), is(EVENT_TYPE_C));

        ArgumentCaptor<Event> captor4 = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(allEventTypesSubscriber4, times(1)).receive(captor4.capture());
        });
        assertThat(captor4.getValue().getType(), is(EVENT_TYPE_C));

        verify(typeBasedSubscriber1, times(0)).receive(isA(Event.class));
        verify(typeBasedSubscriber2, times(0)).receive(isA(Event.class));

    }

    @Test
    public void testDispatching_subscriberUnregistration() throws Exception {
        eventPublisher.post(createEvent(EVENT_TYPE_A));

        waitForAssert(() -> {
            verify(typeBasedSubscriber1, times(1)).receive(isA(Event.class));
        });
        waitForAssert(() -> {
            verify(typeBasedSubscriber2, times(1)).receive(isA(Event.class));
        });

        regSubscriber1.unregister();
        eventPublisher.post(createEvent(EVENT_TYPE_A));

        waitForAssert(() -> {
            verify(typeBasedSubscriber2, times(2)).receive(isA(Event.class));
        });
        verify(typeBasedSubscriber1, times(1)).receive(isA(Event.class));

        regSubscriber2.unregister();
        eventPublisher.post(createEvent(EVENT_TYPE_A));

        Thread.sleep(DEFAULT_SLEEP);

        verify(typeBasedSubscriber1, times(1)).receive(isA(Event.class));
        verify(typeBasedSubscriber2, times(2)).receive(isA(Event.class));
    }

    @Test
    public void testDispatching_factoryUnregistration() throws Exception {
        eventPublisher.post(createEvent(EVENT_TYPE_A));

        waitForAssert(() -> {
            verify(typeBasedSubscriber1, times(1)).receive(isA(Event.class));
        });
        waitForAssert(() -> {
            verify(typeBasedSubscriber2, times(1)).receive(isA(Event.class));
        });

        regFactoryAB.unregister();
        eventPublisher.post(createEvent(EVENT_TYPE_A));

        Thread.sleep(DEFAULT_SLEEP);

        verify(typeBasedSubscriber1, times(1)).receive(isA(Event.class));
        verify(typeBasedSubscriber2, times(1)).receive(isA(Event.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEventValidation_nullEvent() {
        eventPublisher.post(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEventValidation_nullType() {
        eventPublisher.post(createEvent(null, "{a: 'A', b: 'B'}", "smarthome/some/topic"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEventValidation_nullPayload() {
        eventPublisher.post(createEvent(EVENT_TYPE_A, null, "smarthome/some/topic"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEventValidation_nullTpic() {
        eventPublisher.post(createEvent(EVENT_TYPE_A, "{a: 'A', b: 'B'}", null));
    }

    private Event createEvent(String eventType) {
        return createEvent(eventType, "{a: 'A', b: 'B'}", "smarthome/some/topic");
    }

    private Event createEvent(String eventType, String payload, String topic) {
        return new GenericEvent(eventType, topic, payload, null);
    }

}
