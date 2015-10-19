/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type;

import java.util.Set;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class provides common functionality for creating {@link Condition} instances by supplying types with their
 * meta-information. The {@link Condition}s are part of "IF" section of the Rule. Each {@link ConditionType} is defined
 * by unique id in scope of the RuleEngine and defines {@link ConfigDescriptionParameter}s that are meta-information for
 * configuration and meta-information for {@link Input}s used for creation of {@link Condition} instances.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class ConditionType extends ModuleType {

    private Set<Input> inputs;

    /**
     * This constructor is responsible to create an instance of {@link ConditionType} with base properties - UID, a
     * {@link Set} of configuration descriptions and a {@link Set} of {@link Input} descriptions.
     * 
     * @param UID is an unique id of the {@link ActionType}, used as reference from the {@link Module}s, to find their
     *            meta-information.
     * @param configDescriptions is a {@link Set} of meta-information configuration descriptions.
     * @param inputs is a {@link Set} of {@link Input} meta-information descriptions.
     */
    public ConditionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Input> inputs) {
        super(UID, configDescriptions);
        this.inputs = inputs;
    }

    /**
     * This constructor is responsible to create an instance of {@link ConditionType} with UID, label, description, a
     * {@link Set} of tags, visibility, a {@link Set} of configuration descriptions and a {@link Set} of {@link Input}
     * descriptions.
     * 
     * @param UID unique id of the {@link ConditionType}.
     * @param configDescriptions is a {@link Set} of meta-information configuration descriptions.
     * @param label is a short and accurate name of the {@link ConditionType}.
     * @param description is a short and understandable description of which can be used the {@link ConditionType}.
     * @param tags defines categories that fit the {@link ConditionType} and which can serve as criteria for searching
     *            or filtering it.
     * @param visibility determines whether the {@link ConditionType} can be used by anyone if it is
     *            {@link Visibility#PUBLIC} or only by its creator if it is {@link Visibility#PRIVATE}.
     * @param inputs is a {@link Set} of {@link Input} meta-information descriptions.
     */
    public ConditionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, String label,
            String description, Set<String> tags, Visibility visibility, Set<Input> inputs) {
        super(UID, configDescriptions, label, description, tags, visibility);
        this.inputs = inputs;
    }

    /**
     * This method is used for getting the meta-information descriptions of {@link Input}s defined by this
     * {@link ConditionType}.
     * 
     * @return a {@link Set} of {@link Input} meta-information descriptions.
     */
    public Set<Input> getInputs() {
        return inputs;
    }
}
