/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.internal.handler;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.module.script.internal.ScriptModuleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler can execute script actions.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ScriptActionHandler extends AbstractScriptModuleHandler<Action> implements ActionHandler {

    public static final String SCRIPT_ACTION_ID = "script.ScriptAction";

    private final Logger logger = LoggerFactory.getLogger(ScriptActionHandler.class);
    private final String ruleUid;

    /**
     * constructs a new ScriptActionHandler
     *
     * @param module
     * @param ruleUid the UID of the rule this handler is used for
     */
    public ScriptActionHandler(Action module, final String ruleUid) {
        super(module);
        this.ruleUid = ruleUid;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Map<String, Object> execute(final Map<String, ?> context) {
        Object tmp;
        tmp = module.getConfiguration().get(SCRIPT_TYPE);
        if (tmp instanceof String) {
            final String type = (String) tmp;
            tmp = module.getConfiguration().get(SCRIPT);
            if (tmp instanceof String) {
                final String script = (String) tmp;
                final ScriptEngine engine = ScriptModuleActivator.getScriptEngine(type);
                if (engine != null) {
                    return execute(engine, new HashMap<>(context), type, script);
                } else {
                    logger.debug("No engine available for script type '{}' in action '{}'.", type, module.getId());
                }
            } else {
                logger.debug("Script is missing in the configuration of action '{}'.", module.getId());
            }
        } else {
            logger.debug("Script type is missing in the configuration of action '{}'.", module.getId());
        }
        return null;
    }

    private Map<String, Object> execute(final ScriptEngine engine, final Map<String, Object> context,
            final String scriptType, final String script) {
        context.put("ruleUID", ruleUid);
        final ScriptContext executionContext = getExecutionContext(engine, context);
        try {
            Object result = engine.eval(script, executionContext);
            HashMap<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("result", result);
            return resultMap;
        } catch (ScriptException e) {
            logger.error("Script execution failed: {}", e.getMessage());
            return null;
        }
    }

}
