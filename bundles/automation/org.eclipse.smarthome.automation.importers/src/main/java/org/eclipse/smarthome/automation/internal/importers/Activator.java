/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.importers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * This class is an activator of this bundle. Opens the all used service trackers and registers the services -
 * {@link ModuleTypeProvider} and {@link TemplateProvider}.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
@SuppressWarnings("rawtypes")
public class Activator implements ServiceTrackerCustomizer, BundleActivator {

    private static final String MODULE_TYPES_ROOT = "automation/moduletype";
    private static final String TEMPLATES_ROOT = "automation/template";

    private ModuleTypeImporter mti;
    private TemplateImporter ti;
    private ServiceTracker tracker;
    private BundleContext bc;

    /**
     * This method initialize importers for importing automation objects from local file system.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void start(BundleContext bc) throws Exception {
        this.bc = bc;
        try {
            Filter filter = bc.createFilter("(|(objectClass=" + TemplateRegistry.class.getName() + ")(objectClass="
                    + ModuleTypeRegistry.class.getName() + "))");
            tracker = new ServiceTracker(bc, filter, this);
            tracker.open();
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method close all used service trackers, unregisters the services -
     * {@link ModuleTypeProvider} and {@link TemplateProvider}.
     */
    @Override
    public void stop(BundleContext bc) throws Exception {
        mti.close();
        ti.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object addingService(ServiceReference reference) {
        Object service = null;
        if (reference != null) {
            service = bc.getService(reference);
        }
        if (service != null && service instanceof ModuleTypeRegistry) {
            mti = new ModuleTypeImporter(bc, (ModuleTypeRegistry) service);
            importModuleTypes(MODULE_TYPES_ROOT);
        }
        if (service != null && service instanceof TemplateRegistry) {
            ti = new TemplateImporter(bc, (TemplateRegistry) service);
            importTemplates(TEMPLATES_ROOT);
        }
        return service;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        // do nothing
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        if (service instanceof ModuleTypeRegistry) {
            mti.setModuleTypeRegistry(null);
        }
        if (service instanceof TemplateRegistry) {
            ti.setTemplateRegistry(null);
        }
    }

    private void importTemplates(String path) {
        File f = new File(path);
        if (f.exists()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        importTemplates(file.getAbsolutePath());
                    } else {
                        URL url;
                        try {
                            url = file.toURI().toURL();
                            String parserType = getParserType(url);
                            ti.importTemplates(parserType, url);
                        } catch (MalformedURLException e) {
                            // should not happen
                        }
                    }
                }
            }
        }
    }

    private void importModuleTypes(String path) {
        File f = new File(path);
        if (f.exists()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        importTemplates(file.getAbsolutePath());
                    } else {
                        URL url;
                        try {
                            url = file.toURI().toURL();
                            String parserType = getParserType(url);
                            mti.importModuleTypes(parserType, url);
                        } catch (MalformedURLException e) {
                            // should not happen
                        }
                    }
                }
            }
        }
    }

    private String getParserType(URL url) {
        String fileName = url.getPath();
        int fileExtesionStartIndex = fileName.lastIndexOf(".") + 1;
        if (fileExtesionStartIndex == -1) {
            return Parser.FORMAT_JSON;
        }
        String fileExtesion = fileName.substring(fileExtesionStartIndex);
        if (fileExtesion.equals("txt")) {
            return Parser.FORMAT_JSON;
        }
        return fileExtesion;
    }

}
