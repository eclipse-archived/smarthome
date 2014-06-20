/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This activator sets up the needed services for the Philips hue system.
 * 
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Oliver Libutzki - Added logging for hue Binding activation
 */
public final class HueActivator implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(HueActivator.class);


    @Override
    public void start(BundleContext bc) throws Exception {
    	logger.debug("Philips hue binding has been started.");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    	logger.debug("Philips hue binding has been stopped.");
    }


}
