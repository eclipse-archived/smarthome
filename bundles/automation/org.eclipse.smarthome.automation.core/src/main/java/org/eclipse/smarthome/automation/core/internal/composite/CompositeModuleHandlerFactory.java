/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.core.internal.composite;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.internal.RuleEngine;
import org.eclipse.smarthome.automation.core.internal.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeModuleHandlerFactory extends BaseModuleHandlerFactory implements ModuleHandlerFactory {

    private ModuleTypeManager mtManager;
    private RuleEngine ruleEngine;
    Logger logger = LoggerFactory.getLogger(getClass());

    public CompositeModuleHandlerFactory(BundleContext bc, ModuleTypeManager mtManager, RuleEngine re) {
        super(bc);
        this.mtManager = mtManager;
        this.ruleEngine = re;
    }

    @Override
    public Collection<String> getTypes() {
        // It is system factory and must not be registered as service.
        return null;
    }

    @Override
    public void ungetHandler(Module module, String ruleUID, ModuleHandler handler) {
        ModuleHandler handlerOfModule = handlers.get(ruleUID + module.getId());
        if (handlerOfModule instanceof AbstractCompositeModuleHandler) {
            AbstractCompositeModuleHandler<Module, ?, ?> h = (AbstractCompositeModuleHandler) handlerOfModule;
            Set<Module> modules = h.moduleHandlerMap.keySet();
            if (modules != null) {
                for (Module child : modules) {
                    ModuleHandlerFactory mhf = ruleEngine.getModuleHandlerFactory(child.getTypeUID(), ruleUID);
                    mhf.ungetHandler(child, ruleUID, handlerOfModule);
                }
            }
        }
        super.ungetHandler(module, ruleUID, handler);
    }

    @Override
    public ModuleHandler internalCreate(Module module, String ruleUID) {
        ModuleHandler handler = null;
        if (module != null) {
            logger.debug("create composite module:" + module + ", of rule: " + ruleUID);
            String moduleType = module.getTypeUID();
            ModuleType mt = mtManager.getType(moduleType);
            if (mt instanceof CompositeTriggerType) {
                List<Trigger> childModules = ((CompositeTriggerType) mt).getModules();
                LinkedHashMap<Trigger, TriggerHandler> mapModuleToHandler = getChildHandlers(module.getConfiguration(),
                        childModules, ruleUID);
                if (mapModuleToHandler != null) {
                    handler = new CompositeTriggerHandler((Trigger) module, (CompositeTriggerType) mt,
                            mapModuleToHandler, ruleUID);
                }
            } else if (mt instanceof CompositeConditionType) {
                List<Condition> childModules = ((CompositeConditionType) mt).getModules();
                LinkedHashMap<Condition, ConditionHandler> mapModuleToHandler = getChildHandlers(
                        module.getConfiguration(), childModules, ruleUID);
                if (mapModuleToHandler != null) {
                    handler = new CompositeConditionHandler((Condition) module, (CompositeConditionType) mt,
                            mapModuleToHandler, ruleUID);
                }
            } else if (mt instanceof CompositeActionType) {
                List<Action> childModules = ((CompositeActionType) mt).getModules();
                LinkedHashMap<Action, ActionHandler> mapModuleToHandler = getChildHandlers(module.getConfiguration(),
                        childModules, ruleUID);
                if (mapModuleToHandler != null) {
                    handler = new CompositeActionHandler((Action) module, (CompositeActionType) mt, mapModuleToHandler,
                            ruleUID);
                }
            }
        }

        logger.debug("Set handler to composite module:" + module + " -> " + handler);
        return handler;
    }

    private <T extends Module, MT extends ModuleHandler> LinkedHashMap<T, MT> getChildHandlers(
            Map<String, Object> compositeConfig, List<T> childModules, String ruleUID) {
        LinkedHashMap<T, MT> mapModuleToHandler = new LinkedHashMap<>();
        for (T child : childModules) {
            ruleEngine.updateMapModuleTypeToRule(ruleUID, child.getTypeUID());
            ModuleHandlerFactory childMhf = ruleEngine.getModuleHandlerFactory(child.getTypeUID(), ruleUID);
            if (childMhf == null) {
                mapModuleToHandler.clear();
                mapModuleToHandler = null;
                return null;
            }
            MT childHandler = (MT) childMhf.getHandler(child, ruleUID);
            if (childHandler == null) {
                mapModuleToHandler.clear();
                mapModuleToHandler = null;
                return null;
            }
            resolveConfigurationProperties(compositeConfig, child);
            mapModuleToHandler.put(child, childHandler);

            // if (child instanceof RuntimeTrigger) {
            // ((RuntimeTrigger) child).setModuleHandler((TriggerHandler) childHandler);
            // } else if (child instanceof RuntimeAction) {
            // ((RuntimeAction) child).setModuleHandler((ActionHandler) childHandler);
            // } else if (child instanceof RuntimeCondition) {
            // ((RuntimeCondition) child).setModuleHandler((ConditionHandler) childHandler);
            // } else {
            // logger.error("Invalid type of module. It must be runtime module: " + child.getClass());
            // }
            //
            // logger.debug("Set handler to:" + child + " -> " + childHandler);
        }
        return mapModuleToHandler;
    }

    private void resolveConfigurationProperties(Map<String, Object> compositeConfig, Module child) {
        Map<String, Object> childConfig = child.getConfiguration();
        for (Map.Entry<String, Object> e : childConfig.entrySet()) {
            Object value = e.getValue();
            if (value != null && value instanceof String) {
                String ref = (String) value;
                if (ref.startsWith("$") && ref.length() > 1) {
                    ref = ref.substring(1);
                    Object o = compositeConfig.get(ref);
                    if (o != null) {
                        e.setValue(o);
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        mtManager = null;
        ruleEngine = null;
    }

}
