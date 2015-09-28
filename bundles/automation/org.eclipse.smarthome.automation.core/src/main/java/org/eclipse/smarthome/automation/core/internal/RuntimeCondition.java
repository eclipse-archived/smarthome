/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
public class RuntimeCondition extends Condition implements ConnectedModule {

    private ConditionHandler conditionHandler;
    private Map<String, OutputRef> connectedObjects;

    /**
     * Constructor of {@link Condition} module object.
     *
     * @param id id of the module.
     * @param typeUID unique module type id.
     * @param configuration configuration values of the {@link Condition} module.
     * @param connections set of {@link Connection}s used by this module.
     */
    public RuntimeCondition(String id, String typeUID, Map<String, ?> configuration, Set<Connection> connections,
            ConditionHandler conditionHandler) {
        super(id, typeUID, configuration, connections);
        this.conditionHandler = conditionHandler;
    }

    public RuntimeCondition(Condition condition) {
        super(condition.getId(), condition.getTypeUID(), condition.getConfiguration(), condition.getConnections());
        setLabel(condition.getLabel());
        setDescription(condition.getDescription());
    }

    // /**
    // * Cloning constructor of {@link Condition} module. It is used to create a new {@link Condition} module base on
    // * passed {@link Condition} module.
    // *
    // * @param condition
    // */
    // public ConditionImpl(ConditionImpl condition) {
    // super(condition.getId(), condition.getTypeUID(), condition.getConfiguration(), condition.getConnections());
    // setLabel(condition.getLabel());
    // setDescription(condition.getDescription());
    // }

    @Override
    public void setConfiguration(Map<String, ?> configuration) {
        this.configuration = configuration != null ? new HashMap<String, Object>(configuration) : null;
    }

    /**
     * Creates deep copy of passed connection. The copy is used to unlink connection used by this module with the
     * connection object passed as source. In this way the connection can't be changed runtime except by this method.
     *
     * @see org.eclipse.smarthome.automation.Condition#setConnections(java.util.Set)
     */
    @Override
    public void setConnections(Set<Connection> connections) {
        super.setConnections(copyConnections(connections));
    }

    @Override
    public Map<String, OutputRef> getConnectedOutputs() {
        return connectedObjects;
    }

    @Override
    public void setConnectedOutputs(Map<String, OutputRef> connectedObjects) {
        this.connectedObjects = connectedObjects;
    }

    /**
     * Utility method creating deep copy of passed connection set.
     *
     * @param connections connections used by this module.
     * @return copy of passed connections.
     */
    Set<Connection> copyConnections(Set<Connection> connections) {
        if (connections == null) {
            return null;
        }
        Set<Connection> result = new HashSet<Connection>(connections.size());
        for (Iterator<Connection> it = connections.iterator(); it.hasNext();) {
            Connection c = it.next();
            result.add(new Connection(c.getInputName(), c.getOuputModuleId(), c.getOutputName()));
        }
        return result;
    }

    /**
     * This method gets handler which is responsible for handling of this module.
     *
     * @return handler of the module or null.
     */
    ConditionHandler getModuleHandler() {
        return conditionHandler;
    }

    /**
     * This method sets handler of the module.
     *
     * @param conditionHandler
     */
    void setModuleHandler(ConditionHandler conditionHandler) {
        this.conditionHandler = conditionHandler;
    }

}
