/**
 * Copyright (c) 2017 by Kai Kreuzer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * if an item receives a command. The eventType and command value can be set with the
 * configuration.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ItemCommandTriggerHandler extends BaseTriggerModuleHandler implements EventSubscriber, EventFilter {

    private final Logger logger = LoggerFactory.getLogger(ItemCommandTriggerHandler.class);

    private String itemName;
    private String command;
    private String topic;

    private Set<String> types;
    private BundleContext bundleContext;

    public static final String MODULE_TYPE_ID = "core.ItemCommandTrigger";

    private static final String CFG_ITEMNAME = "itemName";
    private static final String CFG_COMMAND = "command";

    @SuppressWarnings("rawtypes")
    private ServiceRegistration eventSubscriberRegistration;

    public ItemCommandTriggerHandler(Trigger module, BundleContext bundleContext) {
        super(module);
        this.itemName = (String) module.getConfiguration().get(CFG_ITEMNAME);
        this.command = (String) module.getConfiguration().get(CFG_COMMAND);
        this.types = Collections.singleton(ItemCommandEvent.TYPE);
        this.bundleContext = bundleContext;
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        this.topic = "smarthome/items/" + itemName + "/command";
        properties.put("event.topics", topic);
        eventSubscriberRegistration = this.bundleContext.registerService(EventSubscriber.class.getName(), this,
                properties);
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
            Map<String, Object> values = new HashMap<>();
            if (event instanceof ItemCommandEvent) {
                Command command = ((ItemCommandEvent) event).getItemCommand();
                if (this.command == null || this.command.equals(command.toFullString())) {
                    values.put("command", command);
                    values.put("event", event);
                    ruleEngineCallback.triggered(this.module, values);
                }
            }
        }
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
        logger.trace("->FILTER: {}:{}", event.getTopic(), itemName);
        return event.getTopic().equals(topic);
    }

}
