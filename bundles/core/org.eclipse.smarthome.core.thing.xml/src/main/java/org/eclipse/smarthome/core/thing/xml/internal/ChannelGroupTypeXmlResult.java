/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;

import com.thoughtworks.xstream.converters.ConversionException;

/**
 * The {@link ChannelGroupTypeXmlResult} is an intermediate XML conversion result object which
 * contains all parts of a {@link ChannelGroupType} object.
 * <p>
 * To create a concrete {@link ChannelGroupType} object, the method {@link #toChannelGroupType(Map)} must be called.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Chris Jackson - Updated to support channel properties
 */
public class ChannelGroupTypeXmlResult {

    private ChannelGroupTypeUID channelGroupTypeUID;
    private boolean advanced;
    private String label;
    private String description;
    private List<ChannelXmlResult> channelTypeReferences;

    public ChannelGroupTypeXmlResult(ChannelGroupTypeUID channelGroupTypeUID, boolean advanced, String label,
            String description, List<ChannelXmlResult> channelTypeReferences) {

        this.channelGroupTypeUID = channelGroupTypeUID;
        this.advanced = advanced;
        this.label = label;
        this.description = description;
        this.channelTypeReferences = channelTypeReferences;
    }

    public ChannelGroupTypeUID getUID() {
        return this.channelGroupTypeUID;
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

                    String typeUID = String.format("%s:%s", this.channelGroupTypeUID.getBindingId(), typeId);

                    ChannelType channelType = channelTypes.get(typeUID);
                    if (channelType != null) {
                        ChannelDefinition channelDefinition = new ChannelDefinition(id, channelType.getUID());
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

    public ChannelGroupType toChannelGroupType(Map<String, ChannelType> channelTypes) throws ConversionException {

        ChannelGroupType channelGroupType = new ChannelGroupType(this.channelGroupTypeUID, this.advanced, this.label,
                this.description, toChannelDefinitions(this.channelTypeReferences, channelTypes));

        return channelGroupType;
    }

    @Override
    public String toString() {
        return "ChannelGroupTypeXmlResult [channelGroupTypeUID=" + channelGroupTypeUID + ", advanced=" + advanced
                + ", label=" + label + ", description=" + description + ", channelTypeReferences="
                + channelTypeReferences + "]";
    }

}
