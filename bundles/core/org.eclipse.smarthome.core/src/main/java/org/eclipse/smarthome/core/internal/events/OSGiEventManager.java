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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.common.SafeCaller;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFactory;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventHandler;

/**
 * The {@link OSGiEventManager} provides an OSGi based default implementation of the Eclipse SmartHome event bus.
 *
 * The OSGiEventHandler tracks {@link EventSubscriber}s and {@link EventFactory}s, receives OSGi events (by
 * implementing the OSGi {@link EventHandler} interface) and dispatches the received OSGi events as ESH {@link Event}s
 * to the {@link EventSubscriber}s if the provided filter applies.
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Markus Rathgeb - Return on received events as fast as possible (handle event in another thread)
 */
@Component(immediate = true, property = { "event.topics:String=smarthome" })
public class OSGiEventManager implements EventHandler {

    private final Map<String, EventFactory> typedEventFactories = new ConcurrentHashMap<String, EventFactory>();

    private ThreadedEventHandler eventHandler;

    private SafeCaller safeCaller;

    @Activate
    protected void activate(ComponentContext componentContext) {
        eventHandler = new ThreadedEventHandler(typedEventFactories, safeCaller);
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (eventHandler != null) {
            eventHandler.close();
            eventHandler = null;
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addEventSubscriber(EventSubscriber eventSubscriber) {
        eventHandler.addEventSubscriber(eventSubscriber);
    }

    protected void removeEventSubscriber(EventSubscriber eventSubscriber) {
        eventHandler.removeEventSubscriber(eventSubscriber);
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

}
