/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.scale.internal;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.transform.AbstractFileTransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by matching it between limits of ranges in a scale file
 *
 * @author Gaël L'hopital
 */
public class ScaleTransformationService extends AbstractFileTransformationService<Map<Range<Double>, String>> {

    private final Logger logger = LoggerFactory.getLogger(ScaleTransformationService.class);

    /** RegEx to extract a scale definition */
    private static final Pattern limits_pattern = Pattern.compile("(\\[|\\])(.*)\\.\\.(.*)(\\[|\\])");

    /**
     * <p>
     * Transforms the input <code>source</code> by matching searching the range where it fits
     * i.e. [min..max]=value or ]min..max]=value
     * </p>
     *
     * @param properties
     *            the list of properties defining all the available ranges
     * @param source
     *            the input to transform
     *
     * @{inheritDoc
     *
     */
    @Override
    protected String internalTransform(Map<Range<Double>, String> data, String source)  throws TransformationException {
        
        Double value = null;
        try {
            value = new Double(source);
            for (Range<Double> range : data.keySet()) {
                if (range.contains(value)) {
                    return data.get(range);
                }
            }
            logger.debug("No matching range for '{}'", source);
        } catch (NumberFormatException e) {
            logger.debug("Scale can not work with non numeric input");          
        }
        return null;
    }
    
    @Override
    protected Map<Range<Double>, String> internalLoadTransform(String filename) throws TransformationException {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(filename));
            Map<Range<Double>,String> data = new HashMap<Range<Double>,String>();
            
            for (Entry<Object, Object> f : properties.entrySet()) {
                String key = (String) f.getKey();
                String value = properties.getProperty(key);
                Matcher matcher = limits_pattern.matcher(key);
                if (matcher.matches() && (matcher.groupCount() == 4)) {
                    
                    BoundType lowBoundType = matcher.group(1).equals("]") ? BoundType.OPEN  : BoundType.CLOSED;
                    BoundType highBoundType = matcher.group(4).equals("[") ? BoundType.OPEN : BoundType.CLOSED;

                    String lowLimit = matcher.group(2);
                    String highLimit = matcher.group(3);

                    Range<Double> range = null;

                    Double lowValue = null;
                    Double highValue = null;

                    try {
                        if (!lowLimit.isEmpty()) lowValue = new Double(lowLimit);
                        if (!highLimit.isEmpty()) highValue = new Double(highLimit);
                    } catch (NumberFormatException e) {
                        throw new TransformationException("Error parsing bounds : "+lowLimit+".."+highLimit);
                    }

                    if (lowValue == null && highValue == null) {
                        range = Ranges.all();
                    } else if (lowValue == null) {
                        range = Ranges.upTo(highValue, highBoundType);
                    } else if (highValue == null) {
                        range = Ranges.downTo(lowValue, lowBoundType);
                    } else {
                        range = Ranges.range(lowValue, lowBoundType, highValue, highBoundType);
                    }
                    
                    data.put(range, value);
                    
                } else {
                    logger.warn("Scale transform entry does not comply with syntax : '{}', '{}'",key,value);
                }
            }
            return data;
        } catch (IOException e) {
            throw new TransformationException("An error occured while opening file.", e);
        }       
    }

}
