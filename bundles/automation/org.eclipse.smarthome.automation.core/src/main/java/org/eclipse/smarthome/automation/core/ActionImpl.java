/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.List;
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
public class ActionImpl extends ModuleImpl<ActionHandler> implements Action, ConnectedModule, SourceModule {

    private Map<Input, List<Input>> inputMap = null;
    private Set<Connection> connections;
    private Map<String, OutputValue> connectedObjects;
    private Map<String, ?> outputs;

    public ActionImpl(String UID, String templateUID, Map<String, ?> configuration, Set<Connection> connections) {
        super(UID, templateUID, configuration);
        setConnections(connections);
    }

    protected ActionImpl(ActionImpl a) {
        super(a);
        setConnections(a.getConnections());
    }

    public Set<Connection> getConnections() {
        return copyConnections(connections);
    }

    public void setConnections(Set<Connection> connections) {
        this.connections = copyConnections(connections);
    }

    public Map<String, OutputValue> getConnectedObjects() {
        return connectedObjects;
    }

    public void setConnectedObjects(Map<String, OutputValue> connectedObjects) {
        this.connectedObjects = connectedObjects;
    }

    public void setOutputs(Map<String, ?> outputs) {
        this.outputs = outputs;
    }

    public Object getOutputValue(String outName) {
        return outputs != null ? outputs.get(outName) : null;
    }

    public Map<Input, List<Input>> getInputMap() {
        return inputMap;
    }

    public void setInputMap(Map<Input, List<Input>> map) {
        this.inputMap = map;

    }

}