/** 
 * Copyright (c) 2015-2017 Simon Merschjohann and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.eclipse.smarthome.automation.module.script;

import javax.script.ScriptEngine;

/**
 *
 * @author Simon Merschjohann - Initial contribution
 */
public class ScriptEngineContainer {
    private ScriptEngine scriptEngine;
    private ScriptEngineFactory factory;
    private String identifier;

    public ScriptEngineContainer(ScriptEngine scriptEngine, ScriptEngineFactory factory, String identifier) {
        super();
        this.scriptEngine = scriptEngine;
        this.factory = factory;
        this.identifier = identifier;
    }

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public ScriptEngineFactory getFactory() {
        return factory;
    }

    public String getIdentifier() {
        return identifier;
    }
}
