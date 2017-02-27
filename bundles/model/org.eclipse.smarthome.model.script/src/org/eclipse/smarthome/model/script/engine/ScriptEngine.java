/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.engine;

import org.eclipse.xtext.xbase.XExpression;

/**
 * The script engine is the main entrypoint for Eclipse SmartHome script use. It can build {@link Script} instances from
 * simple strings. These scripts can then be executed by the caller.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface ScriptEngine {

    /**
     * Parses a string and returns a parsed script object.
     *
     * @param scriptAsString script to parse
     * @return Script object, which can be executed
     * @throws ScriptParsingException
     */
    public Script newScriptFromString(final String scriptAsString) throws ScriptParsingException;

    /**
     * Executes a script that is passed as a string
     *
     * @param scriptAsString
     * @return the return value of the script
     * @throws ScriptParsingException
     * @throws ScriptExecutionException
     */
    public Object executeScript(final String scriptAsString) throws ScriptParsingException, ScriptExecutionException;

    /**
     * Wraps an Xbase XExpression in a Script instance
     *
     * @param expression the XExpression
     * @return the Script instance containing the expression
     */
    public Script newScriptFromXExpression(final XExpression expression);
}
