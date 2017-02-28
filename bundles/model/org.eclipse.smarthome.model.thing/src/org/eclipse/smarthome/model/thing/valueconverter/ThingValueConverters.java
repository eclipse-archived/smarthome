/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.valueconverter;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;

import com.google.inject.Inject;

public class ThingValueConverters extends DefaultTerminalConverters {

    @Inject
    private ValueTypeToStringConverter valueTypeToStringConverter;

    @Inject
    private UIDtoStringConverter uidToStringConverter;

    @ValueConverter(rule = "ValueType")
    public IValueConverter<Object> ValueType() {
        return valueTypeToStringConverter;
    }

    @ValueConverter(rule = "UID")
    public IValueConverter<String> UID() {
        return uidToStringConverter;
    }

}
