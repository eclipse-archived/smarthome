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
package org.eclipse.smarthome.binding.hue.internal;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Detailed information about an object on the hue bridge
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
public class FullHueObject extends HueObject {

    private String type;
    private String modelid;
    @SerializedName("manufacturername")
    private String manufacturerName;
    @SerializedName("productname")
    private String productName;
    private String swversion;
    private String uniqueid;

    public FullHueObject() {
        super();
    }

    /**
     * Returns the type of the object.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type of the object.
     */
    protected void setType(final String type) {
        this.type = type;
    }

    /**
     * Returns the model ID of the object.
     *
     * @return model id
     */
    public String getModelID() {
        return modelid;
    }

    /**
     * Set the model ID of the object.
     */
    protected void setModelID(final String modelId) {
        this.modelid = modelId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Returns the software version of the object.
     *
     * @return software version
     */
    public String getSoftwareVersion() {
        return swversion;
    }

    /**
     * Returns the unique id of the object. The unique is the MAC address of the device with a unique endpoint id in the
     * form: AA:BB:CC:DD:EE:FF:00:11-XX
     *
     * @return the unique id, can be null for some virtual types like the daylight sensor
     */
    @Nullable
    public String getUniqueID() {
        return uniqueid;
    }
}
