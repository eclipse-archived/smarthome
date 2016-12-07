/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.integration.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;

/**
 * This class is a {@link ModuleTypeProvider} test implementation.
 *
 * @author Ana Dimova
 *
 */
public class TestModuleTypeProvider implements Provider<ModuleType>, ModuleTypeProvider {

    private ModuleType[] moduleTypes;

    public TestModuleTypeProvider(ModuleType[] moduleTypes) {
        super();
        this.moduleTypes = moduleTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> T getModuleType(String UID, Locale locale) {
        for (ModuleType moduleType : moduleTypes) {
            if (moduleType.getUID().equals(UID)) {
                return (T) moduleType;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> Collection<T> getModuleTypes(Locale locale) {
        return (Collection<T>) getAll();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        for (ModuleType moduleType : moduleTypes) {
            listener.added(this, moduleType);
        }
    }

    @Override
    public Collection<ModuleType> getAll() {
        return Arrays.asList(moduleTypes);
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {

    }

}
