/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml;

import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.xml.util.GenericUnmarshaller;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link FilterCriteriaConverter} creates a {@link FilterCriteria} instance
 * from a {@code criteria} XML node.
 *
 * @author Alex Tugarev - Initial Contribution
 */
public class FilterCriteriaConverter extends GenericUnmarshaller<FilterCriteria> {

    public FilterCriteriaConverter() {
        super(FilterCriteria.class);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String name = reader.getAttribute("name");
        String criteria = reader.getValue();
        return new FilterCriteria(name, criteria);
    }

}
