/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.setup.discovery.bridge;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The {@link HueBridgeServiceTracker} tracks for {@link UPnPDevice}s having a
 * model name which starts with "Philips hue bridge" and notified the given
 * {@link UPnPDiscoveryListener} on added or removed services.
 * 
 * @author Oliver Libutzki - Initial contribution
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class HueBridgeServiceTracker extends ServiceTracker {

	private UPnPDiscoveryListener listener;

	public HueBridgeServiceTracker(BundleContext bundleContext,
			UPnPDiscoveryListener listener) throws InvalidSyntaxException {
		super(bundleContext, bundleContext.createFilter(getFilter()), null);
		this.listener = listener;
	}

	private static String getFilter() {
		return "(&(" + Constants.OBJECTCLASS + "=" + UPnPDevice.class.getName()
				+ ")(" + UPnPDevice.MODEL_NAME + "=Philips hue bridge*))";
	}

	@Override
	public UPnPDevice addingService(ServiceReference reference) {
		UPnPDevice uPnPDevice = (UPnPDevice) super.addingService(reference);
		listener.onDeviceAdded(reference);
		return uPnPDevice;
	}

	@Override
	public void removedService(ServiceReference reference, Object uPnPDevice) {
		super.removedService(reference, uPnPDevice);
		listener.onDeviceRemoved(reference);
	}

}
