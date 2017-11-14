/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link FirmwareUpdateResultInfoEvent} is sent if the firmware update has been finished. It is created by the
 * {@link FirmwareEventFactory}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class FirmwareUpdateResultInfoEvent extends AbstractEvent {

    /** Constant for the firmware update result info event type. */
    public static final String TYPE = FirmwareUpdateResultInfoEvent.class.getSimpleName();

    private final FirmwareUpdateResultInfo firmwareUpdateResultInfo;

    private final ThingUID thingUID;

    /**
     * Creates a new {@link FirmwareUpdateResultInfoEvent}.
     *
     * @param topic the topic of the event
     * @param payload the payload of the event
     * @param firmwareUpdateResultInfo the firmware update result info to be sent as event
     * @param thingUID the UID of the thing whose firmware update result info is to be sent
     */
    protected FirmwareUpdateResultInfoEvent(String topic, String payload,
            FirmwareUpdateResultInfo firmwareUpdateResultInfo, ThingUID thingUID) {
        super(topic, payload, null);
        this.firmwareUpdateResultInfo = firmwareUpdateResultInfo;
        this.thingUID = thingUID;
    }

    /**
     * Returns the firmware update result info.
     *
     * @return the firmware update result info
     */
    public FirmwareUpdateResultInfo getFirmwareUpdateResultInfo() {
        return firmwareUpdateResultInfo;
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
        int result = super.hashCode();
        result = prime * result + ((firmwareUpdateResultInfo == null) ? 0 : firmwareUpdateResultInfo.hashCode());
        result = prime * result + ((thingUID == null) ? 0 : thingUID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FirmwareUpdateResultInfoEvent other = (FirmwareUpdateResultInfoEvent) obj;
        if (firmwareUpdateResultInfo == null) {
            if (other.firmwareUpdateResultInfo != null) {
                return false;
            }
        } else if (!firmwareUpdateResultInfo.equals(other.firmwareUpdateResultInfo)) {
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
        FirmwareUpdateResult result = firmwareUpdateResultInfo.getResult();

        StringBuilder sb = new StringBuilder(
                String.format("The result of the firmware update for thing %s is %s.", thingUID, result.name()));

        if (result == FirmwareUpdateResult.ERROR) {
            sb.append(String.format(" The error message is %s.", firmwareUpdateResultInfo.getErrorMessage()));
        }

        return sb.toString();
    }

}
