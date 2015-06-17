/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescriptionAction.Type;

/**
 * The {@link ConfigDescriptionActionBuilder} class provides a builder for the {@link ConfigDescriptionAction}
 * class.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ConfigDescriptionActionBuilder {
    private String name;
    private Type type;

    private String groupName;

    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal step;
    private String pattern;
    private boolean multiple;
    private Integer multipleLimit;

    private String context;
    private String defaultValue;
    private String label;
    private String description;

    private boolean limitToOptions;
    private boolean advanced;

    private List<ParameterOption> options = new ArrayList<ParameterOption>();
    private List<FilterCriteria> filterCriteria = new ArrayList<FilterCriteria>();

    private ConfigDescriptionActionBuilder(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Creates a parameter builder
     *
     * @param name configuration parameter name
     * @param type configuration parameter type
     * @return parameter builder
     */
    public static ConfigDescriptionActionBuilder create(String name, Type type) {
        return new ConfigDescriptionActionBuilder(name, type);
    }

    /**
     * Set the minimum value of the configuration parameter
     *
     * @param min
     */
    public ConfigDescriptionActionBuilder withMinimum(BigDecimal min) {
        this.min = min;
        return this;
    }

    /**
     * Set the maximum value of the configuration parameter
     *
     * @param max
     */
    public ConfigDescriptionActionBuilder withMaximum(BigDecimal max) {
        this.max = max;
        return this;
    }

    /**
     * Set the step size of the configuration parameter
     *
     * @param step
     */
    public ConfigDescriptionActionBuilder withStepSize(BigDecimal step) {
        this.step = step;
        return this;
    }

    /**
     * Set the pattern of the configuration parameter
     *
     * @param pattern
     */
    public ConfigDescriptionActionBuilder withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Set the configuration parameter to allow multiple selection
     *
     * @param multiple
     */
    public ConfigDescriptionActionBuilder withMultiple(Boolean multiple) {
        this.multiple = multiple;
        return this;
    }

    /**
     * Set the configuration parameter to allow multiple selection
     *
     * @param multiple
     */
    public ConfigDescriptionActionBuilder withMultipleLimit(Integer multipleLimit) {
        this.multipleLimit = multipleLimit;
        return this;
    }

    /**
     * Set the context of the configuration parameter
     *
     * @param context
     */
    public ConfigDescriptionActionBuilder withContext(String context) {
        this.context = context;
        return this;
    }

    /**
     * Set the default value of the configuration parameter
     *
     * @param defaultValue
     */
    public ConfigDescriptionActionBuilder withDefault(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Set the label of the configuration parameter
     *
     * @param label
     */
    public ConfigDescriptionActionBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Set the description of the configuration parameter
     *
     * @param description
     */
    public ConfigDescriptionActionBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the options of the configuration parameter
     *
     * @param options
     */
    public ConfigDescriptionActionBuilder withOptions(List<ParameterOption> options) {
        this.options = options;
        return this;
    }

    /**
     * Set the configuration parameter as an advanced parameter
     *
     * @param options
     */
    public ConfigDescriptionActionBuilder withAdvanced(Boolean advanced) {
        this.advanced = advanced;
        return this;
    }

    /**
     * Set the configuration parameter to be limited to the values in the options list
     *
     * @param options
     */
    public ConfigDescriptionActionBuilder withLimitToOptions(Boolean limitToOptions) {
        this.limitToOptions = limitToOptions;
        return this;
    }

    /**
     * Set the configuration parameter to be limited to the values in the options list
     *
     * @param options
     */
    public ConfigDescriptionActionBuilder withGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    /**
     * Set the filter criteria of the configuration parameter
     *
     * @param filterCriteria
     */
    public ConfigDescriptionActionBuilder withFilterCriteria(List<FilterCriteria> filterCriteria) {
        this.filterCriteria = filterCriteria;
        return this;
    }

    /**
     * Builds a result with the settings of this builder.
     *
     * @return the desired result
     */
    public ConfigDescriptionAction build() throws IllegalArgumentException {
        return new ConfigDescriptionAction(name, type, min, max, step, pattern, multiple,
                context, defaultValue, label, description, options, filterCriteria, groupName, advanced,
                limitToOptions, multipleLimit);
    }

}
