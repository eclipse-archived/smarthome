/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.handler;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.AbstractModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.module.Activator;
import org.eclipse.smarthome.automation.module.factory.ItemBasedModuleHandlerFactory;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the handler implementation for a trigger that triggers a rule
 * on item state changes.
 * 
 * @author Benedikt Niehues
 *
 */
public class ItemStateChangeTriggerHandler extends AbstractModuleHandler implements TriggerHandler {

	public static final String ITEM_STATE_CHANGE_TRIGGER = "ItemStateChangeTrigger";
	public static final String ITEM_NAME = "itemName";
	public static final String NEW_STATE = "newState";

	private final Logger logger = LoggerFactory.getLogger(ItemStateChangeTriggerHandler.class);
	private Trigger trigger;
	private EventSubscriber itemStateUpdateReceiver;
	
	RuleEngineCallback ruleCallback;

	private ServiceRegistration<EventSubscriber> itemStateUpdateReceiverServiceRegistration;

	public ItemStateChangeTriggerHandler(Trigger trigger) {
		super(trigger);
		this.trigger = trigger;
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("event.topics", "smarthome/*");
		this.itemStateUpdateReceiver = new ItemStateUpdateReceiver(this);
		itemStateUpdateReceiverServiceRegistration = Activator.getContext().registerService(EventSubscriber.class,
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
		return getResolvedOutputs(getResolvedConfiguration(null), null, ret);
	}

	@Override
	public void dispose() {
		super.dispose();
		itemStateUpdateReceiverServiceRegistration.unregister();
		itemStateUpdateReceiver = null;
	}

	@Override
	public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
		this.ruleCallback = ruleCallback;
	}

	/**
	 * this is the callback method for the ItemStateUpdateReceiver
	 * 
	 * @param event
	 */
	public void updateReceived(ItemStateEvent event) {
		logger.debug("update received for " + event.getItemName());
		Map<String, Object> configuration = getResolvedConfiguration(null);
		String itemName = (String) configuration.get(ITEM_NAME);

		if (ruleCallback == null) {
			logger.error("the rule Callback is not initialized for ItemUpdate Event on item: " + itemName);
		}
		if (itemName.equals(event.getItemName())) {
			logger.debug("triggering rule callback on ItemStateEvent: " + event.getItemName() + " --> "
					+ event.getItemState());
			ruleCallback.triggered(trigger, calculateOutputs(itemName, event.getItemState()));
		}

	}

	@Override
	protected ModuleTypeRegistry getModuleTypeRegistry() {
		return ItemBasedModuleHandlerFactory.getModuleTypeRegistry();
	}

}
