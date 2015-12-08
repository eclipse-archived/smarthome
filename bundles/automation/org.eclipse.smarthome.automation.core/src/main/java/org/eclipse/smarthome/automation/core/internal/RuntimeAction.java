/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.slf4j.LoggerFactory;

/**
 * This class is implementation of {@link Action} modules used in the {@link RuleEngine}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class RuntimeAction extends Action {

    /**
     * The handler of this module.
     */
    private ActionHandler actionHandler;
    private Set<Connection> connections;

    /**
     * Utility constructor creating copy of passed action.
     *
     * @param action another action which is uses as base of created
     */
    public RuntimeAction(Action action) {
        super(action.getId(), action.getTypeUID(), action.getConfiguration(), action.getInputs());
        setConnections(Connection.getConnections(action.getInputs(), LoggerFactory.getLogger(getClass())));
        setLabel(action.getLabel());
        setDescription(action.getDescription());
    }

    /**
     * This method set deep copy of passed connections as connections of for this module.
     *
     * @see org.eclipse.smarthome.automation.Action#setConnections(java.util.Set)
     */
    void setConnections(Set<Connection> connections) {
        this.connections = connections;
    }

    public Set<Connection> getConnections() {
        return connections;
    }

    /**
     * This method gets handler which is responsible for handling of this module.
     *
     * @return handler of the module or null.
     */
    ActionHandler getModuleHandler() {
        return actionHandler;
    }

    /**
     * This method sets handler of the module.
     *
     * @param actionHandler
     */
    public void setModuleHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

}