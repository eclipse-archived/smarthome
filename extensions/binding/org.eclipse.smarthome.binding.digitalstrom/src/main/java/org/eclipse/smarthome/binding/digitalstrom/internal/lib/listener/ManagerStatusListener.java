/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerTypes;

/**
 * The {@link ManagerStatusListener} is notified, if the state of digitalSTROM-Manager has changed.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface ManagerStatusListener {

    /**
     * This method is called whenever the state of an digitalkSTROM-Manager has changed.<br>
     * For that it passes the {@link ManagerTypes} and the new {@link ManagerStates}.
     *
     * @param managerType
     * @param newState
     */
    public void onStatusChanged(ManagerTypes managerType, ManagerStates newState);
}
