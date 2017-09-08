/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.json.internal;

import com.google.gson.annotations.SerializedName;

/**
 * Internal data structure of the {@link JsonStorage}
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class StorageEntry {

    @SerializedName("class") // in order to stay backwards compatible
    private final String entityClassName;
    private final Object value;

    public StorageEntry(String entityClassName, Object value) {
        this.entityClassName = entityClassName;
        this.value = value;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public Object getValue() {
        return value;
    }

}
