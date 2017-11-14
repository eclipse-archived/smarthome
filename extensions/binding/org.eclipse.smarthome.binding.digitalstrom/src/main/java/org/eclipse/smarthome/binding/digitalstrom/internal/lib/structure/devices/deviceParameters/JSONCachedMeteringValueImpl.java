/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link JSONCachedMeteringValueImpl} is the implementation of the {@link CachedMeteringValue}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONCachedMeteringValueImpl implements CachedMeteringValue {

    private DSID dsid = null;
    private double value = 0;
    private String date = null;

    public JSONCachedMeteringValueImpl(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.METERING_GET_LATEST_DSID.getKey()) != null) {
            this.dsid = new DSID(jObject.get(JSONApiResponseKeysEnum.METERING_GET_LATEST_DSID.getKey()).getAsString());
        }
        if (jObject.get(JSONApiResponseKeysEnum.METERING_GET_LATEST_VALUE.getKey()) != null) {
            this.value = jObject.get(JSONApiResponseKeysEnum.METERING_GET_LATEST_VALUE.getKey()).getAsDouble();
        }
        if (jObject.get(JSONApiResponseKeysEnum.METERING_GET_LATEST_DATE.getKey()) != null) {
            this.date = jObject.get(JSONApiResponseKeysEnum.METERING_GET_LATEST_DATE.getKey()).getAsString();
        }
    }

    @Override
    public DSID getDsid() {
        return dsid;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "dSID: " + this.getDsid() + ", date: " + this.getDate() + ", value: " + this.getValue();
    }
}
