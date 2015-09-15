/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
 */
public class BridgeTypeXmlResult extends ThingTypeXmlResult {

    public BridgeTypeXmlResult(ThingTypeUID bridgeTypeUID, List<String> supportedBridgeTypeUIDs, String label,
            String description, List<ChannelXmlResult>[] channelTypeReferenceObjects, List<NodeValue> properties,
            Object[] configDescriptionObjects) {

        super(bridgeTypeUID, supportedBridgeTypeUIDs, label, description, channelTypeReferenceObjects, properties,
                configDescriptionObjects);
    }

    @Override
    public ThingType toThingType() throws ConversionException {

        BridgeType bridgeType = new BridgeType(super.thingTypeUID, super.supportedBridgeTypeUIDs, super.label,
                super.description, super.toChannelDefinitions(this.channelTypeReferences),
                super.toChannelGroupDefinitions(this.channelGroupTypeReferences), super.toPropertiesMap(),
                super.configDescriptionURI);

        return bridgeType;
    }

    @Override
    public String toString() {
        return "BridgeTypeXmlResult [thingTypeUID=" + thingTypeUID + ", supportedBridgeTypeUIDs="
                + supportedBridgeTypeUIDs + ", label=" + label + ", description=" + description
                + ", channelTypeReferences=" + channelTypeReferences + ", channelGroupTypeReferences="
                + channelGroupTypeReferences + ", properties=" + properties + ", configDescriptionURI="
                + configDescriptionURI + ", configDescription=" + configDescription + "]";
    }

}
