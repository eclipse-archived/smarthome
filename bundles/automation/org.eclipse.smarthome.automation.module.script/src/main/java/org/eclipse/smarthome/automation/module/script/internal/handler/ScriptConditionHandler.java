/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.internal.handler;

import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.module.script.internal.ScriptModuleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler can evaluate a condition based on a script.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class ScriptConditionHandler extends AbstractScriptModuleHandler<Condition>implements ConditionHandler {

    public final Logger logger = LoggerFactory.getLogger(ScriptConditionHandler.class);

    public static final String SCRIPT_CONDITION = "ScriptCondition";

    public ScriptConditionHandler(Condition module) {
        super(module);
    }

    @Override
    public boolean isSatisfied(Map<String, ?> context) {
        Object type = module.getConfiguration().get(SCRIPT_TYPE);
        Object script = module.getConfiguration().get(SCRIPT);
        if (type instanceof String) {
            if (script instanceof String) {
                ScriptEngine engine = ScriptModuleActivator.getScriptEngine((String) type);
                if (engine != null) {
                    ScriptContext executionContext = getExecutionContext(engine, context);
                    try {
                        Object returnVal = engine.eval((String) script, executionContext);
                        if (returnVal instanceof Boolean) {
                            return (boolean) returnVal;
                        } else {
                            logger.error("Script did not return a boolean value, but '{}'", returnVal.toString());
                        }
                    } catch (ScriptException e) {
                        logger.error("Script execution failed: {}", e.getMessage());
                    }
                } else {
                    logger.debug("No engine available for script type '{}' in condition '{}'.",
                            new Object[] { type, module.getId() });
                }
            } else {
                logger.debug("Script is missing in the configuration of condition '{}'.", module.getId());
            }
        } else {
            logger.debug("Script type is missing in the configuration of action '{}'.", module.getId());
        }
        return true;
    }

}
