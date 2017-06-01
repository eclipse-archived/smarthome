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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.script.ScriptEngine;

import org.eclipse.smarthome.automation.module.script.ScriptEngineFactory;
import org.eclipse.smarthome.automation.module.script.ScriptExtensionProvider;

/**
 * This manager allows a script import extension providers
 *
 * @author Simon Merschjohann
 *
 */
public class ScriptExtensionManager {
    private static Set<ScriptExtensionProvider> scriptExtensionProviders = new CopyOnWriteArraySet<ScriptExtensionProvider>();

    public static Set<ScriptExtensionProvider> getScriptExtensionProviders() {
        return scriptExtensionProviders;
    }

    public void addScriptExtensionProvider(ScriptExtensionProvider provider) {
        scriptExtensionProviders.add(provider);
    }

    public void removeScriptExtensionProvider(ScriptExtensionProvider provider) {
        scriptExtensionProviders.remove(provider);
    }

    public static void addExtension(ScriptExtensionProvider provider) {
        scriptExtensionProviders.add(provider);
    }

    public static void removeExtension(ScriptExtensionProvider provider) {
        scriptExtensionProviders.remove(provider);
    }

    public static List<String> getTypes() {
        ArrayList<String> types = new ArrayList<>();

        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            types.addAll(provider.getTypes());
        }

        return types;
    }

    public static List<String> getPresets() {
        ArrayList<String> presets = new ArrayList<>();

        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            presets.addAll(provider.getPresets());
        }

        return presets;
    }

    public static Object get(String type, String scriptIdentifier) {
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            if (provider.getTypes().contains(type)) {
                return provider.get(scriptIdentifier, type);
            }
        }

        return null;
    }

    public static List<String> getDefaultPresets() {
        ArrayList<String> defaultPresets = new ArrayList<>();

        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            defaultPresets.addAll(provider.getDefaultPresets());
        }

        return defaultPresets;
    }

    public static void importDefaultPresets(ScriptEngineFactory engineProvider, ScriptEngine scriptEngine,
            String scriptIdentifier) {
        for (String preset : getDefaultPresets()) {
            importPreset(preset, engineProvider, scriptEngine, scriptIdentifier);
        }
    }

    public static void importPreset(String preset, ScriptEngineFactory engineProvider, ScriptEngine scriptEngine,
            String scriptIdentifier) {
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            if (provider.getPresets().contains(preset)) {
                Map<String, Object> scopeValues = provider.importPreset(scriptIdentifier, preset);

                engineProvider.scopeValues(scriptEngine, scopeValues);
            }
        }
    }

    public static void dispose(String scriptIdentifier) {
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            provider.unload(scriptIdentifier);
        }
    }

}
