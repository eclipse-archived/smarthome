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

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.type.Input;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class ConditionImpl extends ModuleImpl<ConditionHandler>implements Condition, ConnectedModule {

    private Set<Connection> connections;
    private Map<String, OutputValue> connectedObjects;
    private Map<Input, List<Input>> inputMap;

    public ConditionImpl(String id, String typeUID, Map<String, ?> configuration, Set<Connection> connections) {
        super(id, typeUID, configuration);
        this.connections = connections;
    }

    public ConditionImpl(ConditionImpl c) {
        super(c);
        setConnections(c.getConnections());
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

    public Map<Input, List<Input>> getInputMap() {
        return inputMap;
    }

    public void setInputMap(Map<Input, List<Input>> map) {
        this.inputMap = map;
    }

}
