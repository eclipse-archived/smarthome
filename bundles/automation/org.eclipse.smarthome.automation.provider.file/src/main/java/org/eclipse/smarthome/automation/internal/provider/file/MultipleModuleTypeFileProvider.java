/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;

/**
 * This class is a wrapper of multiple {@link ModuleTypeProvider}s, responsible for importing the {@link ModuleType}s
 * from local file system.
 * <p>
 * It provides functionality for tracking {@link Parser} services and provides common functionality for notifying the
 * {@link ProviderChangeListener}s for adding, updating and removing the {@link ModuleType}s.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class MultipleModuleTypeFileProvider extends AbstractMultipleFileProvider<ModuleType, ModuleTypeFileProvider>
        implements ModuleTypeProvider {

    protected synchronized void modified(Map<String, Object> config) {
        String roots = (String) config.get(ROOTS);
        if (roots != null) {
            this.roots = roots.split(",");
            if (providers != null) {
                for (String root : providers.keySet()) {
                    if (!roots.contains(root)) {
                        ModuleTypeFileProvider provider = providers.remove(root);
                        provider.deactivate();
                    }
                }
                for (int i = 0; i < this.roots.length; i++) {
                    if (!providers.containsKey(this.roots[i])) {
                        ModuleTypeFileProvider provider = new ModuleTypeFileProvider(this.roots[i]);
                        provider.addProviderChangeListener(this);
                        provider.activate(parsers);
                        providers.put(this.roots[i], provider);
                    }
                }
            }
        }
    }

    public void activate(Map<String, Object> config) {
        modified(config);
        providers = new HashMap<String, ModuleTypeFileProvider>(this.roots.length);
        for (int i = 0; i < roots.length; i++) {
            ModuleTypeFileProvider provider = new ModuleTypeFileProvider(roots[i]);
            provider.addProviderChangeListener(this);
            provider.activate(parsers);
            providers.put(roots[i], provider);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> T getModuleType(String UID, Locale locale) {
        T moduleType = null;
        for (ModuleTypeProvider provider : providers.values()) {
            moduleType = (T) provider.getModuleType(UID, locale);
            if (moduleType != null) {
                return moduleType;
            }
        }
        return moduleType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> Collection<T> getModuleTypes(Locale locale) {
        Collection<T> moduleTypes = new ArrayList<T>();
        for (ModuleTypeProvider provider : providers.values()) {
            moduleTypes.addAll((Collection<T>) provider.getModuleTypes(locale));
        }
        return moduleTypes;
    }

    @Override
    public Collection<ModuleType> getAll() {
        return getModuleTypes(null);
    }

}
