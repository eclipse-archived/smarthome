/**
 * Copyright (c) 2015-2017 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.module.script.rulesupport.internal.ScriptedCustomModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.script.rulesupport.internal.ScriptedCustomModuleTypeProvider;
import org.eclipse.smarthome.automation.module.script.rulesupport.internal.ScriptedPrivateModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleActionHandler;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleConditionHandler;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleRuleActionHandler;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleRuleActionHandlerDelegate;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleTriggerHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Registry is used for a single ScriptEngine instance. It allows the adding and removing of handlers.
 * It allows the removal of previously added modules on unload.
 *
 * @author Simon Merschjohann
 *
 */
public class ScriptedAutomationManager {
    private static final Logger logger = LoggerFactory.getLogger(ScriptedAutomationManager.class);

    private RuleSupportRuleRegistryDelegate ruleRegistryDelegate;

    private HashSet<String> modules = new HashSet<>();
    private HashSet<String> moduleHandlers = new HashSet<>();
    private HashSet<String> privateHandlers = new HashSet<>();

    private ScriptedCustomModuleHandlerFactory scriptedCustomModuleHandlerFactory;
    private ScriptedCustomModuleTypeProvider scriptedCustomModuleTypeProvider;
    private ScriptedPrivateModuleHandlerFactory scriptedPrivateModuleHandlerFactory;

    public ScriptedAutomationManager(RuleSupportRuleRegistryDelegate ruleRegistryDelegate,
            ScriptedCustomModuleHandlerFactory scriptedCustomModuleHandlerFactory,
            ScriptedCustomModuleTypeProvider scriptedCustomModuleTypeProvider,
            ScriptedPrivateModuleHandlerFactory scriptedPrivateModuleHandlerFactory) {
        this.ruleRegistryDelegate = ruleRegistryDelegate;
        this.scriptedCustomModuleHandlerFactory = scriptedCustomModuleHandlerFactory;
        this.scriptedCustomModuleTypeProvider = scriptedCustomModuleTypeProvider;
        this.scriptedPrivateModuleHandlerFactory = scriptedPrivateModuleHandlerFactory;
    }

    public void removeModuleType(String UID) {
        if (modules.remove(UID)) {
            scriptedCustomModuleTypeProvider.removeModuleType(UID);
            removeHandler(UID);
        }
    }

    public void removeHandler(String typeUID) {
        if (moduleHandlers.remove(typeUID)) {
            scriptedCustomModuleHandlerFactory.removeModuleHandler(typeUID);
        }
    }

    public void removePrivateHandler(String privId) {
        if (privateHandlers.remove(privId)) {
            scriptedPrivateModuleHandlerFactory.removeHandler(privId);
        }
    }

    public void removeAll() {
        logger.info("removeAll added handlers");

        HashSet<String> types = new HashSet<>(modules);
        for (String moduleType : types) {
            removeModuleType(moduleType);
        }

        HashSet<String> moduleHandlers = new HashSet<>(this.moduleHandlers);
        for (String uid : moduleHandlers) {
            removeHandler(uid);
        }

        HashSet<String> privateHandlers = new HashSet<>(this.privateHandlers);
        for (String privId : privateHandlers) {
            removePrivateHandler(privId);
        }

        ruleRegistryDelegate.removeAllAddedByScript();
    }

    public Rule addRule(Rule element) {
        Rule rule = element.getUID() == null ? new Rule(generateUID()) : new Rule(element.getUID());

        String name = element.getName();
        if (name == null || name.isEmpty()) {
            name = element.getClass().getSimpleName();
            if (name.contains("$")) {
                name = name.substring(0, name.indexOf('$'));
            }
        }

        rule.setName(name);
        rule.setDescription(element.getDescription());
        rule.setTags(element.getTags());

        // used for numbering the modules of the rule
        int moduleIndex = 1;

        try {
            ArrayList<Condition> conditions = new ArrayList<>();
            for (Condition cond : element.getConditions()) {
                Condition toAdd = cond;
                if (cond.getId() == null || cond.getId().isEmpty()) {
                    toAdd = new Condition(Integer.toString(moduleIndex++), cond.getTypeUID(), cond.getConfiguration(),
                            cond.getInputs());
                }

                conditions.add(toAdd);
            }

            rule.setConditions(conditions);
        } catch (Exception ex) {
            // conditions are optional
        }

        try {
            ArrayList<Trigger> triggers = new ArrayList<>();
            for (Trigger trigger : element.getTriggers()) {
                Trigger toAdd = trigger;
                if (trigger.getId() == null || trigger.getId().isEmpty()) {
                    toAdd = new Trigger(Integer.toString(moduleIndex++), trigger.getTypeUID(),
                            trigger.getConfiguration());
                }

                triggers.add(toAdd);
            }

            rule.setTriggers(triggers);
        } catch (Exception ex) {
            // triggers are optional
        }

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(element.getActions());

        if (element instanceof SimpleRuleActionHandler) {
            String privId = addPrivateActionHandler(
                    new SimpleRuleActionHandlerDelegate((SimpleRuleActionHandler) element));

            Action scriptedAction = new Action(Integer.toString(moduleIndex++), "jsr223.ScriptedAction",
                    new Configuration(), null);
            scriptedAction.getConfiguration().put("privId", privId);
            actions.add(scriptedAction);
        }

        rule.setActions(actions);
        ruleRegistryDelegate.add(rule);

        return rule;
    }

    private String generateUID() {
        return UUID.randomUUID().toString();
    }

    public void addConditionType(ConditionType condititonType) {
        modules.add(condititonType.getUID());
        scriptedCustomModuleTypeProvider.addModuleType(condititonType);
    }

    public void addConditionHandler(String uid, ScriptedHandler conditionHandler) {
        moduleHandlers.add(uid);
        scriptedCustomModuleHandlerFactory.addModuleHandler(uid, conditionHandler);
        scriptedCustomModuleTypeProvider.updateModuleHandler(uid);
    }

    public String addPrivateConditionHandler(SimpleConditionHandler conditionHandler) {
        String uid = scriptedPrivateModuleHandlerFactory.addHandler(conditionHandler);
        privateHandlers.add(uid);
        return uid;
    }

    public void addActionType(ActionType actionType) {
        modules.add(actionType.getUID());
        scriptedCustomModuleTypeProvider.addModuleType(actionType);
    }

    public void addActionHandler(String uid, ScriptedHandler actionHandler) {
        moduleHandlers.add(uid);
        scriptedCustomModuleHandlerFactory.addModuleHandler(uid, actionHandler);
        scriptedCustomModuleTypeProvider.updateModuleHandler(uid);
    }

    public String addPrivateActionHandler(SimpleActionHandler actionHandler) {
        String uid = scriptedPrivateModuleHandlerFactory.addHandler(actionHandler);
        privateHandlers.add(uid);
        return uid;
    }

    public void addTriggerType(TriggerType triggerType) {
        modules.add(triggerType.getUID());
        scriptedCustomModuleTypeProvider.addModuleType(triggerType);
    }

    public void addTriggerHandler(String uid, ScriptedHandler triggerHandler) {
        moduleHandlers.add(uid);
        scriptedCustomModuleHandlerFactory.addModuleHandler(uid, triggerHandler);
        scriptedCustomModuleTypeProvider.updateModuleHandler(uid);
    }

    public String addPrivateTriggerHandler(SimpleTriggerHandler triggerHandler) {
        String uid = scriptedPrivateModuleHandlerFactory.addHandler(triggerHandler);
        privateHandlers.add(uid);
        return uid;
    }
}
