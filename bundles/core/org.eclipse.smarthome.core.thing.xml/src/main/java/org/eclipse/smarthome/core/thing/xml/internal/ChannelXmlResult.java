/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.config.xml.util.NodeValue;

/**
 * The {@link ChannelXmlResult} is an intermediate XML conversion result object.
 * 
 * @author Chris Jackson - Initial Contribution
 */
public class ChannelXmlResult {

    private String id;
    private String typeId;
    List<NodeValue> properties;

    public ChannelXmlResult(String id, String typeId, List<NodeValue> properties) {
        this.id = id;
        this.typeId = typeId;
        this.properties = properties;
    }

    /**
     * Retrieves the ID for this channel
     * 
     * @return channel id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Retrieves the type ID for this channel
     * 
     * @return type ID
     */
    public String getTypeId() {
        return this.typeId;
    }

    /**
     * Retrieves the properties for this channel
     * 
     * @return properties list (not null)
     */
    public List<NodeValue> getProperties() {
        if (this.properties == null) {
            return new ArrayList<>(0);
        }
        return this.properties;
    }

    @Override
    public String toString() {
        return "ChannelTypeXmlResult [id=" + id + ", typeId=" + typeId + ", properties=" + properties + "]";
    }
}
