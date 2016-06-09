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
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * This class is implementation of {@link TemplateProvider}. It extends functionality of {@link AbstractImporter}
 * by importing the {@link RuleTemplate}s from local files.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TemplateImporter extends AbstractImporter<RuleTemplate>implements TemplateProvider {

    /**
     * This field holds a reference to the {@link TemplateProvider} service registration.
     */
    @SuppressWarnings("rawtypes")
    protected ServiceRegistration tpReg;
    private TemplateRegistry templateRegistry;

    /**
     * This constructor creates instances of this particular implementation of {@link TemplateProvider}.
     *
     * @param context is the {@link BundleContext}, used for creating a tracker for {@link Parser} services.
     * @param templateRegistry {@link TemplateRegistry}
     */
    public TemplateImporter(BundleContext context, TemplateRegistry templateRegistry) {
        super(context);
        setTemplateRegistry(templateRegistry);
    }

    public void setTemplateRegistry(TemplateRegistry templateRegistry) {
        this.templateRegistry = templateRegistry;
    }

    /**
     * This method differentiates what type of {@link Parser}s is tracked by the tracker.
     * For this concrete provider, this type is a {@link RuleTemplate} {@link Parser}.
     */
    @Override
    public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_TEMPLATE)) {
            @SuppressWarnings("unchecked")
            Parser<RuleTemplate> service = (Parser<RuleTemplate>) bc.getService(reference);
            String parserType = (String) reference.getProperty(Parser.FORMAT);
            parserType = parserType == null ? Parser.FORMAT_JSON : parserType;
            parsers.put(parserType, service);
            List<URL> value = urls.get(parserType);
            if (value != null && !value.isEmpty()) {
                for (URL url : value) {
                    importTemplates(parserType, url);
                }
            }
            return service;
        }
        return null;
    }

    /**
     * This method is responsible for importing a set of RuleTemplates from a specified file or URL resource.
     *
     * @param parserType is relevant to the format that you need for conversion of the RuleTemplates in text.
     * @param url a specified URL for import.
     */
    public void importTemplates(String parserType, URL url) {
        Parser<RuleTemplate> parser = parsers.get(parserType);
        if (parser != null) {
            InputStreamReader inputStreamReader = null;
            try {
                inputStreamReader = new InputStreamReader(new BufferedInputStream(url.openStream()));
                importData(parser, inputStreamReader);
            } catch (IOException e) {
                logger.debug(e.getLocalizedMessage(), e);
            } finally {
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            List<URL> value = urls.get(parserType);
            if (value == null) {
                value = new ArrayList<URL>();
            }
            value.add(url);
            urls.put(parserType, value);
            ParsingException e = new ParsingException(new ParsingNestedException(ParsingNestedException.TEMPLATE, null,
                    new Exception("Parser " + parserType + " not available")));
            logger.debug(e.getLocalizedMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public RuleTemplate getTemplate(String UID, Locale locale) {
        synchronized (providedObjectsHolder) {
            return providedObjectsHolder.get(UID);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<RuleTemplate> getTemplates(Locale locale) {
        synchronized (providedObjectsHolder) {
            return providedObjectsHolder.values();
        }
    }

    @Override
    public void close() {
        if (tpReg != null) {
            tpReg.unregister();
            tpReg = null;
        }
        super.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void importData(Parser<RuleTemplate> parser, InputStreamReader inputStreamReader) {
        Set<RuleTemplate> providedObjects = null;
        try {
            providedObjects = parser.parse(inputStreamReader);
        } catch (ParsingException e) {
            logger.debug(e.getLocalizedMessage(), e);
        }
        if (providedObjects != null && !providedObjects.isEmpty()) {
            List<String> portfolio = new ArrayList<String>();
            List<ParsingNestedException> importDataExceptions = new ArrayList<ParsingNestedException>();
            for (RuleTemplate ruleT : providedObjects) {
                List<ParsingNestedException> exceptions = new ArrayList<ParsingNestedException>();
                String uid = ruleT.getUID();
                checkExistence(uid, exceptions);
                if (exceptions.isEmpty()) {
                    portfolio.add(uid);
                    synchronized (providedObjectsHolder) {
                        providedObjectsHolder.put(uid, ruleT);
                    }
                } else {
                    importDataExceptions.addAll(exceptions);
                }
            }
            if (importDataExceptions.isEmpty()) {
                Dictionary<String, Object> properties = new Hashtable<String, Object>();
                properties.put(REG_PROPERTY_RULE_TEMPLATES, providedObjectsHolder.keySet());
                if (tpReg == null) {
                    tpReg = bc.registerService(TemplateProvider.class.getName(), this, properties);
                } else {
                    tpReg.setProperties(properties);
                }
            } else {
                ParsingException e = new ParsingException(importDataExceptions);
                logger.debug(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * This method is responsible for checking the existence of {@link Template}s with the same
     * UIDs before these objects to be added in the system.
     *
     * @param uid UID of the newly created {@link Template}, which to be checked.
     * @param exceptions accumulates exceptions if {@link Template} with the same UID exists.
     */
    protected void checkExistence(String uid, List<ParsingNestedException> exceptions) {
        if (templateRegistry == null) {
            exceptions.add(new ParsingNestedException(ParsingNestedException.TEMPLATE, uid,
                    new IllegalArgumentException("Failed to create Rule Template with UID \"" + uid
                            + "\"! Can't guarantee yet that other Rule Template with the same UID does not exist.")));
        }
        if (templateRegistry.get(uid) != null) {
            exceptions.add(new ParsingNestedException(ParsingNestedException.TEMPLATE, uid,
                    new IllegalArgumentException("Rule Template with UID \"" + uid
                            + "\" already exists! Failed to create a second with the same UID!")));
        }
    }

}
