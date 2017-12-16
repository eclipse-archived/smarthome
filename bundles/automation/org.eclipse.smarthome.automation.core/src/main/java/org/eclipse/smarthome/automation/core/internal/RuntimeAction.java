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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.slf4j.LoggerFactory;

/**
 * This class is implementation of {@link Action} modules used in the {@link RuleEngineImpl}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
@NonNullByDefault
public class RuntimeAction extends Action {

    /**
     * The handler of this module.
     */
    @Nullable
    private ActionHandler actionHandler;
    private Set<Connection> connections;

    /**
     * Utility constructor creating copy of passed action.
     *
     * @param action another action which is uses as base of created
     */
    public RuntimeAction(final Action action) {
        super(action.getId(), action.getTypeUID(), action.getConfiguration(), action.getInputs());
        setLabel(action.getLabel());
        setDescription(action.getDescription());
        connections = Connection.getConnections(action.getInputs(), LoggerFactory.getLogger(getClass()));
    }

    @Override
    public void setConfiguration(@Nullable Configuration configuration) {
        this.configuration = configuration == null ? new Configuration()
                : new Configuration(configuration.getProperties());
    }

    /**
     * This method set deep copy of passed connections as connections of for this module.
     *
     * @see org.eclipse.smarthome.automation.Action#setConnections(java.util.Set)
     */
    @SuppressWarnings("null")
    void setConnections(Set<Connection> connections) {
        this.connections = connections == null ? new HashSet<>() : connections;
    }

    public Set<Connection> getConnections() {
        return connections;
    }

    /**
     * This method gets handler which is responsible for handling of this module.
     *
     * @return handler of the module or null.
     */
    @Nullable
    ActionHandler getModuleHandler() {
        return actionHandler;
    }

    /**
     * This method sets handler of the module.
     *
     * @param actionHandler
     */
    public void setModuleHandler(@Nullable ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }
}
