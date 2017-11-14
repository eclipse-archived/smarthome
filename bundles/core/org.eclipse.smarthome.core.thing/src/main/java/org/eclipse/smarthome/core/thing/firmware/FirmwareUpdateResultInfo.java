/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import com.google.common.base.Preconditions;

/**
 * The {@link FirmwareUpdateResultInfo} contains information about the result of a firmware update.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class FirmwareUpdateResultInfo {

    private FirmwareUpdateResult result;

    private String errorMessage;

    /**
     * Default constructor. Will allow to instantiate this class by reflection.
     */
    protected FirmwareUpdateResultInfo() {
        // does nothing at all
    }

    /**
     * Creates a new {@link FirmwareUpdateResultInfo}.
     *
     * @param result the result of the firmware update (must not be null)
     * @param errorMessage the error message in case of result is {@link FirmwareUpdateResult#ERROR} (must not be null
     *            or empty for erroneous firmware updates; ignored for successful firmware updates)
     *
     * @throws NullPointerException if result is null
     * @throws IllegalArgumentException if error message is null or empty for erroneous firmware updates
     */
    FirmwareUpdateResultInfo(FirmwareUpdateResult result, String errorMessage) {
        Preconditions.checkNotNull(result, "Firmware update result must not be null");
        this.result = result;

        if (result != FirmwareUpdateResult.SUCCESS) {
            Preconditions.checkArgument(errorMessage != null && !errorMessage.isEmpty(),
                    "Error message must not be null or empty for erroneous firmare updates");
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Returns the result of the firmware update.
     *
     * @return the result of the firmware update
     */
    public FirmwareUpdateResult getResult() {
        return result;
    }

    /**
     * Returns the error message in case of result is {@link FirmwareUpdateResult#ERROR}.
     *
     * @return the error message in case of erroneous firmware updates (is null for successful firmware updates)
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FirmwareUpdateResultInfo other = (FirmwareUpdateResultInfo) obj;
        if (errorMessage == null) {
            if (other.errorMessage != null) {
                return false;
            }
        } else if (!errorMessage.equals(other.errorMessage)) {
            return false;
        }
        if (result != other.result) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FirmwareUpdateResultInfo [result=" + result + ", errorMessage=" + errorMessage + "]";
    }

}
