/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.util.NodeValue;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;

import com.thoughtworks.xstream.converters.ConversionException;

/**
 * The {@link ThingTypeXmlResult} is an intermediate XML conversion result object which
 * contains all fields needed to create a concrete {@link ThingType} object.
 * <p>
 * If a {@link ConfigDescription} object exists, it must be added to the according {@link ConfigDescriptionProvider}.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Ivan Iliev - Added support for system wide channel types
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Chris Jackson - Added channel properties
 */
public class ThingTypeXmlResult {

    protected ThingTypeUID thingTypeUID;
    protected List<String> supportedBridgeTypeUIDs;
    protected String label;
    protected String description;
    protected List<ChannelXmlResult> channelTypeReferences;
    protected List<ChannelXmlResult> channelGroupTypeReferences;
    protected List<NodeValue> properties;
    protected URI configDescriptionURI;
    protected ConfigDescription configDescription;

    public ThingTypeXmlResult(ThingTypeUID thingTypeUID, List<String> supportedBridgeTypeUIDs, String label,
            String description, List<ChannelXmlResult>[] channelTypeReferenceObjects, List<NodeValue> properties,
            Object[] configDescriptionObjects) {

        this.thingTypeUID = thingTypeUID;
        this.supportedBridgeTypeUIDs = supportedBridgeTypeUIDs;
        this.label = label;
        this.description = description;
        this.channelTypeReferences = channelTypeReferenceObjects[0];
        this.channelGroupTypeReferences = channelTypeReferenceObjects[1];
        this.properties = properties;
        this.configDescriptionURI = (URI) configDescriptionObjects[0];
        this.configDescription = (ConfigDescription) configDescriptionObjects[1];
    }

    public ConfigDescription getConfigDescription() {
        return this.configDescription;
    }

    protected List<ChannelDefinition> toChannelDefinitions(List<ChannelXmlResult> channelTypeReferences)
            throws ConversionException {

        List<ChannelDefinition> channelTypeDefinitions = null;

        if ((channelTypeReferences != null) && (channelTypeReferences.size() > 0)) {
            channelTypeDefinitions = new ArrayList<>(channelTypeReferences.size());

            for (ChannelXmlResult channelTypeReference : channelTypeReferences) {
                String id = channelTypeReference.getId();
                String typeId = channelTypeReference.getTypeId();

                String typeUID = String.format("%s:%s", this.thingTypeUID.getBindingId(), typeId);

                int systemPrefixIdx = typeId.indexOf(XmlHelper.SYSTEM_NAMESPACE_PREFIX);
                if (systemPrefixIdx != -1) {
                    typeUID = XmlHelper.getSystemUID(typeId);
                }

                // Convert the channel properties into a map
                Map<String, String> propertiesMap = new HashMap<>();
                for (NodeValue property : channelTypeReference.getProperties()) {
                    propertiesMap.put(property.getAttributes().get("name"), (String) property.getValue());
                }

                ChannelDefinition channelDefinition = new ChannelDefinition(id, new ChannelTypeUID(typeUID),
                        propertiesMap, channelTypeReference.getLabel(), channelTypeReference.getDescription());
                channelTypeDefinitions.add(channelDefinition);
            }

        }

        return channelTypeDefinitions;
    }

    protected List<ChannelGroupDefinition> toChannelGroupDefinitions(List<ChannelXmlResult> channelGroupTypeReferences)
            throws ConversionException {

        List<ChannelGroupDefinition> channelGroupTypeDefinitions = null;

        if ((channelGroupTypeReferences != null) && (channelGroupTypeReferences.size() > 0)) {
            channelGroupTypeDefinitions = new ArrayList<>(channelGroupTypeReferences.size());

            for (ChannelXmlResult channelGroupTypeReference : channelGroupTypeReferences) {
                String id = channelGroupTypeReference.getId();
                String typeId = channelGroupTypeReference.getTypeId();

                String typeUID = String.format("%s:%s", this.thingTypeUID.getBindingId(), typeId);

                ChannelGroupDefinition channelGroupDefinition = new ChannelGroupDefinition(id,
                        new ChannelGroupTypeUID(typeUID));

                channelGroupTypeDefinitions.add(channelGroupDefinition);

            }
        }

        return channelGroupTypeDefinitions;
    }

    protected Map<String, String> toPropertiesMap() {
        if (properties == null) {
            return null;
        }

        Map<String, String> propertiesMap = new HashMap<>();
        for (NodeValue property : properties) {
            propertiesMap.put(property.getAttributes().get("name"), (String) property.getValue());
        }
        return propertiesMap;
    }

    public ThingType toThingType() throws ConversionException {

        ThingType thingType = new ThingType(this.thingTypeUID, this.supportedBridgeTypeUIDs, this.label,
                this.description, toChannelDefinitions(this.channelTypeReferences),
                toChannelGroupDefinitions(this.channelGroupTypeReferences), toPropertiesMap(),
                this.configDescriptionURI);

        return thingType;
    }

    @Override
    public String toString() {
        return "ThingTypeXmlResult [thingTypeUID=" + thingTypeUID + ", supportedBridgeTypeUIDs="
                + supportedBridgeTypeUIDs + ", label=" + label + ", description=" + description
                + ", channelTypeReferences=" + channelTypeReferences + ", channelGroupTypeReferences="
                + channelGroupTypeReferences + ", properties=" + properties + ", configDescriptionURI="
                + configDescriptionURI + ", configDescription=" + configDescription + "]";
    }

}
