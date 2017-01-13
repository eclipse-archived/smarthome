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
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * if an item state event occurs. The eventType and state value can be set with the
 * configuration.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ItemStateTriggerHandler extends BaseTriggerModuleHandler implements EventSubscriber, EventFilter {

    private final Logger logger = LoggerFactory.getLogger(ItemStateTriggerHandler.class);

    private String itemName;
    private String state;
    private Set<String> types;
    private BundleContext bundleContext;

    public static final String UPDATE_MODULE_TYPE_ID = "core.ItemStateUpdateTrigger";
    public static final String CHANGE_MODULE_TYPE_ID = "core.ItemStateChangeTrigger";

    private static final String CFG_ITEMNAME = "itemName";
    private static final String CFG_STATE = "state";

    @SuppressWarnings("rawtypes")
    private ServiceRegistration eventSubscriberRegistration;

    public ItemStateTriggerHandler(Trigger module, BundleContext bundleContext) {
        super(module);
        this.itemName = (String) module.getConfiguration().get(CFG_ITEMNAME);
        this.state = (String) module.getConfiguration().get(CFG_STATE);
        this.types = Collections.singleton(
                UPDATE_MODULE_TYPE_ID.equals(module.getTypeUID()) ? ItemStateEvent.TYPE : ItemStateChangedEvent.TYPE);
        this.bundleContext = bundleContext;
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("event.topics", "smarthome/items/*");
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
            Map<String, Object> values = Maps.newHashMap();
            if (event instanceof ItemStateEvent && UPDATE_MODULE_TYPE_ID.equals(module.getTypeUID())) {
                State state = ((ItemStateEvent) event).getItemState();
                if (this.state == null || this.state.equals(state.toFullString())) {
                    values.put("state", state);
                }
            } else if (event instanceof ItemStateChangedEvent && CHANGE_MODULE_TYPE_ID.equals(module.getTypeUID())) {
                State state = ((ItemStateChangedEvent) event).getItemState();
                if (this.state == null || this.state.equals(state.toFullString())) {
                    values.put("oldState", ((ItemStateChangedEvent) event).getOldItemState());
                    values.put("newState", state);
                }
            }
            if (!values.isEmpty()) {
                ruleEngineCallback.triggered(this.module, values);
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
        return event.getTopic().contains(itemName);
    }

}
