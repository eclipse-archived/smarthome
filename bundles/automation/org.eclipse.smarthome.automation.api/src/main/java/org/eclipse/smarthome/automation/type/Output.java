/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type;

import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;

/**
 * This class defines meta-information for {@link Output} which is used by the RuleEngine when creating connection
 * between modules. The meta-information contains:
 * <ul>
 * <li>name(mandatory) - unique id in scope of containing Module</li>
 * <li>type(mandatory) - accepted data type by this Output</li>
 * <li>label(optional) - short id (one word) of the Output</li>
 * <li>description(optional) - user friendly description of the Output</li>
 * <li>default value(optional) - default value of the Output</li>
 * <li>reference - reference to data source. It defines what part of complex data (i.e. JavaBean, java.lang.Map etc.)
 * has to be used as value of this output.
 * </ul>
 * Outputs are exit points of a {@link Module}. They are used as data source for {@link Input}s of other {@link Module}
 * s.
 * The {@link Output} can be connected
 * to more then one {@link Input} of the same data type.<br>
 * 
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class Output {

    /**
     * is an unique name of the {@code Output} in scope of the {@link Module}.
     */
    private String name;

    /**
     * This field specifies the type of the {@code Output}. The accepted types are all java types defined by fully
     * qualified names.
     */
    private String type;

    /**
     * This field is associated with the {@code Output}. The tags add additional restrictions to connections between
     * {@link Input}s and {@link Output}s. The {@link Input}'s tags must be subset of the {@code Output}'s tags to
     * succeed the connection.</br> For example: When we want to connect {@link Input} to {@code Output} and both have
     * same java.lang.double data type. The the output has assign "temperature" and "celsius" tags then the input must
     * have at least one of these output's tags (i.e. "temperature") to connect this input to the selected
     * {@code Output}.
     */
    private Set<String> tags;

    /**
     * This field keeps a single word description of the {@code Output}.
     */
    private String label;

    /**
     * This field keeps the user friendly description of the {@code Output}.
     */
    private String description;

    /**
     * The value of this field refers to the data source. It defines what part of complex data should be used as source
     * of this {@code Output}.
     */
    private String reference;

    /**
     * The value of this field takes place when there is no runtime value for this {@code Output}. Type of the default
     * value must be the type of the {@code Output}.
     */
    private Object defaultValue;

    /**
     * Constructor of {@code Output} objects. It is based on the type of data and {@code Output}'s name.
     * 
     * @param name is an unique name of the {@code Output}.
     * @param type is the data type accepted by this {@code Output}.
     * 
     * @see #getType()
     */
    public Output(String name, String type) {
        this.name = name;
        setType(type);
    }

    /**
     * Constructor of {@code Output} object. Creates an {@code Output} instance based on the type of accepted data
     * and {@code Output}'s name.
     * 
     * @param type is the data type accepted by this {@code Output}.
     * @param name is an unique name of the {@code Output}.
     * @param label a single word description of the {@code Output}.
     * @param description is an user friendly description of the {@code Output}.
     * @param tags are associated with the {@code Output}. The tags add additional restrictions to connections between
     *            {@link Input}s and {@link Output}s. The {@link Input}'s tags must be subset of the {@code Output}'s
     *            tags to succeed the connection.</br> For example: When we want to connect {@link Input} to
     *            {@code Output} and both have same java.lang.double data type. The the output has assign "temperature"
     *            and "celsius" tags then the input must have at least one of these output's tags (i.e. "temperature")
     *            to connect this input to the selected {@code Output}.
     * @param reference refers to the data source. It defines what part of complex data should be used as source of
     *            this {@code Output}.
     * @param defaultValue takes place when there is no runtime value for this {@code Output}. Type of the default value
     *            must be the type of the {@code Output}.
     */
    public Output(String name, String type, String label, String description, Set<String> tags, String reference,
            Object defaultValue) {
        if (name == null)
            throw new IllegalArgumentException("The name of the input must not be NULL!");
        this.name = name;
        setType(type);
        this.label = label;
        this.description = description;
        this.tags = tags;
        this.reference = reference;
        this.defaultValue = defaultValue;
    }

    /**
     * This method is used for getting the name of {@code Output}. It must be unique in
     * scope of {@link Rule}.
     * 
     * @return name is an unique identifier of the {@code Output}.
     */
    public String getName() {
        return name;
    }

    /**
     * This method is used for getting the type of the {@code Output}. The accepted types
     * are all java types defined by fully qualified names.
     * 
     * @return type is a fully qualified name of java type.
     */
    public String getType() {
        return type;
    }

    /**
     * This method is used for getting the short description of the {@code Output}.
     * Usually the label should be a single word description.
     * 
     * @return label of the Output.
     */
    public String getLabel() {
        return label;
    }

    /**
     * This method is used for getting the long description of the {@code Output}.
     * 
     * @return user friendly description of the {@code Output}.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for getting the reference to data source. It defines what part of complex data (i.e.
     * JavaBean, java.lang.Map etc.) has to be used as a value of this {@code Output}. For example, in the
     * {@code Output} data - java.lang.Map, the reference points to the property that has to be used as an output value.
     * 
     * @return a reference to data source.
     */
    public String getReference() {
        return reference;
    }

    /**
     * This method is used for getting the tags of the {@code Output}. The tags add additional restrictions to
     * connections between {@link Input}s and {@code Output}s. The input tags must be subset of the output tags to
     * succeed the connection.</br> For example: When we want to connect {@link Input} to {@code Output} and they both
     * have same data type - java.lang.double and the {@link Output} has assign "temperature" and "celsius" tags, then
     * the {@link Input} must have at least one of these {@code Output}'s tags (i.e. "temperature") to connect this
     * {@link Input} to the selected {@code Output}.
     * 
     * @return the tags, associated with this {@link Input}.
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * This method is used for getting the default value of the {@code Output}. Default value takes place when there is
     * no runtime value for this {@code Output}. Type of the default value must be the type of the {@code Output}.
     * 
     * @return the default value of this {@code Output}.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * This method is used for setting the type of the {@code Output}. The accepted types are all java types defined by
     * fully qualified names.
     * 
     * @param type is a fully qualified name of the java type.
     */
    private void setType(String type) {
        // TODO verify type if it is a fully qualified class name
        this.type = type;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Output " + name;
    }
}
