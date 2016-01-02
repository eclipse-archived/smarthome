/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedActionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedConditionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedTriggerHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleActionHandler;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleConditionHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.TriggerType;

public interface ScriptedHandlerRegistry {
    public void addConditionType(ConditionType condititonType);

    public void addConditionHandler(String uid, ScriptedConditionHandlerFactory conditionHandler);

    public void addConditionHandler(String uid, SimpleConditionHandler conditionHandler);

    public String addPrivateConditionHandler(SimpleConditionHandler conditionHandler);

    public void addActionType(ActionType actionType);

    public void addActionHandler(String uid, ScriptedActionHandlerFactory actionHandler);

    public void addActionHandler(String uid, SimpleActionHandler actionHandler);

    public String addPrivateActionHandler(SimpleActionHandler actionHandler);

    public void addTriggerType(TriggerType actionType);

    public void addTriggerHandler(String uid, ScriptedTriggerHandlerFactory triggerHandler);

    public void removeModule(String UID);

    public void removeHandler(String privId);

    Rule addRule(RuleClassInterface element);
}
