/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener;

/**
 * The {@link SystemStateChangeListener} can be implemented to get informed by digitalSTROM system state changes. It
 * has to be registered by supported classes, e.g. the {@link TemperatureControlManager} or self implemented classes.
 *
 * @author Michael Ochel
 * @author Matthias Siegele
 */
public interface SystemStateChangeListener {

    /**
     * Will be called, if a digitalSTROM system state has changed.
     *
     * @param stateType of the digitalSTROM system state
     * @param newState of the digitalSTROM system state
     */
    void onSystemStateChanged(String stateType, String newState);
}
