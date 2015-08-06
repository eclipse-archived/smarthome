/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.provider.util;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * This class provides functionality for persistence of the {@link Action}s.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class PersistableAction {

    /**
     * The id of the {@link Action}. It is unique in scope of the {@link Rule}.
     */
    public String id;

    /**
     * This field holds the unique id of the {@link ModuleType} of this {@link Action}.
     */
    public String typeUID;

    /**
     * This field holds a short, user friendly name of this {@link Action}.
     */
    public String label;

    /**
     * This field holds a short, user friendly description of this {@link Action}.
     */
    public String description;

    /**
     * This field holds the current configuration values of this {@link Action}.
     */
    public Map<String, ?> configurations;

    /**
     * A {@link Set} of input {@link Connection}s of the {@link Action}.
     */
    public Set<Connection> connections;

    /**
     * This constructor is used for deserialization of the {@link Action}s.
     */
    public PersistableAction() {
    }

    /**
     * This constructor is used for serialization of the {@link Action}s.
     */
    public PersistableAction(Action action) {
        id = action.getId();
        typeUID = action.getTypeUID();
        label = action.getLabel();
        description = action.getDescription();
        configurations = action.getConfiguration();
        connections = action.getConnections();
    }

    /**
     * This method is used for deserialization of the {@link Action}s to create the {@link Action} with the assistance
     * of the {@link AutomationFactory}.
     */
    public Action createAction(AutomationFactory factory) {
        Action action = factory.createAction(id, typeUID, configurations, connections);
        action.setLabel(label);
        action.setDescription(description);
        return action;
    }

}
