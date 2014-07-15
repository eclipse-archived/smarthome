/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.service;

import nl.q42.jue.HueBridge;

/**
 * The {@link BridgeStatusListener} is notified whenever the connection to the hue bridge is lost or resumed.
 * 
 * @author Oliver Libutzki - Initial contribution
 * @author Kai Kreuzer - added onNotAuthenticated
 *
 */
public interface BridgeStatusListener {

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is lost.
     * @param bridge the hue bridge the connection is lost to
     */
    public void onConnectionLost(HueBridge bridge);

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is resumed.
     * @param bridge the hue bridge the connection is resumed to
     */
    public void onConnectionResumed(HueBridge bridge);

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is available,
     * but requests are not allowed due to a missing or invalid authentication.
     * 
     * @param bridge the hue bridge the connection is not authorized
     */
    public void onNotAuthenticated(HueBridge bridge);
}
