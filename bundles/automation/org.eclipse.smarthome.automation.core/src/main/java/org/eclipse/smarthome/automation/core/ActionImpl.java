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

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Actions are the part of "THEN" section of the {@link Rule} definition.
 * Elements of this section are expected result of {@link Rule} execution. The
 * Action can have {@link Output} elements. These actions are used to process
 * input data as source data of other Actions. Building elements of actions ( {@link ConfigDescriptionParameter}s,
 * {@link Input}s and {@link Output}s) are
 * defined by {@link ActionType}
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class ActionImpl extends ModuleImpl<ActionHandler>implements Action, ConnectedModule, SourceModule {

    private Set<Connection> connections;
    private Map<String, OutputRef> connectedObjects;
    private Map<String, ?> outputs;

    /**
     * Constructor of Action object.
     *
     * @param UID action unique id.
     * @param typeUID module type unique id.
     * @param configuration map of configuration values.
     * @param connections set of connections to other modules (triggers and other actions).
     */
    public ActionImpl(String UID, String typeUID, Map<String, ?> configuration, Set<Connection> connections) {
        super(UID, typeUID, configuration);
        setConnections(connections);
    }

    /**
     * Utility constructor creating copy of passed action.
     *
     * @param action another action which is uses as base of created
     */
    protected ActionImpl(ActionImpl action) {
        super(action);
        setConnections(action.getConnections());
    }

    @Override
    public Set<Connection> getConnections() {
        return connections;
    }

    /**
     * This method set deep copy of passed connections as connections of for this module.
     *
     * @see org.eclipse.smarthome.automation.Action#setConnections(java.util.Set)
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

    @Override
    public void setOutputs(Map<String, ?> outputs) {
        this.outputs = outputs;
    }

    @Override
    public Object getOutputValue(String outName) {
        return outputs != null ? outputs.get(outName) : null;
    }

}