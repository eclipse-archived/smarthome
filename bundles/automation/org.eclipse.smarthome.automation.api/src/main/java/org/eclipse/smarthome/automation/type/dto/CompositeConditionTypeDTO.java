/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.dto.ConditionDTO;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.ConditionType;

public class CompositeConditionTypeDTO extends ConditionType {

    public List<ConditionDTO> modules;

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

    public static Set<CompositeConditionType> createFrom(Set<CompositeConditionTypeDTO> persSet,
            AutomationFactory factory) {
        Set<CompositeConditionType> cctSet = new HashSet<CompositeConditionType>();
        for (CompositeConditionTypeDTO pcct : persSet) {
            cctSet.add(pcct.createCompositeConditionType(factory));
        }
        return cctSet;
    }

    public CompositeConditionType createCompositeConditionType(AutomationFactory factory) {
        List<Condition> modules = new ArrayList<Condition>();
        for (ConditionDTO pCondition : this.modules) {
            modules.add(pCondition.createCondition(factory));
        }
        return new CompositeConditionType(getUID(), getConfigurationDescription(), getLabel(), getDescription(),
                getTags(), getVisibility(), getInputs(), modules);
    }
}
