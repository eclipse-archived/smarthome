/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.dto;

import java.util.Map;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * This class provides functionality for (de)serialization of {@link Trigger}s.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - changed naming to DTO convention
 *
 */
public class TriggerDTO {

    /**
     * The id of the {@link Trigger}. It is unique in scope of the {@link Rule}.
     */
    public String id;

    /**
     * This field holds the unique id of the {@link ModuleType} of this {@link Trigger}.
     */
    public String typeUID;

    /**
     * This field holds a short, user friendly name of this {@link Trigger}.
     */
    public String label;

    /**
     * This field holds a short, user friendly description of this {@link Trigger}.
     */
    public String description;

    /**
     * This field holds the current configuration values of this {@link Trigger}.
     */
    public Map<String, ?> configurations;

    /**
     * This constructor is used for deserialization of the {@link Trigger}s.
     */
    public TriggerDTO() {}

    /**
     * This constructor is used for serialization of the {@link Trigger}s.
     */
    public TriggerDTO(Trigger trigger) {
        id = trigger.getId();
        typeUID = trigger.getTypeUID();
        label = trigger.getLabel();
        description = trigger.getDescription();
        configurations = trigger.getConfiguration();
    }

    /**
     * This method is used for deserialization of the {@link Trigger}s to create the {@link Trigger} with the assistance
     * of the {@link AutomationFactory}.
     */
    public Trigger createTrigger(AutomationFactory factory) {
        Trigger trigger = factory.createTrigger(id, typeUID, configurations);
        trigger.setDescription(description);
        trigger.setLabel(label);
        return trigger;
    }
}
