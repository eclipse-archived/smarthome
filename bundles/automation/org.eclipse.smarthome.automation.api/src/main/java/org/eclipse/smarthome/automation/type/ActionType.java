/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type;

import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class provides common functionality for creating {@link Action} instances by supplying types with their
 * meta-information. The {@link Action}s are part of "THEN" section of the Rule. Each {@link ActionType} is defined by
 * unique id in scope of the RuleEngine and defines {@link ConfigDescriptionParameter}s that are meta-information for
 * configuration and meta-information for {@link Input}s and {@link Output}s used for creation of {@link Action}
 * instances.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class ActionType extends ModuleType {

    /**
     * This field contains meta-information describing the incoming connections of the {@link Action} module to the
     * other {@link Module}s.
     */
    private Set<Input> inputs;

    /**
     * This field contains meta-information describing the outgoing connections of the {@link Action} module to the
     * other {@link Action}s.
     */
    private Set<Output> outputs;

    /**
     * This constructor is responsible to create an instance of {@link ActionType} with base properties - UID, a
     * {@link Set} of configuration descriptions and a {@link Set} of {@link Input} definitions.
     * 
     * @param UID is an unique id of the {@link ActionType}, used as reference from the {@link Module}s, to find their
     *            meta-information.
     * @param configDescriptions is a {@link Set} of meta-information configuration descriptions.
     * @param inputs is a {@link Set} of {@link Input} meta-information descriptions.
     */
    public ActionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Input> inputs) {
        this(UID, configDescriptions, inputs, null);
    }

    /**
     * This constructor is responsible to create an instance of the {@link ActionType} with UID, a {@link Set} of
     * configuration descriptions, a {@link Set} of {@link Input} definitions and a {@link Set} of {@link Output}
     * descriptions.
     * 
     * @param UID is an unique id of the {@link ActionType}, used as reference from the {@link Module}s, to find their
     *            meta-information.
     * @param configDescriptions is a {@link Set} of meta-information configuration descriptions.
     * @param inputs is a {@link Set} of {@link Input} meta-information descriptions.
     * @param outputs is a {@link Set} of {@link Output} meta-information descriptions.
     */
    public ActionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Input> inputs,
            Set<Output> outputs) {
        super(UID, configDescriptions);
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * This constructor is responsible to create an instance of {@link ActionType} with UID, label, description, a
     * {@link Set} of tags, visibility, a {@link Set} of configuration descriptions, a {@link Set} of {@link Input}
     * descriptions and a {@link Set} of {@link Output} descriptions.
     * 
     * @param UID unique id of the {@link ActionType}.
     * @param configDescriptions is a {@link Set} of meta-information configuration descriptions.
     * @param label is a short and accurate name of the {@link ActionType}.
     * @param description is a short and understandable description of which can be used the {@link ActionType}.
     * @param tags defines categories that fit the {@link ActionType} and which can serve as criteria for searching
     *            or filtering it.
     * @param visibility determines whether the {@link ActionType} can be used by anyone if it is
     *            {@link Visibility#PUBLIC} or only by its creator if it is {@link Visibility#PRIVATE}.
     * @param inputs is a {@link Set} of {@link Input} meta-information descriptions.
     * @param outputs is a {@link Set} of {@link Output} meta-information descriptions.
     */
    public ActionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, String label, String description,
            Set<String> tags, Visibility visibility, Set<Input> inputs, Set<Output> outputs) {
        super(UID, configDescriptions, label, description, tags, visibility);
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * This method is used for getting the meta-information descriptions of {@link Input}s defined by this type.<br/>
     * 
     * @return a {@link Set} of {@link Input} definitions.
     */
    public Set<Input> getInputs() {
        return inputs;
    }

    /**
     * This method is used for getting the meta-information descriptions of {@link Output}s defined by this type.<br/>
     * 
     * @return a {@link Set} of {@link Output} definitions.
     */
    public Set<Output> getOutputs() {
        return outputs;
    }

}
