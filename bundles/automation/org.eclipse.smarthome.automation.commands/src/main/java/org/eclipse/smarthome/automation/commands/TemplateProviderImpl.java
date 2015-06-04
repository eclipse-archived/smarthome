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

package org.eclipse.smarthome.automation.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.handler.parser.Parser;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.eclipse.smarthome.automation.handler.provider.TemplateProvider;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TemplateProviderImpl extends GeneralProvider implements TemplateProvider {

    /**
     * @param context
     */
    public TemplateProviderImpl(BundleContext context) {
        super(context);
    }

    /**
     * @see org.eclipse.smarthome.automation.core.provider.GeneralResourceBundleProvider#addingService(ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_TEMPLATE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * @param parserType
     * @param set
     * @param file
     */
    public Status exportTemplates(String parserType, Set set, File file) {
        return super.exportData(parserType, set, file);
    }

    /**
     * @param parserType
     * @param url
     * @return
     */
    public Set<Status> importTemplates(String parserType, URL url) {
        InputStreamReader inputStreamReader = null;
        Parser parser = parsers.get(parserType);
        if (parser != null)
            try {
                inputStreamReader = new InputStreamReader(new BufferedInputStream(url.openStream()));
                ArrayList portfolio = new ArrayList();
                return importData(url.toString(), parser, inputStreamReader, portfolio);
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
     * @see org.eclipse.smarthome.automation.commands.GeneralProvider#getUID(java.lang.Object)
     */
    @Override
    protected String getUID(Object providedObject) {
        return ((RuleTemplate) providedObject).getUID();
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.TemplateProvider#getTemplate(java.lang.String, java.util.Locale)
     */
    @Override
    public Template getTemplate(String UID, Locale locale) {
        synchronized (lock) {
            Localizer l = providedObjectsHolder.get(UID);
            if (l != null) {
                Template t = (Template) l.localize(locale);
                return t;
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.TemplateProvider#getTemplates(java.util.Locale)
     */
    @Override
    public Collection<Template> getTemplates(Locale locale) {
        ArrayList<Template> templatesList = new ArrayList<Template>();
        synchronized (lock) {
            Iterator i = providedObjectsHolder.values().iterator();
            while (i.hasNext()) {
                Localizer l = (Localizer) i.next();
                if (l != null) {
                    Template t = (Template) l.localize(locale);
                    if (t != null)
                        templatesList.add(t);
                }
            }
        }
        return templatesList;
    }

}
