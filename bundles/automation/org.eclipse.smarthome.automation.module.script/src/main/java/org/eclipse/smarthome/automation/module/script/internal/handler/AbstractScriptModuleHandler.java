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
import java.util.Map.Entry;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;

/**
 * This is an abstract class that can be used when implementing any module handler that handles scripts.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 * @param <T> the type of module the concrete handler can handle
 */
abstract public class AbstractScriptModuleHandler<T extends Module> extends BaseModuleHandler<T> {

    /** Constant defining the configuration parameter of modules that specifies the mime type of a script */
    protected static final String SCRIPT_TYPE = "type";

    /** Constant defining the configuration parameter of modules that specifies the script itself */
    protected static final String SCRIPT = "script";

    private ScriptContext executionContext;

    public AbstractScriptModuleHandler(T module) {
        super(module);
    }

    /**
     * Gets an instance of the script context for the current module.
     * The instance is kept across the lifetime of the handler, i.e. it will remain the same over multiple executions of
     * the module.
     *
     * @param engine the scriptengine that is used
     * @param context the variables and types to put into the execution context
     * @return the scope instance used for the module
     */
    protected synchronized ScriptContext getExecutionContext(ScriptEngine engine, Map<String, ?> context) {
        if (executionContext == null) {
            executionContext = engine.getContext();
        }
        // add the rule context to the script engine (only for this execution)
        for (Entry<String, ?> entry : context.entrySet()) {

            final HashMap<String, Object> jsonObj = new HashMap<String, Object>();
            Object value = entry.getValue();
            String key = entry.getKey();
            int dotIndex = key.indexOf('.');
            if (dotIndex != -1) {
                String jsonKey = key.substring(dotIndex + 1);
                key = key.substring(0, dotIndex);
                jsonObj.put(jsonKey, value);
                executionContext.setAttribute(key, jsonObj, ScriptContext.ENGINE_SCOPE);
            } else {
                executionContext.setAttribute(key, value, ScriptContext.ENGINE_SCOPE);
            }
        }
        return executionContext;
    }

}
