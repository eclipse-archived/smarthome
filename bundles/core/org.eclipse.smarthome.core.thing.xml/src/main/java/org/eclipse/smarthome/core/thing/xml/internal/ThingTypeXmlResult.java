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
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.SystemChannelTypeProvider;
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

    protected List<ChannelDefinition> toChannelDefinitions(List<ChannelXmlResult> channelTypeReferences,
            Map<String, ChannelType> channelTypes) throws ConversionException {

        List<ChannelDefinition> channelTypeDefinitions = null;

        if ((channelTypeReferences != null) && (channelTypeReferences.size() > 0)) {
            channelTypeDefinitions = new ArrayList<>(channelTypeReferences.size());

            if (channelTypes != null) {
                for (ChannelXmlResult channelTypeReference : channelTypeReferences) {
                    String id = channelTypeReference.getId();
                    String typeId = channelTypeReference.getTypeId();

                    String typeUID = String.format("%s:%s", this.thingTypeUID.getBindingId(), typeId);

                    int systemPrefixIdx = typeId.indexOf(SystemChannelTypeProvider.NAMESPACE_PREFIX);
                    if (systemPrefixIdx != -1) {
                        typeUID = XmlHelper.getSystemUID(typeId);
                    }

                    ChannelType channelType = channelTypes.get(typeUID);
                    if (channelType != null) {
                        // Convert the channel properties into a map
                        Map<String, String> propertiesMap = new HashMap<>();
                        for (NodeValue property : channelTypeReference.getProperties()) {
                            propertiesMap.put(property.getAttributes().get("name"), (String) property.getValue());
                        }

                        ChannelDefinition channelDefinition = new ChannelDefinition(id, channelType, propertiesMap,
                                channelTypeReference.getLabel(), channelTypeReference.getDescription());
                        channelTypeDefinitions.add(channelDefinition);
                    } else {
                        throw new ConversionException("The channel type for '" + typeUID + "' is missing!");
                    }
                }
            } else {
                throw new ConversionException("Missing the definition of channel types!");
            }
        }

        return channelTypeDefinitions;
    }

    protected List<ChannelGroupDefinition> toChannelGroupDefinitions(List<ChannelXmlResult> channelGroupTypeReferences,
            Map<String, ChannelGroupType> channelGroupTypes) throws ConversionException {

        List<ChannelGroupDefinition> channelGroupTypeDefinitions = null;

        if ((channelGroupTypeReferences != null) && (channelGroupTypeReferences.size() > 0)) {
            channelGroupTypeDefinitions = new ArrayList<>(channelGroupTypeReferences.size());

            if (channelGroupTypes != null) {
                for (ChannelXmlResult channelGroupTypeReference : channelGroupTypeReferences) {
                    String id = channelGroupTypeReference.getId();
                    String typeId = channelGroupTypeReference.getTypeId();

                    String typeUID = String.format("%s:%s", this.thingTypeUID.getBindingId(), typeId);

                    ChannelGroupType channelGroupType = channelGroupTypes.get(typeUID);

                    if (channelGroupType != null) {
                        ChannelGroupDefinition channelGroupDefinition = new ChannelGroupDefinition(id,
                                channelGroupType);

                        channelGroupTypeDefinitions.add(channelGroupDefinition);
                    } else {
                        throw new ConversionException("The channel group type for '" + typeUID + "' is missing!");
                    }
                }
            } else {
                throw new ConversionException("Missing the definition of channel group types!");
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

    public ThingType toThingType(Map<String, ChannelGroupType> channelGroupTypes, Map<String, ChannelType> channelTypes)
            throws ConversionException {

        ThingType thingType = new ThingType(this.thingTypeUID, this.supportedBridgeTypeUIDs, this.label,
                this.description, toChannelDefinitions(this.channelTypeReferences, channelTypes),
                toChannelGroupDefinitions(this.channelGroupTypeReferences, channelGroupTypes), toPropertiesMap(),
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
