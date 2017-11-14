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

import javax.script.ScriptException;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.module.script.ScriptEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler can execute script actions.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Simon Merschjohann
 *
 */
public class ScriptActionHandler extends AbstractScriptModuleHandler<Action> implements ActionHandler {

    public static final String SCRIPT_ACTION_ID = "script.ScriptAction";

    private final Logger logger = LoggerFactory.getLogger(ScriptActionHandler.class);

    /**
     * constructs a new ScriptActionHandler
     *
     * @param module
     * @param ruleUid the UID of the rule this handler is used for
     */
    public ScriptActionHandler(Action module, String ruleUID, ScriptEngineManager scriptEngineManager) {
        super(module, ruleUID, scriptEngineManager);
    }

    @Override
    public void dispose() {
    }

    @Override
    public Map<String, Object> execute(final Map<String, Object> context) {
        HashMap<String, Object> resultMap = new HashMap<String, Object>();

        getScriptEngine().ifPresent(scriptEngine -> {
            setExecutionContext(scriptEngine, context);
            try {
                Object result = scriptEngine.eval(script);
                resultMap.put("result", result);
            } catch (ScriptException e) {
                logger.error("Script execution failed: {}", e.getMessage());
            }
        });

        return resultMap;
    }
}
