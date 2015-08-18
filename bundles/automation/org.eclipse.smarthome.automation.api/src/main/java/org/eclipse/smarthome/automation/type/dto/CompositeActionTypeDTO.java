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

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.dto.ActionDTO;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is responsible to store the information for creation of the {@link CompositeActionType}s in
 * {@link CompositeActionTypeDTO}s. This class provides functionality for (de)serialization of {@link Action}s.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class CompositeActionTypeDTO extends ActionType {

    public List<ActionDTO> modules;

    /**
     * This constructor is used for serialization of the {@link Action}s participating in the
     * {@link CompositeActionType}.
     */
    public CompositeActionTypeDTO(CompositeActionType compositeAction) {
        super(compositeAction.getUID(), compositeAction.getConfigurationDescription(), compositeAction.getLabel(),
                compositeAction.getDescription(), compositeAction.getTags(), compositeAction.getVisibility(),
                compositeAction.getInputs(), compositeAction.getOutputs());
        modules = new ArrayList<ActionDTO>();
        List<Action> actions = compositeAction.getModules();
        for (Action action : actions) {
            modules.add(new ActionDTO(action));
        }
    }

    /**
     * This constructor is used for serialization of the {@link Action}s.
     */
    public CompositeActionTypeDTO(String moduleTypeUID, LinkedHashSet<ConfigDescriptionParameter> configDescriptions,
            String label, String description, Set<String> tags, Visibility v, Set<Input> inputs, Set<Output> outputs,
            List<ActionDTO> actionModules) {
        super(moduleTypeUID, configDescriptions, label, description, tags, v, inputs, outputs);
        modules = actionModules;
    }

    /**
     * This method is used for deserialization of the {@link Action}s to create the {@link Action}s with the assistance
     * of the {@link AutomationFactory} and participating in the {@link CompositeActionType}.
     */
    public CompositeActionType createCompositeActionType(AutomationFactory factory) {
        List<Action> modules = new ArrayList<Action>();
        for (ActionDTO pAction : this.modules) {
            modules.add(pAction.createAction(factory));
        }
        return new CompositeActionType(getUID(), getConfigurationDescription(), getLabel(), getDescription(), getTags(),
                getVisibility(), getInputs(), getOutputs(), modules);
    }

}
