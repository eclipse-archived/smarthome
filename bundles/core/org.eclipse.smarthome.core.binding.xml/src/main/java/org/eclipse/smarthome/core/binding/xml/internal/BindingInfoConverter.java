/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding.xml.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.xml.util.ConverterAttributeMapValidator;
import org.eclipse.smarthome.config.xml.util.GenericUnmarshaller;
import org.eclipse.smarthome.config.xml.util.NodeIterator;
import org.eclipse.smarthome.core.binding.BindingInfo;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link BindingInfoConverter} is a concrete implementation of the {@code XStream} {@link Converter} interface used
 * to convert binding information within an XML document
 * into a {@link BindingInfoXmlResult} object.
 * <p>
 * This converter converts {@code binding} XML tags.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Andre Fuechsel - Made author tag optional
 */
public class BindingInfoConverter extends GenericUnmarshaller<BindingInfoXmlResult> {

    private ConverterAttributeMapValidator attributeMapValidator;

    public BindingInfoConverter() {
        super(BindingInfoXmlResult.class);

        this.attributeMapValidator = new ConverterAttributeMapValidator(
                new String[][] { { "id", "true" }, { "schemaLocation", "false" } });
    }

    private URI readConfigDescriptionURI(NodeIterator nodeIterator) throws ConversionException {
        String uriText = nodeIterator.nextAttribute("config-description-ref", "uri", false);

        if (uriText != null) {
            try {
                return new URI(uriText);
            } catch (NullPointerException | URISyntaxException ex) {
                throw new ConversionException(
                        "The URI '" + uriText + "' in node " + "'config-description-ref' is invalid!", ex);
            }
        }

        return null;
    }

    private ConfigDescription readConfigDescription(NodeIterator nodeIterator) {
        Object nextNode = nodeIterator.next();

        if (nextNode != null) {
            if (nextNode instanceof ConfigDescription) {
                return (ConfigDescription) nextNode;
            }

            nodeIterator.revert();
        }

        return null;
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        BindingInfoXmlResult bindingInfoXmlResult = null;
        BindingInfo bindingInfo = null;

        // read attributes
        Map<String, String> attributes = this.attributeMapValidator.readValidatedAttributes(reader);
        String id = attributes.get("id");

        // set automatically extracted URI for a possible 'config-description' section
        context.put("config-description.uri", "binding:" + id);

        // read values
        List<?> nodes = (List<?>) context.convertAnother(context, List.class);
        NodeIterator nodeIterator = new NodeIterator(nodes);

        String name = (String) nodeIterator.nextValue("name", true);
        String description = (String) nodeIterator.nextValue("description", false);
        String author = (String) nodeIterator.nextValue("author", false);
        String serviceId = (String) nodeIterator.nextValue("service-id", false);

        URI configDescriptionURI = readConfigDescriptionURI(nodeIterator);
        ConfigDescription configDescription = null;
        if (configDescriptionURI == null) {
            configDescription = readConfigDescription(nodeIterator);
            if (configDescription != null) {
                configDescriptionURI = configDescription.getURI();
            }
        }

        nodeIterator.assertEndOfType();

        // create object
        bindingInfo = new BindingInfo(id, name, description, author, serviceId, configDescriptionURI);
        bindingInfoXmlResult = new BindingInfoXmlResult(bindingInfo, configDescription);

        return bindingInfoXmlResult;
    }

}
