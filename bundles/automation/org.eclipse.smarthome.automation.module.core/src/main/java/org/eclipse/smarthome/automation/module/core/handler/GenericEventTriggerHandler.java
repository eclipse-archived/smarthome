/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * if an event occurs. The eventType, eventSource and topic can be set with the
 * configuration. It is an generic approach which makes it easier to specify
 * more concrete event based triggers with the composite module approach of the
 * automation component. Each GenericTriggerHandler instance registers as
 * EventSubscriber, so the dispose method must be called for unregistering the
 * service.
 *
 * @author Benedikt Niehues - Initial contribution and API
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class GenericEventTriggerHandler extends BaseTriggerModuleHandler implements EventSubscriber, EventFilter {

    private final Logger logger = LoggerFactory.getLogger(GenericEventTriggerHandler.class);

    private String source;
    private String topic;
    private Set<String> types;
    private BundleContext bundleContext;

    public static final String MODULE_TYPE_ID = "core.GenericEventTrigger";

    private static final String CFG_EVENT_TOPIC = "eventTopic";
    private static final String CFG_EVENT_SOURCE = "eventSource";
    private static final String CFG_EVENT_TYPES = "eventTypes";

    @SuppressWarnings("rawtypes")
    private ServiceRegistration eventSubscriberRegistration;

    public GenericEventTriggerHandler(Trigger module, BundleContext bundleContext) {
        super(module);
        this.source = (String) module.getConfiguration().get(CFG_EVENT_SOURCE);
        this.topic = (String) module.getConfiguration().get(CFG_EVENT_TOPIC);
        this.types = ImmutableSet.copyOf(((String) module.getConfiguration().get(CFG_EVENT_TYPES)).split(","));
        this.bundleContext = bundleContext;
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("event.topics", topic);
        eventSubscriberRegistration = this.bundleContext.registerService(EventSubscriber.class.getName(), this,
                properties);
        logger.trace("Registered EventSubscriber: Topic: {} Type: {} Source: {}", topic, types, source);
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return types;
    }

    @Override
    public EventFilter getEventFilter() {
        return this;
    }

    @Override
    public void receive(Event event) {
        if (ruleEngineCallback != null) {
            logger.trace("Received Event: Source: {} Topic: {} Type: {}  Payload: {}", event.getSource(),
                    event.getTopic(), event.getType(), event.getPayload());
            if (!event.getTopic().contains(source)) {
                return;
            }
            Map<String, Object> values = Maps.newHashMap();
            values.put("event", event);

            ruleEngineCallback.triggered(this.module, values);
        }
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic
     *            the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * do the cleanup: unregistering eventSubscriber...
     */
    @Override
    public void dispose() {
        super.dispose();
        if (eventSubscriberRegistration != null) {
            eventSubscriberRegistration.unregister();
            eventSubscriberRegistration = null;
        }
    }

    @Override
    public boolean apply(Event event) {
        logger.trace("->FILTER: {}:{}", event.getTopic(), source);
        return event.getTopic().contains(source);
    }

}
