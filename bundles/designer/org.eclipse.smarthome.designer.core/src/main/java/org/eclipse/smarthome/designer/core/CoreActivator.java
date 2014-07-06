/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.smarthome.config.core.IConfigDispatcherService;
import org.eclipse.smarthome.core.scriptengine.action.ActionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
