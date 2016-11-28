/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * This type can be used for items that are dealing with telephony functionality.
 *
 * @author Thomas.Eichstaedt-Engelen / Initially CallType
 * @author Gaël L'hopital - port to Eclipse SmartHome
 *
 */
public class StringListType implements Command, State {

    protected List<String> typeDetails;

    // constants
    static final public String DELIMITER = ",";
    static final public String ESCAPED_DELIMITER = "\\" + DELIMITER;
    static final public String REGEX_SPLITTER = "(?<!\\\\)" + DELIMITER;

    public StringListType() {
        typeDetails = Collections.emptyList();
    }

    public StringListType(StringType... rows) {
        typeDetails = new ArrayList<String>(rows.length);
        for (StringType row : rows) {
            typeDetails.add(row.toString());
        }
    }

    public StringListType(String... rows) {
        typeDetails = Arrays.asList(rows);
    }

    /**
     * Deserialize the input string,
     * splitting it on every delimiter not preceeded by a backslash
     */
    public StringListType(String serialized) {
        String[] rows = serialized.split(REGEX_SPLITTER);
        typeDetails = new ArrayList<String>(rows.length);
        for (String row : rows) {
            typeDetails.add(row.replace(ESCAPED_DELIMITER, DELIMITER));
        }
    }

    public String getValue(final int index) {
        if (index < 0 || index >= typeDetails.size()) {
            throw new IllegalArgumentException("Index is out of range");
        }
        return typeDetails.get(index);
    }

    /**
     * <p>
     * Formats the value of this type according to a pattern (@see
     * {@link Formatter}). One single value of this type can be referenced
     * by the pattern using an index. The item order is defined by the natural
     * (alphabetical) order of their keys.
     * </p>
     *
     * @param pattern the pattern to use containing indexes to reference the
     *            single elements of this type.
     */
    @Override
    public String format(String pattern) {
        return String.format(pattern, typeDetails.toArray());
    }

    @Override
    public String toString() {
        return toFullString();
    }

    @Override
    public String toFullString() {
        List<String> parts = new ArrayList<>(typeDetails.size());
        for (String row : typeDetails) {
            parts.add(row.replace(DELIMITER, ESCAPED_DELIMITER));
        }
        return StringUtils.join(parts, DELIMITER);
    }

    public static StringListType valueOf(String value) {
        return new StringListType(value);
    }

}