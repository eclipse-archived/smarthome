/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.util;

import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link NodeValueConverter} is a concrete implementation of the {@code XStream} {@link Converter} interface used
 * to convert the value of an XML tag within an XML
 * document into a {@link NodeValue} object.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class NodeValueConverter extends GenericUnmarshaller<NodeValue> {

    public NodeValueConverter() {
        super(NodeValue.class);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map<String, String> attributes = ConverterAttributeMapValidator.readValidatedAttributes(reader, null);

        return new NodeValue(reader.getNodeName(), attributes, reader.getValue());
    }

}
