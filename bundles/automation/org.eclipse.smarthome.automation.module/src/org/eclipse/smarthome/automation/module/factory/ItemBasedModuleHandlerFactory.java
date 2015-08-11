/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.factory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.module.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.handler.ItemPostCommandActionHandler;
import org.eclipse.smarthome.automation.module.handler.ItemStateChangeTriggerHandler;
import org.eclipse.smarthome.automation.module.handler.ItemStateConditionHandler;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This HandlerFactory creates ModuleHandlers to control items within the
 * RuleEngine. It contains basic Triggers, Conditions and Actions.
 * 
 * @author Benedikt Niehues
 *
 */
public class ItemBasedModuleHandlerFactory extends BaseModuleHandlerFactory {

	private final Logger logger = LoggerFactory.getLogger(ItemBasedModuleHandlerFactory.class);

	private static final Collection<String> types = Arrays.asList(new String[] {
			ItemStateChangeTriggerHandler.ITEM_STATE_CHANGE_TRIGGER, ItemStateConditionHandler.ITEM_STATE_CONDITION,
			ItemPostCommandActionHandler.ITEM_POST_COMMAND_ACTION });

	private ItemRegistry itemRegistry;
	private EventPublisher eventPublisher;

	private Map<String, ItemStateChangeTriggerHandler> itemStateChangeTriggerHandlers = new HashMap<String, ItemStateChangeTriggerHandler>();
	private Map<String, ItemStateConditionHandler> itemStateConditionHandlers = new HashMap<String, ItemStateConditionHandler>();
	private Map<String, ItemPostCommandActionHandler> itemPostCommandActionHandlers = new HashMap<String, ItemPostCommandActionHandler>();

	@Override
	public Collection<String> getTypes() {
		return types;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ModuleHandler> T create(Module module) {
		logger.debug("create " + module.getId() + "->" + module.getTypeUID());
		if (moduleTypeRegistry != null) {
			String moduleTypeUID = module.getTypeUID();
			String handlerUID = getHandlerUID(moduleTypeUID);
			ModuleType moduleType = moduleTypeRegistry.get(handlerUID, null);
			if (moduleType != null) {
				if (ItemStateChangeTriggerHandler.ITEM_STATE_CHANGE_TRIGGER.equals(handlerUID)
						&& module instanceof Trigger) {
					ItemStateChangeTriggerHandler triggerHandler = itemStateChangeTriggerHandlers.get(module.getId());
					if (triggerHandler == null) {
						triggerHandler = new ItemStateChangeTriggerHandler((Trigger) module);
						itemStateChangeTriggerHandlers.put(module.getId(), triggerHandler);
					}
					return (T) triggerHandler;
				} else if (ItemStateConditionHandler.ITEM_STATE_CONDITION.equals(handlerUID)
						&& module instanceof Condition) {
					ItemStateConditionHandler conditionHandler = itemStateConditionHandlers.get(module.getId());
					if (conditionHandler == null) {
						conditionHandler = new ItemStateConditionHandler((Condition) module);
						conditionHandler.setItemRegistry(itemRegistry);
						itemStateConditionHandlers.put(module.getId(), conditionHandler);
					}
					return (T) conditionHandler;
				} else if (ItemPostCommandActionHandler.ITEM_POST_COMMAND_ACTION.equals(handlerUID)
						&& module instanceof Action) {
					ItemPostCommandActionHandler postCommandActionHandler = itemPostCommandActionHandlers
							.get(module.getId());
					if (postCommandActionHandler == null) {
						postCommandActionHandler = new ItemPostCommandActionHandler((Action) module);
						postCommandActionHandler.setEventPublisher(eventPublisher);
						postCommandActionHandler.setItemRegistry(itemRegistry);
						itemPostCommandActionHandlers.put(module.getId(), postCommandActionHandler);
					}
					return (T) postCommandActionHandler;
				} else {
					logger.error("The ModuleHandler is not supported:" + handlerUID);
				}

			} else {
				logger.error("ModuleType is not registered: " + moduleTypeUID);
			}

		} else {
			logger.error("ModuleTypeRegistry not available to create Module: " + module.getId());
		}
		return null;
	}

	/**
	 * the itemRegistry was added
	 * 
	 * @param itemRegistry
	 */
	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
		for (ItemStateConditionHandler handler : itemStateConditionHandlers.values()) {
			handler.setItemRegistry(itemRegistry);
		}
		for (ItemPostCommandActionHandler handler : itemPostCommandActionHandlers.values()) {
			handler.setItemRegistry(itemRegistry);
		}
	}

	/**
	 * unsetter for itemRegistry (DS)
	 * 
	 * @param itemRegistry
	 */
	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
		for (ItemStateConditionHandler handler : itemStateConditionHandlers.values()) {
			handler.unsetItemRegistry(itemRegistry);
		}
		for (ItemPostCommandActionHandler handler : itemPostCommandActionHandlers.values()) {
			handler.unsetItemRegistry(itemRegistry);
		}
	}

	/**
	 * setter for the eventPublisher (DS)
	 * 
	 * @param eventPublisher
	 */
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
		for (ItemPostCommandActionHandler handler : itemPostCommandActionHandlers.values()) {
			handler.setEventPublisher(eventPublisher);
		}
	}

	/**
	 * unsetter for eventPublisher (DS)
	 * 
	 * @param eventPublisher
	 */
	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
		for (ItemPostCommandActionHandler handler : itemPostCommandActionHandlers.values()) {
			handler.unsetEventPublisher(eventPublisher);
		}
	}
}
