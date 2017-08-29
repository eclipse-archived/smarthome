/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.Map;

import org.eclipse.smarthome.config.xml.util.NodeIterator;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link BridgeTypeConverter} is a concrete implementation of the {@code XStream} {@link Converter} interface used
 * to convert bridge type information within an XML document
 * into a {@link BridgeTypeXmlResult} object.
 * <p>
 * This converter converts {@code bridge-type} XML tags. It uses the {@link ThingTypeConverter} since both contain the
 * same content.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Andre Fuechsel - Added representationProperty
 */
public class BridgeTypeConverter extends ThingTypeConverter {

    public BridgeTypeConverter() {
        super(BridgeTypeXmlResult.class, "thing-type");
    }

    @Override
    protected BridgeTypeXmlResult unmarshalType(HierarchicalStreamReader reader, UnmarshallingContext context,
            Map<String, String> attributes, NodeIterator nodeIterator) throws ConversionException {

        BridgeTypeXmlResult bridgeTypeXmlResult = new BridgeTypeXmlResult(
                new ThingTypeUID(super.getUID(attributes, context)),
                super.readSupportedBridgeTypeUIDs(nodeIterator, context), super.readLabel(nodeIterator),
                super.readDescription(nodeIterator), super.readCategory(nodeIterator), super.getListed(attributes),
                super.getChannelTypeReferenceObjects(nodeIterator), getProperties(nodeIterator),
                super.getRepresentationProperty(nodeIterator), super.getConfigDescriptionObjects(nodeIterator));

        return bridgeTypeXmlResult;
    }

}
