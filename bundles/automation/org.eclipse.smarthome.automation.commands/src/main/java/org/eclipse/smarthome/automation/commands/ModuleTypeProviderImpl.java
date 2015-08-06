/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * This class is implementation of {@link ModuleTypeProvider}. It extends functionality of {@link AbstractProviderImpl}.
 * <p>
 * It is responsible for execution of Automation {@link PluggableCommands}, corresponding to the {@link ModuleType}s:
 * <ul>
 * <li>imports the {@link ModuleType}s from local files or from URL resources
 * <li>provides functionality for persistence of the {@link ModuleType}s
 * <li>removes the {@link ModuleType}s and their persistence
 * <li>lists the {@link ModuleType}s and their details
 * </ul>
 * <p>
 * accordingly to the used command.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public abstract class ModuleTypeProviderImpl<PE> extends AbstractProviderImpl<ModuleType, PE>implements
        ModuleTypeProvider {

    /**
     * This constructor creates instances of this particular implementation of {@link ModuleTypeProvider}. It does not
     * add any new
     * functionality to the constructors of the providers. Only provides consistency by invoking the parent's
     * constructor.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public ModuleTypeProviderImpl(BundleContext context) {
        super(context);
    }

    /**
     * This method differentiates what type of {@link Parser}s is tracked by the tracker.
     * For this concrete provider, this type is a {@link ModuleType} {@link Parser}.
     *
     * @see AbstractProviderImpl#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_MODULE_TYPE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * @see AutomationCommandsPluggable#importModuleTypes(String, URL)
     */
    public Status exportModuleTypes(String parserType, Set<ModuleType> set, File file) {
        return super.exportData(parserType, set, file);
    }

    /**
     * @see AutomationCommandsPluggable#exportModuleTypes(String, Set, File)
     */
    public Set<Status> importModuleTypes(String parserType, URL url) {
        InputStreamReader inputStreamReader = null;
        Parser parser = parsers.get(parserType);
        if (parser != null)
            try {
                InputStream is = url.openStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                inputStreamReader = new InputStreamReader(bis);
                return importData(url, parser, inputStreamReader);
            } catch (IOException e) {
                Status s = new Status(logger, 0, null);
                s.error("Can't read from URL " + url, e);
                LinkedHashSet<Status> res = new LinkedHashSet<Status>();
                res.add(s);
                return res;
            } finally {
                try {
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                } catch (IOException e) {
                }
            }
        return null;
    }

    /**
     * @see org.eclipse.smarthome.automation.ModuleTypeProvider#getModuleType(java.lang.String, java.util.Locale)
     */
    @Override
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
     * @see org.eclipse.smarthome.automation.ModuleTypeProvider#getModuleTypes(java.util.Locale)
     */
    @Override
    public Collection<ModuleType> getModuleTypes(Locale locale) {
        List<ModuleType> moduleTypesList = new ArrayList<ModuleType>();
        synchronized (providedObjectsHolder) {
            Iterator<Localizer> i = providedObjectsHolder.values().iterator();
            while (i.hasNext()) {
                Localizer l = i.next();
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
     * @see AbstractProviderImpl#importData(URL, Parser, InputStreamReader)
     */
    @Override
    protected Set<Status> importData(URL url, Parser parser, InputStreamReader inputStreamReader) {
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        if (providedObjects != null && !providedObjects.isEmpty()) {
            String uid = null;
            List<String> portfolio = new ArrayList<String>();
            synchronized (providerPortfolio) {
                providerPortfolio.put(url, portfolio);
            }
            for (Status s : providedObjects) {
                if (s.hasErrors())
                    continue;
                ModuleType providedObject = (ModuleType) s.getResult();
                uid = providedObject.getUID();
                if (checkExistence(uid, s))
                    continue;
                portfolio.add(uid);
                Localizer lProvidedObject = new Localizer(providedObject);
                synchronized (providedObjectsHolder) {
                    providedObjectsHolder.put(uid, lProvidedObject);
                }
                add(providedObject);
            }
        }
        return providedObjects;
    }

    /**
     * This method is responsible for checking the existence of {@link ModuleType}s with the same
     * UIDs before these objects to be added in the system.
     *
     * @param uid UID of the newly created {@link ModuleType}, which to be checked.
     * @param status {@link Status} of the {@link AutomationCommand} operation. Can be successful or can fail for these
     *            {@link ModuleType}s, for which a {@link ModuleType} with the same UID, exists.
     * @return <code>true</code> if {@link ModuleType} with the same UID exists or <code>false</code> in the opposite
     *         case.
     */
    protected boolean checkExistence(String uid, Status s) {
        if (AutomationCommandsPluggable.moduleTypeRegistry == null) {
            s.error("Failed to create Module Type with UID \"" + uid
                    + "\"! Can't guarantee yet that other Module Type with the same UID does not exist.",
                    new IllegalArgumentException());
            s.success(null);
            return true;
        }
        if (AutomationCommandsPluggable.moduleTypeRegistry.get(uid) != null) {
            s.error("Module Type with UID \"" + uid + "\" already exists! Failed to create a second with the same UID!",
                    new IllegalArgumentException());
            s.success(null);
            return true;
        }
        return false;
    }

}
