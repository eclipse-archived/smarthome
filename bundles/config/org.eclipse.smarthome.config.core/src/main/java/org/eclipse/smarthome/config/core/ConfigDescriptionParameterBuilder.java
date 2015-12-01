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

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;

/**
 * The {@link ConfigDescriptionParameterBuilder} class provides a builder for the {@link ConfigDescriptionParameter}
 * class.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ConfigDescriptionParameterBuilder {
    private String name;
    private Type type;

    private String groupName;

    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal step;
    private String pattern;
    private boolean required;
    private boolean readOnly;
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

    private ConfigDescriptionParameterBuilder(String name, Type type) {
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
    public static ConfigDescriptionParameterBuilder create(String name, Type type) {
        return new ConfigDescriptionParameterBuilder(name, type);
    }

    /**
     * Set the minimum value of the configuration parameter
     *
     * @param min
     */
    public ConfigDescriptionParameterBuilder withMinimum(BigDecimal min) {
        this.min = min;
        return this;
    }

    /**
     * Set the maximum value of the configuration parameter
     *
     * @param max
     */
    public ConfigDescriptionParameterBuilder withMaximum(BigDecimal max) {
        this.max = max;
        return this;
    }

    /**
     * Set the step size of the configuration parameter
     *
     * @param step
     */
    public ConfigDescriptionParameterBuilder withStepSize(BigDecimal step) {
        this.step = step;
        return this;
    }

    /**
     * Set the pattern of the configuration parameter
     *
     * @param pattern
     */
    public ConfigDescriptionParameterBuilder withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Set the configuration parameter as read only
     *
     * @param readOnly
     */
    public ConfigDescriptionParameterBuilder withReadOnly(Boolean readOnly) {
        if (readOnly == null)
            this.readOnly = false;
        else
            this.readOnly = readOnly;
        return this;
    }

    /**
     * Set the configuration parameter to allow multiple selection
     *
     * @param multiple
     */
    public ConfigDescriptionParameterBuilder withMultiple(Boolean multiple) {
        if (multiple == null)
            this.multiple = false;
        else
            this.multiple = multiple;
        return this;
    }

    /**
     * Set the configuration parameter to allow multiple selection
     *
     * @param multiple
     */
    public ConfigDescriptionParameterBuilder withMultipleLimit(Integer multipleLimit) {
        this.multipleLimit = multipleLimit;
        return this;
    }

    /**
     * Set the context of the configuration parameter
     *
     * @param context
     */
    public ConfigDescriptionParameterBuilder withContext(String context) {
        this.context = context;
        return this;
    }

    /**
     * Set the configuration parameter to be required
     *
     * @param required
     */
    public ConfigDescriptionParameterBuilder withRequired(Boolean required) {
        if (required == null)
            this.required = false;
        else
            this.required = required;
        return this;
    }

    /**
     * Set the default value of the configuration parameter
     *
     * @param defaultValue
     */
    public ConfigDescriptionParameterBuilder withDefault(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Set the label of the configuration parameter
     *
     * @param label
     */
    public ConfigDescriptionParameterBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Set the description of the configuration parameter
     *
     * @param description
     */
    public ConfigDescriptionParameterBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the options of the configuration parameter
     *
     * @param options
     */
    public ConfigDescriptionParameterBuilder withOptions(List<ParameterOption> options) {
        this.options = options;
        return this;
    }

    /**
     * Set the configuration parameter as an advanced parameter
     *
     * @param options
     */
    public ConfigDescriptionParameterBuilder withAdvanced(Boolean advanced) {
        if (advanced == null)
            this.advanced = false;
        else
            this.advanced = advanced;
        return this;
    }

    /**
     * Set the configuration parameter to be limited to the values in the options list
     *
     * @param options
     */
    public ConfigDescriptionParameterBuilder withLimitToOptions(Boolean limitToOptions) {
        if (limitToOptions == null)
            this.limitToOptions = false;
        else
            this.limitToOptions = limitToOptions;
        return this;
    }

    /**
     * Set the configuration parameter to be limited to the values in the options list
     *
     * @param options
     */
    public ConfigDescriptionParameterBuilder withGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    /**
     * Set the filter criteria of the configuration parameter
     *
     * @param filterCriteria
     */
    public ConfigDescriptionParameterBuilder withFilterCriteria(List<FilterCriteria> filterCriteria) {
        this.filterCriteria = filterCriteria;
        return this;
    }

    /**
     * Builds a result with the settings of this builder.
     *
     * @return the desired result
     */
    public ConfigDescriptionParameter build() throws IllegalArgumentException {
        return new ConfigDescriptionParameter(name, type, min, max, step, pattern, required, readOnly, multiple,
                context, defaultValue, label, description, options, filterCriteria, groupName, advanced, limitToOptions,
                multipleLimit);
    }

}
