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

package org.eclipse.smarthome.automation.core.provider;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.provider.ModuleTypeProvider;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;

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
 * 
 */
public abstract class ModuleTypeResourceBundleProvider<PE> extends AbstractResourceBundleProvider<ModuleType, PE> implements
        ModuleTypeProvider {

    protected ModuleTypeRegistry moduleTypeRegistry;
    private ServiceTracker<ModuleTypeRegistry, ModuleTypeRegistry> moduleTypesTracker;

    /**
     * This constructor is responsible for initializing the path to resources and tracking the managing service of the
     * {@link ModuleType}s.
     * 
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     * @param providerClass the class object, used for creation of a {@link Logger}, which belongs to this specific
     *            provider.
     */
    public ModuleTypeResourceBundleProvider(BundleContext context, Class providerClass) {
        super(context, providerClass);
        path = PATH + "/moduletypes/";
        moduleTypesTracker = new ServiceTracker<ModuleTypeRegistry, ModuleTypeRegistry>(context, ModuleTypeRegistry.class.getName(),
                new ServiceTrackerCustomizer<ModuleTypeRegistry, ModuleTypeRegistry>() {

                    public ModuleTypeRegistry addingService(ServiceReference<ModuleTypeRegistry> reference) {
                        moduleTypeRegistry = bc.getService(reference);
                        if (moduleTypeRegistry != null && isReady && queue != null) {
                            queue.open();
                        }
                        return moduleTypeRegistry;
                    }

                    public void modifiedService(ServiceReference<ModuleTypeRegistry> reference, ModuleTypeRegistry service) {
                    }

                    public void removedService(ServiceReference<ModuleTypeRegistry> reference, ModuleTypeRegistry service) {
                        moduleTypeRegistry = null;
                    }
                });
        moduleTypesTracker.open();
    }
    
    /**
     * This method is inherited from {@link AbstractResourceBundleProvider}.
     * <p>
     * Extends parent's functionality with closing the {@link #moduleTypesTracker} and
     * <p>
     * sets <code>null</code> to {@link #moduleTypeRegistry}.
     * 
     * @see AbstractResourceBundleProvider#close()
     */
    @Override
    public void close() {
        if (moduleTypesTracker != null) {
            moduleTypesTracker.close();
            moduleTypesTracker = null;
            moduleTypeRegistry = null;
        }
        super.close();
    }

    /**
     * @see ModuleTypeProvider#getModuleType(java.lang.String, java.util.Locale)
     */
    public ModuleType getModuleType(String UID, Locale locale) {
        Localizer l = null;
        synchronized (providedObjectsHolder) {
            l = providedObjectsHolder.get(UID);
        }
        if (l != null) {
            ModuleType mt = (ModuleType) l.getPerLocale(locale);
            return mt;
        }
        return null;
    }

    /**
     * @see ModuleTypeProvider#getModuleTypes(java.util.Locale)
     */
    public Collection<ModuleType> getModuleTypes(Locale locale) {
        ArrayList moduleTypesList = new ArrayList();
        synchronized (providedObjectsHolder) {
            Iterator i = providedObjectsHolder.values().iterator();
            while (i.hasNext()) {
                Localizer l = (Localizer) i.next();
                if (l != null) {
                    ModuleType mt = (ModuleType) l.getPerLocale(locale);
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
    public Object addingService(ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_MODULE_TYPE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * @see AbstractResourceBundleProvider#importData(Vendor, Parser, java.io.InputStreamReader, java.util.ArrayList)
     */
    @Override
    protected Set<Status> importData(Vendor vendor, Parser parser, InputStreamReader inputStreamReader,
            ArrayList<String> portfolio) {
        if (vendor != null) {
            synchronized (providerPortfolio) {
                providerPortfolio.put(vendor, portfolio);
            }
        }
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        if (providedObjects != null && !providedObjects.isEmpty()) {
            Iterator i = providedObjects.iterator();
            while (i.hasNext()) {
                Status status = (Status) i.next();
                ModuleType providedObject = (ModuleType) status.getResult();
                if (providedObject != null) {
                    String uid = providedObject.getUID();
                    if (checkExistence(uid, status))
                        continue;
                    portfolio.add(uid);
                    Localizer lProvidedObject = new Localizer(providedObject);
                    synchronized (providedObjectsHolder) {
                        providedObjectsHolder.put(uid, lProvidedObject);
                    }
                    add(providedObject);
                }
            }
        }
        return providedObjects;
    }

    /**
     * This method is responsible for checking the existence of {@link ModuleType}s or {@link Template}s with the same
     * UIDs before these objects to be added in the system.
     * 
     * @param uid UID of the newly created {@link ModuleType}, which to be checked.
     * @param status {@link Status} of the import operation. Can be successful or can fail for these {@link ModuleType}
     *            s,
     *            for which a {@link ModuleType} with the same UID, exists.
     * @return {@code true} if {@link ModuleType} with the same UID exists or {@code false} in the opposite
     *         case.
     */
    private boolean checkExistence(String uid, Status status) {
        if (moduleTypeRegistry.get(uid) != null) {
            status.error("Module Type with UID \"" + uid
                    + "\" already exists! Failed to create a second with the same UID!", new IllegalArgumentException());
            status.success(null);
            return true;
        }
        return false;
    }

}
