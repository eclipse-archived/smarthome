/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml;

import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.config.xml.util.GenericUnmarshaller;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link ParameterOptionConverter} creates a {@link ParameterOption} instance from a {@code option} XML node.
 *
 * @author Alex Tugarev - Initial Contribution
 */
public class ParameterOptionConverter extends GenericUnmarshaller<ParameterOption> {

    public ParameterOptionConverter() {
        super(ParameterOption.class);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String value = reader.getAttribute("value");
        String label = reader.getValue();
        return new ParameterOption(value, label);
    }

}
