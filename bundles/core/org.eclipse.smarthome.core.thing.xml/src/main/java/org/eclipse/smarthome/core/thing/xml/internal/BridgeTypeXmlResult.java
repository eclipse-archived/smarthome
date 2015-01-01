/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.util.NodeAttributes;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelType;

import com.thoughtworks.xstream.converters.ConversionException;


/**
 * The {@link BridgeTypeXmlResult} is an intermediate XML conversion result object which
 * contains all fields needed to create a concrete {@link BridgeType} object.
 * <p>
 * If a {@link ConfigDescription} object exists, it must be added to the according
 * {@link ConfigDescriptionProvider}. 
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class BridgeTypeXmlResult extends ThingTypeXmlResult {

    public BridgeTypeXmlResult(ThingTypeUID thingTypeUID, List<String> supportedBridgeTypeUIDs,
            String label, String description, List<NodeAttributes> channelDefinitionTypes,
            Object[] configDescriptionObjects) {

        super(thingTypeUID, supportedBridgeTypeUIDs, label, description,
                channelDefinitionTypes, configDescriptionObjects);
    }

    public BridgeType toThingType(Map<String, ChannelType> channelTypes) throws ConversionException {
        BridgeType bridgeType = new BridgeType(
                super.thingTypeUID,
                super.supportedBridgeTypeUIDs,
                super.label,
                super.description,
                super.toChannelDefinitions(channelTypes),
                super.configDescriptionURI);

        return bridgeType;
    }

}
