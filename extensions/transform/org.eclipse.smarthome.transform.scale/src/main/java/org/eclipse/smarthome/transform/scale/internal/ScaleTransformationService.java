/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.scale.internal;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.transform.AbstractFileTransformationService;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by matching it between limits of ranges in a scale file
 *
 * @author Gaël L'hopital
 * @author Markus Rathgeb - drop usage of Guava
 */
public class ScaleTransformationService extends AbstractFileTransformationService<Map<Range, String>> {

    private final Logger logger = LoggerFactory.getLogger(ScaleTransformationService.class);

    /** RegEx to extract a scale definition */
    private static final Pattern LIMITS_PATTERN = Pattern.compile("(\\[|\\])(.*)\\.\\.(.*)(\\[|\\])");

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
    protected String internalTransform(Map<Range, String> data, String source) throws TransformationException {

        try {
            final BigDecimal value = new BigDecimal(source);

            for (final Range range : data.keySet()) {
                if (range.contains(value)) {
                    return data.get(range);
                }
            }

            throw new TransformationException("No matching range for '" + source + "'");
        } catch (NumberFormatException e) {
            throw new TransformationException("Scale can only be used with numeric inputs");
        }
    }

    @Override
    protected Map<Range, String> internalLoadTransform(String filename) throws TransformationException {
        try (FileReader reader = new FileReader(filename)) {
            final Properties properties = new Properties();
            properties.load(reader);
            final Map<Range, String> data = new HashMap<>();

            for (Entry<Object, Object> f : properties.entrySet()) {
                final String key = (String) f.getKey();
                final String value = properties.getProperty(key);
                final Matcher matcher = LIMITS_PATTERN.matcher(key);
                if (matcher.matches() && (matcher.groupCount() == 4)) {

                    final boolean lowerInclusive = matcher.group(1).equals("]") ? false : true;
                    final boolean upperInclusive = matcher.group(4).equals("[") ? false : true;

                    final String lowLimit = matcher.group(2);
                    final String highLimit = matcher.group(3);

                    final BigDecimal lowValue;
                    final BigDecimal highValue;

                    try {
                        if (lowLimit.isEmpty()) {
                            lowValue = null;
                        } else {
                            lowValue = new BigDecimal(lowLimit);
                        }
                        if (highLimit.isEmpty()) {
                            highValue = null;
                        } else {
                            highValue = new BigDecimal(highLimit);
                        }
                    } catch (final NumberFormatException ex) {
                        throw new TransformationException("Error parsing bounds: " + lowLimit + ".." + highLimit);
                    }

                    final Range range = Range.range(lowValue, lowerInclusive, highValue, upperInclusive);

                    data.put(range, value);

                } else {
                    logger.warn("Scale transform entry does not comply with syntax : '{}', '{}'", key, value);
                }
            }
            return data;
        } catch (final IOException ex) {
            throw new TransformationException("An error occured while opening file.", ex);
        }
    }

}
