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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;

/**
 * This class is a {@link Rule}s importer. It extends functionality of {@link AbstractProviderImpl}.
 * <p>
 * It is responsible for execution of Automation Commands, corresponding to the {@link Rule}s:
 * <ul>
 * <li>imports the {@link Rule}s from local files or from URL resources
 * <li>provides functionality for persistence of the {@link Rule}s
 * <li>removes the {@link Rule}s and their persistence
 * <li>lists the {@link Rule}s and their details
 * </ul>
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
public abstract class RuleImporterImpl<PE> extends AbstractProviderImpl<URL, PE> {

    /**
     * This constructor creates instances of this particular implementation of Rule Importer. It does not add any new
     * functionality to the constructors of the providers. Only provides consistency by invoking the parent's
     * constructor.
     * 
     * @param context is the {@link BundleContext}, used for creating a tracker for {@link Parser} services.
     * @param providerClass the class object, used for creation of a {@link Logger}, which belongs to this specific
     *            provider.
     */
    public RuleImporterImpl(BundleContext context, Class providerClass) {
        super(context, providerClass);
    }

    /**
     * This method differentiates what type of {@link Parser}s is tracked by the tracker.
     * For this concrete provider, this type is a {@link Rule} {@link Parser}.
     * 
     * @see AbstractProviderImpl#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_RULE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * @see AutomationCommandsPluggable#exportRules(String, Set, File)
     */
    public Status exportRules(String parserType, Set set, File file) {
        return super.exportData(parserType, set, file);
    }

    /**
     * @see AutomationCommandsPluggable#importRules(String, URL)
     */
    public Set<Status> importRules(String parserType, URL url) {
        InputStreamReader inputStreamReader = null;
        Parser parser = parsers.get(parserType);
        if (parser != null)
            try {
                inputStreamReader = new InputStreamReader(new BufferedInputStream(url.openStream()));
                return importData(url, parser, inputStreamReader);
            } catch (IOException e) {
                Status s = new Status(log, 0, null);
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
     * @see AbstractProviderImpl#importData(URL, Parser, InputStreamReader)
     */
    @Override
    protected Set<Status> importData(URL url, Parser parser, InputStreamReader inputStreamReader) {
        Set<Status> providedRulesStatus = parser.importData(inputStreamReader);
        if (providedRulesStatus != null && !providedRulesStatus.isEmpty()) {
            Iterator<Status> i = providedRulesStatus.iterator();
            ArrayList portfolio = new ArrayList();
            while (i.hasNext()) {
                Status s = i.next();
                if (s.hasErrors())
                    continue;
                Rule rule = (Rule) s.getResult();
                if (rule != null) {
                    AutomationCommandsPluggable.ruleReg.add(rule);
                    String uid = rule.getUID();
                    portfolio.add(uid);
                }
            } // while
            synchronized (providerPortfolio) {
                providerPortfolio.put(url, portfolio);
            }
            add(url);
        }
        return providedRulesStatus;
    }

}
