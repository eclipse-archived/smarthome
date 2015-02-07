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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@link ConfigDescriptionParameter} class contains the description of a
 * concrete configuration parameter. Such parameter descriptions are collected
 * within the {@link ConfigDescription} and can be retrieved from the {@link ConfigDescriptionRegistry}.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Alex Tugarev - Added options, filter criteria, and more parameter
 *         attributes
 */
public class ConfigDescriptionParameter {

    /**
     * The {@link Type} defines an enumeration of all supported data types
     * a configuration parameter can take.
     *
     * @author Michael Grammling - Initial Contribution
     */
    public enum Type {

        /**
         * The data type for a UTF8 text value.
         */
        TEXT,

        /**
         * The data type for a signed integer value in the range of
         * [{@link Integer#MIN_VALUE}, {@link Integer#MAX_VALUE}].
         */
        INTEGER,

        /**
         * The data type for a signed floating point value (IEEE 754) in the range of
         * [{@link Float#MIN_VALUE}, {@link Float#MAX_VALUE}].
         */
        DECIMAL,

        /**
         * The data type for a boolean ({@code true} or {@code false}).
         */
        BOOLEAN;

    }

    private String name;
    private Type type;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal step;
    private String pattern;
    private boolean required;
    private boolean readOnly;
    private boolean multiple;

    private String context;
    private String defaultValue;
    private String label;
    private String description;

    private List<ParameterOption> options = new ArrayList<ParameterOption>();
    private List<FilterCriteria> filterCriteria = new ArrayList<FilterCriteria>();

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param name the name of the configuration parameter (must neither be null nor empty)
     * @param type the data type of the configuration parameter (must not be null)
     *
     * @throws IllegalArgumentException if the name is null or empty, or the type is null
     */
    public ConfigDescriptionParameter(String name, Type type) throws IllegalArgumentException {
        this(name, type, null, null, null, null, false, false, false, null, null, null, null, null, null);
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param name
     *            the name of the configuration parameter (must neither be null
     *            nor empty)
     * @param type
     *            the data type of the configuration parameter (nullable)
     * @param minimum
     *            the minimal value for numeric types, or the minimal length of
     *            strings, or the minimal number of selected options (nullable)
     * @param maximum
     *            the maximal value for numeric types, or the maximal length of
     *            strings, or the maximal number of selected options (nullable)
     * @param stepsize
     *            the value granularity for a numeric value (nullable)
     * @param pattern
     *            the regular expression for a text type (nullable)
     * @param required
     *            specifies whether the value is required
     * @param readOnly
     *            specifies whether the value is read-only
     * @param multiple
     *            specifies whether multiple selections of options are allowed
     * @param context
     *            the context of the configuration parameter (can be null or
     *            empty)
     * @param defaultValue
     *            the default value of the configuration parameter (can be null)
     * @param label
     *            a human readable label for the configuration parameter (can be
     *            null or empty)
     * @param description
     *            a human readable description for the configuration parameter
     *            (can be null or empty)
     * @param filterCriteria
     *            a list of filter criteria for values of a dynamic selection
     *            list (nullable)
     * @param options
     *            a list of element definitions of a static selection list
     *            (nullable)
     *
     * @throws IllegalArgumentException
     *             if the name is null or empty, or the type is null
     */
    public ConfigDescriptionParameter(String name, Type type, BigDecimal minimum, BigDecimal maximum,
            BigDecimal stepsize, String pattern, Boolean required, Boolean readOnly, Boolean multiple, String context,
            String defaultValue, String label, String description, List<ParameterOption> options,
            List<FilterCriteria> filterCriteria) throws IllegalArgumentException {

        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("The name must neither be null nor empty!");
        }

        if (type == null) {
            throw new IllegalArgumentException("The type must not be null!");
        }

        this.name = name;
        this.type = type;
        this.min = minimum;
        this.max = maximum;
        this.step = stepsize;
        this.pattern = pattern;
        this.readOnly = readOnly;
        this.multiple = multiple;

        this.context = context;
        this.required = required;
        this.defaultValue = defaultValue;
        this.label = label;
        this.description = description;

        if (options != null)
            this.options = Collections.unmodifiableList(options);
        else
            this.options = Collections.unmodifiableList(new LinkedList<ParameterOption>());

        if (filterCriteria != null)
            this.filterCriteria = Collections.unmodifiableList(filterCriteria);
        else
            this.filterCriteria = Collections.unmodifiableList(new LinkedList<FilterCriteria>());
    }

    /**
     * Returns the name of the configuration parameter.
     *
     * @return the name of the configuration parameter (neither null, nor empty)
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the data type of the configuration parameter.
     *
     * @return the data type of the configuration parameter (not null)
     */
    public Type getType() {
        return this.type;
    }

    /**
     * @return the minimal value for numeric types, or the minimal length of
     *         strings, or the minimal number of selected options (nullable)
     */
    public BigDecimal getMinimum() {
        return min;
    }

    /**
     * @return the maximal value for numeric types, or the maximal length of
     *         strings, or the maximal number of selected options (nullable)
     */
    public BigDecimal getMaximum() {
        return max;
    }

    /**
     * @return the value granularity for a numeric value (nullable)
     */
    public BigDecimal getStepSize() {
        return step;
    }

    /**
     * @return the regular expression for a text type (nullable)
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @return true if the value is required, otherwise false.
     */
    public Boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @return true if multiple selections of options are allowed, otherwise false.
     */
    public Boolean isMultiple() {
        return multiple;
    }

    /**
     * Returns the context of the configuration parameter.
     * <p>
     * The context defines an enumeration of some specific context a configuration parameter can take. A context is
     * usually used for specific input validation or user interfaces.
     * <p>
     * <b>Valid values:</b>
     *
     * <pre>
     * network-address, password, password-create,
     * color, date, datetime, email, month, week, time, tel, url,
     * item, thing, group, tag, service
     * </pre>
     *
     * @return the context of the configuration parameter (could be null or
     *         empty)
     */
    public String getContext() {
        return this.context;
    }

    /**
     * Returns {@code true} if the configuration parameter has to be set, otherwise {@code false}.
     *
     * @return true if the configuration parameter has to be set, otherwise false
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Returns the default value of the configuration parameter.
     *
     * @return the default value of the configuration parameter (could be null)
     */
    public String getDefault() {
        return this.defaultValue;
    }

    /**
     * Returns a human readable label for the configuration parameter.
     *
     * @return a human readable label for the configuration parameter (could be null or empty)
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns a human readable description for the configuration parameter.
     *
     * @return a human readable description for the configuration parameter (could be null or empty)
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns a static selection list for the value of this parameter.
     *
     * @return static selection list for the value of this parameter
     */
    public List<ParameterOption> getOptions() {
        return this.options;
    }

    /**
     * Returns a list of filter criteria for a dynamically created selection
     * list.
     * <p>
     * The clients should consider the relation between the filter criteria and the parameter's context.
     *
     * @return list of filter criteria for a dynamically created selection list
     */
    public List<FilterCriteria> getFilterCriteria() {
        return this.filterCriteria;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append(" [name=");
        sb.append(name);
        sb.append(", ");
        sb.append("type=");
        sb.append(type);
        if (min != null) {
            sb.append(", ");
            sb.append("min=");
            sb.append(min);
        }
        if (max != null) {
            sb.append(", ");
            sb.append("max=");
            sb.append(max);
        }
        if (step != null) {
            sb.append(", ");
            sb.append("step=");
            sb.append(step);
        }
        if (pattern != null) {
            sb.append(", ");
            sb.append("pattern=");
            sb.append(pattern);
        }
        sb.append(", ");
        sb.append("readOnly=");
        sb.append(readOnly);

        sb.append(", ");
        sb.append("required=");
        sb.append(required);

        sb.append(", ");
        sb.append("multiple=");
        sb.append(multiple);
        if (context != null) {
            sb.append(", ");
            sb.append("context=");
            sb.append(context);
        }
        if (label != null) {
            sb.append(", ");
            sb.append("label=");
            sb.append(label);
        }
        if (description != null) {
            sb.append(", ");
            sb.append("description=");
            sb.append(description);
        }
        if (defaultValue != null) {
            sb.append(", ");
            sb.append("defaultValue=");
            sb.append(defaultValue);
        }
        sb.append("]");
        return sb.toString();
    }

}
