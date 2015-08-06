/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.handler.ConditionHandler;

/**
 * This class is implementation of {@link Condition} modules used in {@link Rule}s. The {@link Condition} modules are
 * also {@link ConnectedModule} becouse the can have inputs.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class ConditionImpl extends ModuleImpl<ConditionHandler>implements Condition, ConnectedModule {

    private Set<Connection> connections;
    private Map<String, OutputRef> connectedObjects;

    /**
     * Constructor of {@link Condition} module object.
     *
     * @param id id of the module.
     * @param typeUID unique module type id.
     * @param configuration configuration values of the {@link Condition} module.
     * @param connections set of {@link Connection}s used by this module.
     */
    public ConditionImpl(String id, String typeUID, Map<String, ?> configuration, Set<Connection> connections) {
        super(id, typeUID, configuration);
        this.connections = connections;
    }

    /**
     * Cloning constructor of {@link Condition} module. It is used to create a new {@link Condition} module base on
     * passed {@link Condition} module.
     *
     * @param c
     */
    public ConditionImpl(ConditionImpl c) {
        super(c);
        setConnections(c.getConnections());
    }

    @Override
    public Set<Connection> getConnections() {
        return connections;
    }

    /**
     * Creates deep copy of passed connection. The copy is used to unlink connection used by this module with the
     * connection object passed as source. In this way the connection can't be changed runtime except by this method.
     *
     * @see org.eclipse.smarthome.automation.Condition#setConnections(java.util.Set)
     */
    @Override
    public void setConnections(Set<Connection> connections) {
        this.connections = copyConnections(connections);
    }

    @Override
    public Map<String, OutputRef> getConnectedOutputs() {
        return connectedObjects;
    }

    @Override
    public void setConnectedOutputs(Map<String, OutputRef> connectedObjects) {
        this.connectedObjects = connectedObjects;
    }

}
