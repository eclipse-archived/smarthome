/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type.dto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.dto.ConditionDTO;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is responsible to store the information for creation of the {@link CompositeConditionType}s in
 * {@link CompositeConditionTypeDTO}s. This class provides functionality for (de)serialization of {@link Condition}s.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class CompositeConditionTypeDTO extends ConditionType {

    public List<ConditionDTO> modules;

    /**
     * This constructor is used for serialization of the {@link CompositeConditionType}s.
     */
    public CompositeConditionTypeDTO(CompositeConditionType compositeCondition) {
        super(compositeCondition.getUID(), compositeCondition.getConfigurationDescription(),
                compositeCondition.getLabel(), compositeCondition.getDescription(), compositeCondition.getTags(),
                compositeCondition.getVisibility(), compositeCondition.getInputs());
        modules = new ArrayList<ConditionDTO>();
        List<Condition> conditions = compositeCondition.getModules();
        for (Condition condition : conditions) {
            modules.add(new ConditionDTO(condition));
        }
    }

    /**
     * This constructor is used for serialization of the {@link Condition}s participating in the
     * {@link CompositeConditionType}.
     */
    public CompositeConditionTypeDTO(String moduleTypeUID, LinkedHashSet<ConfigDescriptionParameter> configDescriptions,
            String label, String description, Set<String> tags, Visibility v, Set<Input> inputs,
            List<ConditionDTO> conditionModules) {
        super(moduleTypeUID, configDescriptions, label, description, tags, v, inputs);
        modules = conditionModules;
    }

    /**
     * This method is used for deserialization of the {@link Condition}s to create the {@link Condition}s with the
     * assistance of the {@link AutomationFactory} and participating in the {@link CompositeConditionType}.
     */
    public CompositeConditionType createCompositeConditionType(AutomationFactory factory) {
        List<Condition> modules = new ArrayList<Condition>();
        for (ConditionDTO pCondition : this.modules) {
            modules.add(pCondition.createCondition(factory));
        }
        return new CompositeConditionType(getUID(), getConfigurationDescription(), getLabel(), getDescription(),
                getTags(), getVisibility(), getInputs(), modules);
    }
}
