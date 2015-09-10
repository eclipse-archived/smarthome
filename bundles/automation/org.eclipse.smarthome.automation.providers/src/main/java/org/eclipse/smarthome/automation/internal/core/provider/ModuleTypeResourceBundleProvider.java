/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.ConfigDescriptionParameterI18nUtil;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.ModuleI18nUtil;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.ModuleTypeI18nUtil;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.template.Template;
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
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

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
 * @author Ana Dimova - provides localization
 */
public class ModuleTypeResourceBundleProvider extends AbstractResourceBundleProvider<ModuleType>
        implements ModuleTypeProvider {

    protected ModuleTypeRegistry moduleTypeRegistry;

    @SuppressWarnings("rawtypes")
    private ServiceTracker moduleTypesTracker;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration /* <T> */ mtpReg;

    @SuppressWarnings("rawtypes")
    private ServiceTracker localizationTracker;

    /**
     * This constructor is responsible for initializing the path to resources and tracking the managing service of the
     * {@link ModuleType}s.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ModuleTypeResourceBundleProvider(BundleContext context) {
        super(context);
        path = PATH + "/moduletypes/";
        moduleTypesTracker = new ServiceTracker(context, ModuleTypeRegistry.class.getName(),
                new ServiceTrackerCustomizer() {

                    @Override
                    public Object addingService(ServiceReference reference) {
                        moduleTypeRegistry = (ModuleTypeRegistry) bc.getService(reference);
                        queue.open();
                        return moduleTypeRegistry;
                    }

                    @Override
                    public void modifiedService(ServiceReference reference, Object service) {
                    }

                    @Override
                    public void removedService(ServiceReference reference, Object service) {
                        moduleTypeRegistry = null;
                    }
                });
        localizationTracker = new ServiceTracker(bc, I18nProvider.class.getName(), new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference reference) {
                i18nProvider = bc.getService(reference);
                return i18nProvider;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                i18nProvider = null;
            }
        });
        localizationTracker.open();
    }

    @Override
    public void setQueue(AutomationResourceBundlesEventQueue queue) {
        super.setQueue(queue);
        moduleTypesTracker.open();
    }

    /**
     * This method is inherited from {@link AbstractResourceBundleProvider}. Extends parent's functionality with closing
     * the {@link #moduleTypesTracker} and sets <code>null</code> to {@link #moduleTypeRegistry}.
     *
     * @see AbstractResourceBundleProvider#close()
     */
    @Override
    public void close() {
        if (localizationTracker != null) {
            localizationTracker.close();
            localizationTracker = null;
            i18nProvider = null;
        }
        if (moduleTypesTracker != null) {
            moduleTypesTracker.close();
            moduleTypesTracker = null;
            moduleTypeRegistry = null;
        }
        if (mtpReg != null) {
            mtpReg.unregister();
            mtpReg = null;
        }
        super.close();
    }

    /**
     * @see ModuleTypeProvider#getModuleType(java.lang.String, java.util.Locale)
     */
    @Override
    public <T extends ModuleType> T getModuleType(String UID, Locale locale) {
        ModuleType defModuleType = null;
        synchronized (providedObjectsHolder) {
            defModuleType = providedObjectsHolder.get(UID);
        }
        if (defModuleType != null) {
            @SuppressWarnings("unchecked")
            T mt = (T) getPerLocale(defModuleType, locale);
            return mt;
        }
        return null;
    }

    /**
     * @see ModuleTypeProvider#getModuleTypes(java.util.Locale)
     */
    @Override
    public Collection<ModuleType> getModuleTypes(Locale locale) {
        List<ModuleType> moduleTypesList = new ArrayList<ModuleType>();
        synchronized (providedObjectsHolder) {
            Iterator<ModuleType> i = providedObjectsHolder.values().iterator();
            while (i.hasNext()) {
                ModuleType defModuleType = i.next();
                if (defModuleType != null) {
                    ModuleType mt = getPerLocale(defModuleType, locale);
                    if (mt != null)
                        moduleTypesList.add(mt);
                }
            }
        }
        return moduleTypesList;
    }

    /**
     * @see AbstractResourceBundleProvider#addingService(ServiceReference)
     */
    @Override
    public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_MODULE_TYPE)) {
            return super.addingService(reference);
        }
        return null;
    }

    @Override
    public boolean isReady() {
        return moduleTypeRegistry != null && queue != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<Status> importData(Vendor vendor, Parser<ModuleType> parser, InputStreamReader inputStreamReader) {
        List<String> portfolio = null;
        if (vendor != null) {
            synchronized (providerPortfolio) {
                portfolio = providerPortfolio.get(vendor);
                if (portfolio == null) {
                    portfolio = new ArrayList<String>();
                    providerPortfolio.put(vendor, portfolio);
                }
            }
        }
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        if (providedObjects != null && !providedObjects.isEmpty()) {
            Iterator<Status> i = providedObjects.iterator();
            while (i.hasNext()) {
                Status status = i.next();
                ModuleType providedObject = (ModuleType) status.getResult();
                if (providedObject != null) {
                    String uid = providedObject.getUID();
                    if (checkExistence(uid, status))
                        continue;
                    if (portfolio != null) {
                        portfolio.add(uid);
                    }
                    synchronized (providedObjectsHolder) {
                        providedObjectsHolder.put(uid, providedObject);
                    }
                }
            }
        }
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(REG_PROPERTY_MODULE_TYPES, providedObjectsHolder.keySet());
        if (mtpReg == null)
            mtpReg = bc.registerService(ModuleTypeProvider.class.getName(), this, properties);
        else {
            mtpReg.setProperties(properties);
        }
        return providedObjects;
    }

    /**
     * This method is responsible for checking the existence of {@link ModuleType}s or {@link Template}s with the same
     * UIDs before these objects to be added in the system.
     *
     * @param uid UID of the newly created {@link ModuleType}, which to be checked.
     * @param status {@link Status} of an import operation. Can be successful or can fail for these {@link ModuleType}s,
     *            for which a {@link ModuleType} with the same UID, exists.
     * @return {@code true} if {@link ModuleType} with the same UID exists or {@code false} in the opposite case.
     */
    private boolean checkExistence(String uid, Status status) {
        if (moduleTypeRegistry.get(uid) != null) {
            status.error(
                    "Module Type with UID \"" + uid + "\" already exists! Failed to create a second with the same UID!",
                    new IllegalArgumentException());
            status.success(null);
            return true;
        }
        return false;
    }

    /**
     * This method is used to localize the {@link ModuleType}s.
     *
     * @param element is the {@link ModuleType} that must be localized.
     * @param locale represents a specific geographical, political, or cultural region.
     * @return the localized {@link ModuleType}.
     */
    private ModuleType getPerLocale(ModuleType defModuleType, Locale locale) {
        if (locale == null)
            return defModuleType;
        String uid = defModuleType.getUID();
        Bundle bundle = getBundle(uid);
        String llabel = ModuleTypeI18nUtil.getLocalizedModuleTypeLabel(i18nProvider, bundle, uid,
                defModuleType.getLabel(), locale);
        String ldescription = ModuleTypeI18nUtil.getLocalizedModuleTypeDescription(i18nProvider, bundle, uid,
                defModuleType.getDescription(), locale);
        Set<ConfigDescriptionParameter> lconfigDescriptions = ConfigDescriptionParameterI18nUtil
                .getLocalizedConfigurationDescription(i18nProvider, defModuleType.getConfigurationDescription(), bundle,
                        uid, ModuleTypeI18nUtil.MODULE_TYPE, locale);
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
            Set<ConfigDescriptionParameter> lconfigDescriptions, String llabel, String ldescription) {
        Set<Input> inputs = ModuleTypeI18nUtil.getLocalizedInputs(i18nProvider, at.getInputs(), bundle, moduleTypeUID,
                locale);
        Set<Output> outputs = ModuleTypeI18nUtil.getLocalizedOutputs(i18nProvider, at.getOutputs(), bundle,
                moduleTypeUID, locale);
        ActionType lat = null;
        if (at instanceof CompositeActionType) {
            List<Action> modules = ModuleI18nUtil.getLocalizedModules(i18nProvider,
                    ((CompositeActionType) at).getModules(), bundle, moduleTypeUID, ModuleTypeI18nUtil.MODULE_TYPE,
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
            Locale locale, Set<ConfigDescriptionParameter> lconfigDescriptions, String llabel, String ldescription) {
        Set<Input> inputs = ModuleTypeI18nUtil.getLocalizedInputs(i18nProvider, ct.getInputs(), bundle, moduleTypeUID,
                locale);
        ConditionType lct = null;
        if (ct instanceof CompositeConditionType) {
            List<Condition> modules = ModuleI18nUtil.getLocalizedModules(i18nProvider,
                    ((CompositeConditionType) ct).getModules(), bundle, moduleTypeUID, ModuleTypeI18nUtil.MODULE_TYPE,
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
            Set<ConfigDescriptionParameter> lconfigDescriptions, String llabel, String ldescription) {
        Set<Output> outputs = ModuleTypeI18nUtil.getLocalizedOutputs(i18nProvider, tt.getOutputs(), bundle,
                moduleTypeUID, locale);
        TriggerType ltt = null;
        if (tt instanceof CompositeTriggerType) {
            List<Trigger> modules = ModuleI18nUtil.getLocalizedModules(i18nProvider,
                    ((CompositeTriggerType) tt).getModules(), bundle, moduleTypeUID, ModuleTypeI18nUtil.MODULE_TYPE,
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
