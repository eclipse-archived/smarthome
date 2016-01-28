/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal;

import java.util.HashSet;
import java.util.UUID;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.factory.ScriptedModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.LoaderRuleRegistry;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.RuleClassInterface;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.ScriptedHandlerRegistry;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.builder.ActionBuilder;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.builder.RuleBuilder;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedActionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedConditionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedTriggerHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleActionHandler;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleConditionHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptedHandlerRegistryImpl implements ScriptedHandlerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ScriptedHandlerRegistryImpl.class);

    private LoaderRuleRegistry ruleRegistry;

    private ScriptedModuleHandlerFactory scriptedModuleHandlerFactory;

    private HashSet<ModuleType> modules = new HashSet<>();
    private HashSet<String> moduleHandlers = new HashSet<>();
    private HashSet<String> privateHandlers = new HashSet<>();
    private HashSet<Rule> rules = new HashSet<>();

    public ScriptedHandlerRegistryImpl(RuleRegistry ruleRegistry,
            ScriptedModuleHandlerFactory scriptedModuleHandlerFactory) {
        this.ruleRegistry = new LoaderRuleRegistry(ruleRegistry);
        this.scriptedModuleHandlerFactory = scriptedModuleHandlerFactory;
    }

    @Override
    public void removeModule(String UID) {
        if (modules.remove(UID)) {
            scriptedModuleHandlerFactory.removeModule(UID);
        }
    }

    @Override
    public void removeHandler(String privId) {
        if (privateHandlers.remove(privId)) {
            scriptedModuleHandlerFactory.removeHandler(privId);
        }
    }

    public void removeAll() {
        logger.info("removeAll added handlers");

        for (ModuleType moduleType : modules) {
            scriptedModuleHandlerFactory.removeModule(moduleType.getUID());
        }
        modules.clear();

        for (String uid : moduleHandlers) {
            scriptedModuleHandlerFactory.removeModule(uid);
        }
        moduleHandlers.clear();

        for (String privId : privateHandlers) {
            scriptedModuleHandlerFactory.removeHandler(privId);
        }
        privateHandlers.clear();

        ruleRegistry.removeAllAddedByScript();
    }

    public LoaderRuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    @Override
    public Rule addRule(RuleClassInterface element) {
        RuleBuilder ruleBuilder = new RuleBuilder();

        String uid = element.getUid() != null ? element.getUid()
                : element.getClass().getSimpleName() + "_" + UUID.randomUUID();
        String name = element.getName() != null ? element.getName() : uid;

        ruleBuilder.setUID(uid);
        ruleBuilder.setName(name);
        ruleBuilder.setVisibility(Visibility.HIDDEN);

        try {
            for (Condition condition : element.getConditions()) {
                ruleBuilder.addCondition(condition);
            }
        } catch (Exception ex) {
            // conditions are optional
        }

        for (Trigger trigger : element.getTriggers()) {
            ruleBuilder.addTrigger(trigger);
        }

        if (element.getActions() != null) {
            for (Action action : element.getActions()) {
                ruleBuilder.addAction(action);
            }
        }

        if (element instanceof SimpleActionHandler) {
            String privId = addPrivateActionHandler((SimpleActionHandler) element);
            ruleBuilder.addAction(new ActionBuilder().setTypeUID("ScriptedAction").addConfiguration("privId", privId));
        }

        Rule rule = ruleBuilder.build();
        ruleRegistry.add(rule);

        return rule;
    }

    @Override
    public void addConditionType(ConditionType condititonType) {
        modules.add(condititonType);
        scriptedModuleHandlerFactory.addModuleType(condititonType);
    }

    @Override
    public void addConditionHandler(String uid, ScriptedConditionHandlerFactory conditionHandler) {
        moduleHandlers.add(uid);
        this.scriptedModuleHandlerFactory.addModuleHandler(uid, conditionHandler);
    }

    @Override
    public void addConditionHandler(String uid, SimpleConditionHandler conditionHandler) {
        moduleHandlers.add(uid);
        this.scriptedModuleHandlerFactory.addModuleHandler(uid, conditionHandler);
    }

    @Override
    public String addPrivateConditionHandler(SimpleConditionHandler conditionHandler) {
        String uid = this.scriptedModuleHandlerFactory.addHandler(conditionHandler);
        privateHandlers.add(uid);
        return uid;
    }

    @Override
    public void addActionType(ActionType actionType) {
        modules.add(actionType);
        scriptedModuleHandlerFactory.addModuleType(actionType);
    }

    @Override
    public void addActionHandler(String uid, ScriptedActionHandlerFactory actionHandler) {
        moduleHandlers.add(uid);
        this.scriptedModuleHandlerFactory.addModuleHandler(uid, actionHandler);
    }

    @Override
    public void addActionHandler(String uid, SimpleActionHandler actionHandler) {
        moduleHandlers.add(uid);
        this.scriptedModuleHandlerFactory.addModuleHandler(uid, actionHandler);
    }

    @Override
    public String addPrivateActionHandler(SimpleActionHandler actionHandler) {
        String uid = this.scriptedModuleHandlerFactory.addHandler(actionHandler);
        privateHandlers.add(uid);
        return uid;
    }

    @Override
    public void addTriggerType(TriggerType triggerType) {
        modules.add(triggerType);
        scriptedModuleHandlerFactory.addModuleType(triggerType);
    }

    @Override
    public void addTriggerHandler(String uid, ScriptedTriggerHandlerFactory triggerHandler) {
        moduleHandlers.add(uid);
        this.scriptedModuleHandlerFactory.addModuleHandler(uid, triggerHandler);
    }
}
