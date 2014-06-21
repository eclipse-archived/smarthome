/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.internal;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class ConfigActivator implements BundleActivator {
	
	/** Tracker for the ConfigurationAdmin service */
	public static ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> configurationAdminTracker;
	
	/**
	 * Called whenever the OSGi framework starts our bundle
	 */
	public void start(BundleContext bc) throws Exception {
        configurationAdminTracker = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(bc,
                ConfigurationAdmin.class.getName(), null) {
        	
        };
		configurationAdminTracker.open();
	}

	/**
	 * Called whenever the OSGi framework stops our bundle
	 */
	public void stop(BundleContext bc) throws Exception {
		configurationAdminTracker.close();
	}
	
}
