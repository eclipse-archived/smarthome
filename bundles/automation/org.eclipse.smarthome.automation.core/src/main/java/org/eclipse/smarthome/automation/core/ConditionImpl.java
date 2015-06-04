/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

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
public class ConditionImpl extends ModuleImpl<ConditionHandler> implements Condition, ConnectedModule {

    private Set<Connection> connections;
    private Map<String, OutputValue> connectedObjects;
    private Map<Input, List<Input>> inputMap;

    public ConditionImpl(String id, String templateUID, Map<String, ?> configuration, Set<Connection> connections) {
        super(id, templateUID, configuration);
        this.connections = connections;
    }

    public ConditionImpl(ConditionImpl c) {
        super(c);
        setConnections(c.getConnections());
    }

    /**
     * @see org.eclipse.smarthome.automation.Condition#getConnections()
     */
    @Override
    public Set<Connection> getConnections() {
        return copyConnections(connections);
    }

    /**
     * @see org.eclipse.smarthome.automation.Condition#setConnections(java.util.Set)
     */
    @Override
    public void setConnections(Set<Connection> connections) {
        this.connections = copyConnections(connections);
    }

    @Override
    public Map<String, OutputValue> getConnectedObjects() {
        return connectedObjects;
    }

    @Override
    public void setConnectedObjects(Map<String, OutputValue> connectedObjects) {
        this.connectedObjects = connectedObjects;
    }

    @Override
    public Map<Input, List<Input>> getInputMap() {
        return inputMap;
    }

    @Override
    public void setInputMap(Map<Input, List<Input>> map) {
        this.inputMap = map;
    }

}
