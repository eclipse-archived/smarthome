/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.firmware;

/**
 * The {@link ProgressStep} enumeration defines the possible progress steps for a firmware update. The actual sequence
 * of the firmware update is defined by the operation {@link ProgressCallback#defineSequence(ProgressStep...)}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public enum ProgressStep {

    /**
     * The {@link FirmwareUpdateHandler} is going to download / read the firmware image by reading the input stream from
     * {@link Firmware#getContent()}.
     */
    DOWNLOADING,

    /** The {@link FirmwareUpdateHandler} is going to transfer the firmware to the actual device. */
    TRANSFERRING,

    /** The {@link FirmwareUpdateHandler} is going to trigger the firmware update for the actual device. */
    UPDATING,

    /** The {@link FirmwareUpdateHandler} is going to reboot the device. */
    REBOOTING;
}
