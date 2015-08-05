/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.provider.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.ConditionType;

public class PersistableCompositeConditionType extends ConditionType {

    public List<PersistableCondition> modules;

    public PersistableCompositeConditionType(CompositeConditionType compositeCondition) {
        super(compositeCondition.getUID(), compositeCondition.getConfigurationDescription(),
                compositeCondition.getLabel(), compositeCondition.getDescription(), compositeCondition.getTags(),
                compositeCondition.getVisibility(), compositeCondition.getInputs());
        modules = new ArrayList<PersistableCondition>();
        List<Condition> conditions = compositeCondition.getModules();
        for (Condition condition : conditions) {
            modules.add(new PersistableCondition(condition));
        }
    }

    public static Set<CompositeConditionType> createFrom(Set<PersistableCompositeConditionType> persSet,
            AutomationFactory factory) {
        Set<CompositeConditionType> cctSet = new HashSet<CompositeConditionType>();
        for (PersistableCompositeConditionType pcct : persSet) {
            cctSet.add(pcct.createCompositeConditionType(factory));
        }
        return cctSet;
    }

    public CompositeConditionType createCompositeConditionType(AutomationFactory factory) {
        List<Condition> modules = new ArrayList<Condition>();
        for (PersistableCondition pCondition : this.modules) {
            modules.add(pCondition.createCondition(factory));
        }
        return new CompositeConditionType(getUID(), getConfigurationDescription(), getLabel(), getDescription(),
                getTags(), getVisibility(), getInputs(), modules);
    }
}
