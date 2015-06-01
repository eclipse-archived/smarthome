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

import org.eclipse.smarthome.config.xml.util.ConverterAttributeMapValidator;
import org.eclipse.smarthome.config.xml.util.NodeIterator;
import org.eclipse.smarthome.config.xml.util.NodeValue;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link ChannelConverter} is a concrete implementation of the {@code XStream} {@link Converter} interface used
 * to convert channel information within an XML document
 * into a {@link ChannelXmlResult} object.
 * <p>
 * This converter converts {@code channel} XML tags. It uses the {@link AbstractDescriptionTypeConverter} which offers
 * base functionality for each type definition.
 * 
 * @author Chris Jackson - Initial Contribution
 */
public class ChannelConverter extends AbstractDescriptionTypeConverter<ChannelXmlResult> {

    public ChannelConverter() {
        super(ChannelXmlResult.class, "channel");

        super.attributeMapValidator = new ConverterAttributeMapValidator(new String[][] { { "id", "true" },
                { "typeId", "false" } });
    }

    @SuppressWarnings("unchecked")
    protected List<NodeValue> getProperties(NodeIterator nodeIterator) {
        return (List<NodeValue>) nodeIterator.nextList("properties", false);
    }

    @Override
    protected ChannelXmlResult unmarshalType(HierarchicalStreamReader reader, UnmarshallingContext context,
            Map<String, String> attributes, NodeIterator nodeIterator) throws ConversionException {

        String id = attributes.get("id");
        String typeId = attributes.get("typeId");
        List<NodeValue> properties = getProperties(nodeIterator);

        ChannelXmlResult channelXmlResult = new ChannelXmlResult(id, typeId, properties);

        return channelXmlResult;
    }
}
