/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.valueconverter;

import java.util.regex.Pattern;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.impl.AbstractNullSafeConverter;
import org.eclipse.xtext.conversion.impl.STRINGValueConverter;
import org.eclipse.xtext.nodemodel.INode;

import com.google.inject.Inject;

public class SitemapConverters extends DefaultTerminalConverters {

    private static final Pattern ID_PATTERN = Pattern.compile("\\p{Alpha}\\w*");

    @Inject
    private STRINGValueConverter stringValueConverter;

    @ValueConverter(rule = "Icon")
    public IValueConverter<String> BIG_DECIMAL() {
        return stringValueConverter;
    }

    @ValueConverter(rule = "Command")
    public IValueConverter<String> Command() {
        return new AbstractNullSafeConverter<String>() {
            @Override
            protected String internalToValue(String string, INode node) {
                if ((string.startsWith("'") && string.endsWith("'"))
                        || (string.startsWith("\"") && string.endsWith("\""))) {
                    return STRING().toValue(string, node);
                }
                return ID().toValue(string, node);
            }

            @Override
            protected String internalToString(String value) {
                if (ID_PATTERN.matcher(value).matches()) {
                    return ID().toString(value);
                } else {
                    return STRING().toString(value);
                }
            }
        };
    }
}
