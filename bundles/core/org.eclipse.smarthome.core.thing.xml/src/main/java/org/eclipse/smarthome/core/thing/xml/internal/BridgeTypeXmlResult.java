/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.util.NodeValue;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ThingType;

import com.thoughtworks.xstream.converters.ConversionException;

/**
 * The {@link BridgeTypeXmlResult} is an intermediate XML conversion result object which
 * contains all fields needed to create a concrete {@link BridgeType} object.
 * <p>
 * If a {@link ConfigDescription} object exists, it must be added to the according {@link ConfigDescriptionProvider}.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Andre Fuechsel - Added representationProperty
 */
public class BridgeTypeXmlResult extends ThingTypeXmlResult {

    public BridgeTypeXmlResult(ThingTypeUID bridgeTypeUID, List<String> supportedBridgeTypeUIDs, String label,
            String description, String category, boolean listed, List<ChannelXmlResult>[] channelTypeReferenceObjects,
            List<NodeValue> properties, String representationProperty, Object[] configDescriptionObjects) {

        super(bridgeTypeUID, supportedBridgeTypeUIDs, label, description, category, listed, channelTypeReferenceObjects,
                properties, representationProperty, configDescriptionObjects);
    }

    @Override
    public ThingType toThingType() throws ConversionException {

        BridgeType bridgeType = new BridgeType(super.thingTypeUID, super.supportedBridgeTypeUIDs, super.label,
                super.description, super.category, super.listed, super.representationProperty,
                super.toChannelDefinitions(this.channelTypeReferences),
                super.toChannelGroupDefinitions(this.channelGroupTypeReferences), super.toPropertiesMap(),
                super.configDescriptionURI);

        return bridgeType;
    }

    @Override
    public String toString() {
        return "BridgeTypeXmlResult [thingTypeUID=" + thingTypeUID + ", supportedBridgeTypeUIDs="
                + supportedBridgeTypeUIDs + ", label=" + label + ", description=" + description + ", category="
                + category + ", listed=" + listed + ", representationProperty=" + representationProperty
                + ", channelTypeReferences=" + channelTypeReferences + ", channelGroupTypeReferences="
                + channelGroupTypeReferences + ", properties=" + properties + ", configDescriptionURI="
                + configDescriptionURI + ", configDescription=" + configDescription + "]";
    }

}
