/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;

/**
 * This class defines meta-information which is used by the RuleEngine when creating connection between modules. The
 * meta-information contains:
 * <ul>
 * <li>name(mandatory) - unequal id in scope of containing Module</li>
 * <li>type(mandatory) - accepted data type by this Parameter</li>
 * <li>label(optional) - short id (one word) of the Parameter</li>
 * <li>description(optional) - user friendly description of the Parameter</li>
 * <li>default value(optional) - default value of the Parameter</li>
 * <li>is required(optional) - defines if the Parameter is required or optional. If missing the value is true.</li>
 * </ul>
 * The Inputs are entry points of {@link Module}s for data coming from other
 * modules. The {@link Input} can be connected to a single {@link Output} of
 * other module which produces data of the same type.</br>
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class Input {

    /**
     * is an unique name of the {@code Input} in scope of the {@link Module}.
     */
    private String name;

    /**
     * This field specifies the type of the {@code Input}. The accepted types are all java types defined by fully
     * qualified names.
     */
    private String type;

    /**
     * This field keeps a single word description of the {@code Input}.
     */
    private String label;

    /**
     * This field keeps the user friendly description of the {@code Input}.
     */
    private String description;

    /**
     * This field determines if the {@code Input} is required or optional.
     */
    private boolean required = false;
    private Set<String> tags;
    private String reference;
    private Object defaultValue;

    /**
     * Default constructor for deserialization e.g. by Gson.
     */
    protected Input() {
    }

    /**
     * Constructor of the {@code Input} object. Creates Input base on type of accepted data and {@code Input}'s name.
     *
     * @param type data type accepted by this Input. The accepted types are any java types defined by fully qualified
     *            names.
     * @param name unique name of the Input.
     */
    public Input(String name, String type) {
        this(name, type, null, null, null, false, null, null);
    }

    /**
     * Constructor of Input object. Creates Input base on type of accepted data
     * and input name
     *
     * @param type data type accepted by this {@code Input}.
     * @param name unique name of the {@code Input}.
     * @param label a single word description of the {@code Input}.
     * @param description user friendly description of the {@code Input}.
     * @param tags are associated with the {@code Input}. The tags adds additional restrictions to connections between
     *            {@code Input}s and {@link Output}s. The input tags must be subset of the output tags to succeed the
     *            connection.</br>
     *            For example: When we want to connect input to output and both have same
     *            java.lang.double data type. The the output has assign "temperature" and "celsius" tags then the input
     *            must have at least one of these output's tags (i.e. "temperature") to connect this {@code Input} to
     *            the selected output.
     * @param required determines if the {@code Input} is required or optional.
     * @param reference refers to the input of parent module type or null. If this input is part of the system module
     *            the reference should be null.
     * @param defaultValue default value takes place when there is no value for this Input. Type of the default value
     *            must be the type the Input.
     */
    public Input(String name, String type, String label, String description, Set<String> tags, boolean required,
            String reference, Object defaultValue) {
        if (name == null) {
            throw new IllegalArgumentException("The name of the input must not be NULL!");
        }
        this.name = name;
        setType(type);
        this.label = label;
        this.description = description;
        this.tags = tags;
        this.required = required;
        this.reference = reference;
        this.defaultValue = defaultValue;
    }

    /**
     * This method is used for getting the name of Input. It must be unique in scope of the {@link Rule}.
     *
     * @return name is an unique identifier of the Input.
     */
    public String getName() {
        return name;
    }

    /**
     * This method is used for getting the short description of the Input. Usually the label should be a single word
     * description.
     *
     * @return label of the Input.
     */
    public String getLabel() {
        return label;
    }

    /**
     * This method is used for getting the long description of the Input.
     *
     * @return user friendly description of the Input.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for determining if the Input is required or optional.
     *
     * @return true when required, false otherwise.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * This method is used for getting the type of the Input. The accepted types are all java types defined by fully
     * qualified names.
     *
     * @return type is a fully qualified name of java type.
     */
    public String getType() {
        return type;
    }

    /**
     * This method is used for getting the tags of the Input. The tags adds additional restrictions to connections
     * between {@link Input}s and {@link Output}s. The input tags must be subset of the output tags to succeed the
     * connection.</br>
     * For example: When we want to connect input to output and they both have same java.lang.double
     * data type, and the output has assign "temperature" and "celsius" tags then the input must have at least one of
     * these output's tags (i.e. "temperature") to connect this input to the selected output.
     *
     * @return tags associated with this Input.
     */
    public Set<String> getTags() {
        return tags != null ? tags : Collections.<String> emptySet();
    }

    /**
     * This method is used for getting the reference to data source. It is used to link custom inputs (inputs of custom
     * module type) to system input (defined by the system module type. The system module type uses only system inputs).
     *
     * @return reference to data source.
     */
    public String getReference() {
        return reference;
    }

    /**
     * This method is used for getting the default value of the Input. Default value takes place when there is no value
     * for this Input. Type of the default value must be the type the Input.
     *
     * @return default Input value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * This method is used for setting the type of the Input. The accepted types are all java types defined by fully
     * qualified names.
     *
     * @param type is a fully qualified name of java type.
     */
    private void setType(String type) {
        // TODO verify type if it is a fully qualified class name
        this.type = type;
    }

    @Override
    public String toString() {
        return "Input " + name;
    }
}
