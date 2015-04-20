/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

/**
 * {@link ThingStatusDetail} defines possible status details of a {@link ThingStatusInfo}.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public enum ThingStatusDetail {
    NONE(0),
    HANDLER_MISSING_ERROR(1),
    HANDLER_INITIALIZING_ERROR(2),
    CONFIGURATION_PENDING(3),
    COMMUNICATION_ERROR(4),
    CONFIGURATION_ERROR(5),
    BRIDGE_OFFLINE(6),
    FIRMWARE_UPDATING(7),
    DUTY_CYCLE(8);

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
    
    public static UninitializedStatus UNINITIALIZED = new UninitializedStatus();

    public static OnlineStatus ONLINE = new OnlineStatus();

    public static OfflineStatus OFFLINE = new OfflineStatus();
    

    public static final class UninitializedStatus {
        private UninitializedStatus() {}
        
        public ThingStatusDetail HANDLER_MISSING_ERROR = ThingStatusDetail.HANDLER_MISSING_ERROR;
        public ThingStatusDetail HANDLER_INITIALIZING_ERROR = ThingStatusDetail.HANDLER_INITIALIZING_ERROR;
    };

    public static final class OnlineStatus {
        private OnlineStatus() {}

        public ThingStatusDetail CONFIGURATION_PENDING = ThingStatusDetail.CONFIGURATION_PENDING;
    };
    
    public static final class OfflineStatus {
        private OfflineStatus() {}

        public ThingStatusDetail COMMUNICATION_ERROR = ThingStatusDetail.COMMUNICATION_ERROR;
        public ThingStatusDetail CONFIGURATION_ERROR = ThingStatusDetail.CONFIGURATION_ERROR;
        public ThingStatusDetail BRIDGE_OFFLINE = ThingStatusDetail.BRIDGE_OFFLINE;
        public ThingStatusDetail FIRMWARE_UPDATING = ThingStatusDetail.FIRMWARE_UPDATING;
        public ThingStatusDetail DUTY_CYCLE = ThingStatusDetail.DUTY_CYCLE;
    };

}
