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

import org.eclipse.smarthome.config.core.ConfigDescriptionAction.Type;

/**
 * This is a java bean that is used with JAX-RS to serialize parameter of a
 * configuration action to JSON.
 *
 * @author Chris Jackson - Initial contribution
 */
public class ConfigActionBean {

    public String context;
    public String defaultValue;
    public String description;
    public String label;
    public String name;
    public Type type;
    public BigDecimal minimum;
    public BigDecimal maximum;
    public BigDecimal stepsize;
    public String pattern;
    public Boolean multiple;
    public Integer multipleLimit;
    public String groupName;
    public Boolean advanced;
    public Boolean limitToOptions;

    public List<ParameterOptionBean> options;
    public List<FilterCriteriaBean> filterCriteria;

    public ConfigActionBean() {
    }

    public ConfigActionBean(String name, Type type, BigDecimal minimum, BigDecimal maximum,
            BigDecimal stepsize, String pattern, Boolean multiple, String context,
            String defaultValue, String label, String description, List<ParameterOptionBean> options,
            List<FilterCriteriaBean> filterCriteria, String groupName, Boolean advanced, Boolean limitToOptions,
            Integer multipleLimit) {
        this.name = name;
        this.type = type;
        this.minimum = minimum;
        this.maximum = maximum;
        this.stepsize = stepsize;
        this.pattern = pattern;
        this.multiple = multiple;
        this.context = context;
        this.defaultValue = defaultValue;
        this.label = label;
        this.description = description;
        this.options = options;
        this.filterCriteria = filterCriteria;
        this.groupName = groupName;
        this.advanced = advanced;
        this.limitToOptions = limitToOptions;
        this.multipleLimit = multipleLimit;
    }
}
