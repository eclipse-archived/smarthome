/** 
 * Copyright (c) 2015-2017 Simon Merschjohann and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.eclipse.smarthome.automation.module.script;

import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;

/**
 * This class is used by the ScriptManager to load ScriptEngines.
 * This is meant as a way to allow other OSGi bundles to provide custom Script-Languages with special needs (like
 * Nashorn, Groovy, etc.)
 *
 * @author Simon Merschjohann
 *
 */
public interface ScriptEngineFactory {

    /**
     * @return the list of supported language endings e.g. py, jy
     */
    List<String> getLanguages();

    /**
     * "scopes" new values into the given ScriptEngine
     *
     * @param scriptEngine
     * @param scopeValues
     */
    void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues);

    /**
     * created a new ScriptEngine
     *
     * @param fileExtension
     * @return
     */
    ScriptEngine createScriptEngine(String fileExtension);

    /**
     * checks if the script is supported. Does not necessarily be equal to getLanguages()
     *
     * @param fileExtension
     * @return
     */
    boolean isSupported(String fileExtension);

}
