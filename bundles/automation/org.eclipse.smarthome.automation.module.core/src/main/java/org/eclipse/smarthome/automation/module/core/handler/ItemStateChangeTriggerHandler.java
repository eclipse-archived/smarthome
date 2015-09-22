/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the handler implementation for a trigger that triggers a rule
 * on item state changes.
 *
 * @author Benedikt Niehues
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class ItemStateChangeTriggerHandler extends BaseModuleHandler<Trigger>implements TriggerHandler {

    public static final String ITEM_STATE_CHANGE_TRIGGER = "ItemStateChangeTrigger";
    public static final String ITEM_NAME = "itemName";
    public static final String NEW_STATE = "newState";

    private final Logger logger = LoggerFactory.getLogger(ItemStateChangeTriggerHandler.class);

    private BundleContext context;

    private Trigger trigger;
    private EventSubscriber itemStateUpdateReceiver;

    private ServiceRegistration<EventSubscriber> itemStateUpdateReceiverServiceRegistration;
    private RuleEngineCallback ruleEngineCallback;

    public ItemStateChangeTriggerHandler(Trigger trigger, BundleContext context) {
        super(trigger);
        this.context = context;
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("event.topics", "smarthome/*");
        this.itemStateUpdateReceiver = new ItemStateUpdateReceiver(this);
        itemStateUpdateReceiverServiceRegistration = this.context.registerService(EventSubscriber.class,
                itemStateUpdateReceiver, properties);
    }

    /**
     * sets the output values
     *
     * @param itemName
     * @param newState
     * @return
     */
    private Map<String, ?> calculateOutputs(String itemName, State newState) {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put(ITEM_NAME, itemName);
        ret.put(NEW_STATE, newState.toString());
        return ret;
    }

    @Override
    public void dispose() {
        itemStateUpdateReceiver = null;
        itemStateUpdateReceiverServiceRegistration.unregister();
    }

    /**
     * this is the callback method for the ItemStateUpdateReceiver
     *
     * @param event
     */
    public void updateReceived(ItemStateEvent event) {
        logger.debug("update received for " + event.getItemName());
        String itemName = (String) module.getConfiguration().get(ITEM_NAME);

        if (ruleEngineCallback == null) {
            logger.error("the rule Callback is not initialized for ItemUpdate Event on item: " + itemName);
        }
        if (itemName.equals(event.getItemName())) {
            logger.debug("triggering rule callback on ItemStateEvent: " + event.getItemName() + " --> "
                    + event.getItemState());
            ruleEngineCallback.triggered(trigger, calculateOutputs(itemName, event.getItemState()));
        }

    }

    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleEngineCallback) {
        this.ruleEngineCallback = ruleEngineCallback;
    }

}
