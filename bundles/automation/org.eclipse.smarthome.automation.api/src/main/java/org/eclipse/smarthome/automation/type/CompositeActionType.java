/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * {@code CompositeActionType} is as {@link ActionType} which logically combines {@link Action} instances. The composite
 * action hides internal logic and inner connections between participating {@link Action}s and it can be used as a
 * regular
 * {@link Action} module.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class CompositeActionType extends ActionType {

    private List<Action> modules;

    /**
     * This constructor is responsible for creation of a {@code CompositeActionType} with ordered set of {@link Action}
     * s.
     * It initialize only base properties of the {@code CompositeActionType}.
     * 
     * @param UID is the unique id of this module type in scope of the RuleEngine.
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param modules is a {@link LinkedHashSet} of {@link Action}s.
     * @param inputs is a {@link Set} of {@link Input} descriptions.
     * @param outputs is a {@link Set} of {@link Output} descriptions.
     */
    public CompositeActionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Input> inputs,
            Set<Output> outputs, List<Action> modules) {
        super(UID, configDescriptions, inputs, outputs);
        this.modules = modules;

    }

    /**
     * This constructor is responsible for creation of a {@code CompositeActionType} with ordered set of {@link Action}
     * s.
     * It initialize all properties of the {@code CompositeActionType}.
     * 
     * @param UID is the unique id of this module type in scope of the RuleEngine.
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param label is a short and accurate name of the {@code CompositeActionType}.
     * @param description is a short and understandable description of which can be used the {@code CompositeActionType}
     *            .
     * @param tags defines categories that fit the {@code CompositeActionType} and which can serve as criteria for
     *            searching
     *            or filtering it.
     * @param visibility determines whether the {@code CompositeActionType} can be used by anyone if it is
     *            {@link Visibility#PUBLIC} or only by its creator if it is {@link Visibility#PRIVATE}.
     * @param inputs is a {@link Set} of {@link Input} descriptions.
     * @param outputs is a {@link Set} of {@link Output} descriptions.
     * @param modules is a {@link LinkedHashSet} of {@link Action}s.
     */
    public CompositeActionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, String label,
            String description, Set<String> tags, Visibility visibility, Set<Input> inputs, Set<Output> outputs,
            List<Action> modules) {
        super(UID, configDescriptions, label, description, tags, visibility, inputs, outputs);
        this.modules = modules;
    }

    /**
     * This method is used for getting the {@link Action}s of the {@code CompositeActionType}.
     * 
     * @return a {@link LinkedHashSet} of the {@link Action} modules of this {@code CompositeActionType}.
     */
    public List<Action> getModules() {
        return modules;
    }

}
