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
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.dto.TriggerDTO;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is responsible to store the information for creation of the {@link CompositeTriggerTypes}s in
 * {@link CompositeTriggerTypeDTO}s. This class provides functionality for (de)serialization of {@link Trigger}s.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class CompositeTriggerTypeDTO extends TriggerType {

    public List<TriggerDTO> modules;

    /**
     * This constructor is used for serialization of the {@link Trigger}s participating in the
     * {@link CompositeTriggerType}.
     */
    public CompositeTriggerTypeDTO(CompositeTriggerType compositeTrigger) {
        super(compositeTrigger.getUID(), compositeTrigger.getConfigurationDescription(), compositeTrigger.getLabel(),
                compositeTrigger.getDescription(), compositeTrigger.getTags(), compositeTrigger.getVisibility(),
                compositeTrigger.getOutputs());
        modules = new ArrayList<TriggerDTO>();
        List<Trigger> triggers = compositeTrigger.getModules();
        for (Trigger trigger : triggers) {
            modules.add(new TriggerDTO(trigger));
        }
    }

    /**
     * This constructor is used for serialization of the {@link CompositeTriggerType}s.
     */
    public CompositeTriggerTypeDTO(String moduleTypeUID, LinkedHashSet<ConfigDescriptionParameter> configDescriptions,
            String label, String description, Set<String> tags, Visibility v, Set<Output> outputs,
            List<TriggerDTO> modules) {
        super(moduleTypeUID, configDescriptions, label, description, tags, v, outputs);
        this.modules = modules;
    }

    /**
     * This method is used for deserialization of the {@link Trigger}s to create the {@link Trigger}s with the
     * assistance of the {@link AutomationFactory} and participating in the {@link CompositeTriggerType}.
     */
    public CompositeTriggerType createCompositeTriggerType(AutomationFactory factory) {
        List<Trigger> modules = new ArrayList<Trigger>();
        for (TriggerDTO pTrigger : this.modules) {
            modules.add(pTrigger.createTrigger(factory));
        }
        return new CompositeTriggerType(getUID(), getConfigurationDescription(), getLabel(), getDescription(),
                getTags(), getVisibility(), getOutputs(), modules);
    }

}
