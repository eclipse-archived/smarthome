/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.provider;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.util.ConnectionValidator;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * This class is implementation of {@link TemplateProvider}. It serves for providing {@link RuleTemplates}s by loading
 * bundle resources. It extends functionality of {@link AbstractResourceBundleProvider} by specifying:
 * <ul>
 * <li>the path to resources, corresponding to the {@link RuleTemplates}s - root directory
 * {@link AbstractResourceBundleProvider#PATH} with sub-directory "templates".
 * <li>type of the {@link Parser}s, corresponding to the {@link RuleTemplates}s - {@link Parser#PARSER_TEMPLATE}
 * <li>specific functionality for loading the {@link RuleTemplates}s
 * <li>tracking the managing service of the {@link ModuleType}s.
 * <li>tracking the managing of the {@link RuleTemplates}s.
 * </ul>
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public abstract class TemplateResourceBundleProvider<PE> extends AbstractResourceBundleProvider<RuleTemplate, PE>
        implements TemplateProvider {

    protected TemplateRegistry templateRegistry;
    protected ModuleTypeRegistry moduleTypeRegistry;
    @SuppressWarnings("rawtypes")
    private ServiceTracker tracker;

    /**
     * This constructor is responsible for initializing the path to resources and tracking the managing service of the
     * {@link ModuleType}s and the managing service of the {@link RuleTemplates}s.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TemplateResourceBundleProvider(BundleContext context) {
        super(context);
        path = PATH + "/templates/";
        Filter filter;
        try {
            filter = bc.createFilter("(|(objectClass=" + TemplateRegistry.class.getName() + ")(objectClass="
                    + ModuleTypeRegistry.class.getName() + "))");
            tracker = new ServiceTracker(context, filter, new ServiceTrackerCustomizer() {

                @Override
                public Object addingService(ServiceReference reference) {
                    Object service = bc.getService(reference);
                    if (service instanceof TemplateRegistry)
                        templateRegistry = (TemplateRegistry) service;
                    else
                        moduleTypeRegistry = (ModuleTypeRegistry) service;
                    if (moduleTypeRegistry != null && templateRegistry != null && isReady && queue != null) {
                        queue.open();
                    }
                    return service;
                }

                @Override
                public void modifiedService(ServiceReference reference, Object service) {
                }

                @Override
                public void removedService(ServiceReference reference, Object service) {
                    if (service instanceof TemplateRegistry)
                        templateRegistry = null;
                    else
                        moduleTypeRegistry = null;
                }
            });
            tracker.open();
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is inherited from {@link AbstractResourceBundleProvider}. Extends parent's functionality with closing
     * the {@link #tracker} and sets <code>null</code> to {@link #moduleTypeRegistry} and {@link #templateRegistry}.
     *
     * @see org.eclipse.smarthome.automation.core.provider.AbstractResourceBundleProvider#close()
     */
    @Override
    public void close() {
        if (tracker != null) {
            tracker.close();
            tracker = null;
            moduleTypeRegistry = null;
            templateRegistry = null;
        }
        super.close();
    }

    /**
     * @see TemplateProvider#getTemplate(java.lang.String, java.util.Locale)
     */
    @Override
    public Template getTemplate(String UID, Locale locale) {
        Localizer l = null;
        synchronized (providerPortfolio) {
            l = providedObjectsHolder.get(UID);
        }
        if (l != null) {
            Template t = (Template) l.getPerLocale(locale);
            return t;
        }
        return null;
    }

    /**
     * @see TemplateProvider#getTemplates(java.util.Locale)
     */
    @Override
    public Collection<Template> getTemplates(Locale locale) {
        ArrayList<Template> templatesList = new ArrayList<Template>();
        synchronized (providedObjectsHolder) {
            Iterator<Localizer> i = providedObjectsHolder.values().iterator();
            while (i.hasNext()) {
                Localizer l = i.next();
                if (l != null) {
                    Template t = (Template) l.getPerLocale(locale);
                    if (t != null)
                        templatesList.add(t);
                }
            }
        }
        return templatesList;
    }

    /**
     * @see AbstractResourceBundleProvider#addingService(ServiceReference)
     */
    @Override
    public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_TEMPLATE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * @see AbstractResourceBundleProvider#importData(Vendor, Parser, java.io.InputStreamReader, java.util.ArrayList)
     */
    @Override
    protected Set<Status> importData(Vendor vendor, Parser parser, InputStreamReader inputStreamReader,
            List<String> portfolio) {
        synchronized (providerPortfolio) {
            providerPortfolio.put(vendor, portfolio);
        }
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        if (providedObjects != null && !providedObjects.isEmpty()) {
            for (Status status : providedObjects) {
                if (status.hasErrors())
                    continue;
                RuleTemplate ruleT = (RuleTemplate) status.getResult();
                String uid = ruleT.getUID();
                try {
                    ConnectionValidator.validateConnections(moduleTypeRegistry, ruleT.getModules(Trigger.class),
                            ruleT.getModules(Condition.class), ruleT.getModules(Action.class));
                } catch (Exception e) {
                    status.success(null);
                    status.error("Failed to validate connections of RuleTemplate with UID \"" + uid + "\"! "
                            + e.getMessage(), e);
                    continue;
                }
                if (checkExistence(uid, status))
                    continue;
                portfolio.add(uid);
                Localizer lruleT = new Localizer(ruleT);
                synchronized (providedObjectsHolder) {
                    providedObjectsHolder.put(uid, lruleT);
                }
                add(ruleT);
            }
        }
        return providedObjects;
    }

    /**
     * This method is responsible for checking the existence of {@link ModuleType}s or {@link Template}s with the same
     * UIDs before these objects to be added in the system.
     *
     * @param uid UID of the newly created {@link Template}, which to be checked.
     * @param status {@link Status} of the import operation. Can be successful or can fail for these {@link Template}s,
     *            for which a {@link Template} with the same UID, exists.
     * @return {@code true} if {@link Template} with the same UID exists or {@code false} in the opposite
     *         case.
     */
    private boolean checkExistence(String uid, Status status) {
        if (templateRegistry != null && templateRegistry.get(uid) != null) {
            status.error(
                    "Rule Template with UID \"" + uid
                            + "\" already exists! Failed to create a second with the same UID!",
                    new IllegalArgumentException());
            status.success(null);
            return true;
        }
        return false;
    }

}
