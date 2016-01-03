package org.eclipse.smarthome.automation.module.script.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.script.ScriptEngine;

import org.eclipse.smarthome.automation.module.script.ScriptEngineProvider;
import org.eclipse.smarthome.automation.module.script.ScriptExtensionProvider;

public class ScriptExtensionManager {
    private ScriptEngine scriptEngine;

    private static Set<ScriptExtensionProvider> scriptExtensionProviders = new CopyOnWriteArraySet<ScriptExtensionProvider>();

    public static Set<ScriptExtensionProvider> getScriptExtensionProviders() {
        return scriptExtensionProviders;
    }

    public static void addScriptExtensionProvider(ScriptExtensionProvider provider) {
        scriptExtensionProviders.add(provider);
    }

    public static void removeScriptExtensionProvider(ScriptExtensionProvider provider) {
        scriptExtensionProviders.remove(provider);
    }

    public ScriptExtensionManager(ScriptEngine engine) {
        this.scriptEngine = engine;
    }

    @Override
    public void finalize() {
        dispose(scriptEngine.hashCode());
    }

    public List<String> getTypes() {
        ArrayList<String> types = new ArrayList<>();

        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            types.addAll(provider.getTypes());
        }

        return types;
    }

    public List<String> getPresets() {
        ArrayList<String> presets = new ArrayList<>();

        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            presets.addAll(provider.getPresets());
        }

        return presets;
    }

    public Object get(String type) {
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            if (provider.getTypes().contains(type)) {
                return provider.get(scriptEngine.hashCode(), type);
            }
        }

        return null;
    }

    public void importPreset(String preset) {
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            if (provider.getPresets().contains(preset)) {
                Map<String, Object> scopeValues = provider.importPreset(scriptEngine.hashCode(), preset);

                ScriptEngineProvider.scopeValues(scriptEngine, scopeValues);
            }
        }
    }

    public static void dispose(int scriptEngineId) {
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            provider.unLoad(scriptEngineId);
        }
    }

}
