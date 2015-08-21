/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.handler.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module Handler Factory Sample implementation
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class SampleHandlerFactory extends BaseModuleHandlerFactory implements ModuleHandlerFactory {
    public static final String SUPPORTED_TRIGGER = "SampleTrigger";
    public static final String SUPPORTED_CONDITION = "SampleCondition";
    public static final String SUPPORTED_ACTION = "SampleAction";
    public static final String MODULE_HANDLER_FACTORY_NAME = "[SampleHandlerFactory]";
    private Logger log;
    private List<SampleTriggerHandler> createdTriggerHandler;
    private static final Collection<String> types;

    static {
        List<String> temp = new ArrayList<String>();
        temp.add(SUPPORTED_TRIGGER);
        temp.add(SUPPORTED_CONDITION);
        temp.add(SUPPORTED_ACTION);
        types = Collections.unmodifiableCollection(temp);
    }

    public SampleHandlerFactory(BundleContext bc) throws InvalidSyntaxException {
        super(bc);
        createdTriggerHandler = new ArrayList<SampleTriggerHandler>();
        log = LoggerFactory.getLogger(SampleHandlerFactory.class);
    }

    /**
     * Removes disposed handler from cache.
     *
     * @param handler the handler
     */
    protected void disposeHandler(ModuleHandler handler) {
        createdTriggerHandler.remove(handler);
    }

    @Override
    public Collection<String> getTypes() {
        return types;
    }

    // @Override
    // @SuppressWarnings("unchecked")
    // public <T extends ModuleHandler> T create(Module module) {
    // ModuleHandler moduleHandler = null;
    // if (moduleTypeRegistry != null) {
    // String typeUID = module.getTypeUID();
    // String handlerId = getHandlerUID(typeUID);
    // ModuleType moduleType = moduleTypeRegistry.get(handlerId);
    // if (moduleType != null) {
    // // create needed handler
    // if (SUPPORTED_TRIGGER.equals(handlerId)) {
    // moduleHandler = new SampleTriggerHandler(this, moduleTypeRegistry, (Trigger) module,
    // (TriggerType) moduleType, log);
    // createdTriggerHandler.add((SampleTriggerHandler) moduleHandler);
    // } else if (SUPPORTED_CONDITION.equals(handlerId)) {
    // moduleHandler = new SampleConditionHandler(moduleTypeRegistry, (Condition) module);
    // } else if (SUPPORTED_ACTION.equals(handlerId)) {
    // moduleHandler = new SampleActionHandler(moduleTypeRegistry, (Action) module,
    // (ActionType) moduleType);
    // } else {
    // log.error(MODULE_HANDLER_FACTORY_NAME + "Not supported moduleHandler: " + handlerId);
    // }
    // } else {
    // log.error(MODULE_HANDLER_FACTORY_NAME + "Not supported moduleType: " + typeUID);
    // }
    // } else {
    // log.error(MODULE_HANDLER_FACTORY_NAME + "ModuleTypeRegistry service is not available");
    // }
    // return (T) moduleHandler;
    // }

    /**
     * Release used resources
     */
    @Override
    public void dispose() {
        createdTriggerHandler.clear();
    }

    /**
     * Retrieves created TriggerHandlers from this HandlerFactory.
     *
     * @return list of created TriggerHandlers
     */
    public List<SampleTriggerHandler> getCreatedTriggerHandler() {
        return createdTriggerHandler;
    }

    @Override
    protected ModuleHandler createModuleHandlerInternal(Module module, String systemModuleTypeUID,
            List<ModuleType> moduleTypes) {
        ModuleHandler moduleHandler = null;
        if (SUPPORTED_TRIGGER.equals(systemModuleTypeUID)) {
            moduleHandler = new SampleTriggerHandler(this, (Trigger) module, moduleTypes);
            createdTriggerHandler.add((SampleTriggerHandler) moduleHandler);
        } else if (SUPPORTED_CONDITION.equals(systemModuleTypeUID)) {
            moduleHandler = new SampleConditionHandler((Condition) module, moduleTypes);
        } else if (SUPPORTED_ACTION.equals(systemModuleTypeUID)) {
            moduleHandler = new SampleActionHandler((Action) module, moduleTypes);
        } else {
            log.error(MODULE_HANDLER_FACTORY_NAME + "Not supported moduleHandler: " + systemModuleTypeUID);
        }

        return moduleHandler;
    }

}
