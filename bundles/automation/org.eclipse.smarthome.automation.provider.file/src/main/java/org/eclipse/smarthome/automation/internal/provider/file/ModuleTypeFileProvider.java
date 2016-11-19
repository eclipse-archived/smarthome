/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;

/**
 * This class is implementation of {@link ModuleTypeProvider}. It extends functionality of
 * {@link AbstractFileProvider} for importing the {@link ModuleType}s from local files.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ModuleTypeFileProvider extends AbstractFileProvider<ModuleType> implements ModuleTypeProvider {

    private final String moduleTypesRoot = "moduletypes";

    public ModuleTypeFileProvider(String root) {
        WatchingDir = root + File.separator + moduleTypesRoot;
        super.activate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ModuleType getModuleType(String UID, Locale locale) {
        synchronized (providedObjectsHolder) {
            return providedObjectsHolder.get(UID);
        }
    }

    @Override
    public Collection<ModuleType> getModuleTypes(Locale locale) {
        synchronized (providedObjectsHolder) {
            return !providedObjectsHolder.isEmpty() ? providedObjectsHolder.values()
                    : Collections.<ModuleType>emptyList();
        }
    }

    @Override
    protected String getUID(ModuleType providedObject) {
        return providedObject.getUID();
    }

}