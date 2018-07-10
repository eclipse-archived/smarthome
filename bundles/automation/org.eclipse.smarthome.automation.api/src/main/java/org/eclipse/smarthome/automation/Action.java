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
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * Actions are the part of "THEN" section of the {@link Rule} definition. Elements of this section are expected result
 * of {@link Rule} execution. The Action can have {@link Output} elements. These actions are used to process input data
 * as source data of other Actions. Building elements of actions ( {@link ConfigDescriptionParameter}s, {@link Input}s
 * and {@link Output}s) are defined by {@link ActionType}
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 * @author Markus Rathgeb - Remove interface and implementation split
 */
@NonNullByDefault
public class Action extends Module {

    private transient @Nullable ActionHandler actionHandler;
    private transient Set<Connection> connections = Collections.emptySet();
    private Map<String, String> inputs = Collections.emptyMap();

    // Gson
    Action() {
    }

    /**
     * Constructor of Action object.
     *
     * @param UID action unique id.
     * @param typeUID module type unique id.
     * @param configuration map of configuration values.
     * @param label the label
     * @param description description
     * @param inputs set of connections to other modules (triggers and other actions).
     */
    public Action(String UID, String typeUID, @Nullable Configuration configuration, @Nullable String label,
            @Nullable String description, @Nullable Map<String, String> inputs) {
        super(UID, typeUID, configuration, label, description);
        this.inputs = inputs == null ? Collections.emptyMap() : Collections.unmodifiableMap(inputs);
    }

    /**
     * This method sets the connections for this module.
     *
     * @param connections the set of connections for this action
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
    public ActionHandler getModuleHandler() {
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
