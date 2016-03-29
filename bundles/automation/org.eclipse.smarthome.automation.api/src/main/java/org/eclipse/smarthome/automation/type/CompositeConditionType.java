/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * {@code CompositeConditionType} is as {@link ConditionType} which logically combines {@link Condition} modules. The
 * composite condition hides internal logic between participating conditions and it can be used as a regular
 * {@link Condition} module.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class CompositeConditionType extends ConditionType {

    private List<Condition> children;

    /**
     * Default constructor for deserialization e.g. by Gson.
     */
    protected CompositeConditionType() {
    }

    /**
     * This constructor is responsible for creation of a {@code CompositeConditionType} with ordered set of
     * {@link Condition}s.
     * It initialize only base properties of the {@code CompositeConditionType}.
     *
     * @param UID is the unique id of this module type in scope of the RuleEngine.
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param children is a LinkedHashSet of {@link Condition}s.
     * @param inputs is a {@link Set} of {@link Input} descriptions.
     */
    public CompositeConditionType(String UID, List<ConfigDescriptionParameter> configDescriptions, List<Input> inputs,
            List<Condition> children) {
        super(UID, configDescriptions, inputs);
        this.children = children;
    }

    /**
     * This constructor is responsible for creation of a {@code CompositeConditionType} with ordered set of
     * {@link Condition}s.
     * It initialize all properties of the {@code CompositeConditionType}.
     *
     * @param UID is the unique id of this module type in scope of the RuleEngine.
     * @param configDescriptions is a {@link List} of configuration descriptions.
     * @param label is a short and accurate name of the {@code CompositeConditionType}.
     * @param description is a short and understandable description of which can be used the
     *            {@code CompositeConditionType}.
     * @param tags defines categories that fit the {@code CompositeConditionType} and which can serve as criteria for
     *            searching
     *            or filtering it.
     * @param visibility determines whether the {@code CompositeConditionType} can be used by anyone if it is
     *            {@link Visibility#VISIBLE} or only by its creator if it is {@link Visibility#HIDDEN}.
     * @param inputs is a {@link List} of {@link Input} descriptions.
     * @param children is a {@link LinkedHashSet} of {@link Condition}s.
     */
    public CompositeConditionType(String UID, List<ConfigDescriptionParameter> configDescriptions, String label,
            String description, Set<String> tags, Visibility visibility, List<Input> inputs, List<Condition> children) {
        super(UID, configDescriptions, label, description, tags, visibility, inputs);
        this.children = children;
    }

    /**
     * This method is used for getting Conditions of the {@code CompositeConditionType}.
     *
     * @return a {@link LinkedHashSet} of the {@link Condition} modules of this {@code CompositeConditionType}.
     */
    public List<Condition> getChildren() {
        return children != null ? children : Collections.<Condition> emptyList();
    }

}
