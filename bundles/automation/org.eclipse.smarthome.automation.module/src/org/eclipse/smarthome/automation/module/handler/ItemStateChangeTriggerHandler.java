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
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerHandler;
import org.eclipse.smarthome.automation.type.ModuleType;
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
 *
 */
public class ItemStateChangeTriggerHandler extends BaseTriggerHandler {

	public static final String ITEM_STATE_CHANGE_TRIGGER = "ItemStateChangeTrigger";
	public static final String ITEM_NAME = "itemName";
	public static final String NEW_STATE = "newState";

	private final Logger logger = LoggerFactory.getLogger(ItemStateChangeTriggerHandler.class);
	
	private BundleContext context;
	
	private Trigger trigger;
	private EventSubscriber itemStateUpdateReceiver;
	
	private ServiceRegistration<EventSubscriber> itemStateUpdateReceiverServiceRegistration;

	public ItemStateChangeTriggerHandler(Trigger trigger, List<ModuleType> moduleTypes, BundleContext context) {
		super(trigger,moduleTypes);
		this.trigger = trigger;
		this.context=context;
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
		return getResolvedOutputs(getResolvedConfiguration(null), null, ret);
	}

	public void dispose() {
		itemStateUpdateReceiver = null;
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

		if (ruleCallBack == null) {
			logger.error("the rule Callback is not initialized for ItemUpdate Event on item: " + itemName);
		}
		if (itemName.equals(event.getItemName())) {
			logger.debug("triggering rule callback on ItemStateEvent: " + event.getItemName() + " --> "
					+ event.getItemState());
			ruleCallBack.triggered(trigger, calculateOutputs(itemName, event.getItemState()));
		}

	}

	@Override
	protected Map<String, Object> getTriggerValues() {
		// TODO Auto-generated method stub
		return null;
	}

}
