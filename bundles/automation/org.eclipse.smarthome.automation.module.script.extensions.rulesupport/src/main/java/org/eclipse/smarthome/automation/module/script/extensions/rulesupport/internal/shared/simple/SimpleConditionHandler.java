/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple;

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.handler.ScriptedHandler;

public abstract class SimpleConditionHandler implements ScriptedHandler {
    public void init(Condition condition) {
    }

    public abstract boolean isSatisfied(Condition condition, Map<String, ?> inputs);
}
