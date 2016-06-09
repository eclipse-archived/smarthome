/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.importers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * This class is implementation of {@link ModuleTypeProvider}. It extends functionality of
 * {@link AbstractImporter} by importing the {@link ModuleType}s from local files.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ModuleTypeImporter extends AbstractImporter<ModuleType>implements ModuleTypeProvider {

    /**
     * This field holds a reference to the {@link TemplateProvider} service registration.
     */
    @SuppressWarnings("rawtypes")
    protected ServiceRegistration mtpReg;
    private ModuleTypeRegistry moduleTypeRegistry;

    /**
     * This constructor creates instances of this particular implementation of {@link ModuleTypeProvider}.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     * @param moduleTypeRegistry {@link ModuleTypeRegistry}
     */
    public ModuleTypeImporter(BundleContext context, ModuleTypeRegistry moduleTypeRegistry) {
        super(context);
        setModuleTypeRegistry(moduleTypeRegistry);
    }

    public void setModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        this.moduleTypeRegistry = moduleTypeRegistry;
    }

    /**
     * This method differentiates what type of {@link Parser}s is tracked by the tracker.
     * For this concrete provider, this type is a {@link ModuleType} {@link Parser}.
     */
    @Override
    public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_MODULE_TYPE)) {
            @SuppressWarnings("unchecked")
            Parser<ModuleType> service = (Parser<ModuleType>) bc.getService(reference);
            String parserType = (String) reference.getProperty(Parser.FORMAT);
            parserType = parserType == null ? Parser.FORMAT_JSON : parserType;
            parsers.put(parserType, service);
            List<URL> value = urls.get(parserType);
            if (value != null && !value.isEmpty()) {
                for (URL url : value) {
                    importModuleTypes(parserType, url);
                }
            }
            return service;
        }
        return null;
    }

    /**
     * This method is responsible for importing a set of ModuleTypes from a specified URL resource.
     *
     * @param parserType is relevant to the format that you need for conversion of the ModuleTypes in text.
     * @param url a specified URL for import.
     */
    public void importModuleTypes(String parserType, URL url) {
        Parser<ModuleType> parser = parsers.get(parserType);
        if (parser != null) {
            InputStream is = null;
            InputStreamReader inputStreamReader = null;
            try {
                is = url.openStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                inputStreamReader = new InputStreamReader(bis);
                importData(parser, inputStreamReader);
            } catch (IOException e) {
                logger.debug(e.getMessage(), e);
            } finally {
                try {
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                } catch (IOException e) {
                }
            }
        } else {
            List<URL> value = urls.get(parserType);
            if (value == null) {
                value = new ArrayList<URL>();
            }
            value.add(url);
            urls.put(parserType, value);
            ParsingException e = new ParsingException(new ParsingNestedException(ParsingNestedException.MODULE_TYPE,
                    null, new Exception("Parser " + parserType + " not available")));
            logger.debug(e.getMessage(), e);
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
            return !providedObjectsHolder.isEmpty() ? providedObjectsHolder.values()
                    : Collections.<ModuleType> emptyList();
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
    protected void importData(Parser<ModuleType> parser, InputStreamReader inputStreamReader) {
        Set<ModuleType> providedObjects = null;
        try {
            providedObjects = parser.parse(inputStreamReader);
        } catch (ParsingException e) {
            logger.debug(e.getMessage(), e);
        }
        if (providedObjects != null && !providedObjects.isEmpty()) {
            String uid = null;
            List<String> portfolio = new ArrayList<String>();
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
                } else {
                    importDataExceptions.addAll(exceptions);
                }
            }
            if (importDataExceptions.isEmpty()) {
                Dictionary<String, Object> properties = new Hashtable<String, Object>();
                properties.put(REG_PROPERTY_MODULE_TYPES, providedObjectsHolder.keySet());
                if (mtpReg == null) {
                    mtpReg = bc.registerService(ModuleTypeProvider.class.getName(), this, properties);
                } else {
                    mtpReg.setProperties(properties);
                }
            } else {
                ParsingException e = new ParsingException(importDataExceptions);
                logger.debug(e.getMessage(), e);
            }
        }
    }

    /**
     * This method is responsible for checking the existence of {@link ModuleType}s with the same
     * UIDs before these objects to be added in the system.
     *
     * @param uid UID of the newly created {@link ModuleType}, which to be checked.
     * @param exceptions accumulates exceptions if {@link ModuleType} with the same UID exists.
     */
    protected void checkExistence(String uid, List<ParsingNestedException> exceptions) {
        if (moduleTypeRegistry == null) {
            exceptions.add(new ParsingNestedException(ParsingNestedException.MODULE_TYPE, uid,
                    new IllegalArgumentException("Failed to create Module Type with UID \"" + uid
                            + "\"! Can't guarantee yet that other Module Type with the same UID does not exist.")));
        }
        if (moduleTypeRegistry.get(uid) != null) {
            exceptions.add(new ParsingNestedException(ParsingNestedException.MODULE_TYPE, uid,
                    new IllegalArgumentException("Module Type with UID \"" + uid
                            + "\" already exists! Failed to create a second with the same UID!")));
        }
    }

}