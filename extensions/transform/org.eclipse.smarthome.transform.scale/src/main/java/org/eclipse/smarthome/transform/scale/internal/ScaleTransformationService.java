/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.transform.scale.internal;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.transform.AbstractFileTransformationService;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by matching it between limits of ranges in a scale file
 *
 * @author Gaël L'hopital
 * @author Markus Rathgeb - drop usage of Guava
 */
@Component(immediate = true, property = { "smarthome.transform=SCALE" })
public class ScaleTransformationService extends AbstractFileTransformationService<Map<Range, String>> {

    private final Logger logger = LoggerFactory.getLogger(ScaleTransformationService.class);

    /** RegEx to extract a scale definition */
    private static final Pattern LIMITS_PATTERN = Pattern.compile("(\\[|\\])(.*)\\.\\.(.*)(\\[|\\])");

    /**
     * The implementation of {@link OrderedProperties} that let access
     * properties in the same order than presented in the source file
     * by using the orderedKeys function.
     *
     * This implementation is limited to the sole purpose of the class
     * (e.g. it does not handle removing elements)
     *
     * @author Gaël L'hopital
     */
    static class OrderedProperties extends Properties {
        private static final long serialVersionUID = 3860553217028220119L;
        private final HashSet<Object> keys = new LinkedHashSet<>();

        Set<Object> orderedKeys() {
            return keys;
        }

        @Override
        public Enumeration<Object> keys() {
            return Collections.<Object> enumeration(keys);
        }

        @Override
        public Object put(Object key, Object value) {
            keys.add(key);
            return super.put(key, value);
        }
    }

    /**
     * Performs transformation of the input <code>source</code>
     *
     * The method transforms the input <code>source</code> by matching searching
     * the range where it fits i.e. [min..max]=value or ]min..max]=value
     *
     * @param properties the list of properties defining all the available ranges
     * @param source the input to transform
     *
     */
    @Override
    protected String internalTransform(Map<Range, String> data, String source) throws TransformationException {
        try {
            final BigDecimal value = new BigDecimal(source);

            return data.entrySet().stream().filter(e -> e.getKey().contains(value)).findFirst().map(Map.Entry::getValue)
                    .orElseThrow(() -> new TransformationException("No matching range for '" + source + "'"));
        } catch (NumberFormatException e) {
            throw new TransformationException("Scale can only be used with numeric inputs");
        }
    }

    @Override
    protected Map<Range, String> internalLoadTransform(String filename) throws TransformationException {
        try (FileReader reader = new FileReader(filename)) {
            final Map<Range, String> data = new LinkedHashMap<>();
            final OrderedProperties properties = new OrderedProperties();
            properties.load(reader);

            for (Object orderedKey : properties.orderedKeys()) {
                final String entry = (String) orderedKey;
                final String value = properties.getProperty(entry);
                final Matcher matcher = LIMITS_PATTERN.matcher(entry);
                if (matcher.matches() && (matcher.groupCount() == 4)) {
                    final boolean lowerInclusive = matcher.group(1).equals("]") ? false : true;
                    final boolean upperInclusive = matcher.group(4).equals("[") ? false : true;

                    final String lowLimit = matcher.group(2);
                    final String highLimit = matcher.group(3);

                    try {
                        final BigDecimal lowValue = lowLimit.isEmpty() ? null : new BigDecimal(lowLimit);
                        final BigDecimal highValue = highLimit.isEmpty() ? null : new BigDecimal(highLimit);
                        final Range range = Range.range(lowValue, lowerInclusive, highValue, upperInclusive);

                        data.put(range, value);
                    } catch (NumberFormatException ex) {
                        throw new TransformationException("Error parsing bounds: " + lowLimit + ".." + highLimit);
                    }
                } else {
                    logger.warn("Scale transform file '{}' does not comply with syntax for entry : '{}', '{}'",
                            filename, entry, value);
                }
            }

            return data;
        } catch (final IOException ex) {
            throw new TransformationException("An error occurred while opening file.", ex);
        }
    }

}
