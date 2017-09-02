/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.factory;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.module.core.handler.ChannelEventTriggerHandler;
import org.eclipse.smarthome.automation.module.core.handler.CompareConditionHandler;
import org.eclipse.smarthome.automation.module.core.handler.GenericEventConditionHandler;
import org.eclipse.smarthome.automation.module.core.handler.GenericEventTriggerHandler;
import org.eclipse.smarthome.automation.module.core.handler.ItemCommandActionHandler;
import org.eclipse.smarthome.automation.module.core.handler.ItemCommandTriggerHandler;
import org.eclipse.smarthome.automation.module.core.handler.ItemStateConditionHandler;
import org.eclipse.smarthome.automation.module.core.handler.ItemStateTriggerHandler;
import org.eclipse.smarthome.automation.module.core.handler.RuleEnablementActionHandler;
import org.eclipse.smarthome.automation.module.core.handler.RunRuleActionHandler;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This HandlerFactory creates ModuleHandlers to control items within the
 * RuleEngine. It contains basic Triggers, Conditions and Actions.
 *
 * @author Benedikt Niehues - Initial contribution and API
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class CoreModuleHandlerFactory extends BaseModuleHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(CoreModuleHandlerFactory.class);

    private static final Collection<String> types = Arrays.asList(ItemCommandTriggerHandler.MODULE_TYPE_ID,
            ItemStateTriggerHandler.UPDATE_MODULE_TYPE_ID, ItemStateTriggerHandler.CHANGE_MODULE_TYPE_ID,
            ItemStateConditionHandler.ITEM_STATE_CONDITION, ItemCommandActionHandler.ITEM_COMMAND_ACTION,
            GenericEventTriggerHandler.MODULE_TYPE_ID, ChannelEventTriggerHandler.MODULE_TYPE_ID,
            GenericEventConditionHandler.MODULETYPE_ID, GenericEventConditionHandler.MODULETYPE_ID,
            CompareConditionHandler.MODULE_TYPE, RuleEnablementActionHandler.UID, RunRuleActionHandler.UID);

    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;
    private RuleRegistry ruleRegistry;

    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext.getBundleContext());
    }

    protected void deactivate(ComponentContext componentContext) {
        super.deactivate();
    }

    protected void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    protected void unsetRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = null;
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
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        for (ModuleHandler handler : getHandlers().values()) {
            if (handler instanceof ItemStateConditionHandler) {
                ((ItemStateConditionHandler) handler).setItemRegistry(this.itemRegistry);
            } else if (handler instanceof ItemCommandActionHandler) {
                ((ItemCommandActionHandler) handler).setItemRegistry(this.itemRegistry);
            }
        }
    }

    /**
     * unsetter for itemRegistry (called by serviceTracker)
     *
     * @param itemRegistry
     */
    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        for (ModuleHandler handler : getHandlers().values()) {
            if (handler instanceof ItemStateConditionHandler) {
                ((ItemStateConditionHandler) handler).unsetItemRegistry(this.itemRegistry);
            } else if (handler instanceof ItemCommandActionHandler) {
                ((ItemCommandActionHandler) handler).unsetItemRegistry(this.itemRegistry);
            }
        }
        this.itemRegistry = null;
    }

    /**
     * setter for the eventPublisher (called by serviceTracker)
     *
     * @param eventPublisher
     */
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        for (ModuleHandler handler : getHandlers().values()) {
            if (handler instanceof ItemCommandActionHandler) {
                ((ItemCommandActionHandler) handler).setEventPublisher(eventPublisher);
            }
        }
    }

    /**
     * unsetter for eventPublisher (called by serviceTracker)
     *
     * @param eventPublisher
     */
    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
        for (ModuleHandler handler : getHandlers().values()) {
            if (handler instanceof ItemCommandActionHandler) {
                ((ItemCommandActionHandler) handler).unsetEventPublisher(eventPublisher);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    protected synchronized ModuleHandler internalCreate(final Module module, final String ruleUID) {
        logger.trace("create {} -> {} : {}", module.getId(), module.getTypeUID(), ruleUID);
        final String moduleTypeUID = module.getTypeUID();
        if (module instanceof Trigger) {
            // Handle triggers

            if (GenericEventTriggerHandler.MODULE_TYPE_ID.equals(moduleTypeUID)) {
                return new GenericEventTriggerHandler((Trigger) module, this.bundleContext);
            } else if (ChannelEventTriggerHandler.MODULE_TYPE_ID.equals(moduleTypeUID)) {
                return new ChannelEventTriggerHandler((Trigger) module, this.bundleContext);
            } else if (ItemCommandTriggerHandler.MODULE_TYPE_ID.equals(moduleTypeUID)) {
                return new ItemCommandTriggerHandler((Trigger) module, this.bundleContext);
            } else if (ItemStateTriggerHandler.CHANGE_MODULE_TYPE_ID.equals(moduleTypeUID)
                    || ItemStateTriggerHandler.UPDATE_MODULE_TYPE_ID.equals(moduleTypeUID)) {
                return new ItemStateTriggerHandler((Trigger) module, this.bundleContext);
            }
        } else if (module instanceof Condition) {
            // Handle conditions
            if (ItemStateConditionHandler.ITEM_STATE_CONDITION.equals(moduleTypeUID)) {
                ItemStateConditionHandler handler = new ItemStateConditionHandler((Condition) module);
                handler.setItemRegistry(itemRegistry);
                return handler;
            } else if (GenericEventConditionHandler.MODULETYPE_ID.equals(moduleTypeUID)) {
                return new GenericEventConditionHandler((Condition) module);
            } else if (CompareConditionHandler.MODULE_TYPE.equals(moduleTypeUID)) {
                return new CompareConditionHandler((Condition) module);
            }
        } else if (module instanceof Action) {
            // Handle actions

            if (ItemCommandActionHandler.ITEM_COMMAND_ACTION.equals(moduleTypeUID)) {
                final ItemCommandActionHandler postCommandActionHandler = new ItemCommandActionHandler((Action) module);
                postCommandActionHandler.setEventPublisher(eventPublisher);
                postCommandActionHandler.setItemRegistry(itemRegistry);
                return postCommandActionHandler;
            } else if (RuleEnablementActionHandler.UID.equals(moduleTypeUID)) {
                return new RuleEnablementActionHandler((Action) module, ruleRegistry);
            } else if (RunRuleActionHandler.UID.equals(moduleTypeUID)) {
                return new RunRuleActionHandler((Action) module, ruleRegistry);
            }
        }

        logger.error("The ModuleHandler is not supported:{}", moduleTypeUID);
        return null;
    }
}
