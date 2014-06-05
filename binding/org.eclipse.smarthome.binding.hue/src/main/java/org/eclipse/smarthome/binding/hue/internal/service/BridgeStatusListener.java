/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.service;

import nl.q42.jue.HueBridge;

public interface BridgeStatusListener {

    public void onConnectionLost(HueBridge bridge);

    public void onConnectionResumed(HueBridge bridge);
}
