/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.xml.util.NodeValue;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

import com.thoughtworks.xstream.converters.ConversionException;

/**
 * The {@link ChannelXmlResult} is an intermediate XML conversion result object.
 *
 * @author Chris Jackson - Initial Contribution
 */
public class ChannelXmlResult {

    private String id;
    private String typeId;
    String label;
    String description;
    List<NodeValue> properties;

    /**
     * Constructs a new {@link ChannelXmlResult}
     *
     * @param id the channel id
     * @param typeId the channel type id
     * @param label the channel label
     * @param description the channel description
     * @param properties a {@link List} of channel properties
     */
    public ChannelXmlResult(String id, String typeId, String label, String description, List<NodeValue> properties) {
        this.id = id;
        this.typeId = typeId;
        this.label = label;
        this.description = description;
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

    /**
     * Get the label for this channel
     *
     * @return the channel label. Can be null
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the description for this channel
     *
     * @return the channel description. Can be null
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ChannelTypeXmlResult [id=" + id + ", typeId=" + typeId + ", properties=" + properties + "]";
    }

    protected ChannelDefinition toChannelDefinition(String bindingId) throws ConversionException {
        String id = getId();
        String typeId = getTypeId();

        String typeUID = getTypeUID(bindingId, typeId);

        // Convert the channel properties into a map
        Map<String, String> propertiesMap = new HashMap<>();
        for (NodeValue property : getProperties()) {
            propertiesMap.put(property.getAttributes().get("name"), (String) property.getValue());
        }

        ChannelDefinition channelDefinition = new ChannelDefinition(id, new ChannelTypeUID(typeUID), propertiesMap,
                getLabel(), getDescription());

        return channelDefinition;
    }

    private String getTypeUID(String bindingId, String typeId) {
        if (typeId.startsWith(XmlHelper.SYSTEM_NAMESPACE_PREFIX)) {
            return XmlHelper.getSystemUID(typeId);
        } else {
            return String.format("%s:%s", bindingId, typeId);
        }
    }

}
