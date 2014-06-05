/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.setup.discovery.bridge;

import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;

/**
 * The {@link UPnPDiscoveryListener} is notified when a UPnP device is added or removed.
 * @author Oliver Libutzki - Initial contribution
 *
 */
public interface UPnPDiscoveryListener {

	/**
	 * The method is called when a {@link UPnPDevice} has been added.
	 * @param uPnPDeviceRef the {@link ServiceReference} to the added device.
	 */
	void onDeviceAdded(ServiceReference<?> uPnPDeviceRef);
	
	/**
	 * The method is called when a {@link UPnPDevice} has been removed.
	 * @param uPnPDeviceRef the {@link ServiceReference} to the removed device.
	 */
	void onDeviceRemoved(ServiceReference<?> uPnPDeviceRef);
}
