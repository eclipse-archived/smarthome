/**
 * Copyright (c) 2017 by Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.thing.events.ChannelTriggeredEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * This is an ModuleHandler implementation for trigger channels with specific events
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class ChannelEventTriggerHandler extends BaseTriggerModuleHandler implements EventSubscriber, EventFilter {

    private final Logger logger = LoggerFactory.getLogger(ChannelEventTriggerHandler.class);

    public static final String MODULE_TYPE_ID = "core.ChannelEventTrigger";

    private final String eventOnChannel;
    private final String channelUID;
    private final String TOPIC = "smarthome/channels/*/triggered";
    private Set<String> types = new HashSet<String>();
    private BundleContext bundleContext;

    private final String CFG_CHANNEL_EVENT = "event";
    private final String CFG_CHANNEL = "channelUID";

    @SuppressWarnings("rawtypes")
    private ServiceRegistration eventSubscriberRegistration;

    public ChannelEventTriggerHandler(Trigger module, BundleContext bundleContext) {
        super(module);

        this.eventOnChannel = (String) module.getConfiguration().get(CFG_CHANNEL_EVENT);
        this.channelUID = (String) module.getConfiguration().get(CFG_CHANNEL);
        this.bundleContext = bundleContext;
        this.types.add("ChannelTriggeredEvent");

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("event.topics", TOPIC);
        eventSubscriberRegistration = this.bundleContext.registerService(EventSubscriber.class.getName(), this,
                properties);
    }

    @Override
    public void receive(Event event) {
        if (ruleEngineCallback != null) {
            logger.trace("Received Event: Source: {} Topic: {} Type: {}  Payload: {}", event.getSource(),
                    event.getTopic(), event.getType(), event.getPayload());

            Map<String, Object> values = Maps.newHashMap();
            values.put("event", event);

            ruleEngineCallback.triggered(this.module, values);
        }
    }

    @Override
    public boolean apply(Event event) {
        logger.trace("->FILTER: {}:{}", event.getTopic(), TOPIC);

        boolean eventMatches = false;
        if (event instanceof ChannelTriggeredEvent) {
            ChannelTriggeredEvent cte = (ChannelTriggeredEvent) event;
            if (cte.getTopic().contains(this.channelUID)) {
                logger.trace("->FILTER: {}:{}", cte.getEvent(), eventOnChannel);
                eventMatches = true;
                if (eventOnChannel != null && !eventOnChannel.isEmpty() && !eventOnChannel.equals(cte.getEvent())) {
                    eventMatches = false;
                }
            }
        }
        return eventMatches;
    }

    @Override
    public EventFilter getEventFilter() {
        return this;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return types;
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

}
