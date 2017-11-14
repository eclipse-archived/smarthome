/** 
 * Copyright (c) 2015-2017 Simon Merschjohann and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.eclipse.smarthome.automation.module.script.internal;

import java.util.List;

import org.eclipse.smarthome.automation.module.script.ScriptEngineContainer;
import org.eclipse.smarthome.automation.module.script.ScriptExtensionProvider;

/**
 *
 * @author Simon Merschjohann - Initial contribution
 */
public class ScriptExtensionManagerWrapper {
    private ScriptEngineContainer container;

    public ScriptExtensionManagerWrapper(ScriptEngineContainer container) {
        this.container = container;
    }

    public void addScriptExtensionProvider(ScriptExtensionProvider provider) {
        ScriptExtensionManager.addExtension(provider);
    }

    public void removeScriptExtensionProvider(ScriptExtensionProvider provider) {
        ScriptExtensionManager.removeExtension(provider);
    }

    public List<String> getTypes() {
        return ScriptExtensionManager.getTypes();
    }

    public List<String> getPresets() {
        return ScriptExtensionManager.getPresets();
    }

    public Object get(String type) {
        return ScriptExtensionManager.get(type, container.getIdentifier());
    }

    public List<String> getDefaultPresets() {
        return ScriptExtensionManager.getDefaultPresets();
    }

    public void importPreset(String preset) {
        ScriptExtensionManager.importPreset(preset, container.getFactory(), container.getScriptEngine(),
                container.getIdentifier());
    }
}
