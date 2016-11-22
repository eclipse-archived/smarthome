/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;

/**
 * This class is implementation of {@link ModuleTypeProvider}. It extends functionality of {@link AbstractFileProvider}
 * for importing the {@link ModuleType}s from local files.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public abstract class ModuleTypeFileProvider extends AbstractFileProvider<ModuleType> implements ModuleTypeProvider {

    public ModuleTypeFileProvider() {
        super("moduletypes");
    }

    @Override
    protected String getUID(ModuleType providedObject) {
        return providedObject.getUID();
    }

    @Override
    public Collection<ModuleType> getAll() {
        return getModuleTypes(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> T getModuleType(String UID, Locale locale) {
        return (T) providedObjectsHolder.get(UID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> Collection<T> getModuleTypes(Locale locale) {
        Collection<ModuleType> values = providedObjectsHolder.values();
        if (values.isEmpty()) {
            return Collections.<T>emptyList();
        }
        return (Collection<T>) new LinkedList<ModuleType>(values);
    }
}