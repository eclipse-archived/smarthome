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

/**
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class PersistableAction {

    public String id;
    public String typeUID;
    public String label;
    public String description;
    public Map<String, ?> configurations;
    public Set<Connection> connections;

    /**
     * This constructor is used for deserialization of the {@link Action}s.
     */
    public PersistableAction() {
    }

    public PersistableAction(Action action) {
        id = action.getId();
        typeUID = action.getTypeUID();
        label = action.getLabel();
        description = action.getDescription();
        configurations = action.getConfiguration();
        connections = action.getConnections();
    }

    public Action createAction(AutomationFactory factory) {
        Action action = factory.createAction(id, typeUID, configurations, connections);
        action.setLabel(label);
        action.setDescription(description);
        return action;
    }

}
