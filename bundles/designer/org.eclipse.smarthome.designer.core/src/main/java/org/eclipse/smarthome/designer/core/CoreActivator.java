/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.config.core.IConfigDispatcherService;
import org.eclipse.smarthome.core.scriptengine.action.ActionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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

	public static IConfigDispatcherService configDispatcher;

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
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		actionServiceTracker = new ServiceTracker<ActionService, ActionService>(
				context, ActionService.class, null);
		actionServiceTracker.open();

		ServiceReference<?> serviceReference = context
				.getServiceReference(IConfigDispatcherService.class.getName());
		configDispatcher = (IConfigDispatcherService) context
				.getService(serviceReference);

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
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		actionServiceTracker.close();
		configurationAdminTracker.close();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void updateFolderObserver() throws IOException {
		ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) CoreActivator.configurationAdminTracker.getService();
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
}
