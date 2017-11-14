/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;

/**
 * The {@link FirmwareStatus} enumeration defines all possible statuses for the {@link Firmware} of a {@link Thing}
 * . The property {@link Thing#PROPERTY_FIRMWARE_VERSION} must be set for a thing in order that its firmware status can
 * be determined.
 *
 * @author Thomas Höfer - Initial contribution
 */
public enum FirmwareStatus {

    /**
     * The firmware status can not be determined and hence it is unknown. Either the
     * {@link Thing#PROPERTY_FIRMWARE_VERSION} is not set for the thing or there is no {@link FirmwareProvider} that
     * provides a firmware for the {@link ThingTypeUID} of the thing.
     */
    UNKNOWN,

    /** The firmware of the thing is up to date. */
    UP_TO_DATE,

    /**
     * There is a newer firmware of the thing available. However the thing is not in a state where its firmware can be
     * updated, i.e. the operation {@link FirmwareUpdateHandler#isUpdateExecutable()} returned false.
     */
    UPDATE_AVAILABLE,

    /** There is a newer firmware of the thing available and the firmware update for the thing can be executed. */
    UPDATE_EXECUTABLE;

}
