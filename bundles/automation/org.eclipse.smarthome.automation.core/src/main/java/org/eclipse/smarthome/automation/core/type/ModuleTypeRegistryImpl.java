/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.type;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;

/**
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 */
public class ModuleTypeRegistryImpl implements ModuleTypeRegistry {

    private ModuleTypeManager moduleTypeManager;

    public ModuleTypeRegistryImpl(ModuleTypeManager moduleTypeManager) {
        this.moduleTypeManager = moduleTypeManager;
    }

    @Override
    public <T extends ModuleType> T get(String key) {
        return moduleTypeManager.getType(key);
    }

    @Override
    public <T extends ModuleType> T get(String moduleTypeUID, Locale locale) {
        return moduleTypeManager.getType(moduleTypeUID, locale);
    }

    @Override
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag) {
        return moduleTypeManager.getTypesByTag(moduleTypeTag);
    }

    @Override
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag, Locale locale) {
        return moduleTypeManager.getTypesByTag(moduleTypeTag, locale);
    }

    @Override
    public <T extends ModuleType> Collection<T> getTypesByTags(Set<String> tags) {
        return getTypesByTags(tags, null);
    }

    @Override
    public <T extends ModuleType> Collection<T> getTypesByTags(Set<String> tags, Locale locale) {
        return moduleTypeManager.getTypesByTags(tags, locale);
    }

    @Override
    public <T extends ModuleType> Collection<T> get(Class<T> moduleType, Locale locale) {
        return moduleTypeManager.getTypes(moduleType, locale);
    }

    public void dispose() {
        moduleTypeManager.dispose();
    }

    @Override
    public <T extends ModuleType> Collection<T> get(Class<T> classModuleType) {
        return moduleTypeManager.getTypes(classModuleType);
    }

}
