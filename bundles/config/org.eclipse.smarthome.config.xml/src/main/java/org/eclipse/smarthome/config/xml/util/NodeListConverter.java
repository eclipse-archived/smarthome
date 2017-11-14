/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.util;

import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link NodeListConverter} is a concrete implementation of the {@code XStream} {@link Converter} interface used to
 * convert a list of XML tags within an XML document
 * into a {@link NodeList} object.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class NodeListConverter extends GenericUnmarshaller<NodeList> {

    public NodeListConverter() {
        super(NodeList.class);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map<String, String> attributes = ConverterAttributeMapValidator.readValidatedAttributes(reader, null);

        String nodeName = reader.getNodeName();
        List<?> values = (List<?>) context.convertAnother(context, List.class);

        return new NodeList(nodeName, attributes, values);
    }

}
