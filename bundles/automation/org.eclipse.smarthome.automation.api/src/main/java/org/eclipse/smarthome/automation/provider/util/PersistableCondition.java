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

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;

public class PersistableCondition {

    public String id;
    public String typeUID;
    public String label;
    public String description;
    public Map<String, ?> configurations;
    public Set<Connection> connections;

    /**
     * This constructor is used for deserialization of the {@link Condition}s.
     */
    public PersistableCondition() {
    }

    public PersistableCondition(Condition condition) {
        id = condition.getId();
        typeUID = condition.getTypeUID();
        label = condition.getLabel();
        description = condition.getDescription();
        configurations = condition.getConfiguration();
        connections = condition.getConnections();
    }

    public Condition createCondition(AutomationFactory factory) {
        Condition cond = factory.createCondition(id, typeUID, configurations, connections);
        cond.setDescription(description);
        cond.setLabel(label);
        return cond;
    }
}
