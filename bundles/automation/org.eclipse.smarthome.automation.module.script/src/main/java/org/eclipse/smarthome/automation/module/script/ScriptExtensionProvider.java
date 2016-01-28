package org.eclipse.smarthome.automation.module.script;

import java.util.Collection;
import java.util.Map;

/**
 * A {@link ScriptExtensionProvider} can provide variable and types on ScriptEngine instance basis.
 *
 * @author Simon Merschjohann- Initial contribution
 *
 */
public interface ScriptExtensionProvider {

    /**
     * These presets will always get injected into the ScriptEngine on instance creation.
     *
     * @return collection of presets
     */
    public Collection<String> getDefaultPresets();

    /**
     * Returns the provided Presets which are supported by this ScriptExtensionProvider.
     * Presets define imports which will be injected into the ScriptEngine if called by "importPreset".
     *
     * @return provided presets
     */
    public Collection<String> getPresets();

    /**
     * Returns the supported types which can be received by the given ScriptExtensionProvider
     *
     * @return provided types
     */
    public Collection<String> getTypes();

    /**
     * This method should return an Object of the given type. Note: get can be called multiple times in the scripts use
     * caching where appropriate.
     *
     * @param scriptEngine the script engine instance requesting the given type
     * @param type
     * @return
     */
    public Object get(int scriptEngineId, String type);

    /**
     * This method should return variables and types of the concrete type which will be injected into the ScriptEngines
     * scope.
     *
     * @param scriptEngineId - the script engine which will receive the preset
     */
    public Map<String, Object> importPreset(int scriptEngineId, String preset);

    /**
     * This will be called when the ScriptEngine will be unloaded (e.g. if the Script is deleted or updated).
     * Every Context information stored in the ScriptExtensionProvider should be removed.
     *
     * @param scriptEngineId
     */
    public void unLoad(int scriptEngineId);

}
