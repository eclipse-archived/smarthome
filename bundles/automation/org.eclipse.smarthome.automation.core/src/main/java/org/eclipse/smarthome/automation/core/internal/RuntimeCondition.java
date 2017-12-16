/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.slf4j.LoggerFactory;

/**
 * This class is implementation of {@link Condition} modules used in the {@link RuleEngineImpl}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
@NonNullByDefault
public class RuntimeCondition extends Condition {

    @Nullable
    private ConditionHandler conditionHandler;
    private Set<Connection> connections;

    public RuntimeCondition(Condition condition) {
        super(condition.getId(), condition.getTypeUID(), condition.getConfiguration(), condition.getInputs());
        setLabel(condition.getLabel());
        setDescription(condition.getDescription());
        connections = Connection.getConnections(condition.getInputs(), LoggerFactory.getLogger(getClass()));
    }

    @Override
    public void setConfiguration(@Nullable Configuration configuration) {
        this.configuration = configuration == null ? new Configuration()
                : new Configuration(configuration.getProperties());
    }

    /**
     * Creates deep copy of passed connection. The copy is used to unlink connection used by this module with the
     * connection object passed as source. In this way the connection can't be changed runtime except by this method.
     *
     * @see org.eclipse.smarthome.automation.Condition#setConnections(java.util.Set)
     */
    @SuppressWarnings("null")
    void setConnections(Set<Connection> connections) {
        this.connections = connections == null ? new HashSet<>() : connections;
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
    @Nullable
    ConditionHandler getModuleHandler() {
        return conditionHandler;
    }

    /**
     * This method sets handler of the module.
     *
     * @param conditionHandler
     */
    public void setModuleHandler(@Nullable ConditionHandler conditionHandler) {
        this.conditionHandler = conditionHandler;
    }

}
