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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.automation.handler.parser.Parser;
import org.eclipse.smarthome.automation.handler.provider.ModuleTypeProvider;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ModuleTypeResourceBundleProvider extends GeneralResourceBundleProvider implements ModuleTypeProvider {

    /**
     * @param bc
     */
    public ModuleTypeResourceBundleProvider(BundleContext bc) {
        super(bc);
        path = PATH + "/module.types/";
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.provider.StreamModuleTypeProvider#remove(java.net.URI)
     */
    public void remove(URL url) {
        synchronized (lock) {
            List portfolio = providerPortfolio.get(url.toString());
            if (portfolio == null || portfolio.isEmpty())
                return;
            Iterator i = portfolio.iterator();
            while (i.hasNext()) {
                String uid = (String) i.next();
                providedObjectsHolder.remove(uid);
            }
        }
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.ModuleTypeResourceBundleProvider#getModuleType(java.lang.String,
     *      java.util.Locale)
     */
    @Override
    public ModuleType getModuleType(String UID, Locale locale) {
        Localizer l = null;
        synchronized (lock) {
            l = providedObjectsHolder.get(UID);
        }
        if (l != null) {
            ModuleType mt = (ModuleType) l.localize(locale);
            return mt;
        }
        return null;
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.ModuleTypeResourceBundleProvider#getModuleTypes(java.util.Locale)
     */
    @Override
    public Collection<ModuleType> getModuleTypes(Locale locale) {
        ArrayList moduleTypesList = new ArrayList();
        synchronized (lock) {
            Iterator i = providedObjectsHolder.values().iterator();
            while (i.hasNext()) {
                Localizer l = (Localizer) i.next();
                if (l != null) {
                    ModuleType mt = (ModuleType) l.localize(locale);
                    if (mt != null)
                        moduleTypesList.add(mt);
                }
            }
        }
        return moduleTypesList;
    }

    /**
     * @see org.eclipse.smarthome.automation.core.provider.GeneralResourceBundleProvider#addingService(ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_MODULE_TYPE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * @see org.eclipse.smarthome.automation.core.provider.GeneralResourceBundleProvider#getUID(java.lang.Object)
     */
    @Override
    protected String getUID(Object providedObject) {
        return ((ModuleType) providedObject).getUID();
    }

    protected Object getKey(Object element) {
        ModuleType mt = (ModuleType) element;
        return mt.getUID();
    }

    protected String getStorageName() {
        return "automation-module-types";
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
