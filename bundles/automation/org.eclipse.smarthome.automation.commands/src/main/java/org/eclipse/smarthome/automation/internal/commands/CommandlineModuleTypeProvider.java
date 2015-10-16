/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * This class is implementation of {@link ModuleTypeProvider}. It extends functionality of
 * {@link AbstractCommandProvider}.
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
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 * @author Ana Dimova - refactor Parser interface.
 *
 */
public class CommandlineModuleTypeProvider extends AbstractCommandProvider<ModuleType>implements ModuleTypeProvider {

    /**
     * This field holds a reference to the {@link TemplateProvider} service registration.
     */
    @SuppressWarnings("rawtypes")
    protected ServiceRegistration mtpReg;

    /**
     * This constructor creates instances of this particular implementation of {@link ModuleTypeProvider}. It does not
     * add any new functionality to the constructors of the providers. Only provides consistency by invoking the
     * parent's constructor.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public CommandlineModuleTypeProvider(BundleContext context) {
        super(context);
    }

    /**
     * This method differentiates what type of {@link Parser}s is tracked by the tracker.
     * For this concrete provider, this type is a {@link ModuleType} {@link Parser}.
     *
     * @see AbstractCommandProvider#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_MODULE_TYPE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * This method is responsible for exporting a set of ModuleTypes in a specified file.
     *
     * @param parserType is relevant to the format that you need for conversion of the ModuleTypes in text.
     * @param set a set of ModuleTypes to export.
     * @param file a specified file for export.
     * @throws Exception when I/O operation has failed or has been interrupted or generating of the text fails
     *             for some reasons.
     * @see AutomationCommandsPluggable#exportModuleTypes(String, Set, File)
     */
    public void exportModuleTypes(String parserType, Set<ModuleType> set, File file) throws Exception {
        super.exportData(parserType, set, file);
    }

    /**
     * This method is responsible for importing a set of ModuleTypes from a specified file or URL resource.
     *
     * @param parserType is relevant to the format that you need for conversion of the ModuleTypes in text.
     * @param url a specified URL for import.
     * @throws IOException when I/O operation has failed or has been interrupted.
     * @throws ParsingException when parsing of the text fails for some reasons.
     * @see AutomationCommandsPluggable#importModuleTypes(String, URL)
     */
    public Set<ModuleType> importModuleTypes(String parserType, URL url) throws IOException, ParsingException {
        InputStreamReader inputStreamReader = null;
        Parser<ModuleType> parser = parsers.get(parserType);
        if (parser != null) {
            InputStream is = url.openStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            inputStreamReader = new InputStreamReader(bis);
            return importData(url, parser, inputStreamReader);
        } else {
            throw new ParsingException(new ParsingNestedException(ParsingNestedException.MODULE_TYPE, null,
                    new Exception("Parser " + parserType + " not available")));
        }

    }

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
            return providedObjectsHolder.values();
        }
    }

    @Override
    public void close() {
        if (mtpReg != null) {
            mtpReg.unregister();
            mtpReg = null;
        }
        super.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<ModuleType> importData(URL url, Parser<ModuleType> parser, InputStreamReader inputStreamReader)
            throws ParsingException {
        Set<ModuleType> providedObjects = parser.parse(inputStreamReader);
        if (providedObjects != null && !providedObjects.isEmpty()) {
            String uid = null;
            List<String> portfolio = new ArrayList<String>();
            synchronized (providerPortfolio) {
                providerPortfolio.put(url, portfolio);
            }
            List<ParsingNestedException> importDataExceptions = new ArrayList<ParsingNestedException>();
            for (ModuleType providedObject : providedObjects) {
                List<ParsingNestedException> exceptions = new ArrayList<ParsingNestedException>();
                uid = providedObject.getUID();
                checkExistence(uid, exceptions);
                if (exceptions.isEmpty()) {
                    portfolio.add(uid);
                    synchronized (providedObjectsHolder) {
                        providedObjectsHolder.put(uid, providedObject);
                    }
                } else
                    importDataExceptions.addAll(exceptions);
            }
            if (importDataExceptions.isEmpty()) {
                Dictionary<String, Object> properties = new Hashtable<String, Object>();
                properties.put(REG_PROPERTY_MODULE_TYPES, providedObjectsHolder.keySet());
                if (mtpReg == null)
                    mtpReg = bc.registerService(ModuleTypeProvider.class.getName(), this, properties);
                else {
                    mtpReg.setProperties(properties);
                }
            } else {
                throw new ParsingException(importDataExceptions);
            }
        }
        return providedObjects;
    }

    /**
     * This method is responsible for checking the existence of {@link ModuleType}s with the same
     * UIDs before these objects to be added in the system.
     *
     * @param uid UID of the newly created {@link ModuleType}, which to be checked.
     * @param exceptions accumulates exceptions if {@link ModuleType} with the same UID exists.
     */
    protected void checkExistence(String uid, List<ParsingNestedException> exceptions) {
        if (AutomationCommandsPluggable.moduleTypeRegistry == null) {
            exceptions.add(new ParsingNestedException(ParsingNestedException.MODULE_TYPE, uid,
                    new IllegalArgumentException("Failed to create Module Type with UID \"" + uid
                            + "\"! Can't guarantee yet that other Module Type with the same UID does not exist.")));
        }
        if (AutomationCommandsPluggable.moduleTypeRegistry.get(uid) != null) {
            exceptions.add(new ParsingNestedException(ParsingNestedException.MODULE_TYPE, uid,
                    new IllegalArgumentException("Module Type with UID \"" + uid
                            + "\" already exists! Failed to create a second with the same UID!")));
        }
    }

}
