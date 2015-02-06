/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.config.xml.util.ConverterAttributeMapValidator;
import org.eclipse.smarthome.config.xml.util.ConverterValueMap;
import org.eclipse.smarthome.config.xml.util.GenericUnmarshaller;
import org.eclipse.smarthome.config.xml.util.NodeValue;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link ConfigDescriptionParameterConverter} is a concrete implementation of the {@code XStream} {@link Converter}
 * interface used to convert config description parameters
 * information within an XML document into a {@link ConfigDescriptionParameter} object.
 * <p>
 * This converter converts {@code parameter} XML tags.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Alex Tugarev - Extended for options and filter criteria
 */
public class ConfigDescriptionParameterConverter extends GenericUnmarshaller<ConfigDescriptionParameter> {

    private ConverterAttributeMapValidator attributeMapValidator;

    public ConfigDescriptionParameterConverter() {
        super(ConfigDescriptionParameter.class);

        this.attributeMapValidator = new ConverterAttributeMapValidator(new String[][] { { "name", "true" },
                { "type", "true" }, { "min", "false" }, { "max", "false" }, { "step", "false" },
                { "pattern", "false" }, { "required", "false" }, { "readOnly", "false" }, { "multiple", "false" } });
    }

    private Type toType(String xmlType) {
        if (xmlType != null) {
            return Type.valueOf(xmlType.toUpperCase());
        }

        return null;
    }

    private BigDecimal toNumber(String value) {
        try {
            if (value != null)
                return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ConversionException("The value '" + value + "' could not be converted to a decimal number.", e);
        }
        return null;
    }

    private Boolean toBoolean(String val) {
        if (val == null)
            return null;
        return new Boolean(val);
    }

    private Boolean falseIfNull(Boolean b) {
        return (b != null) ? b : false;
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ConfigDescriptionParameter configDescriptionParam = null;

        // read attributes
        Map<String, String> attributes = this.attributeMapValidator.readValidatedAttributes(reader);
        String name = attributes.get("name");
        Type type = toType(attributes.get("type"));
        BigDecimal min = toNumber(attributes.get("min"));
        BigDecimal max = toNumber(attributes.get("max"));
        BigDecimal step = toNumber(attributes.get("step"));
        String patternString = attributes.get("pattern");
        Boolean required = toBoolean(attributes.get("required"));
        Boolean readOnly = falseIfNull(toBoolean(attributes.get("readOnly")));
        Boolean multiple = falseIfNull(toBoolean(attributes.get("multiple")));

        // read values
        ConverterValueMap valueMap = new ConverterValueMap(reader, context);
        String parameterContext = valueMap.getString("context");
        if (required == null) {
            // fallback to deprecated "required" element
            required = valueMap.getBoolean("required", false);
        }
        String defaultValue = valueMap.getString("default");
        String label = valueMap.getString("label");
        String description = valueMap.getString("description");

        // read options and filter criteria
        List<ParameterOption> options = readParameterOptions(valueMap.getObject("options"));
        @SuppressWarnings("unchecked")
        List<FilterCriteria> filterCriteria = (List<FilterCriteria>) valueMap.getObject("filter");

        // create object
        configDescriptionParam = new ConfigDescriptionParameter(name, type, min, max, step, patternString, required,
                readOnly, multiple, parameterContext, defaultValue, label, description, options, filterCriteria);

        return configDescriptionParam;
    }

    private List<ParameterOption> readParameterOptions(Object rawNodeValueList) {
        if (rawNodeValueList instanceof List<?>) {
            List<?> list = (List<?>) rawNodeValueList;
            List<ParameterOption> result = new ArrayList<>();
            for (Object object : list) {
                if (object instanceof NodeValue) {
                    NodeValue nodeValue = (NodeValue) object;
                    String value = nodeValue.getAttributes().get("value");
                    String label = nodeValue.getValue().toString();
                    result.add(new ParameterOption(value, label));
                }
            }
            return result;
        }
        return null;
    }

}
