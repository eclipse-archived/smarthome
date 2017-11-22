/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
