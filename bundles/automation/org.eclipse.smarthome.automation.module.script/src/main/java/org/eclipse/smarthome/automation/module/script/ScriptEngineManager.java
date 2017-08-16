/** 
 * Copyright (c) 2015-2017 Simon Merschjohann and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.eclipse.smarthome.automation.module.script;

import java.io.InputStreamReader;

/**
 *
 * @author Simon Merschjohann - Initial contribution
 */
public interface ScriptEngineManager {

    /**
     * Checks if a given fileExtension is supported
     *
     * @param fileExtension
     * @return true if supported
     */
    boolean isSupported(String fileExtension);

    /**
     * Creates a new ScriptEngine based on the given fileExtension
     *
     * @param fileExtension
     * @param scriptIdentifier
     * @return
     */
    ScriptEngineContainer createScriptEngine(String fileExtension, String scriptIdentifier);

    /**
     * Loads a script and initializes its scope variables
     *
     * @param fileExtension
     * @param scriptIdentifier
     * @param scriptData
     * @return
     */
    void loadScript(String scriptIdentifier, InputStreamReader scriptData);

    /**
     * Unloads the ScriptEngine loaded with the scriptIdentifer
     *
     * @param scriptIdentifier
     */
    void removeEngine(String scriptIdentifier);

}
