/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
    private String category;
    private List<ChannelXmlResult> channelTypeReferences;

    public ChannelGroupTypeXmlResult(ChannelGroupTypeUID channelGroupTypeUID, boolean advanced, String label,
            String description, String category, List<ChannelXmlResult> channelTypeReferences) {

        this.channelGroupTypeUID = channelGroupTypeUID;
        this.advanced = advanced;
        this.label = label;
        this.description = description;
        this.category = category;
        this.channelTypeReferences = channelTypeReferences;
    }

    public ChannelGroupTypeUID getUID() {
        return this.channelGroupTypeUID;
    }

    protected List<ChannelDefinition> toChannelDefinitions(List<ChannelXmlResult> channelTypeReferences)
            throws ConversionException {

        List<ChannelDefinition> channelTypeDefinitions = null;

        if ((channelTypeReferences != null) && (channelTypeReferences.size() > 0)) {
            channelTypeDefinitions = new ArrayList<>(channelTypeReferences.size());

            for (ChannelXmlResult channelTypeReference : channelTypeReferences) {
                channelTypeDefinitions
                        .add(channelTypeReference.toChannelDefinition(this.channelGroupTypeUID.getBindingId()));
            }
        }

        return channelTypeDefinitions;
    }

    public ChannelGroupType toChannelGroupType() throws ConversionException {
        ChannelGroupType channelGroupType = new ChannelGroupType(this.channelGroupTypeUID, this.advanced, this.label,
                this.description, this.category, toChannelDefinitions(this.channelTypeReferences));

        return channelGroupType;
    }

    @Override
    public String toString() {
        return "ChannelGroupTypeXmlResult [channelGroupTypeUID=" + channelGroupTypeUID + ", advanced=" + advanced
                + ", label=" + label + ", description=" + description + ", category=" + category
                + ", channelTypeReferences=" + channelTypeReferences + "]";
    }

}
