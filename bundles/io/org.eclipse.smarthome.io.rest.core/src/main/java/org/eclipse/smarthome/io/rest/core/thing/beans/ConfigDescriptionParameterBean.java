/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;

/**
 * This is a java bean that is used with JAX-RS to serialize parameter of a
 * configuration description to JSON.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Alex Tugarev - Extended for options and filter criteria
 *
 */
public class ConfigDescriptionParameterBean {

    public String context;
    public String defaultValue;
    public String description;
    public String label;
    public String name;
    public boolean required;
    public Type type;
    public BigDecimal minimum;
    public BigDecimal maximum;
    public BigDecimal stepsize;
    public String pattern;
    public Boolean readOnly;
    public Boolean multiple;

    public List<ParameterOptionBean> options;
    public List<FilterCriteriaBean> filterCriteria;

    public ConfigDescriptionParameterBean() {
    }

    public ConfigDescriptionParameterBean(String name, Type type, BigDecimal minimum, BigDecimal maximum,
            BigDecimal stepsize, String pattern, Boolean required, Boolean readOnly, Boolean multiple, String context,
            String defaultValue, String label, String description, List<ParameterOptionBean> options,
            List<FilterCriteriaBean> filterCriteria) {
        this.name = name;
        this.type = type;
        this.minimum = minimum;
        this.maximum = maximum;
        this.stepsize = stepsize;
        this.pattern = pattern;
        this.readOnly = readOnly;
        this.multiple = multiple;
        this.context = context;
        this.required = required;
        this.defaultValue = defaultValue;
        this.label = label;
        this.description = description;
        this.options = options;
        this.filterCriteria = filterCriteria;
    }

}
