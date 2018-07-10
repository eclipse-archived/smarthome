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
package org.eclipse.smarthome.automation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * Condition module is used into "IF" section of the {@link Rule} definition. The "IF" section defines conditions which
 * must be satisfied to continue {@link Rule} execution. Building elements of condition.
 * {@link ConfigDescriptionParameter}s and {@link Input}s are defined by {@link ConditionType}. Conditions don't have
 * {@link Output} elements.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Markus Rathgeb - Remove interface and implementation split
 */
@NonNullByDefault
public class Condition extends Module {

    private Map<String, String> inputs = Collections.emptyMap();
    private transient Set<Connection> connections = Collections.emptySet();

    private transient @Nullable ConditionHandler conditionHandler;

    public Condition() {
    }

    public Condition(Condition condition) {
        this(condition.getId(), condition.getTypeUID(), condition.getConfiguration(), condition.getInputs());
        setLabel(condition.getLabel());
        setDescription(condition.getDescription());
    }

    /**
     * Constructor of {@link Condition} module object.
     *
     * @param id id of the module.
     * @param typeUID unique module type id.
     * @param configuration configuration values of the {@link Condition} module.
     * @param inputs set of {@link Input}s used by this module.
     */
    public Condition(String id, String typeUID, Configuration configuration, Map<String, String> inputs) {
        super(id, typeUID, configuration);
        setInputs(inputs);
    }

    /**
     * This method sets the connections for this module.
     *
     * @param connections the set of connections for this condition
     */
    public void setConnections(@Nullable Set<Connection> connections) {
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
    public ConditionHandler getModuleHandler() {
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
