/**
 * Copyright (c) 2015-2017 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.internal.delegates;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;

/**
 * The {@link SimpleTriggerHandlerDelegate} allows to define triggers in a script language in different ways.
 *
 * @author Simon Merschjohann
 */
public class SimpleTriggerHandlerDelegate extends BaseModuleHandler<Trigger> implements TriggerHandler {
    private org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleTriggerHandler triggerHandler;

    public SimpleTriggerHandlerDelegate(Trigger module,
            org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleTriggerHandler triggerHandler) {
        super(module);
        this.triggerHandler = triggerHandler;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        triggerHandler.setRuleEngineCallback(this.module,
                new SimpleRuleEngineCallbackDelegate(this.module, ruleCallback));
    }
}
