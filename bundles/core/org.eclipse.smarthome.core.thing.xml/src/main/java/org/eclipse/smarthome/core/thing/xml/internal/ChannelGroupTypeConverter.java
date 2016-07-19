/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link ChannelGroupTypeConverter} is a concrete implementation of the {@code XStream} {@link Converter} interface
 * used to convert channel group type information within an
 * XML document into a {@link ChannelGroupTypeXmlResult} object.
 * <p>
 * This converter converts {@code channel-group-type} XML tags. It uses the {@link AbstractDescriptionTypeConverter}
 * which offers base functionality for each type definition.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Chris Jackson - Modified to support channel properties
 */
public class ChannelGroupTypeConverter extends AbstractDescriptionTypeConverter<ChannelGroupTypeXmlResult> {

    public ChannelGroupTypeConverter() {
        super(ChannelGroupTypeXmlResult.class, "channel-group-type");

        super.attributeMapValidator = new ConverterAttributeMapValidator(new String[][] { { "id", "true" },
                { "advanced", "false" } });
    }

    private boolean isAdvanced(Map<String, String> attributes, boolean defaultValue) {
        Object advancedObj = attributes.get("advanced");

        if (advancedObj != null) {
            return Boolean.parseBoolean((String) advancedObj);
        }

        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    protected List<ChannelXmlResult> readChannelTypeDefinitions(NodeIterator nodeIterator) throws ConversionException {

        return (List<ChannelXmlResult>) nodeIterator.nextList("channels", true);
    }

    @Override
    protected ChannelGroupTypeXmlResult unmarshalType(HierarchicalStreamReader reader, UnmarshallingContext context,
            Map<String, String> attributes, NodeIterator nodeIterator) throws ConversionException {

        ChannelGroupTypeUID channelGroupTypeUID = new ChannelGroupTypeUID(super.getUID(attributes, context));

        boolean advanced = isAdvanced(attributes, false);

        String label = super.readLabel(nodeIterator);
        String description = super.readDescription(nodeIterator);
        List<ChannelXmlResult> channelTypeDefinitions = readChannelTypeDefinitions(nodeIterator);

        ChannelGroupTypeXmlResult groupChannelType = new ChannelGroupTypeXmlResult(channelGroupTypeUID, advanced,
                label, description, channelTypeDefinitions);

        return groupChannelType;
    }

}
