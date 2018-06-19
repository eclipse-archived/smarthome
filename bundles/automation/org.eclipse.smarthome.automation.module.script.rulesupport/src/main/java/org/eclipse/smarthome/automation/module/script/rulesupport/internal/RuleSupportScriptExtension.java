/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.module.script.ScriptExtensionProvider;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.RuleSupportRuleRegistryDelegate;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.ScriptedAutomationManager;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.ScriptedRuleProvider;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.factories.ScriptedActionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.factories.ScriptedConditionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.factories.ScriptedTriggerHandlerFactory;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleActionHandler;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleConditionHandler;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleRule;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleTriggerHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This Script-Extension provides types and presets to support writing Rules using a ScriptEngine.
 * One can write and register Rules and Modules by adding them through the HandlerRegistry and/or RuleRegistry
 *
 * @author Simon Merschjohann
 *
 */
public class RuleSupportScriptExtension implements ScriptExtensionProvider {
    private static final String RULE_SUPPORT = "RuleSupport";
    private static final String RULE_REGISTRY = "ruleRegistry";
    private static final String AUTOMATION_MANAGER = "automationManager";

    private static HashMap<String, Collection<String>> presets = new HashMap<>();
    private static HashMap<String, Object> staticTypes = new HashMap<>();
    private static HashSet<String> types = new HashSet<String>();
    private final ConcurrentHashMap<String, HashMap<String, Object>> objectCache = new ConcurrentHashMap<>();

    private RuleRegistry ruleRegistry;
    private ScriptedRuleProvider ruleProvider;
    private ScriptedCustomModuleHandlerFactory scriptedCustomModuleHandlerFactory;
    private ScriptedCustomModuleTypeProvider scriptedCustomModuleTypeProvider;
    private ScriptedPrivateModuleHandlerFactory scriptedPrivateModuleHandlerFactory;

    static {
        staticTypes.put("SimpleActionHandler", SimpleActionHandler.class);
        staticTypes.put("SimpleConditionHandler", SimpleConditionHandler.class);
        staticTypes.put("SimpleTriggerHandler", SimpleTriggerHandler.class);
        staticTypes.put("SimpleRule", SimpleRule.class);

        staticTypes.put("ActionHandlerFactory", ScriptedActionHandlerFactory.class);
        staticTypes.put("ConditionHandlerFactory", ScriptedConditionHandlerFactory.class);
        staticTypes.put("TriggerHandlerFactory", ScriptedTriggerHandlerFactory.class);

        staticTypes.put("Configuration", Configuration.class);
        staticTypes.put("Action", Action.class);
        staticTypes.put("Condition", Condition.class);
        staticTypes.put("Trigger", Trigger.class);
        staticTypes.put("Rule", Rule.class);
        staticTypes.put("ModuleType", ModuleType.class);
        staticTypes.put("ActionType", ActionType.class);
        staticTypes.put("TriggerType", TriggerType.class);
        staticTypes.put("Visibility", Visibility.class);
        staticTypes.put("ConfigDescriptionParameter", ConfigDescriptionParameter.class);

        types.addAll(staticTypes.keySet());

        types.add(AUTOMATION_MANAGER);
        types.add(RULE_REGISTRY);

        presets.put(RULE_SUPPORT, Arrays.asList("Configuration", "Action", "Condition", "Trigger", "Rule"));
        presets.put("RuleSimple", Arrays.asList("SimpleActionHandler", "SimpleConditionHandler", "SimpleTriggerHandler",
                "SimpleRule", "TriggerType", "ConfigDescriptionParameter", "ModuleType", "ActionType", "Visibility"));
        presets.put("RuleFactories",
                Arrays.asList("ActionHandlerFactory", "ConditionHandlerFactory", "TriggerHandlerFactory", "TriggerType",
                        "ConfigDescriptionParameter", "ModuleType", "ActionType", "Visibility"));
    }

    public void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    public void setRuleProvider(ScriptedRuleProvider ruleProvider) {
        this.ruleProvider = ruleProvider;
    }

    public void setScriptedCustomModuleHandlerFactory(ScriptedCustomModuleHandlerFactory factory) {
        this.scriptedCustomModuleHandlerFactory = factory;
    }

    public void setScriptedCustomModuleTypeProvider(ScriptedCustomModuleTypeProvider scriptedCustomModuleTypeProvider) {
        this.scriptedCustomModuleTypeProvider = scriptedCustomModuleTypeProvider;
    }

    public void setScriptedPrivateModuleHandlerFactory(ScriptedPrivateModuleHandlerFactory factory) {
        this.scriptedPrivateModuleHandlerFactory = factory;
    }

    @Override
    public Collection<String> getDefaultPresets() {
        return Collections.emptyList();
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
    public Object get(String scriptIdentifier, String type) {
        Object obj = staticTypes.get(type);
        if (obj != null) {
            return obj;
        }

        HashMap<String, Object> objects = objectCache.get(scriptIdentifier);

        if (objects == null) {
            objects = new HashMap<>();
            objectCache.put(scriptIdentifier, objects);
        }

        obj = objects.get(type);
        if (obj != null) {
            return obj;
        }

        if (type.equals(AUTOMATION_MANAGER) || type.equals(RULE_REGISTRY)) {
            RuleSupportRuleRegistryDelegate ruleRegistryDelegate = new RuleSupportRuleRegistryDelegate(ruleRegistry,
                    ruleProvider);
            ScriptedAutomationManager automationManager = new ScriptedAutomationManager(ruleRegistryDelegate,
                    scriptedCustomModuleHandlerFactory, scriptedCustomModuleTypeProvider,
                    scriptedPrivateModuleHandlerFactory);

            objects.put(AUTOMATION_MANAGER, automationManager);
            objects.put(RULE_REGISTRY, ruleRegistryDelegate);

            obj = objects.get(type);
        }

        return obj;
    }

    @Override
    public Map<String, Object> importPreset(String scriptIdentifier, String preset) {
        Map<String, Object> scopeValues = new HashMap<>();

        Collection<String> values = presets.get(preset);

        for (String value : values) {
            scopeValues.put(value, staticTypes.get(value));
        }

        if (preset.equals(RULE_SUPPORT)) {
            scopeValues.put(AUTOMATION_MANAGER, get(scriptIdentifier, AUTOMATION_MANAGER));

            Object ruleRegistry = get(scriptIdentifier, RULE_REGISTRY);
            scopeValues.put(RULE_REGISTRY, ruleRegistry);
        }

        return scopeValues;
    }

    @Override
    public void unload(String scriptIdentifier) {
        HashMap<String, Object> objects = objectCache.remove(scriptIdentifier);

        if (objects != null) {
            Object hr = objects.get(AUTOMATION_MANAGER);
            if (hr != null) {
                ScriptedAutomationManager automationManager = (ScriptedAutomationManager) hr;

                automationManager.removeAll();
            }
        }
    }

}
