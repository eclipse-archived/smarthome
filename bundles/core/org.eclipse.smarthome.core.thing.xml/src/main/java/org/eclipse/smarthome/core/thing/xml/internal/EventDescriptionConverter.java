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
import org.eclipse.smarthome.config.xml.util.GenericUnmarshaller;
import org.eclipse.smarthome.config.xml.util.NodeIterator;
import org.eclipse.smarthome.config.xml.util.NodeList;
import org.eclipse.smarthome.config.xml.util.NodeValue;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.StateOption;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link EventDescriptionConverter} is a concrete implementation of the {@code XStream} {@link Converter} interface
 * used to convert a event description within an XML document
 * into a {@link EventDescription} object.
 * <p>
 * This converter converts {@code state} XML tags.
 */
public class EventDescriptionConverter extends GenericUnmarshaller<EventDescription> {

    public EventDescriptionConverter() {
        super(EventDescription.class);
    }

    private List<StateOption> toListOfChannelState(NodeList nodeList) throws ConversionException {

        if ("options".equals(nodeList.getNodeName())) {
            List<StateOption> stateOptions = new ArrayList<>();

            for (Object nodeObject : nodeList.getList()) {
                stateOptions.add(toChannelStateOption((NodeValue) nodeObject));
            }

            return stateOptions;
        }

        throw new ConversionException("Unknown type '" + nodeList.getNodeName() + "'!");
    }

    private StateOption toChannelStateOption(NodeValue nodeValue) throws ConversionException {
        if ("option".equals(nodeValue.getNodeName())) {
            String value;
            String label;

            Map<String, String> attributes = nodeValue.getAttributes();
            if ((attributes != null) && (attributes.containsKey("value"))) {
                value = attributes.get("value");
            } else {
                throw new ConversionException("The node 'option' requires the attribute 'value'!");
            }

            label = (String) nodeValue.getValue();

            return new StateOption(value, label);
        }

        throw new ConversionException("Unknown type in the list of 'options'!");
    }

    @Override
    public final Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        List<StateOption> channelOptions = null;

        NodeList nodes = (NodeList) context.convertAnother(context, NodeList.class);
        NodeIterator nodeIterator = new NodeIterator(nodes.getList());

        NodeList optionNodes = (NodeList) nodeIterator.next();
        if (optionNodes != null) {
            channelOptions = toListOfChannelState(optionNodes);
        }

        nodeIterator.assertEndOfType();

        EventDescription eventDescription = new EventDescription(channelOptions);

        return eventDescription;
    }

}
