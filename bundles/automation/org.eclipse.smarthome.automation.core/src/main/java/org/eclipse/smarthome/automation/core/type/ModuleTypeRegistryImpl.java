/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.core.type;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;

/**
 * @author Yordan Mihaylov - Initial Contribution
 */
public class ModuleTypeRegistryImpl extends AbstractRegistry<ModuleType, String> implements ModuleTypeRegistry {

    private ModuleTypeManager moduleTypeManager;

    public ModuleTypeRegistryImpl(ModuleTypeManager moduleTypeManager) {
        this.moduleTypeManager = moduleTypeManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.core.common.registry.Registry#get(java.lang.Object)
     */
    @Override
    public ModuleType get(String key) {
        return moduleTypeManager.getType(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#get(java.lang.String, java.util.Locale)
     */
    @Override
    public <T extends ModuleType> T get(String moduleTypeUID, Locale locale) {
        return moduleTypeManager.getType(moduleTypeUID, locale);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#getByTag(java.lang.String, java.util.Locale)
     */
    @Override
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag, Locale locale) {
        return moduleTypeManager.getTypesByTag(moduleTypeTag, locale);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#get(java.lang.Class, java.util.Locale)
     */
    @Override
    public <T extends ModuleType> Collection<T> get(Class<T> moduleType, Locale locale) {
        return moduleTypeManager.getTypes(moduleType, locale);
    }

    /**
   *
   */
    public void dispose() {
        moduleTypeManager.dispose();
    }

    @Override
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag) {
        return moduleTypeManager.getTypesByTag(moduleTypeTag);
    }

    @Override
    public <T extends ModuleType> Collection<T> get(Class<T> classModuleType) {
        return moduleTypeManager.getTypes(classModuleType);
    }

}
