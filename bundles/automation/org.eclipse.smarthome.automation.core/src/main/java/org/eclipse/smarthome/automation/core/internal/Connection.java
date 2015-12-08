/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.slf4j.Logger;

/**
 * This class defines connection between {@link Input} of the current {@link Module} and {@link Output} of the external
 * one. The current module is the module containing {@link Connection} instance and the external one is the
 * module where the current is connected to. <br>
 * The input of the current module is defined by name of the {@link Input}. The {@link Output} of the external module is
 * defined by id of the module and name of the output.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class Connection {

    public static final String REF_IDENTIFIER = "$";

    private String ouputModuleId;
    private String outputName;
    private String inputName;

    /**
     * This constructor is responsible for creation of connections between modules in the rule.
     *
     * @param inputName is an unique name of the {@code Input} in scope of the {@link Module}.
     * @param ouputModuleId is an unique id of the {@code Module} in scope of the {@link Rule}.
     * @param outputName is an unique name of the {@code Output} in scope of the {@link Module}.
     */
    public Connection(String inputName, String ouputModuleId, String outputName) {
        validate("inputName", inputName);
        validate("outputName", outputName);
        this.inputName = inputName;
        this.ouputModuleId = ouputModuleId;
        this.outputName = outputName;
    }

    /**
     * This method is used to get id of external {@link Module} of this
     * connection.
     *
     * @return id of external {@link Module}
     */
    public String getOuputModuleId() {
        return ouputModuleId;
    }

    /**
     * This method is used to get output name of external {@link Module} of this
     * connection.
     *
     * @return name of {@link Output} of external {@link Module}
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * This method is used to get input name of current {@link Module} of this
     * connection.
     *
     * @return name {@link Input} of the current {@link Module}
     */
    public String getInputName() {
        return inputName;
    }

    /**
     * Compare two connection objects.
     */
    @Override
    public boolean equals(Object obj) {
        return (inputName != null && obj instanceof Connection) && inputName.equals(((Connection) obj).getInputName());

    };

    @Override
    public int hashCode() {
        return inputName.hashCode();
    };

    /**
     * This method is used to validate the connection.
     *
     * @param field serves to construct an understandable message that indicates what property of the connection is not
     *            correct.
     * @param id is the value of the specified property. It can't be empty string.
     */
    private void validate(String field, String id) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("Invalid identifier for " + field);
        }
    }

    @Override
    public String toString() {
        return "Connection " + ouputModuleId + "." + outputName + "->" + inputName;
    }

    /**
     * This method is used for collecting of Connections of {@link Module}s.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param jsonModule is a JSONObject representing the module.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Module}s creation.
     * @param log is used for logging of exceptions.
     * @return collected Connections
     */
    public static Set<Connection> getConnections(Map<String, String> inputs, Logger log) {
        Set<Connection> connections = new HashSet<Connection>(11);
        if (inputs != null) {
            for (Entry<String, String> input : inputs.entrySet()) {
                String inputName = input.getKey();
                String outputName = null;

                String output = input.getValue();
                if (output.startsWith(REF_IDENTIFIER)) {
                    outputName = output;
                    Connection connection = new Connection(inputName, null, outputName);
                    connections.add(connection);

                } else {
                    int index = output.indexOf('.');
                    if (index != -1) {
                        String outputId = output.substring(0, index);
                        outputName = output.substring(index + 1);
                        Connection connection = new Connection(inputName, outputId, outputName);
                        connections.add(connection);

                    } else {
                        log.error("Wrong format of Output : " + inputName + ": " + output);
                        continue;
                    }
                }
            }
        }
        return connections;
    }

}
