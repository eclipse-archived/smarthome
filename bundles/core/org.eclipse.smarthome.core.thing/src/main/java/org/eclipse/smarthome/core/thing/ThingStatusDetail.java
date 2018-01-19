/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.thing;

/**
 * {@link ThingStatusDetail} defines possible status details of a {@link ThingStatusInfo}.
 *
 * @author Stefan Bußweiler - Initial contribution, added new status details
 * @author Chris Jackson - Added GONE status
 */
public enum ThingStatusDetail {
    NONE,
    HANDLER_MISSING_ERROR,
    HANDLER_REGISTERING_ERROR,
    HANDLER_INITIALIZING_ERROR,
    HANDLER_CONFIGURATION_PENDING,
    CONFIGURATION_PENDING,
    COMMUNICATION_ERROR,
    CONFIGURATION_ERROR,
    BRIDGE_OFFLINE,
    FIRMWARE_UPDATING,
    DUTY_CYCLE,
    BRIDGE_UNINITIALIZED,
    /**
     * Device has been removed. Used for example when the device has been removed from its bridge and
     * the thing handler should be removed.
     */
    GONE;

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
