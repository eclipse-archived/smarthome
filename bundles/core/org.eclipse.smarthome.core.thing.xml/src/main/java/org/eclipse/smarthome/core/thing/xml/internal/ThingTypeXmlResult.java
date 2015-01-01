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
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.util.NodeAttributes;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;

import com.thoughtworks.xstream.converters.ConversionException;


/**
 * The {@link ThingTypeXmlResult} is an intermediate XML conversion result object which
 * contains all fields needed to create a concrete {@link ThingType} object.
 * <p>
 * If a {@link ConfigDescription} object exists, it must be added to the according
 * {@link ConfigDescriptionProvider}. 
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class ThingTypeXmlResult {

    protected ThingTypeUID thingTypeUID;
    protected List<String> supportedBridgeTypeUIDs;
    protected String label;
    protected String description;
    protected List<NodeAttributes> channelDefinitionTypes;
    protected URI configDescriptionURI;
    protected ConfigDescription configDescription;


    public ThingTypeXmlResult(ThingTypeUID thingTypeUID, List<String> supportedBridgeTypeUIDs,
            String label, String description, List<NodeAttributes> channelDefinitionTypes,
            Object[] configDescriptionObjects) {

        this.thingTypeUID = thingTypeUID;
        this.supportedBridgeTypeUIDs = supportedBridgeTypeUIDs;
        this.label = label;
        this.description = description;
        this.channelDefinitionTypes = channelDefinitionTypes;
        this.configDescriptionURI = (URI) configDescriptionObjects[0];
        this.configDescription = (ConfigDescription) configDescriptionObjects[1];
    }

    public ConfigDescription getConfigDescription() {
        return this.configDescription;
    }

    protected List<ChannelDefinition> toChannelDefinitions(Map<String, ChannelType> channelTypes)
            throws ConversionException {

        List<ChannelDefinition> channelDefinitions = null;

        if ((this.channelDefinitionTypes != null) && (this.channelDefinitionTypes.size() > 0)) {
            channelDefinitions = new ArrayList<>(this.channelDefinitionTypes.size());

            if (channelTypes != null) {
                for (NodeAttributes channelDefinitionType : this.channelDefinitionTypes) {
                    String id = channelDefinitionType.getAttribute("id");
                    String typeId = channelDefinitionType.getAttribute("typeId");

                    String typeUID = String.format("%s:%s",
                            this.thingTypeUID.getBindingId(), typeId);

                    ChannelType channelType = channelTypes.get(typeUID);
                    if (channelType != null) {
                        ChannelDefinition channelDefinition = new ChannelDefinition(id, channelType);
                        channelDefinitions.add(channelDefinition);
                    } else {
                        throw new ConversionException(
                                "The channel type for '" + typeUID + "' is missing!");
                    }
                }
            } else {
                throw new ConversionException("Missing the definition of channel types!");
            }
        }

        return channelDefinitions;
    }

    public ThingType toThingType(Map<String, ChannelType> channelTypes) throws ConversionException {
        ThingType thingType = new ThingType(
                this.thingTypeUID,
                this.supportedBridgeTypeUIDs,
                this.label,
                this.description,
                toChannelDefinitions(channelTypes),
                this.configDescriptionURI);

        return thingType;
    }

    @Override
    public String toString() {
        return "ThingTypeXmlResult [thingTypeUID=" + thingTypeUID
                + ", supportedBridgeTypeUIDs=" + supportedBridgeTypeUIDs
                + ", label=" + label + ", description=" + description
                + ", channelDefinitionTypes=" + channelDefinitionTypes
                + ", configDescriptionURI=" + configDescriptionURI
                + ", configDescription=" + configDescription + "]";
    }

}
