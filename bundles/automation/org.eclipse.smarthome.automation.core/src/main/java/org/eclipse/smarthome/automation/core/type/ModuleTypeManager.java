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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.smarthome.automation.handler.provider.ModuleTypeProvider;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Yordan Mihaylov - Initial Contribution
 */
public class ModuleTypeManager {

    private static ModuleTypeManager instance;
    private ServiceTracker moduleTypeTracker;

    /**
     * @param bc
     */
    public ModuleTypeManager(BundleContext bc) {
        if (instance == null) {
            moduleTypeTracker = new ServiceTracker(bc, ModuleTypeProvider.class.getName(), null);
            moduleTypeTracker.open();
            instance = this;
        }
    }

    public static ModuleTypeManager getInstance() {
        return instance;
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#getType(java.lang.String)
     */
    public <T extends ModuleType> T getType(String typeUID) {
        return getType(typeUID, null);
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#getType(java.lang.String, java.util.Locale)
     */
    public <T extends ModuleType> T getType(String typeUID, Locale locale) {
        ModuleType mType = null;
        Object[] providers = moduleTypeTracker.getServices();
        for (int i = 0; providers != null && i < providers.length; i++) {
            mType = ((ModuleTypeProvider) providers[i]).getModuleType(typeUID, locale);
            if (mType != null) {
                return (T) createCopy(mType);
            }
        }

        return null;
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#getTypesByTag(java.lang.String)
     */
    public <T extends ModuleType> Collection<T> getTypesByTag(String tag) {
        return getTypesByTag(tag, null);
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#getTypesByTag(java.lang.String, java.util.Locale)
     */
    public <T extends ModuleType> Collection<T> getTypesByTag(String tag, Locale locale) {
        Collection<T> result = new ArrayList<T>(20);
        Collection<ModuleType> moduleTypes = null;
        Object[] providers = moduleTypeTracker.getServices();
        for (int i = 0; providers != null && i < providers.length; i++) {
            moduleTypes = ((ModuleTypeProvider) providers[i]).getModuleTypes(locale);
            if (moduleTypes != null) {
                for (Iterator<ModuleType> it = moduleTypes.iterator(); it.hasNext();) {
                    ModuleType mt = it.next();
                    if (tag != null) {
                        Collection<String> tags = mt.getTags();
                        if (tags != null && tags.contains(tag)) {
                            result.add((T) createCopy(mt));
                        }
                    } else {
                        result.add((T) createCopy(mt));
                    }
                }
            }
        }
        return result;
    }

    /**
     * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#getTypes(java.lang.Class)
     */
    public <T extends ModuleType> Collection<T> getTypes(Class<T> classModuleType) {
        return getTypes(classModuleType, null);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#getTypes(java.lang.Class, java.util.Locale)
     */
    public <T extends ModuleType> Collection<T> getTypes(Class<T> moduleType, Locale locale) {
        Collection<T> result = new ArrayList<T>(20);
        Collection<ModuleType> moduleTypes = null;
        Object[] providers = moduleTypeTracker.getServices();
        for (int i = 0; providers != null && i < providers.length; i++) {
            moduleTypes = ((ModuleTypeProvider) providers[i]).getModuleTypes(locale);
            if (moduleTypes != null) {
                for (Iterator<ModuleType> it = moduleTypes.iterator(); it.hasNext();) {
                    ModuleType mt = it.next();
                    if (moduleType != null) {
                        if (moduleType.getName().equals(mt.getClass().getName())) {
                            result.add((T) createCopy(mt));
                        }
                    } else {
                        result.add((T) createCopy(mt));
                    }
                }
            }
        }
        return result;
    }

    /**
     *
     */
    public void dispose() {
        moduleTypeTracker.close();
        moduleTypeTracker = null;
        instance = null;
    }

    /**
     * @param template
     * @return copy of template
     */
    private ModuleType createCopy(ModuleType mType) {
        if (mType == null) {
            return null;
        }

        ModuleType result;
        if (mType instanceof CompositeTriggerType) {
            CompositeTriggerType m = (CompositeTriggerType) mType;
            result = new CompositeTriggerType(mType.getUID(), mType.getConfigurationDescription(), m.getOutputs(),
                    m.getModules());

        } else if (mType instanceof TriggerType) {
            TriggerType m = (TriggerType) mType;
            result = new TriggerType(mType.getUID(), mType.getConfigurationDescription(), m.getOutputs());

        } else if (mType instanceof CompositeConditionType) {
            CompositeConditionType m = (CompositeConditionType) mType;
            result = new CompositeConditionType(mType.getUID(), mType.getConfigurationDescription(), m.getInputs(),
                    m.getModules());

        } else if (mType instanceof ConditionType) {
            ConditionType m = (ConditionType) mType;
            result = new ConditionType(mType.getUID(), mType.getConfigurationDescription(), m.getInputs());

        } else if (mType instanceof CompositeActionType) {
            CompositeActionType m = (CompositeActionType) mType;
            result = new CompositeActionType(mType.getUID(), mType.getConfigurationDescription(), m.getInputs(),
                    m.getOutputs(), m.getModules());

        } else if (mType instanceof ActionType) {
            ActionType m = (ActionType) mType;
            result = new ActionType(mType.getUID(), mType.getConfigurationDescription(), m.getInputs(), m.getOutputs());

        } else {
            throw new IllegalArgumentException("Invalid template type: " + mType);
        }
        result.setTags(mType.getTags());
        result.setLabel(mType.getLabel());
        result.setDescription(mType.getDescription());
        result.setVisibility(mType.getVisibility());

        return result;
    }

}
