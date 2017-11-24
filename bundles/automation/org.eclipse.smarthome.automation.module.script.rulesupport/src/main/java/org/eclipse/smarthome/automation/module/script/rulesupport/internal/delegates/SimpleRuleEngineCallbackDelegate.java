/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.automation.module.script.rulesupport.internal.delegates;

import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleRuleEngineCallback;

/**
 * The {@link SimpleRuleEngineCallbackDelegate} allows a script to define callbacks for triggers in different ways.
 *
 * @author Simon Merschjohann
 *
 */
public class SimpleRuleEngineCallbackDelegate implements SimpleRuleEngineCallback {
    private Trigger trigger;
    private RuleEngineCallback callback;

    public SimpleRuleEngineCallbackDelegate(Trigger trigger, RuleEngineCallback callback) {
        this.trigger = trigger;
        this.callback = callback;
    }

    @Override
    public void triggered(Trigger trigger, Map<String, ?> context) {
        callback.triggered(trigger, context);
    }

    @Override
    public void triggered(Map<String, ?> context) {
        callback.triggered(this.trigger, context);
    }
}
