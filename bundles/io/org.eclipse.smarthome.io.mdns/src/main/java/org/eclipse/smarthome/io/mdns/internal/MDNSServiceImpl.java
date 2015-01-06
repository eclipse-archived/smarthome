/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.mdns.internal;

import java.io.IOException;
import java.util.concurrent.Executors;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.io.mdns.MDNSService;
import org.eclipse.smarthome.io.mdns.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts the JmDNS and implements interface to register and unregister services.
 * 
 * @author Victor Belov
 *
 */
public class MDNSServiceImpl implements MDNSService {

	private final Logger logger = LoggerFactory.getLogger(MDNSServiceImpl.class);
	private JmDNS jmdns;
	
	public MDNSServiceImpl() {
	}
	
	/**
	 * @{inheritDoc}
	 */
	public void registerService(final ServiceDescription description) {
		Runnable runnable = new Runnable() {
			public void run() {
				ServiceInfo serviceInfo = ServiceInfo.create(description.serviceType, description.serviceName, description.servicePort,
						0, 0, description.serviceProperties);		
				try {
					logger.debug("Registering new service " + description.serviceType + " at port " + 
							String.valueOf(description.servicePort));
					jmdns.registerService(serviceInfo);
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		};
		Executors.newSingleThreadExecutor().execute(runnable);
	}

	/**
	 * @{inheritDoc}
	 */
	public void unregisterService(ServiceDescription description) {
		ServiceInfo serviceInfo = ServiceInfo.create(description.serviceType, description.serviceName, description.servicePort,
				0, 0, description.serviceProperties);
		logger.debug("Unregistering service " + description.serviceType + " at port " + 
				String.valueOf(description.servicePort));
		jmdns.unregisterService(serviceInfo);
	}

	/**
	 * This method unregisters all services from Bonjour/MDNS
	 */
	protected void unregisterAllServices() {
		jmdns.unregisterAllServices();
	}
	
	public void activate() {
		try {
			jmdns = JmDNS.create();
			logger.debug("mDNS service has been started");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public void deactivate() {
		unregisterAllServices();
		try {
			jmdns.close();
			logger.debug("mDNS service has been stopped");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

}
