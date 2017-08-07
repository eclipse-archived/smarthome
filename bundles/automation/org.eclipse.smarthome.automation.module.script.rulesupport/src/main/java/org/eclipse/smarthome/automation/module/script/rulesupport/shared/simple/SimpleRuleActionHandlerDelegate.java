/** 
 * Copyright (c) 2015-2017 Simon Merschjohann and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple;

import java.util.Map;

import org.eclipse.smarthome.automation.Action;

/**
 *
 * @author Simon Merschjohann - Initial contribution
 */
public class SimpleRuleActionHandlerDelegate extends SimpleActionHandler {

    private SimpleRuleActionHandler handler;

    public SimpleRuleActionHandlerDelegate(SimpleRuleActionHandler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public Object execute(Action module, Map<String, ?> inputs) {
        return handler.execute(module, inputs);
    }

}
