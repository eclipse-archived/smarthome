/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal.type;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * The implementation of {@link ModuleTypeRegistry} that is registered as a service.
 *
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
        return moduleTypeManager.get(key);
    }

    @Override
    public <T extends ModuleType> T get(String moduleTypeUID, Locale locale) {
        return moduleTypeManager.get(moduleTypeUID, locale);
    }

    @Override
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag) {
        return moduleTypeManager.getByTag(moduleTypeTag);
    }

    @Override
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag, Locale locale) {
        return moduleTypeManager.getByTag(moduleTypeTag, locale);
    }

    @Override
    public <T extends ModuleType> Collection<T> getByTags(String... tags) {
        return getByTags(null, tags);
    }

    @Override
    public <T extends ModuleType> Collection<T> getByTags(Locale locale, String... tags) {
        Set<String> tagSet = tags != null ? new HashSet<String>(Arrays.asList(tags)) : null;
        return moduleTypeManager.getByTags(tagSet, locale);
    }

    @Override
    public Collection<TriggerType> getTriggers(Locale locale) {
        return moduleTypeManager.getAll(TriggerType.class, locale);
    }

    @Override
    public Collection<ConditionType> getConditions() {
        return moduleTypeManager.getAll(ConditionType.class);
    }

    @Override
    public Collection<ConditionType> getConditions(Locale locale) {
        return moduleTypeManager.getAll(ConditionType.class, locale);
    }

    @Override
    public Collection<ActionType> getActions() {
        return moduleTypeManager.getAll(ActionType.class);
    }

    @Override
    public Collection<ActionType> getActions(Locale locale) {
        return moduleTypeManager.getAll(ActionType.class, locale);
    }

    @Override
    public Collection<TriggerType> getTriggers() {
        return moduleTypeManager.getAll(TriggerType.class);
    }

    public void dispose() {
        moduleTypeManager.dispose();
    }

}
