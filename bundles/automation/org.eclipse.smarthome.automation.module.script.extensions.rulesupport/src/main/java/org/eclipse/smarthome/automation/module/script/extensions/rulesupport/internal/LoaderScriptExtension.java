/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.module.script.ScriptExtensionProvider;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.ScriptedRule;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.builder.ActionBuilder;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.builder.ConditionBuilder;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.builder.RuleBuilder;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.builder.TriggerBuilder;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedActionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedConditionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedTriggerHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleActionHandler;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleConditionHandler;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleRule;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoaderScriptExtension implements ScriptExtensionProvider {
    private final static Logger logger = LoggerFactory.getLogger(LoaderScriptExtension.class);

    private static RuleRegistry ruleRegistry;

    private static HashMap<String, Collection<String>> presets = new HashMap<>();

    private static HashMap<String, Object> staticTypes = new HashMap<>();
    private static HashSet<String> types = new HashSet<String>();

    private ConcurrentHashMap<Integer, HashMap<String, Object>> objectCache = new ConcurrentHashMap<>();

    static {
        staticTypes.put("ActionBuilder", ActionBuilder.class);
        staticTypes.put("ConditionBuilder", ConditionBuilder.class);
        staticTypes.put("RuleBuilder", RuleBuilder.class);
        staticTypes.put("TriggerBuilder", TriggerBuilder.class);

        staticTypes.put("ScriptedRule", ScriptedRule.class);
        staticTypes.put("SimpleRule", SimpleRule.class);
        staticTypes.put("SimpleActionHandler", SimpleActionHandler.class);
        staticTypes.put("SimpleConditionHandler", SimpleConditionHandler.class);

        staticTypes.put("ActionHandlerFactory", ScriptedActionHandlerFactory.class);
        staticTypes.put("ConditionHandlerFactory", ScriptedConditionHandlerFactory.class);
        staticTypes.put("TriggerHandlerFactory", ScriptedTriggerHandlerFactory.class);

        staticTypes.put("Action", Action.class);
        staticTypes.put("Condition", Condition.class);
        staticTypes.put("Trigger", Trigger.class);
        staticTypes.put("Rule", Rule.class);
        staticTypes.put("ModuleType", ModuleType.class);
        staticTypes.put("ActionType", ActionType.class);
        staticTypes.put("ConfigDescriptionParameter", ConfigDescriptionParameter.class);

        types.addAll(staticTypes.keySet());

        types.add("HandlerRegistry");
        types.add("RuleRegistry");
        types.add("rules");

        presets.put("RuleSupport", Arrays.asList("Action", "Condition", "Trigger", "Rule", "ModuleType", "ActionType"));
        presets.put("RuleBuilder", Arrays.asList("ActionBuilder", "ConditionBuilder", "RuleBuilder", "TriggerBuilder"));
        presets.put("RuleSimple", Arrays.asList("ScriptedRule", "SimpleRule"));
        presets.put("RuleFactories",
                Arrays.asList("ActionHandlerFactory", "ConditionHandlerFactory", "TriggerHandlerFactory"));
    }

    public static RuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    public void setRuleRegistry(RuleRegistry ruleRegistry) {
        logger.info("rule registry registered");
        LoaderScriptExtension.ruleRegistry = ruleRegistry;
    }

    @Override
    public Collection<String> getDefaultPresets() {
        return new ArrayList<String>(0);
    }

    @Override
    public Collection<String> getPresets() {
        return presets.keySet();
    }

    @Override
    public Collection<String> getTypes() {
        return types;
    }

    @Override
    public Object get(int scriptEngineId, String type) {
        Object obj = staticTypes.get(type);
        if (obj != null) {
            return obj;
        }

        HashMap<String, Object> objects = objectCache.get(scriptEngineId);

        if (objects == null) {
            objects = new HashMap<>();
            objectCache.put(scriptEngineId, objects);
        }

        obj = objects.get(type);
        if (obj != null) {
            return obj;
        }

        if (type.equals("HandlerRegistry") || type.equals("RuleRegistry")) {
            ScriptedHandlerRegistryImpl handlerRegistry = new ScriptedHandlerRegistryImpl(
                    LoaderScriptExtension.getRuleRegistry(), RuleSupportActivator.getModuleHandlerFactory());
            objects.put("HandlerRegistry", handlerRegistry);
            RuleRegistry ruleRegistry = handlerRegistry.getRuleRegistry();
            objects.put("RuleRegistry", ruleRegistry);

            obj = objects.get(type);
        }

        return obj;
    }

    @Override
    public Map<String, Object> importPreset(int scriptEngineId, String preset) {
        Map<String, Object> scopeValues = new HashMap<>();

        Collection<String> values = presets.get(preset);

        for (String value : values) {
            scopeValues.put(value, staticTypes.get(value));
        }

        if (preset.equals("RuleSupport")) {
            scopeValues.put("HandlerRegistry", get(scriptEngineId, "HandlerRegistry"));

            Object ruleRegistry = get(scriptEngineId, "RuleRegistry");
            scopeValues.put("RuleRegistry", ruleRegistry);
            scopeValues.put("rules", ruleRegistry);
        }

        return scopeValues;
    }

    @Override
    public void unLoad(int scriptEngineId) {
        HashMap<String, Object> objects = objectCache.remove(scriptEngineId);

        if (objects != null) {
            Object hr = objects.get("HandlerRegistry");
            if (hr != null) {
                ScriptedHandlerRegistryImpl handlerRegistry = (ScriptedHandlerRegistryImpl) hr;

                handlerRegistry.removeAll();
            }
        }
    }

}
