/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link FirmwareStatusInfoEvent} is sent if the {@link FirmwareStatusInfo} of a {@link Thing} has been changed.
 * It is created by the {@link FirmwareEventFactory}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class FirmwareStatusInfoEvent extends AbstractEvent {

    /** Constant for the firmware status info event type. */
    public static final String TYPE = FirmwareStatusInfoEvent.class.getSimpleName();

    private final FirmwareStatusInfo firmwareStatusInfo;

    private final ThingUID thingUID;

    /**
     * Creates a new {@link FirmwareStatusInfoEvent}.
     *
     * @param topic the topic of the event
     * @param payload the payload of the event
     * @param firmwareStatusInfo the firmware status info to be sent as event
     * @param thingUID the UID of the thing whose firmware status info is to be sent
     */
    protected FirmwareStatusInfoEvent(String topic, String payload, FirmwareStatusInfo firmwareStatusInfo,
            ThingUID thingUID) {
        super(topic, payload, null);
        this.firmwareStatusInfo = firmwareStatusInfo;
        this.thingUID = thingUID;
    }

    /**
     * Returns the firmware status info.
     *
     * @return the firmware status info
     */
    public FirmwareStatusInfo getFirmwareStatusInfo() {
        return firmwareStatusInfo;
    }

    /**
     * Returns the thing UID.
     *
     * @return the thing UID
     */
    public ThingUID getThingUID() {
        return thingUID;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firmwareStatusInfo == null) ? 0 : firmwareStatusInfo.hashCode());
        result = prime * result + ((thingUID == null) ? 0 : thingUID.hashCode());
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
        FirmwareStatusInfoEvent other = (FirmwareStatusInfoEvent) obj;
        if (firmwareStatusInfo == null) {
            if (other.firmwareStatusInfo != null) {
                return false;
            }
        } else if (!firmwareStatusInfo.equals(other.firmwareStatusInfo)) {
            return false;
        }
        if (thingUID == null) {
            if (other.thingUID != null) {
                return false;
            }
        } else if (!thingUID.equals(other.thingUID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        FirmwareStatus status = firmwareStatusInfo.getFirmwareStatus();

        StringBuilder sb = new StringBuilder(
                String.format("Firmware status of thing %s changed to %s.", thingUID, status.name()));

        if (status == FirmwareStatus.UPDATE_EXECUTABLE) {
            sb.append(String.format(" The new updatable firmware version is %s.",
                    firmwareStatusInfo.getUpdatableFirmwareUID().getFirmwareVersion()));
        }

        return sb.toString();
    }

}
