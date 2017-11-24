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
