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

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;

public class PersistableCompositeActionType extends ActionType {

    public List<PersistableAction> modules;

    public PersistableCompositeActionType(CompositeActionType compositeAction) {
        super(compositeAction.getUID(), compositeAction.getConfigurationDescription(), compositeAction.getLabel(),
                compositeAction.getDescription(), compositeAction.getTags(), compositeAction.getVisibility(),
                compositeAction.getInputs(), compositeAction.getOutputs());
        modules = new ArrayList<PersistableAction>();
        List<Action> actions = compositeAction.getModules();
        for (Action action : actions) {
            modules.add(new PersistableAction(action));
        }
    }

    public static Set<CompositeActionType> createFrom(Set<PersistableCompositeActionType> persSet,
            AutomationFactory factory) {
        Set<CompositeActionType> catSet = new HashSet<CompositeActionType>();
        for (PersistableCompositeActionType pcat : persSet) {
            catSet.add(pcat.createCompositeActionType(factory));
        }
        return catSet;
    }

    public CompositeActionType createCompositeActionType(AutomationFactory factory) {
        List<Action> modules = new ArrayList<Action>();
        for (PersistableAction pAction : this.modules) {
            modules.add(pAction.createAction(factory));
        }
        return new CompositeActionType(getUID(), getConfigurationDescription(), getLabel(), getDescription(), getTags(),
                getVisibility(), getInputs(), getOutputs(), modules);
    }
}
