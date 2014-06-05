/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.setup.discovery.bulb;

import org.eclipse.smarthome.core.thing.Bridge;

/**
 * The {@link HueLightDiscoveryListener} offers the possibility to react on discovered hue lights.
 * 
 * @author Oliver Libutzki - Initial contribution
 */
public interface HueLightDiscoveryListener {

	/**
	 * This method is called whenever a new light is discovered.
	 * @param hueBridge The bridge the discovered light is paired to.
	 * @param lightId The id of the discovered light.
	 * @param lightName The name of the discovered light.
	 */
	void onHueLightDiscovered(Bridge hueBridge, String lightId, String lightName);
}
