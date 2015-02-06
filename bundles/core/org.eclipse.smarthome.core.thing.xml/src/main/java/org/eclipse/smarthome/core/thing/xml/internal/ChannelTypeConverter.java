/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.xml.util.ConverterAttributeMapValidator;
import org.eclipse.smarthome.config.xml.util.NodeIterator;
import org.eclipse.smarthome.config.xml.util.NodeValue;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link ChannelTypeConverter} is a concrete implementation of the {@code XStream} {@link Converter} interface used
 * to convert channel type information within an XML document
 * into a {@link ChannelTypeXmlResult} object.
 * <p>
 * This converter converts {@code channel-type} XML tags. It uses the {@link AbstractDescriptionTypeConverter} which
 * offers base functionality for each type definition.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class ChannelTypeConverter extends AbstractDescriptionTypeConverter<ChannelTypeXmlResult> {

    public ChannelTypeConverter() {
        super(ChannelTypeXmlResult.class, "channel-type");

        super.attributeMapValidator = new ConverterAttributeMapValidator(new String[][] { { "id", "true" },
                { "advanced", "false" } });
    }

    private boolean isAdvanced(Map<String, String> attributes, boolean defaultValue) {
        String advancedFlag = attributes.get("advanced");

        if (advancedFlag != null) {
            return Boolean.parseBoolean(advancedFlag);
        }

        return defaultValue;
    }

    private String readItemType(NodeIterator nodeIterator) throws ConversionException {
        return (String) nodeIterator.nextValue("item-type", true);
    }

    private String readCategory(NodeIterator nodeIterator) throws ConversionException {
        return (String) nodeIterator.nextValue("category", false);
    }

    private Set<String> readTags(NodeIterator nodeIterator) throws ConversionException {
        Set<String> tags = null;

        List<?> tagsNode = nodeIterator.nextList("tags", false);

        if (tagsNode != null) {
            tags = new HashSet<>(tagsNode.size());

            for (Object tagNodeObject : tagsNode) {
                NodeValue tagNode = (NodeValue) tagNodeObject;

                if ("tag".equals(tagNode.getNodeName())) {
                    String tag = (String) tagNode.getValue();

                    if (tag != null) {
                        tags.add(tag);
                    }
                } else {
                    throw new ConversionException("The 'tags' node must only contain 'tag' nodes!");
                }
            }
        }

        return tags;
    }

    private StateDescription readStateDescription(NodeIterator nodeIterator) {
        Object nextNode = nodeIterator.next();

        if (nextNode != null) {
            if (nextNode instanceof StateDescription) {
                return (StateDescription) nextNode;
            }

            nodeIterator.revert();
        }

        return null;
    }

    @Override
    protected ChannelTypeXmlResult unmarshalType(HierarchicalStreamReader reader, UnmarshallingContext context,
            Map<String, String> attributes, NodeIterator nodeIterator) throws ConversionException {

        ChannelTypeUID channelTypeUID = new ChannelTypeUID(super.getUID(attributes, context));
        boolean advanced = isAdvanced(attributes, false);

        String itemType = readItemType(nodeIterator);
        String label = super.readLabel(nodeIterator);
        String description = super.readDescription(nodeIterator);
        String category = readCategory(nodeIterator);
        Set<String> tags = readTags(nodeIterator);

        StateDescription stateDescription = readStateDescription(nodeIterator);

        Object[] configDescriptionObjects = super.getConfigDescriptionObjects(nodeIterator);

        ChannelType channelType = new ChannelType(channelTypeUID, advanced, itemType, label, description, category,
                tags, stateDescription, (URI) configDescriptionObjects[0]);

        ChannelTypeXmlResult channelTypeXmlResult = new ChannelTypeXmlResult(channelType,
                (ConfigDescription) configDescriptionObjects[1]);

        return channelTypeXmlResult;
    }

}
