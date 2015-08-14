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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.dto.RuleDTO;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * This class is a {@link Rule}s importer. It extends functionality of {@link AbstractCommandProvider}.
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
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
public class CommandlineRuleImporter extends AbstractCommandProvider<Rule> {

    /**
     * This constructor creates instances of this particular implementation of Rule Importer. It does not add any new
     * functionality to the constructors of the providers. Only provides consistency by invoking the parent's
     * constructor.
     *
     * @param context is the {@link BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public CommandlineRuleImporter(BundleContext context) {
        super(context);
    }

    /**
     * This method differentiates what type of {@link Parser}s is tracked by the tracker.
     * For this concrete provider, this type is a {@link Rule} {@link Parser}.
     *
     * @see AbstractCommandProvider#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_RULE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * @see AutomationCommandsPluggable#exportRules(String, Set, File)
     */
    public Status exportRules(String parserType, Set<Rule> set, File file) {
        return super.exportData(parserType, set, file);
    }

    /**
     * @see AutomationCommandsPluggable#importRules(String, URL)
     */
    public Set<Status> importRules(String parserType, URL url) {
        InputStreamReader inputStreamReader = null;
        Parser<Rule> parser = parsers.get(parserType);
        if (parser != null)
            try {
                inputStreamReader = new InputStreamReader(new BufferedInputStream(url.openStream()));
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
     * @see AbstractCommandProvider#importData(URL, Parser, InputStreamReader)
     */
    @Override
    protected Set<Status> importData(URL url, Parser<Rule> parser, InputStreamReader inputStreamReader) {

        Set<Status> providedRulesStatus = parser.importData(inputStreamReader);
        if (providedRulesStatus != null && !providedRulesStatus.isEmpty()) {
            Iterator<Status> i = providedRulesStatus.iterator();
            while (i.hasNext()) {
                Status s = i.next();
                if (s.hasErrors())
                    continue;
                RuleDTO rule = (RuleDTO) s.getResult();
                if (rule != null) {
                    if (AutomationCommandsPluggable.ruleRegistry.get(rule.uid) != null) {
                        AutomationCommandsPluggable.ruleRegistry.update(factory.createRule(rule));
                    } else {
                        AutomationCommandsPluggable.ruleRegistry.add(factory.createRule(rule));
                    }
                }
            } // while
        }
        return providedRulesStatus;
    }

}
