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
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.module.handler.GenericEventTriggerHandler;
import org.eclipse.smarthome.automation.module.handler.ItemPostCommandActionHandler;
import org.eclipse.smarthome.automation.module.handler.ItemStateChangeTriggerHandler;
import org.eclipse.smarthome.automation.module.handler.ItemStateConditionHandler;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
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
			ItemStateConditionHandler.ITEM_STATE_CONDITION, ItemStateChangeTriggerHandler.ITEM_STATE_CHANGE_TRIGGER,
			ItemPostCommandActionHandler.ITEM_POST_COMMAND_ACTION, GenericEventTriggerHandler.MODULE_TYPE_ID });

	private ServiceTracker itemRegistryTracker;
	private ServiceTracker eventPublisherTracker;

	private ItemRegistry itemRegistry;
	private EventPublisher eventPublisher;

	private Map<String, GenericEventTriggerHandler> genericEventTriggerHandlers = new HashMap<String, GenericEventTriggerHandler>();
	private Map<String, ItemStateConditionHandler> itemStateConditionHandlers = new HashMap<String, ItemStateConditionHandler>();
	private Map<String, ItemPostCommandActionHandler> itemPostCommandActionHandlers = new HashMap<String, ItemPostCommandActionHandler>();

	public ItemBasedModuleHandlerFactory(BundleContext bundleContext) {
		super(bundleContext);
		initializeServiceTrackers();

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initializeServiceTrackers() {
		this.itemRegistryTracker = new ServiceTracker(this.bundleContext, ItemRegistry.class,
				new ServiceTrackerCustomizer() {

					@Override
					public Object addingService(ServiceReference reference) {
						setItemRegistry((ItemRegistry) bundleContext.getService(reference));
						return itemRegistry;
					}

					@Override
					public void modifiedService(ServiceReference reference, Object service) {

					}

					@Override
					public void removedService(ServiceReference reference, Object service) {
						unsetItemRegistry((ItemRegistry) service);
					}
				});
		this.itemRegistryTracker.open();

		this.eventPublisherTracker = new ServiceTracker(this.bundleContext, EventPublisher.class,
				new ServiceTrackerCustomizer() {

					@Override
					public Object addingService(ServiceReference reference) {
						setEventPublisher((EventPublisher) bundleContext.getService(reference));
						return eventPublisher;
					}

					@Override
					public void modifiedService(ServiceReference reference, Object service) {

					}

					@Override
					public void removedService(ServiceReference reference, Object service) {
						unsetEventPublisher((EventPublisher) service);
					}

				});
		this.eventPublisherTracker.open();
	}

	@Override
	public Collection<String> getTypes() {
		return types;
	}

	/**
	 * the itemRegistry was added (called by serviceTracker)
	 * 
	 * @param itemRegistry
	 */
	private void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
		for (ItemStateConditionHandler handler : itemStateConditionHandlers.values()) {
			handler.setItemRegistry(itemRegistry);
		}
		for (ItemPostCommandActionHandler handler : itemPostCommandActionHandlers.values()) {
			handler.setItemRegistry(itemRegistry);
		}
	}

	/**
	 * unsetter for itemRegistry (called by serviceTracker)
	 * 
	 * @param itemRegistry
	 */
	private void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
		for (ItemStateConditionHandler handler : itemStateConditionHandlers.values()) {
			handler.unsetItemRegistry(itemRegistry);
		}
		for (ItemPostCommandActionHandler handler : itemPostCommandActionHandlers.values()) {
			handler.unsetItemRegistry(itemRegistry);
		}
	}

	/**
	 * setter for the eventPublisher (called by serviceTracker)
	 * 
	 * @param eventPublisher
	 */
	private void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
		for (ItemPostCommandActionHandler handler : itemPostCommandActionHandlers.values()) {
			handler.setEventPublisher(eventPublisher);
		}
	}

	/**
	 * unsetter for eventPublisher (called by serviceTracker)
	 * 
	 * @param eventPublisher
	 */
	private void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
		for (ItemPostCommandActionHandler handler : itemPostCommandActionHandlers.values()) {
			handler.unsetEventPublisher(eventPublisher);
		}
	}

	@Override
	protected ModuleHandler createModuleHandlerInternal(Module module, String systemModuleTypeUID,
			List<ModuleType> moduleTypes) {
		logger.debug("create " + module.getId() + "->" + module.getTypeUID());
		String moduleTypeUID = module.getTypeUID();
		if (systemModuleTypeUID != null) {
			if (GenericEventTriggerHandler.MODULE_TYPE_ID.equals(systemModuleTypeUID)
					&& module instanceof Trigger) {
				GenericEventTriggerHandler triggerHandler = genericEventTriggerHandlers.get(module.getId());
				if (triggerHandler == null) {
					triggerHandler = new GenericEventTriggerHandler((Trigger) module, moduleTypes,
							this.bundleContext);
					genericEventTriggerHandlers.put(module.getId(), triggerHandler);
				}
				return triggerHandler;
			} else if (ItemStateConditionHandler.ITEM_STATE_CONDITION.equals(systemModuleTypeUID)
					&& module instanceof Condition) {
				ItemStateConditionHandler conditionHandler = itemStateConditionHandlers.get(module.getId());
				if (conditionHandler == null) {
					conditionHandler = new ItemStateConditionHandler((Condition) module, moduleTypes);
					conditionHandler.setItemRegistry(itemRegistry);
					itemStateConditionHandlers.put(module.getId(), conditionHandler);
				}
				return conditionHandler;
			} else if (ItemPostCommandActionHandler.ITEM_POST_COMMAND_ACTION.equals(systemModuleTypeUID)
					&& module instanceof Action) {
				ItemPostCommandActionHandler postCommandActionHandler = itemPostCommandActionHandlers
						.get(module.getId());
				if (postCommandActionHandler == null) {
					postCommandActionHandler = new ItemPostCommandActionHandler((Action) module, moduleTypes);
					postCommandActionHandler.setEventPublisher(eventPublisher);
					postCommandActionHandler.setItemRegistry(itemRegistry);
					itemPostCommandActionHandlers.put(module.getId(), postCommandActionHandler);
				}
				return postCommandActionHandler;
			} else {
				logger.error("The ModuleHandler is not supported:" + systemModuleTypeUID);
			}

		} else {
			logger.error("ModuleType is not registered: " + moduleTypeUID);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory#dispose
	 * ()
	 */
	@Override
	public void dispose() {
		super.dispose();
		itemRegistryTracker.close();
		eventPublisherTracker.close();
	}
}
