/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.dto;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * This class provides functionality for (de)serialization of {@link Condition}s.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - changed naming to DTO convention
 *
 */
public class ConditionDTO {

    /**
     * The id of the {@link Condition}. It is unique in scope of the {@link Rule}.
     */
    public String id;

    /**
     * This field holds the unique id of the {@link ModuleType} of this {@link Condition}.
     */
    public String typeUID;

    /**
     * This field holds a short, user friendly name of this {@link Condition}.
     */
    public String label;

    /**
     * This field holds a short, user friendly description of this {@link Condition}.
     */
    public String description;

    /**
     * This field holds the current configuration values of this {@link Condition}.
     */
    public Map<String, ?> configurations;

    /**
     * A {@link Set} of input {@link Connection}s of the {@link Condition}.
     */
    public Set<Connection> connections;

    /**
     * This constructor is used for deserialization of the {@link Condition}s.
     */
    public ConditionDTO() {}

    /**
     * This constructor is used for serialization of the {@link Condition}s.
     */
    public ConditionDTO(Condition condition) {
        id = condition.getId();
        typeUID = condition.getTypeUID();
        label = condition.getLabel();
        description = condition.getDescription();
        configurations = condition.getConfiguration();
        connections = condition.getConnections();
    }

    /**
     * This method is used for deserialization of the {@link Condition}s to create the {@link Condition} with the
     * assistance of the {@link AutomationFactory}.
     */
    public Condition createCondition(AutomationFactory factory) {
        Condition cond = factory.createCondition(id, typeUID, configurations, connections);
        cond.setDescription(description);
        cond.setLabel(label);
        return cond;
    }
}
