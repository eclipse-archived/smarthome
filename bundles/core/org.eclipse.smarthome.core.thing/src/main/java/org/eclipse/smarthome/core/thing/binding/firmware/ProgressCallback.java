/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.firmware;

/**
 * The {@link ProgressCallback} is injected into the
 * {@link FirmwareUpdateHandler#updateFirmware(Firmware, ProgressCallback)} operation in order to post progress
 * information about the firmware update process.
 *
 * @author Thomas Höfer - Initial contribution
 */
public interface ProgressCallback {

    /**
     * Callback operation to define the {@link ProgressStep}s for the sequence of the firmware update. So if the
     * operation is invoked with the following progress steps
     * <ul>
     * <li>{@link ProgressStep#DOWNLOADING}</li>
     * <li>{@link ProgressStep#TRANSFERRING}</li>
     * <li>{@link ProgressStep#UPDATING}</li>
     * </ul>
     * then this will mean that the firmware update implementation will initially download the firmware, then
     * it will transfer the firmware to the actual device and in a final step it will trigger the update.
     *
     * @param sequence the progress steps describing the sequence of the firmware update process (must not be null
     *            or empty)
     *
     * @throws IllegalArgumentException if given sequence is null or empty
     */
    void defineSequence(ProgressStep... sequence);

    /**
     * Callback operation to indicate that the next progress step is going to be executed. Following the example of the
     * {@link ProgressCallback#defineSequence(ProgressStep...)} operation then the first invocation of this operation
     * will indicate that firmware update handler is going to download the firmware, the second invocation will indicate
     * that the handler is going to transfer the firmware to the device and consequently the third invocation will
     * indicate that the handler is going to trigger the update.
     *
     * @throws IllegalStateException if
     *             <ul>
     *             <li>there is no further step to be executed</li>
     *             <li>if no sequence was defined</li>
     *             </ul>
     */
    void next();

    /**
     * Callback operation to indicate that the firmware update has failed.
     *
     * @param errorMessageKey the key of the error message to be internationalized (must not be null or empty)
     * @param arguments the arguments to be injected into the internationalized error message (can be null)
     *
     * @throws IllegalArgumentException if given error message key is null or empty
     */
    void failed(String errorMessageKey, Object... arguments);

    /**
     * Callback operation to indicate that the firmware update was successful.
     */
    void success();
}
