/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.slf4j.LoggerFactory;

/**
 * This class is implementation of {@link Condition} modules used in the {@link RuleEngine}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class RuntimeCondition extends Condition {

    private ConditionHandler conditionHandler;
    private Set<Connection> connections;

    public RuntimeCondition(Condition condition) {
        super(condition.getId(), condition.getTypeUID(), condition.getConfiguration(), condition.getInputs());
        setConnections(Connection.getConnections(condition.getInputs(), LoggerFactory.getLogger(getClass())));
        setLabel(condition.getLabel());
        setDescription(condition.getDescription());
    }

    /**
     * Creates deep copy of passed connection. The copy is used to unlink connection used by this module with the
     * connection object passed as source. In this way the connection can't be changed runtime except by this method.
     *
     * @see org.eclipse.smarthome.automation.Condition#setConnections(java.util.Set)
     */
    void setConnections(Set<Connection> connections) {
        this.connections = connections;
    }

    public Set<Connection> getConnections() {
        return connections;
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
    public void setModuleHandler(ConditionHandler conditionHandler) {
        this.conditionHandler = conditionHandler;
    }

}
