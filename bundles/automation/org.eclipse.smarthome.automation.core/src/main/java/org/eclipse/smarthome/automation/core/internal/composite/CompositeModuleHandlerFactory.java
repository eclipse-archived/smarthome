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

/**
 * This class is a factory for system module handler for modules of composite module types: {@link CompositeTriggerType}
 * , {@link CompositeConditionType} and {@link CompositeActionType}. The composite module type is a type which contains
 * one or more internal (child) modules and these modules have access to configuration properties and inputs of
 * composite module. The outputs of module of composite type (if they exists) are set these handlers and they are base
 * on the values of child module outputs.
 * The {@link CompositeModuleHandlerFactory} is a system handler factory and it is not registered as service in OSGi
 * framework, but it will be used by the rule engine to serve composite module types without any action of the user.
 *
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class CompositeModuleHandlerFactory extends BaseModuleHandlerFactory implements ModuleHandlerFactory {

    private ModuleTypeManager mtManager;
    private RuleEngine ruleEngine;
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The constructor of system handler factory for composite module types
     *
     * @param bc is a bundle context
     * @param mtManager is a module type manager
     * @param re is a rule engine
     */
    public CompositeModuleHandlerFactory(BundleContext bc, ModuleTypeManager mtManager, RuleEngine re) {
        super(bc);
        this.mtManager = mtManager;
        this.ruleEngine = re;
    }

    /**
     * It is system factory and must not be registered as service. This method is not used.
     *
     * @see org.eclipse.smarthome.automation.handler.ModuleHandlerFactory#getTypes()
     */
    @Override
    public Collection<String> getTypes() {
        return null;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void ungetHandler(Module module, String ruleUID, ModuleHandler handler) {
        ModuleHandler handlerOfModule = handlers.get(ruleUID + module.getId());
        if (handlerOfModule instanceof AbstractCompositeModuleHandler) {
            AbstractCompositeModuleHandler<Module, ?, ?> h = (AbstractCompositeModuleHandler<Module, ?, ?>) handlerOfModule;
            Set<Module> modules = h.moduleHandlerMap.keySet();
            if (modules != null) {
                for (Module child : modules) {
                    ModuleHandler childHandler = h.moduleHandlerMap.get(child);
                    ModuleHandlerFactory mhf = ruleEngine.getModuleHandlerFactory(child.getTypeUID(), ruleUID);
                    mhf.ungetHandler(child, ruleUID, childHandler);
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
            ModuleType mt = mtManager.get(moduleType);
            if (mt instanceof CompositeTriggerType) {
                List<Trigger> childModules = ((CompositeTriggerType) mt).getChildren();
                LinkedHashMap<Trigger, TriggerHandler> mapModuleToHandler = getChildHandlers(module.getConfiguration(),
                        childModules, ruleUID);
                if (mapModuleToHandler != null) {
                    handler = new CompositeTriggerHandler((Trigger) module, (CompositeTriggerType) mt,
                            mapModuleToHandler, ruleUID);
                }
            } else if (mt instanceof CompositeConditionType) {
                List<Condition> childModules = ((CompositeConditionType) mt).getChildren();
                LinkedHashMap<Condition, ConditionHandler> mapModuleToHandler = getChildHandlers(
                        module.getConfiguration(), childModules, ruleUID);
                if (mapModuleToHandler != null) {
                    handler = new CompositeConditionHandler((Condition) module, (CompositeConditionType) mt,
                            mapModuleToHandler, ruleUID);
                }
            } else if (mt instanceof CompositeActionType) {
                List<Action> childModules = ((CompositeActionType) mt).getChildren();
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

    /**
     * This method associates module handlers to the child modules of composite module types. It links module types of
     * child modules to the rule which contains this composite module. It also resolve links between child configuration
     * properties and configuration of composite module see: {@link #resolveConfigurationProperties(Map, Module)}.
     *
     * @param compositeConfig configuration values of composite module.
     * @param childModules list of child modules
     * @param ruleUID UID of rule where the composite module is participating
     * @return map of pairs of module and its handler. Return null when some of the child modules can not find its
     *         handler.
     */
    @SuppressWarnings("unchecked")
    private <T extends Module, MT extends ModuleHandler> LinkedHashMap<T, MT> getChildHandlers(
            Map<String, Object> compositeConfig, List<T> childModules, String ruleUID) {
        LinkedHashMap<T, MT> mapModuleToHandler = new LinkedHashMap<T, MT>();
        for (T child : childModules) {
            ruleEngine.updateMapModuleTypeToRule(ruleUID, child.getTypeUID());
            ModuleHandlerFactory childMhf = ruleEngine.getModuleHandlerFactory(child.getTypeUID(), ruleUID);
            if (childMhf == null) {
                mapModuleToHandler.clear();
                mapModuleToHandler = null;
                return null;
            }
            resolveConfigurationProperties(compositeConfig, child);
            MT childHandler = (MT) childMhf.getHandler(child, ruleUID);
            if (childHandler == null) {
                mapModuleToHandler.clear();
                mapModuleToHandler = null;
                return null;
            }
            mapModuleToHandler.put(child, childHandler);

        }
        return mapModuleToHandler;
    }

    /**
     * Resolves links between child configuration property values and composite config property ones.
     * When the child configuration value is a string and start "$" sign, the rest of value is a
     * name of configuration property of composite type. This method gets this composite configuration value and sets
     * it as child configuration values.
     *
     * @param compositeConfig configuration of composite module type.
     * @param child child module of composite module type.
     */
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
