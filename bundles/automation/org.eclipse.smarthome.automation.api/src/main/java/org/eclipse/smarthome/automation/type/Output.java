/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.type;

import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;

/**
 * This class defines meta info for {@link Output} which is used by the
 * RuleEngine when creating connection between modules. The meta info contains:
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
 * s. The {@link Output} can be connected
 * to more then one {@link Input} of the same data type.<br>
 *
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public class Output {

    private String name;
    private String type;
    private Set<String> tags;
    private String label;
    private String description;
    private String reference;
    private Object defaultValue;

    /**
     * Constructor of Output objects. It is based on the type of data and Output's
     * name
     *
     * @param type data type accepted by this Output.
     * @param name unique name of the Output.
     * @see #getType()
     */
    public Output(String name, String type) {
        this.name = name;
        setType(type);
    }

    /**
     * Constructor of Output object. Creates Output base on type of accepted data
     * and Output name
     *
     * @param type data type accepted by this Output.;
     * @param name unique name of the Output.
     * @param label a single word description of the Output.
     * @param description user friendly description of the Output.
     * @param tags tags associated with the Output. The tags adds additional
     *            restrictions to connections between {@link Input}s and {@link Output}s. The input tags must be subset
     *            of the output tags
     *            to succeed the connection.</br> For example: When we want to
     *            connect input to output and both have same java.lang.double data
     *            type. The the output has assign "temperature" and "celsius" tags
     *            then the input must have at least one of these output's tags (i.e.
     *            "temperature") to connect this input to the selected output.
     * @param reference a reference to data source. It defines what part of
     *            complex data should be used as source of this Output
     * @param defaultValue default value takes place when there is no value for
     *            this Output. Type of the default value must be the type the
     *            output.
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
     * This method is used for getting the name of Output. It must be unique in
     * scope of {@link Rule}.
     *
     * @return name is an unique identifier of the Output.
     */
    public String getName() {
        return name;
    }

    /**
     * This method is used for getting the type of the Output. The accepted types
     * are all java types defined by fully qualified names.
     *
     * @return type is a fully qualified name of java type.
     */
    public String getType() {
        return type;
    }

    /**
     * This method is used for getting the short description of the Output.
     * Usually the label should be a single word description.
     *
     * @return label of the Output.
     */
    public String getLabel() {
        return label;
    }

    /**
     * This method is used for getting the long description of the Output.
     *
     * @return user friendly description of the Output.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for getting the reference to data source. It defines
     * what part of complex data (i.e. JavaBean, java.lang.Map etc.) has to be
     * used as value of this output. For example in the Output data is
     * java.lang.Map the reference points which property has to be used as output
     * value.
     *
     * @return reference to data source.
     */
    public String getReference() {
        return reference;
    }

    /**
     * This method is used for getting the tags of the Output. The tags adds
     * additional restrictions to connections between {@link Input}s and {@link Output}s. The input tags must be subset
     * of the output tags to
     * succeed the connection.</br> For example: When we want to connect input to
     * output and they both have same java.lang.double data type, and the output
     * has assign "temperature" and "celsius" tags then the input must have at
     * least one of these output's tags (i.e. "temperature") to connect this input
     * to the selected output.
     *
     * @return tags associated with this Input.
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * This method is used for getting the default value of the Output. Default
     * value takes place when there is no value for this Ouput. Type of the
     * default value must be the type the Output.
     *
     * @return default Output value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * This method is used for setting the type of the Output. The accepted types
     * are all java types defined by fully qualified names.
     *
     * @param type is a fully qualified name of java type.
     */
    private void setType(String type) {
        // TODO verify type if it is a fully qualified class name
        this.type = type;
    }

}
