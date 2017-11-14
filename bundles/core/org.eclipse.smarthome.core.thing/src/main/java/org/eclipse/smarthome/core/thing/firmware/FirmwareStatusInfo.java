/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;

import com.google.common.base.Preconditions;

/**
 * The {@link FirmwareStatusInfo} represents the {@link FirmwareStatus} of a {@link Thing}. If the firmware status is
 * {@link FirmwareStatus#UPDATE_EXECUTABLE} then the information object will also provide the {@link FirmwareUID} of the
 * latest updatable firmware for the thing.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class FirmwareStatusInfo {

    private FirmwareStatus firmwareStatus;

    private FirmwareUID updatableFirmwareUID;

    /**
     * Default constructor. Will allow to instantiate this class by reflection.
     */
    protected FirmwareStatusInfo() {
        // does nothing at all
    }

    private FirmwareStatusInfo(FirmwareStatus firmwareStatus, FirmwareUID updatableFirmwareUID) {
        Preconditions.checkNotNull(firmwareStatus, "Firmware status must not be null.");
        this.firmwareStatus = firmwareStatus;
        this.updatableFirmwareUID = updatableFirmwareUID;
    }

    /**
     * Creates a new {@link FirmwareStatusInfo} having {@link FirmwareStatus#UNKNOWN) as firmware status.
     *
     * @return the firmware status info (not null)
     */
    static FirmwareStatusInfo createUnknownInfo() {
        return new FirmwareStatusInfo(FirmwareStatus.UNKNOWN, null);
    }

    /**
     * Creates a new {@link FirmwareStatusInfo} having {@link FirmwareStatus#UP_TO_DATE) as firmware status.
     *
     * @return the firmware status info (not null)
     */
    static FirmwareStatusInfo createUpToDateInfo() {
        return new FirmwareStatusInfo(FirmwareStatus.UP_TO_DATE, null);
    }

    /**
     * Creates a new {@link FirmwareStatusInfo} having {@link FirmwareStatus#UPDATE_AVAILABLE) as firmware status.
     *
     * @return the firmware status info (not null)
     */
    static FirmwareStatusInfo createUpdateAvailableInfo() {
        return new FirmwareStatusInfo(FirmwareStatus.UPDATE_AVAILABLE, null);
    }

    /**
     * Creates a new {@link FirmwareStatusInfo} having {@link FirmwareStatus#UPDATE_EXECUTBALE) as firmware status. The
     * given {@link FirmwareUID} represents the UID of the latest updatable firmware for the thing.
     *
     * @param updatableFirmwareUID the UID of the latest updatable firmware for the thing (must not be null)
     *
     * @return the firmware status info (not null)
     *
     * @throws NullPointerException if given firmware UID is null
     */
    static FirmwareStatusInfo createUpdateExecutableInfo(FirmwareUID updatableFirmwareUID) {
        Preconditions.checkNotNull(updatableFirmwareUID, "Updatable firmware UID must not be null.");
        return new FirmwareStatusInfo(FirmwareStatus.UPDATE_EXECUTABLE, updatableFirmwareUID);
    }

    /**
     * Returns the firmware status.
     *
     * @return the firmware status (not null)
     */
    public FirmwareStatus getFirmwareStatus() {
        return firmwareStatus;
    }

    /**
     * Returns the firmware UID of the latest updatable firmware for the thing.
     *
     * @return the firmware UID (only set if firmware status is {@link FirmwareStatus#UPDATE_EXECUTABLE})
     */
    public FirmwareUID getUpdatableFirmwareUID() {
        return updatableFirmwareUID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firmwareStatus == null) ? 0 : firmwareStatus.hashCode());
        result = prime * result + ((updatableFirmwareUID == null) ? 0 : updatableFirmwareUID.hashCode());
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
        FirmwareStatusInfo other = (FirmwareStatusInfo) obj;
        if (firmwareStatus != other.firmwareStatus) {
            return false;
        }
        if (updatableFirmwareUID == null) {
            if (other.updatableFirmwareUID != null) {
                return false;
            }
        } else if (!updatableFirmwareUID.equals(other.updatableFirmwareUID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FirmwareStatusInfo [firmwareStatus=" + firmwareStatus + ", updatableFirmwareUID=" + updatableFirmwareUID
                + "]";
    }

}
