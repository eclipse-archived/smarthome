/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.util;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;


/**
 * The {@link ConverterValueMap} reads all children elements of a node and provides
 * them as key-value pair map.
 * <p>
 * This class should be used for nodes whose children elements <i>only</i> contain simple values
 * (without children) and whose order is unpredictable. There must be only one children with the
 * same name.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class ConverterValueMap {

    private HierarchicalStreamReader reader;
    private Map<String, String> valueMap;


    /**
     * Creates a new instance of this class with the specified parameter.
     * 
     * @param reader the reader to be used to read-in all children (must not be null)
     */
    public ConverterValueMap(HierarchicalStreamReader reader) {
        this(reader, -1);
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     * 
     * @param reader the reader to be used to read-in all children (must not be null)
     * @param numberOfValues the number of children to be read-in (< 0 = until end of section)
     * 
     * @throws ConversionException if not all children could be read-in
     */
    public ConverterValueMap(HierarchicalStreamReader reader, int numberOfValues)
            throws ConversionException {

        if (numberOfValues < -1) {
            numberOfValues = -1;
        }

        this.reader = reader;
        this.valueMap = readValueMap(this.reader, numberOfValues);
    }

    /**
     * Returns the key-value map containing all read-in children.
     * 
     * @return the key-value map containing all read-in children (not null, could be empty)
     */
    public Map<String, String> getValueMap() {
        return this.valueMap;
    }

    /**
     * Reads-in all simple children in a key-value map and returns it.
     * 
     * @param reader the reader to be used to read-in all children (must not be null)
     * @return the key-value map containing all read-in children (not null, could be empty)
     */
    public static Map<String, String> readValueMap(HierarchicalStreamReader reader) {
        return readValueMap(reader, -1);
    }

    /**
     * Reads-in {@code N} children in a key-value map and returns it.
     * 
     * @param reader the reader to be used to read-in the children (must not be null)
     * @param numberOfValues the number of children to be read in (< 0 = until end of section)
     * 
     * @return the key-value map containing the read-in children (not null, could be empty)
     * 
     * @throws ConversionException if not all children could be read-in
     */
    public static Map<String, String> readValueMap(
            HierarchicalStreamReader reader, int numberOfValues) throws ConversionException {

        Map<String, String> valueMap = new HashMap<>((numberOfValues >= 0) ? numberOfValues : 10);
        int counter = 0;

        while (reader.hasMoreChildren() && ((counter < numberOfValues) || (numberOfValues == -1))) {
            reader.moveDown();
            valueMap.put(reader.getNodeName(), reader.getValue());
            reader.moveUp();
            counter++;
        }

        if ((counter < numberOfValues) && (numberOfValues > 0)) {
            throw new ConversionException("Not all children could be read-in!");
        }

        return valueMap;
    }

    /**
     * Returns the object associated with the specified name of the child's node.
     * 
     * @param nodeName the name of the child's node (must not be null)
     * @return the object associated with the specified name of the child's node (could be null)
     */
    public Object getObject(String nodeName) {
        return this.valueMap.get(nodeName);
    }

    /**
     * Returns the object associated with the specified name of the child's node.
     * 
     * @param nodeName the name of the child's node (must not be null)
     * @param defaultValue the value to be returned if the node could not be found (could be null)
     * 
     * @return the object associated with the specified name of the child's node (could be null)
     */
    public Object getObject(String nodeName, Object defaultValue) {
        Object value = this.valueMap.get(nodeName);

        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    /**
     * Returns the text associated with the specified name of the child's node.
     * 
     * @param nodeName the name of the child's node (must not be null)
     * @return the text associated with the specified name of the child's node (could be null)
     */
    public String getString(String nodeName) {
        return getString(nodeName, null);
    }

    /**
     * Returns the text associated with the specified name of the child's node.
     * 
     * @param nodeName the name of the child's node (must not be null)
     * @param defaultValue the text to be returned if the node could not be found (could be null)
     * 
     * @return the text associated with the specified name of the child's node (could be null)
     */
    public String getString(String nodeName, String defaultValue) {
        String value = this.valueMap.get(nodeName);

        if (value != null) {
            // fixes a formatting problem with line breaks in text
            return ((String) value).replaceAll("\\n\\s*", " ").trim();
        }

        return defaultValue;
    }

    /**
     * Returns the boolean associated with the specified name of the child's node.
     * 
     * @param nodeName the name of the child's node (must not be null)
     * @return the boolean associated with the specified name of the child's node (could be null)
     */
    public Boolean getBoolean(String nodeName) {
        return getBoolean(nodeName, null);
    }

    /**
     * Returns the boolean associated with the specified name of the child's node.
     * 
     * @param nodeName the name of the child's node (must not be null)
     * @param defaultValue the boolean to be returned if the node could not be found (could be null)
     * 
     * @return the boolean associated with the specified name of the child's node (could be null)
     */
    public Boolean getBoolean(String nodeName, Boolean defaultValue) {
        String value = this.valueMap.get(nodeName);

        if (value != null) {
            return Boolean.parseBoolean(value);
        }

        return defaultValue;
    }

    /**
     * Returns the numeric value associated with the specified name of the child's node.
     * 
     * @param nodeName the name of the child's node (must not be null)
     * 
     * @return the numeric value associated with the specified name of the child's node
     *     (could be null)
     * 
     * @throws ConversionException if the value could not be converted to a numeric value
     */
    public Integer getInteger(String nodeName) throws ConversionException {
        return getInteger(nodeName, null);
    }

    /**
     * Returns the numeric value associated with the specified name of the child's node.
     * 
     * @param nodeName the name of the child's node (must not be null)
     * @param defaultValue the numeric value to be returned if the node could not be found
     *     (could be null)
     * 
     * @return the numeric value associated with the specified name of the child's node
     *    (could be null)
     * 
     * @throws ConversionException if the value could not be converted to a numeric value
     */
    public Integer getInteger(String nodeName, Integer defaultValue) throws ConversionException {
        String value = this.valueMap.get(nodeName);

        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                throw new ConversionException("The value '" + value
                        + "' cannot be converted to a numeric value!", nfe);
            }
        }

        return defaultValue;
    }

}
