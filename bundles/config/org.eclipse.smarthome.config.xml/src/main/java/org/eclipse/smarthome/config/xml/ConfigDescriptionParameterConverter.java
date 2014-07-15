/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml;

import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.xml.util.ConverterAttributeMapValidator;
import org.eclipse.smarthome.config.xml.util.ConverterValueMap;
import org.eclipse.smarthome.config.xml.util.GenericUnmarshaller;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;


/**
 * The {@link ConfigDescriptionParameterConverter} is a concrete implementation of the
 * {@code XStream} {@link Converter} interface used to convert config description parameters
 * information within an XML document into a {@link ConfigDescriptionParameter} object.
 * <p>
 * This converter converts {@code parameter} XML tags.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class ConfigDescriptionParameterConverter
        extends GenericUnmarshaller<ConfigDescriptionParameter> {

    private ConverterAttributeMapValidator attributeMapValidator;


    public ConfigDescriptionParameterConverter() {
        super(ConfigDescriptionParameter.class);

        this.attributeMapValidator = new ConverterAttributeMapValidator(new String[][] {
                { "name" , "true" },
                { "type", "true" }});
    }

    private Type toType(String xmlType) {
        if (xmlType != null) {
            return Type.valueOf(xmlType.toUpperCase());
        }

        return null;
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ConfigDescriptionParameter configDescriptionParam = null;

        // read attributes
        Map<String, String> attributes = this.attributeMapValidator.readValidatedAttributes(reader);
        String name = attributes.get("name");
        Type type = toType(attributes.get("type"));

        // read values
        ConverterValueMap valueMap = new ConverterValueMap(reader);

        // create object
        configDescriptionParam = new ConfigDescriptionParameter(name, type,
                valueMap.getString("context"),
                valueMap.getBoolean("required", false),
                valueMap.getString("default"),
                valueMap.getString("label"),
                valueMap.getString("description"));

        return configDescriptionParam;
    }

}
