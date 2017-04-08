/**
 * Copyright (c) 2015-2017 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.smarthome.automation.module.script.ScriptEngineFactory;

public class GenericScriptEngineFactory implements ScriptEngineFactory {
    private ScriptEngineManager engineManager = new ScriptEngineManager();

    @Override
    public List<String> getLanguages() {
        ArrayList<String> languages = new ArrayList<>();

        for (javax.script.ScriptEngineFactory f : engineManager.getEngineFactories()) {
            languages.addAll(f.getExtensions());
        }

        return languages;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        for (Entry<String, Object> entry : scopeValues.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public ScriptEngine createScriptEngine(String fileExtension) {
        ScriptEngine engine = engineManager.getEngineByExtension(fileExtension);

        if (engine == null) {
            engine = engineManager.getEngineByName(fileExtension);
        }

        if (engine == null) {
            engine = engineManager.getEngineByMimeType(fileExtension);
        }

        return engine;
    }

    @Override
    public boolean isSupported(String fileExtension) {
        for (javax.script.ScriptEngineFactory f : engineManager.getEngineFactories()) {
            if (f.getExtensions().contains(fileExtension)) {
                return true;
            }
        }

        return false;
    }

}
