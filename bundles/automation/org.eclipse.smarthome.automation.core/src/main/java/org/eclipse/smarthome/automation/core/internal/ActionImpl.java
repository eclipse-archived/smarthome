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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This class is implementation of {@link Action} modules used in the {@link RuleEngineImpl}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
@NonNullByDefault
public class ActionImpl extends ModuleImpl implements Action {

    @Nullable
    private ActionHandler actionHandler;
    private Set<Connection> connections = Collections.emptySet();
    private Map<String, String> inputs = Collections.emptyMap();

    public ActionImpl() {
    }

    /**
     * Utility constructor creating copy of passed action.
     *
     * @param action another action which is uses as base of created
     */
    public ActionImpl(final Action action) {
        this(action.getId(), action.getTypeUID(), action.getConfiguration(), action.getInputs());
        setLabel(action.getLabel());
        setDescription(action.getDescription());
    }

    /**
     * Constructor of Action object.
     *
     * @param UID           action unique id.
     * @param typeUID       module type unique id.
     * @param configuration map of configuration values.
     * @param inputs        set of connections to other modules (triggers and other actions).
     */
    public ActionImpl(String UID, String typeUID, Configuration configuration, @Nullable Map<String, String> inputs) {
        super(UID, typeUID, configuration);
        setInputs(inputs);
    }

    /**
     * This method sets the connections for this module.
     *
     * @param connections the set of connections for this action
     */
    void setConnections(@Nullable Set<Connection> connections) {
        this.connections = connections == null ? Collections.emptySet() : connections;
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

    /**
     * This method is used to get input connections of the Action. The connections
     * are links between {@link Input}s of the this {@link Module} and {@link Output}s
     * of other {@link Module}s.
     *
     * @return map that contains the inputs of this action.
     */
    @Override
    public Map<String, String> getInputs() {
        return inputs;
    }

    /**
     * This method is used to connect {@link Input}s of the action to {@link Output}s of other {@link Module}s.
     *
     * @param inputs map that contains the inputs for this action.
     */
    public void setInputs(@Nullable Map<String, String> inputs) {
        this.inputs = inputs == null ? Collections.emptyMap() : Collections.unmodifiableMap(inputs);
    }
}
