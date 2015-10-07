/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.core;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class CoreActivator extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.smarthome.designer.core"; //$NON-NLS-1$

    // The shared instance
    private static CoreActivator plugin;

    public static ServiceTracker<ActionService, ActionService> actionServiceTracker;

    /** Tracker for the ConfigurationAdmin service */
    public static ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> configurationAdminTracker;

    /**
     * The constructor
     */
    public CoreActivator() {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        actionServiceTracker = new ServiceTracker<ActionService, ActionService>(context, ActionService.class, null);
        actionServiceTracker.open();

        configurationAdminTracker = new ServiceTracker<>(context, ConfigurationAdmin.class.getName(), null);
        configurationAdminTracker.open();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        actionServiceTracker.close();
        configurationAdminTracker.close();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void updateFolderObserver() throws IOException {
        ConfigurationAdmin configurationAdmin = CoreActivator.configurationAdminTracker.getService();
        if (configurationAdmin != null) {
            Configuration configuration;
            configuration = configurationAdmin.getConfiguration("org.eclipse.smarthome.folder", null);
            if (configuration != null) {
                Dictionary configProperties = new Properties();
                configProperties.put("items", "items");
                configuration.update(configProperties);
            }
        }
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static CoreActivator getDefault() {
        return plugin;
    }

    public static void setConfigFolder(String absolutePath) {
        Properties props = System.getProperties();
        props.setProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, absolutePath);
    }
}
