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
import java.io.InputStream;
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
import org.eclipse.smarthome.automation.handler.provider.ModuleTypeProvider;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ModuleTypeProviderImpl extends GeneralProvider implements ModuleTypeProvider {

    /**
     * @param context
     */
    public ModuleTypeProviderImpl(BundleContext context) {
        super(context);
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
     * @param parserType
     * @param set
     * @param file
     */
    public Status exportModuleTypes(String parserType, Set set, File file) {
        return super.exportData(parserType, set, file);
    }

    /**
     * @param parserType
     * @param url
     * @return
     */
    public Set<Status> importModuleTypes(String parserType, URL url) {
        InputStreamReader inputStreamReader = null;
        Parser parser = parsers.get(parserType);
        if (parser != null)
            try {
                InputStream is = url.openStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                inputStreamReader = new InputStreamReader(bis);
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
        return ((ModuleType) providedObject).getUID();
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.ModuleTypeResourceBundleProvider#getModuleType(java.lang.String,
     *      java.util.Locale)
     */
    @Override
    public ModuleType getModuleType(String UID, Locale locale) {
        synchronized (lock) {
            Localizer l = providedObjectsHolder.get(UID);
            if (l != null) {
                ModuleType mt = (ModuleType) l.localize(locale);
                return mt;
            }
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

}
