/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import org.eclipse.smarthome.binding.hue.internal.setup.discovery.bridge.HueBridgeDiscoveryService;
import org.eclipse.smarthome.binding.hue.internal.setup.discovery.bulb.HueLightDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * This activator sets up the needed services for the Philips hue system.
 * 
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Oliver Libutzki - Added DiscoveryService implementation
 */
public final class HueActivator implements BundleActivator {

	private ServiceRegistration<?> bridgeDiscoveryServiceRegistration;
	private ServiceRegistration<?> lightDiscoveryServiceRegistration;

    @Override
    public void start(BundleContext bc) throws Exception {
    	HueLightDiscoveryService lightDiscoveryService = new HueLightDiscoveryService(bc);
    	lightDiscoveryServiceRegistration = bc.registerService(DiscoveryService.class.getName(), lightDiscoveryService, null);
    	HueBridgeDiscoveryService bridgeDiscoveryService = new HueBridgeDiscoveryService(bc);
    	bridgeDiscoveryServiceRegistration = bc.registerService(DiscoveryService.class.getName(), bridgeDiscoveryService, null);
    	
    	lightDiscoveryService.setAutoDiscoveryEnabled(true);
        bridgeDiscoveryService.setAutoDiscoveryEnabled(true);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

        HueBridgeDiscoveryService hueBridgeDiscoveryService = (HueBridgeDiscoveryService) bundleContext.getService(bridgeDiscoveryServiceRegistration.getReference());
        hueBridgeDiscoveryService.setAutoDiscoveryEnabled(false);
        bridgeDiscoveryServiceRegistration.unregister();
        bridgeDiscoveryServiceRegistration = null;

        HueLightDiscoveryService hueLightDiscoveryService = (HueLightDiscoveryService) bundleContext.getService(lightDiscoveryServiceRegistration.getReference());
        hueLightDiscoveryService.setAutoDiscoveryEnabled(false);
        lightDiscoveryServiceRegistration.unregister();
        lightDiscoveryServiceRegistration = null;
    }


}
