/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

/**
 * The {@link FirmwareUpdateResult} enumeration defines the possible results for a firmware update.
 *
 * @author Thomas Höfer - Initial contribution
 */
public enum FirmwareUpdateResult {

    /** Indicates that the firmware update was successful. */
    SUCCESS,

    /** Indicates that the firmware update has failed. */
    ERROR,
    
    /** Indicates that the firmware update was canceled. */
    CANCELED;
}
