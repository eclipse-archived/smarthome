/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.ModuleI18nUtil;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.ModuleTypeI18nUtil;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.osgi.framework.Bundle;

/**
 * This class is implementation of {@link ModuleTypeProvider}. It serves for providing {@link ModuleType}s by loading
 * bundle resources. It extends functionality of {@link AbstractResourceBundleProvider} by specifying:
 * <ul>
 * <li>the path to resources, corresponding to the {@link ModuleType}s - root directory
 * {@link AbstractResourceBundleProvider#PATH} with sub-directory "moduletypes".
 * <li>type of the {@link Parser}s, corresponding to the {@link ModuleType}s - {@link Parser#PARSER_MODULE_TYPE}
 * <li>specific functionality for loading the {@link ModuleType}s
 * <li>tracking the managing service of the {@link ModuleType}s.
 * </ul>
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 * @author Yordan Mihaylov - updates related to api changes
 */
public class ModuleTypeResourceBundleProvider extends AbstractResourceBundleProvider<ModuleType>
        implements ModuleTypeProvider, Provider<ModuleType> {

    protected ModuleTypeRegistry moduleTypeRegistry;

    /**
     * This constructor is responsible for initializing the path to resources and tracking the
     * {@link ModuleTypeRegistry}.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public ModuleTypeResourceBundleProvider() {
        listeners = new LinkedList<ProviderChangeListener<ModuleType>>();
        path = PATH + "/moduletypes/";
    }

    @Override
    public Collection<ModuleType> getAll() {
        return providedObjectsHolder.values();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * @see ModuleTypeProvider#getModuleType(java.lang.String, java.util.Locale)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> T getModuleType(String UID, Locale locale) {
        return (T) getPerLocale(providedObjectsHolder.get(UID), locale);
    }

    /**
     * @see ModuleTypeProvider#getModuleTypes(java.util.Locale)
     */
    @Override
    public Collection<ModuleType> getModuleTypes(Locale locale) {
        List<ModuleType> moduleTypesList = new ArrayList<ModuleType>();
        for (ModuleType mt : providedObjectsHolder.values()) {
            moduleTypesList.add(getPerLocale(mt, locale));
        }
        return moduleTypesList;
    }

    protected void setModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        this.moduleTypeRegistry = moduleTypeRegistry;
    }

    protected void removeModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        this.moduleTypeRegistry = null;
    }

    /**
     * This method is responsible for checking the existence of {@link ModuleType}s with the same UIDs before these
     * objects to be added in the system.
     *
     * @param uid UID of the newly created {@link ModuleType}, which to be checked.
     * @return {@code true} if {@link ModuleType} with the same UID exists or {@code false} in the opposite case.
     */
    @Override
    protected boolean checkExistence(String uid) {
        if (moduleTypeRegistry.get(uid) != null) {
            logger.error("Module Type with UID \"{}\" already exists! Failed to create a second with the same UID!",
                    uid, new IllegalArgumentException());
            return true;
        }
        return false;
    }

    @Override
    protected String getUID(ModuleType parsedObject) {
        return parsedObject.getUID();
    }

    /**
     * This method is used to localize the {@link ModuleType}s.
     *
     * @param element is the {@link ModuleType} that must be localized.
     * @param locale represents a specific geographical, political, or cultural region.
     * @return the localized {@link ModuleType}.
     */
    private ModuleType getPerLocale(ModuleType defModuleType, Locale locale) {
        if (locale == null || defModuleType == null || i18nProvider == null) {
            return defModuleType;
        }
        String uid = defModuleType.getUID();
        Bundle bundle = getBundle(uid);
        String llabel = ModuleTypeI18nUtil.getLocalizedModuleTypeLabel(i18nProvider, bundle, uid,
                defModuleType.getLabel(), locale);
        String ldescription = ModuleTypeI18nUtil.getLocalizedModuleTypeDescription(i18nProvider, bundle, uid,
                defModuleType.getDescription(), locale);
        List<ConfigDescriptionParameter> lconfigDescriptions = getLocalizedConfigurationDescription(i18nProvider,
                defModuleType.getConfigurationDescriptions(), bundle, uid, ModuleTypeI18nUtil.MODULE_TYPE, locale);
        if (defModuleType instanceof ActionType) {
            return createLocalizedActionType((ActionType) defModuleType, bundle, uid, locale, lconfigDescriptions,
                    llabel, ldescription);
        }
        if (defModuleType instanceof ConditionType) {
            return createLocalizedConditionType((ConditionType) defModuleType, bundle, uid, locale, lconfigDescriptions,
                    llabel, ldescription);
        }
        if (defModuleType instanceof TriggerType) {
            return createLocalizedTriggerType((TriggerType) defModuleType, bundle, uid, locale, lconfigDescriptions,
                    llabel, ldescription);
        }
        return null;
    }

    /**
     * Utility method for localization of ActionTypes.
     *
     * @param at is an ActionType for localization.
     * @param bundle the bundle providing localization resources.
     * @param moduleTypeUID is an ActionType uid.
     * @param locale represents a specific geographical, political, or cultural region.
     * @param lconfigDescriptions are ActionType localized config descriptions.
     * @param llabel is an ActionType localized label.
     * @param ldescription is an ActionType localized description.
     * @return localized ActionType.
     */
    private ActionType createLocalizedActionType(ActionType at, Bundle bundle, String moduleTypeUID, Locale locale,
            List<ConfigDescriptionParameter> lconfigDescriptions, String llabel, String ldescription) {
        List<Input> inputs = ModuleTypeI18nUtil.getLocalizedInputs(i18nProvider, at.getInputs(), bundle, moduleTypeUID,
                locale);
        List<Output> outputs = ModuleTypeI18nUtil.getLocalizedOutputs(i18nProvider, at.getOutputs(), bundle,
                moduleTypeUID, locale);
        ActionType lat = null;
        if (at instanceof CompositeActionType) {
            List<Action> modules = ModuleI18nUtil.getLocalizedModules(i18nProvider,
                    ((CompositeActionType) at).getChildren(), bundle, moduleTypeUID, ModuleTypeI18nUtil.MODULE_TYPE,
                    locale);
            lat = new CompositeActionType(moduleTypeUID, lconfigDescriptions, llabel, ldescription, at.getTags(),
                    at.getVisibility(), inputs, outputs, modules);
        } else {
            lat = new ActionType(moduleTypeUID, lconfigDescriptions, llabel, ldescription, at.getTags(),
                    at.getVisibility(), inputs, outputs);
        }
        return lat;
    }

    /**
     * Utility method for localization of ConditionTypes.
     *
     * @param ct is a ConditionType for localization.
     * @param bundle the bundle providing localization resources.
     * @param moduleTypeUID is a ConditionType uid.
     * @param locale represents a specific geographical, political, or cultural region.
     * @param lconfigDescriptions are ConditionType localized config descriptions.
     * @param llabel is a ConditionType localized label.
     * @param ldescription is a ConditionType localized description.
     * @return localized ConditionType.
     */
    private ConditionType createLocalizedConditionType(ConditionType ct, Bundle bundle, String moduleTypeUID,
            Locale locale, List<ConfigDescriptionParameter> lconfigDescriptions, String llabel, String ldescription) {
        List<Input> inputs = ModuleTypeI18nUtil.getLocalizedInputs(i18nProvider, ct.getInputs(), bundle, moduleTypeUID,
                locale);
        ConditionType lct = null;
        if (ct instanceof CompositeConditionType) {
            List<Condition> modules = ModuleI18nUtil.getLocalizedModules(i18nProvider,
                    ((CompositeConditionType) ct).getChildren(), bundle, moduleTypeUID, ModuleTypeI18nUtil.MODULE_TYPE,
                    locale);
            lct = new CompositeConditionType(moduleTypeUID, lconfigDescriptions, llabel, ldescription, ct.getTags(),
                    ct.getVisibility(), inputs, modules);
        } else {
            lct = new ConditionType(moduleTypeUID, lconfigDescriptions, llabel, ldescription, ct.getTags(),
                    ct.getVisibility(), inputs);
        }
        return lct;
    }

    /**
     * Utility method for localization of TriggerTypes.
     *
     * @param ct is a TriggerType for localization.
     * @param bundle the bundle providing localization resources.
     * @param moduleTypeUID is a TriggerType uid.
     * @param locale represents a specific geographical, political, or cultural region.
     * @param lconfigDescriptions are TriggerType localized config descriptions.
     * @param llabel is a TriggerType localized label.
     * @param ldescription is a TriggerType localized description.
     * @return localized TriggerType.
     */
    private TriggerType createLocalizedTriggerType(TriggerType tt, Bundle bundle, String moduleTypeUID, Locale locale,
            List<ConfigDescriptionParameter> lconfigDescriptions, String llabel, String ldescription) {
        List<Output> outputs = ModuleTypeI18nUtil.getLocalizedOutputs(i18nProvider, tt.getOutputs(), bundle,
                moduleTypeUID, locale);
        TriggerType ltt = null;
        if (tt instanceof CompositeTriggerType) {
            List<Trigger> modules = ModuleI18nUtil.getLocalizedModules(i18nProvider,
                    ((CompositeTriggerType) tt).getChildren(), bundle, moduleTypeUID, ModuleTypeI18nUtil.MODULE_TYPE,
                    locale);
            ltt = new CompositeTriggerType(moduleTypeUID, lconfigDescriptions, llabel, ldescription, tt.getTags(),
                    tt.getVisibility(), outputs, modules);
        } else {
            ltt = new TriggerType(moduleTypeUID, lconfigDescriptions, llabel, ldescription, tt.getTags(),
                    tt.getVisibility(), outputs);
        }
        return ltt;
    }

}
