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
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link BaseTemperatureControl} is a base implementation for temperature controls status and configurations. For
 * that it extends the {@link BaseZoneIdentifier}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public abstract class BaseTemperatureControl extends BaseZoneIdentifier {

    protected String controlDSUID;
    protected Short controlMode;
    protected Boolean isConfigured;

    /**
     * Creates a new {@link BaseTemperatureControl} through the {@link JsonObject} which will be returned by an zone
     * call.<br>
     * Because zone calls do not include a zoneID or zoneName in the json response, the zoneID and zoneName have to
     * be handed over the constructor.
     *
     * @param jObject must not be null
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public BaseTemperatureControl(JsonObject jObject, Integer zoneID, String zoneName) {
        super(zoneID, zoneName);
        if (jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()) != null) {
            this.isConfigured = jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()).getAsBoolean();
        }
        if (isConfigured) {
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()) != null) {
                this.controlMode = jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()) != null) {
                this.controlDSUID = jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()).getAsString();
            }
        }
    }

    /**
     * Creates a new {@link BaseTemperatureControl} through the {@link JsonObject} which will be returned by an
     * apartment call.
     *
     * @param jObject must not be null
     */
    public BaseTemperatureControl(JsonObject jObject) {
        super(jObject);
        if (jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()) != null) {
            this.isConfigured = jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()).getAsBoolean();
        }
        if (isConfigured) {
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()) != null) {
                this.controlMode = jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()) != null) {
                this.controlDSUID = jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()).getAsString();
            }
        }
    }

    /**
     * Returns the dSUID of the control sensor for heating of the zone.
     *
     * @return the controlDSUID
     */
    public String getControlDSUID() {
        return controlDSUID;
    }

    /**
     * Returns controlMode for heating of the zone.
     *
     * @return the controlMode
     */
    public Short getControlMode() {
        return controlMode;
    }

    /**
     * Returns true, if heating for this zone is configured, otherwise false.
     *
     * @return the isConfigured
     */
    public Boolean getIsConfigured() {
        return isConfigured;
    }

}
