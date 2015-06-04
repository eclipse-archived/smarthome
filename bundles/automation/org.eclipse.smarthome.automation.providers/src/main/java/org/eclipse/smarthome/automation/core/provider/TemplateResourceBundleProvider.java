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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.smarthome.automation.handler.parser.Parser;
import org.eclipse.smarthome.automation.handler.provider.TemplateProvider;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TemplateResourceBundleProvider extends GeneralResourceBundleProvider implements TemplateProvider {

    public TemplateResourceBundleProvider(BundleContext context) {
        super(context);
        path = PATH + "/templates/";
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.TemplateProvider#getTemplate(java.lang.String, java.util.Locale)
     */
    @Override
    public Template getTemplate(String UID, Locale locale) {
        Localizer l = null;
        synchronized (lock) {
            l = providedObjectsHolder.get(UID);
        }
        if (l != null) {
            Template t = (Template) l.localize(locale);
            return t;
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
     * @see org.eclipse.smarthome.automation.core.provider.GeneralResourceBundleProvider#getUID(java.lang.Object)
     */
    @Override
    protected String getUID(Object providedObject) {
        return ((RuleTemplate) providedObject).getUID();
    }

    protected Object getKey(Object element) {
        RuleTemplate rt = (RuleTemplate) element;
        return rt.getUID();
    }

    protected String getStorageName() {
        return "automation-templates";
    }

    protected String keyToString(Object key) {
        return (String) key;
    }

    protected Object toElement(String key, Object persistableElement) {
        return persistableElement;
    }

    protected Object toPersistableElement(Object element) {
        return element;
    }

}
