/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link FirmwareUpdateProgressInfoEvent} is sent if there is a new progress step for a firmware update. It is
 * created by the {@link FirmwareEventFactory}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class FirmwareUpdateProgressInfoEvent extends AbstractEvent {

    /** Constant for the firmware update progress info event type. */
    public static final String TYPE = FirmwareUpdateProgressInfoEvent.class.getSimpleName();

    private final FirmwareUpdateProgressInfo progressInfo;

    private final ThingUID thingUID;

    /**
     * Creates a new {@link FirmwareUpdateProgressInfoEvent}.
     *
     * @param topic the topic of the event
     * @param payload the payload of the event
     * @param progressInfo the progress info to be sent as event
     * @param thingUID the UID of the thing whose progress info of the firmware update is to be sent
     */
    protected FirmwareUpdateProgressInfoEvent(String topic, String payload, FirmwareUpdateProgressInfo progressInfo,
            ThingUID thingUID) {
        super(topic, payload, null);
        this.progressInfo = progressInfo;
        this.thingUID = thingUID;
    }

    /**
     * Returns the {@link FirmwareUpdateProgressInfo}.
     *
     * @return the firmware update progress info
     */
    public FirmwareUpdateProgressInfo getProgressInfo() {
        return progressInfo;
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
        result = prime * result + ((progressInfo == null) ? 0 : progressInfo.hashCode());
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
        FirmwareUpdateProgressInfoEvent other = (FirmwareUpdateProgressInfoEvent) obj;
        if (progressInfo == null) {
            if (other.progressInfo != null) {
                return false;
            }
        } else if (!progressInfo.equals(other.progressInfo)) {
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
        String stepName = progressInfo.getProgressStep() == null ? null : progressInfo.getProgressStep().name();
        return String.format("The firmware update progress for thing %s changed. Step: %s Progress: %d.", thingUID,
                stepName, progressInfo.getProgress());
    }

}
