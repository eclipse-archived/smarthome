/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

/**
 * {@link ThingStatusDetail} defines possible status details of a {@link ThingStatusInfo}.
 *
 * @author Stefan Bußweiler - Initial contribution, added new status details
 */
public enum ThingStatusDetail {
    NONE(0),
    HANDLER_MISSING_ERROR(1),
    HANDLER_REGISTERING_ERROR(2),
    HANDLER_INITIALIZING_ERROR(3),
    HANDLER_CONFIGURATION_PENDING(4),
    CONFIGURATION_PENDING(5),
    COMMUNICATION_ERROR(6),
    CONFIGURATION_ERROR(7),
    BRIDGE_OFFLINE(8),
    FIRMWARE_UPDATING(9),
    DUTY_CYCLE(10);

    private final int value;

    private ThingStatusDetail(final int newValue) {
        value = newValue;
    }

    /**
     * Gets the value of a thing status detail.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    public static OnlineStatus ONLINE = new OnlineStatus();

    public static OfflineStatus OFFLINE = new OfflineStatus();

    public static final class OnlineStatus {
        private OnlineStatus() {
        }

        public ThingStatusDetail CONFIGURATION_PENDING = ThingStatusDetail.CONFIGURATION_PENDING;
    };

    public static final class OfflineStatus {
        private OfflineStatus() {
        }

        public ThingStatusDetail COMMUNICATION_ERROR = ThingStatusDetail.COMMUNICATION_ERROR;
        public ThingStatusDetail CONFIGURATION_ERROR = ThingStatusDetail.CONFIGURATION_ERROR;
        public ThingStatusDetail BRIDGE_OFFLINE = ThingStatusDetail.BRIDGE_OFFLINE;
        public ThingStatusDetail FIRMWARE_UPDATING = ThingStatusDetail.FIRMWARE_UPDATING;
        public ThingStatusDetail DUTY_CYCLE = ThingStatusDetail.DUTY_CYCLE;
    };

}
