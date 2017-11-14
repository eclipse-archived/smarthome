/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.xml.util.ConverterAttributeMapValidator;
import org.eclipse.smarthome.config.xml.util.GenericUnmarshaller;
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
 * This converter converts {@code channel} XML tags.
 * 
 * @author Chris Jackson - Initial Contribution
 * @author Simon Kaufmann - Fixing wrong inheritance
 * @author Chris Jackson - Added label and description
 */
public class ChannelConverter extends GenericUnmarshaller<ChannelXmlResult> {

    private ConverterAttributeMapValidator attributeMapValidator;

    public ChannelConverter() {
        super(ChannelXmlResult.class);

        attributeMapValidator = new ConverterAttributeMapValidator(new String[][] { { "id", "true" },
                { "typeId", "false" } });
    }

    @SuppressWarnings("unchecked")
    protected List<NodeValue> getProperties(NodeIterator nodeIterator) {
        return (List<NodeValue>) nodeIterator.nextList("properties", false);
    }

    protected ChannelXmlResult unmarshalType(HierarchicalStreamReader reader, UnmarshallingContext context,
            Map<String, String> attributes, NodeIterator nodeIterator) throws ConversionException {

        String id = attributes.get("id");
        String typeId = attributes.get("typeId");
        String label = (String) nodeIterator.nextValue("label", false);
        String description = (String) nodeIterator.nextValue("description", false);
        List<NodeValue> properties = getProperties(nodeIterator);

        ChannelXmlResult channelXmlResult = new ChannelXmlResult(id, typeId, label, description, properties);

        return channelXmlResult;
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // read attributes
        Map<String, String> attributes = this.attributeMapValidator.readValidatedAttributes(reader);

        // read values
        List<?> nodes = (List<?>) context.convertAnother(context, List.class);
        NodeIterator nodeIterator = new NodeIterator(nodes);

        // create object
        Object object = unmarshalType(reader, context, attributes, nodeIterator);

        nodeIterator.assertEndOfType();

        return object;
    }
}
