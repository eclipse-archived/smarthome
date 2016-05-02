/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums;

/**
 * The {@link ManagerStates} contains all reachable states of the digitalSTROM-Manager in {@link ManagerTypes}
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum ManagerStates {
    RUNNING,
    STOPPED,
    INITIALIZING,
    GENERATING_SCENES;
}
