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
import org.eclipse.smarthome.config.xml.util.NodeList;
import org.eclipse.smarthome.config.xml.util.NodeValue;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link ThingTypeConverter} is a concrete implementation of the {@code XStream} {@link Converter} interface used
 * to convert {@code Thing} type information within
 * an XML document into a {@link ThingTypeXmlResult} object.
 * <p>
 * This converter converts {@code thing-type} XML tags. It uses the {@link AbstractDescriptionTypeConverter} which
 * offers base functionality for each type definition.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Chris Jackson - Added channel properties
 */
public class ThingTypeConverter extends AbstractDescriptionTypeConverter<ThingTypeXmlResult> {

    public ThingTypeConverter() {
        this(ThingTypeXmlResult.class, "thing-type");
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param clazz the class of the result object (must not be null)
     * @param type the name of the type (e.g. "bridge-type")
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected ThingTypeConverter(Class clazz, String type) {
        super(clazz, type);
        this.attributeMapValidator = new ConverterAttributeMapValidator(
                new String[][] { { "id", "true" }, { "listed", "false" } });
    }

    protected List<String> readSupportedBridgeTypeUIDs(NodeIterator nodeIterator, UnmarshallingContext context) {

        Object nextNode = nodeIterator.next("supported-bridge-type-refs", false);

        if (nextNode != null) {
            String bindingID = (String) context.get("thing-descriptions.bindingId");

            String uidFormat = String.format("%s:%s", bindingID, "%s");

            return ((NodeList) nextNode).getAttributes("bridge-type-ref", "id", uidFormat);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    protected List<ChannelXmlResult>[] getChannelTypeReferenceObjects(NodeIterator nodeIterator)
            throws ConversionException {

        List<ChannelXmlResult> channelTypeReferences = null;
        List<ChannelXmlResult> channelGroupTypeReferences = null;

        channelTypeReferences = (List<ChannelXmlResult>) nodeIterator.nextList("channels", false);
        if (channelTypeReferences == null) {
            channelGroupTypeReferences = (List<ChannelXmlResult>) nodeIterator.nextList("channel-groups", false);
        }

        return new List[] { channelTypeReferences, channelGroupTypeReferences };
    }

    @SuppressWarnings("unchecked")
    protected List<NodeValue> getProperties(NodeIterator nodeIterator) {
        return (List<NodeValue>) nodeIterator.nextList("properties", false);
    }

    @Override
    protected ThingTypeXmlResult unmarshalType(HierarchicalStreamReader reader, UnmarshallingContext context,
            Map<String, String> attributes, NodeIterator nodeIterator) throws ConversionException {

        ThingTypeXmlResult thingTypeXmlResult = new ThingTypeXmlResult(
                new ThingTypeUID(super.getUID(attributes, context)), readSupportedBridgeTypeUIDs(nodeIterator, context),
                super.readLabel(nodeIterator), super.readDescription(nodeIterator), getListed(attributes),
                getChannelTypeReferenceObjects(nodeIterator), getProperties(nodeIterator),
                super.getConfigDescriptionObjects(nodeIterator));

        return thingTypeXmlResult;
    }

    protected boolean getListed(Map<String, String> attributes) {
        String listedFlag = attributes.get("listed");
        if (listedFlag != null) {
            return Boolean.parseBoolean(listedFlag);
        }
        return true;
    }

}
