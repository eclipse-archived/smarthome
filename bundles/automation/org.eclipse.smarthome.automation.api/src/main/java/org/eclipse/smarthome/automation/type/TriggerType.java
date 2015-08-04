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
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is used to define trigger types. The triggers types contains meta
 * info of the {@link Trigger} instances. Each trigger type has unique id in
 * scope of the rule engine and defines {@link ConfigDescriptionParameter}s and {@link Output}s of the {@link Trigger}
 * instance.
 * 
 * This class provides common functionality for creating {@link Trigger} instances by supplying types with their
 * meta-information. The {@link Trigger}s are part of "ON" section of the Rule. Each {@link TriggerType} is defined by
 * unique id in scope of the RuleEngine and defines {@link ConfigDescriptionParameter}s that are meta-information for
 * configuration and meta-information for {@link Output}s used for creation of {@link Trigger} instances.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 */
public class TriggerType extends ModuleType {

    private Set<Output> outputs;

    /**
     * This constructor is responsible to create an instance of {@link TriggerType} with base properties - UID, a
     * {@link Set} of configuration descriptions and a {@link Set} of {@link Output} descriptions.
     * 
     * @param UID is an unique id of the {@link ActionType}, used as reference from the {@link Module}s, to find their
     *            meta-information.
     * @param configDescriptions is a {@link Set} of meta-information configuration descriptions.
     * @param outputs is a {@link Set} of {@link Output} meta-information descriptions.
     */
    public TriggerType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Output> outputs) {
        super(UID, configDescriptions);
        this.outputs = outputs;
    }

    /**
     * This constructor is responsible to create an instance of {@link TriggerType} with UID, label, description, a
     * {@link Set} of tags, visibility, a {@link Set} of configuration descriptions and a {@link Set} of {@link Output}
     * descriptions.
     * 
     * @param UID unique id of the {@link TriggerType}.
     * @param configDescriptions is a {@link Set} of meta-information configuration descriptions.
     * @param label is a short and accurate name of the {@link TriggerType}.
     * @param description is a short and understandable description of which can be used the {@link TriggerType}.
     * @param tags defines categories that fit the {@link TriggerType} and which can serve as criteria for searching
     *            or filtering it.
     * @param visibility determines whether the {@link TriggerType} can be used by anyone if it is
     *            {@link Visibility#PUBLIC} or only by its creator if it is {@link Visibility#PRIVATE}.
     * @param outputs is a {@link Set} of {@link Output} meta-information descriptions.
     */
    public TriggerType(String UID, Set<ConfigDescriptionParameter> configDescriptions, String label, String description,
            Set<String> tags, Visibility visibility, Set<Output> outputs) {
        super(UID, configDescriptions, label, description, tags, visibility);
        this.outputs = outputs;
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
