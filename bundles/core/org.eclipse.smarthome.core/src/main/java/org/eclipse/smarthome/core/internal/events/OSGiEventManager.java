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
package org.eclipse.smarthome.core.internal.events;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.common.SafeCaller;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFactory;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The {@link OSGiEventManager} provides an OSGi based default implementation of the Eclipse SmartHome event bus.
 *
 * The OSGiEventHandler tracks {@link EventSubscriber}s and {@link EventFactory}s, receives OSGi events (by
 * implementing the OSGi {@link EventHandler} interface) and dispatches the received OSGi events as ESH {@link Event}s
 * to the {@link EventSubscriber}s if the provided filter applies.
 *
 * The {@link OSGiEventManager} also serves as {@link EventPublisher} by implementing the EventPublisher interface.
 * Events are send in an asynchronous way via OSGi Event Admin mechanism.
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Markus Rathgeb - Return on received events as fast as possible (handle event in another thread)
 */
@Component(immediate = true, property = { "event.topics:String=smarthome" })
public class OSGiEventManager implements EventHandler, EventPublisher {

    @SuppressWarnings("rawtypes")
    private class EventSubscriberServiceTracker extends ServiceTracker {

        @SuppressWarnings("unchecked")
        public EventSubscriberServiceTracker(BundleContext context) {
            super(context, EventSubscriber.class.getName(), null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object addingService(ServiceReference reference) {
            EventSubscriber eventSubscriber = (EventSubscriber) this.context.getService(reference);
            if (eventSubscriber != null) {
                eventHandler.addEventSubscriber(eventSubscriber);
                return eventSubscriber;
            } else {
                return null;
            }
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            eventHandler.removeEventSubscriber((EventSubscriber) service);
        }

    }

    private final Map<String, EventFactory> typedEventFactories = new ConcurrentHashMap<String, EventFactory>();

    private ThreadedEventHandler eventHandler;

    private EventSubscriberServiceTracker eventSubscriberServiceTracker;

    private EventAdmin osgiEventAdmin;

    private SafeCaller safeCaller;

    @Activate
    protected void activate(ComponentContext componentContext) {
        eventHandler = new ThreadedEventHandler(typedEventFactories, safeCaller);

        eventSubscriberServiceTracker = new EventSubscriberServiceTracker(componentContext.getBundleContext());
        eventSubscriberServiceTracker.open();
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (eventSubscriberServiceTracker != null) {
            eventSubscriberServiceTracker.close();
            eventSubscriberServiceTracker = null;
        }

        if (eventHandler != null) {
            eventHandler.close();
            eventHandler = null;
        }
    }

    @Reference
    protected void setEventAdmin(EventAdmin eventAdmin) {
        this.osgiEventAdmin = eventAdmin;
    }

    protected void unsetEventAdmin(EventAdmin eventAdmin) {
        this.osgiEventAdmin = null;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addEventFactory(EventFactory eventFactory) {
        Set<String> supportedEventTypes = eventFactory.getSupportedEventTypes();

        for (String supportedEventType : supportedEventTypes) {
            synchronized (this) {
                if (!typedEventFactories.containsKey(supportedEventType)) {
                    typedEventFactories.put(supportedEventType, eventFactory);
                }
            }
        }
    }

    protected void removeEventFactory(EventFactory eventFactory) {
        Set<String> supportedEventTypes = eventFactory.getSupportedEventTypes();

        for (String supportedEventType : supportedEventTypes) {
            typedEventFactories.remove(supportedEventType);
        }
    }

    @Reference
    protected void setSafeCaller(SafeCaller safeCaller) {
        this.safeCaller = safeCaller;
    }

    protected void unsetSafeCaller(SafeCaller safeCaller) {
        this.safeCaller = null;
    }

    @Override
    public void handleEvent(org.osgi.service.event.Event osgiEvent) {
        eventHandler.handleEvent(osgiEvent);
    }

    @Override
    public void post(final Event event) throws IllegalArgumentException, IllegalStateException {
        EventAdmin eventAdmin = this.osgiEventAdmin;
        assertValidArgument(event);
        assertValidState(eventAdmin);
        postAsOSGiEvent(eventAdmin, event);
    }

    private void postAsOSGiEvent(final EventAdmin eventAdmin, final Event event) throws IllegalStateException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    Dictionary<String, Object> properties = new Hashtable<String, Object>(3);
                    properties.put("type", event.getType());
                    properties.put("payload", event.getPayload());
                    properties.put("topic", event.getTopic());
                    if (event.getSource() != null) {
                        properties.put("source", event.getSource());
                    }
                    eventAdmin.postEvent(new org.osgi.service.event.Event("smarthome", properties));
                    return null;
                }
            });
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            throw new IllegalStateException("Cannot post the event via the event bus. Error message: " + e.getMessage(),
                    e);
        }
    }

    private void assertValidArgument(Event event) throws IllegalArgumentException {
        String errorMsg = "The %s of the 'event' argument must not be null or empty.";
        String value;

        if (event == null) {
            throw new IllegalArgumentException("Argument 'event' must not be null.");
        }
        if ((value = event.getType()) == null || value.isEmpty()) {
            throw new IllegalArgumentException(String.format(errorMsg, "type"));
        }
        if ((value = event.getPayload()) == null || value.isEmpty()) {
            throw new IllegalArgumentException(String.format(errorMsg, "payload"));
        }
        if ((value = event.getTopic()) == null || value.isEmpty()) {
            throw new IllegalArgumentException(String.format(errorMsg, "topic"));
        }
    }

    private void assertValidState(EventAdmin eventAdmin) throws IllegalStateException {
        if (eventAdmin == null) {
            throw new IllegalStateException("The event bus module is not available!");
        }
    }

}
