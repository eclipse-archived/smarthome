/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    private static final String MODULE_TYPES_ROOT = "automation/moduletype";

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
    public void activate() {
        super.activate();
        importResources(new File(MODULE_TYPES_ROOT));
    }

    @Override
    protected void updateProvidedObjectsHolder(URL url, Set<ModuleType> providedObjects) {
        if (providedObjects != null && !providedObjects.isEmpty()) {
            List<String> uids = new ArrayList<String>();
            for (ModuleType providedObject : providedObjects) {
                String uid = providedObject.getUID();
                ModuleType oldModuleType = getOldElement(uid);
                notifyListeners(oldModuleType, providedObject);
                uids.add(uid);
                synchronized (providedObjectsHolder) {
                    providedObjectsHolder.put(uid, providedObject);
                }
            }
            synchronized (providerPortfolio) {
                providerPortfolio.put(url, uids);
            }
        }
    }

    @Override
    protected String getSourcePath() {
        return MODULE_TYPES_ROOT;
    }

    @Override
    protected void removeElements(List<String> objectsForRemove) {
        if (objectsForRemove != null) {
            for (String removededObject : objectsForRemove) {
                ModuleType mtRemoved;
                synchronized (providedObjectsHolder) {
                    mtRemoved = providedObjectsHolder.remove(removededObject);
                }
                notifyListeners(mtRemoved);
            }
        }
    }

}