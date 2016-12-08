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
    DUTY_CYCLE;

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
