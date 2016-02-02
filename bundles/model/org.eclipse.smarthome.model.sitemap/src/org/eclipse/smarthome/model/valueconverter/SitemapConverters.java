/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.valueconverter;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractDeclarativeValueConverterService;
import org.eclipse.xtext.nodemodel.INode;

public class SitemapConverters extends AbstractDeclarativeValueConverterService {

    @ValueConverter(rule = "Icon")
    public IValueConverter<String> Icon() {

        return new IValueConverter<String>() {

            @Override
            public String toValue(String string, INode node) throws ValueConverterException {
                if (string != null && string.startsWith("\"")) {
                    return string.substring(1, string.length() - 1);
                }
                return string;
            }

            @Override
            public String toString(String value) throws ValueConverterException {
                if (containsWhiteSpace(value)) {
                    return "\"" + value + "\"";
                }
                return value;
            }

        };
    }

    public static boolean containsWhiteSpace(final String string) {
        if (string != null) {
            for (int i = 0; i < string.length(); i++) {
                if (Character.isWhitespace(string.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
