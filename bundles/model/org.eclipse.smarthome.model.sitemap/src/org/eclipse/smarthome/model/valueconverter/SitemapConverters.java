/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.valueconverter;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.impl.STRINGValueConverter;

import com.google.inject.Inject;

public class SitemapConverters extends DefaultTerminalConverters {

    @Inject
    private STRINGValueConverter stringValueConverter;

    @ValueConverter(rule = "Icon")
    public IValueConverter<String> BIG_DECIMAL() {
        return stringValueConverter;
    }
}
