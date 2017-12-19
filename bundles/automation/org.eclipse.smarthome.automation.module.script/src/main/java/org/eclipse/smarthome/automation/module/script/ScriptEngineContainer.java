/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
