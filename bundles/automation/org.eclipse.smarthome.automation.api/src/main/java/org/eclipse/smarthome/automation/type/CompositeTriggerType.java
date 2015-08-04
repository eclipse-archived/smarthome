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

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * {@code CompositeTriggerType} is as {@link TriggerType} which logically combines {@link Trigger} modules. The
 * composite trigger hides internal logic between participating {@link Trigger}s and it can be used as a regular
 * {@link Trigger} module.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class CompositeTriggerType extends TriggerType {

    private List<Trigger> modules;

    /**
     * This constructor is responsible for creation of a {@code CompositeTriggerType} with ordered set of
     * {@link Trigger}s.
     * It initialize only base properties of the {@code CompositeTriggerType}.
     * 
     * @param UID is the unique id of this module type in scope of the RuleEngine.
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param modules is a {@link LinkedHashSet} of {@link Trigger}s.
     * @param outputs is a {@link Set} of {@link Output} descriptions.
     * 
     */
    public CompositeTriggerType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Output> outputs,
            List<Trigger> modules) {
        super(UID, configDescriptions, outputs);
        this.modules = modules;

    }

    /**
     * This constructor is responsible for creation of a {@code CompositeTriggerType} with ordered set of
     * {@link Trigger}s.
     * It initialize all properties of the {@code CompositeTriggerType}.
     * 
     * @param UID is the unique id of this module type in scope of the RuleEngine.
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param label is a short and accurate name of the {@code CompositeTriggerType}.
     * @param description is a short and understandable description of which can be used the {@code CompositeActionType}
     *            .
     * @param tags defines categories that fit the {@code CompositTriggerType} and which can serve as criteria for
     *            searching
     *            or filtering it.
     * @param visibility determines whether the {@code CompositeTriggerType} can be used by anyone if it is
     *            {@link Visibility#PUBLIC} or only by its creator if it is {@link Visibility#PRIVATE}.
     * @param outputs is a {@link Set} of {@link Output} descriptions.
     * @param modules is a {@link LinkedHashSet} of {@link Trigger}s.
     */
    public CompositeTriggerType(String UID, Set<ConfigDescriptionParameter> configDescriptions, String label,
            String description, Set<String> tags, Visibility visibility, Set<Output> outputs, List<Trigger> modules) {
        super(UID, configDescriptions, label, description, tags, visibility, outputs);
        this.modules = modules;
    }

    /**
     * This method is used for getting the {@link Trigger}s of the {@code CompositeTriggerType}.
     * 
     * @return a {@link LinkedHashSet} of the {@link Trigger} modules of this {@code CompositeTriggerType}.
     */
    public List<Trigger> getModules() {
        return modules;
    }

}
