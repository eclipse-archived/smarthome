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
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This class is implementation of {@link Condition} modules used in the {@link RuleEngineImpl}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
@NonNullByDefault
public class ConditionImpl extends ModuleImpl implements Condition {

    private Map<String, String> inputs = Collections.emptyMap();
    private Set<Connection> connections = Collections.emptySet();

    @Nullable
    private ConditionHandler conditionHandler;

    public ConditionImpl() {
    }

    public ConditionImpl(Condition condition) {
        this(condition.getId(), condition.getTypeUID(), condition.getConfiguration(), condition.getInputs());
        setLabel(condition.getLabel());
        setDescription(condition.getDescription());
    }

    /**
     * Constructor of {@link Condition} module object.
     *
     * @param id            id of the module.
     * @param typeUID       unique module type id.
     * @param configuration configuration values of the {@link Condition} module.
     * @param inputs        set of {@link Input}s used by this module.
     */
    public ConditionImpl(String id, String typeUID, Configuration configuration, Map<String, String> inputs) {
        super(id, typeUID, configuration);
        setInputs(inputs);
    }

    /**
     * This method sets the connections for this module.
     *
     * @param connections the set of connections for this condition
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

    /**
     * This method is used to get input connections of the Condition. The connections
     * are links between {@link Input}s of the current {@link Module} and {@link Output}s of other
     * {@link Module}s.
     *
     * @return map that contains the inputs of this condition.
     */
    @Override
    public Map<String, String> getInputs() {
        return inputs;
    }

    /**
     * This method is used to connect {@link Input}s of the Condition to {@link Output}s of other {@link Module}s.
     *
     * @param inputs map that contains the inputs for this condition.
     */
    public void setInputs(@Nullable Map<String, String> inputs) {
        this.inputs = inputs == null ? Collections.emptyMap() : Collections.unmodifiableMap(inputs);
    }

}
