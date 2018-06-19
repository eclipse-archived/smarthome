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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * This class contains utility methods for comparison of data types between connected inputs and outputs of modules
 * participating in a rule.
 *
 * @author Ana Dimova - Initial contribution and API
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 * @author Benedikt Niehues - validation of connection-types respects inheriting types
 *
 */
public class ConnectionValidator {

    public static final String REF_IDENTIFIER = "$";

    /**
     * Validates connections between inputs and outputs of modules participated in rule. It compares data
     * types of connected inputs and outputs and throws exception when there is a lack of coincidence.
     *
     * @param r rule which must be checked
     * @throws IllegalArgumentException when validation fails.
     */
    public static void validateConnections(ModuleTypeRegistry mtRegistry, Rule r) {
        if (r == null) {
            throw new IllegalArgumentException("Validation of rule  is failed! Rule must not be null!");
        }
        validateConnections(mtRegistry, r.getTriggers(), r.getConditions(), r.getActions());
    }

    /**
     * Validates connections between inputs and outputs of the modules participated in a rule. It checks is
     * there unconnected required inputs and compatibility of data types of connected inputs and outputs. Throws
     * exception if they are incompatible.
     *
     * @param triggers   is a list with triggers of the rule whose connections have to be validated
     * @param conditions is a list with conditions of the rule whose connections have to be validated
     * @param actions    is a list with actions of the rule whose connections have to be validated
     * @throws IllegalArgumentException when validation fails.
     */
    public static void validateConnections(ModuleTypeRegistry mtRegistry,
            @NonNull List<? extends @NonNull Trigger> triggers, @NonNull List<? extends @NonNull Condition> conditions,
            @NonNull List<? extends @NonNull Action> actions) {
        if (!triggers.isEmpty()) {
            for (Condition condition : conditions) {
                validateConditionConnections(mtRegistry, condition, triggers);
            }
        }
        if (!triggers.isEmpty()) {
            for (Action action : actions) {
                validateActionConnections(mtRegistry, action, triggers, actions);
            }
        }
    }

    /**
     * Validates connections between outputs of triggers and actions and action's inputs. It checks is there
     * unconnected required inputs and compatibility of data types of connected inputs and outputs. Throws exception if
     * they are incompatible.
     *
     * @param action   is an Action module whose connections have to be validated
     * @param triggers is a list with triggers of the rule on which the action belongs
     * @param actions  is a list with actions of the rule on which the action belongs
     * @throws IllegalArgumentException when validation fails.
     */
    private static void validateActionConnections(ModuleTypeRegistry mtRegistry, Action action,
            @NonNull List<? extends @NonNull Trigger> triggers, @NonNull List<? extends @NonNull Action> actions) {

        ActionType type = (ActionType) mtRegistry.get(action.getTypeUID()); // get module type of the condition
        if (type == null) {
            // if module type not exists in the system - throws exception
            throw new IllegalArgumentException("Action Type \"" + action.getTypeUID() + "\" does not exist!");
        }

        List<Input> inputs = type.getInputs(); // get inputs of the condition according to module type definition

        // gets connected inputs from the condition module and put them into map
        Set<Connection> cons = getConnections(action.getInputs());
        Map<String, Connection> connectionsMap = new HashMap<>();
        Iterator<Connection> connectionsI = cons.iterator();
        while (connectionsI.hasNext()) {
            Connection connection = connectionsI.next();
            String inputName = connection.getInputName();
            connectionsMap.put(inputName, connection);
        }

        // checks is there unconnected required inputs
        if (inputs != null && !inputs.isEmpty()) {
            for (Input input : inputs) {
                String name = input.getName();
                Connection connection = connectionsMap.get(name);
                if (connection == null && input.isRequired()) {
                    throw new IllegalArgumentException("Required input \"" + name + "\" of the condition \""
                            + action.getId() + "\" not connected");
                } else if (connection != null) {
                    checkConnection(mtRegistry, connection, input, triggers, actions);
                }
            }
        }
    }

    /**
     * Validates the connection between outputs of list of triggers and actions to the action's input. It
     * checks if the input is unconnected and compatibility of data types of the input and connected output. Throws
     * exception if they are incompatible.
     *
     * @param connection that should be validated
     * @param input      that should be validated
     * @param triggers   is a list with triggers of the rule on which the action belongs
     * @param actions    is a list with actions of the rule on which the action belongs
     * @throws IllegalArgumentException when validation fails.
     */
    private static void checkConnection(ModuleTypeRegistry mtRegistry, Connection connection, Input input,
            @NonNull List<? extends @NonNull Trigger> triggers, @NonNull List<? extends @NonNull Action> actions) {
        Map<String, Action> actionsMap = new HashMap<>();
        for (Action a : actions) {
            actionsMap.put(a.getId(), a);
        }
        String moduleId = connection.getOuputModuleId();
        Action action = actionsMap.get(moduleId);
        String msg = " Invalid Connection \"" + connection.getInputName() + "\" : ";
        if (moduleId != null && action != null) {
            String typeUID = action.getTypeUID();
            ActionType actionType = (ActionType) mtRegistry.get(typeUID);
            if (actionType == null) {
                throw new IllegalArgumentException(msg + " Action Type with UID \"" + typeUID + "\" does not exist!");
            }
            checkCompatibility(msg, connection, input, actionType.getOutputs());
        } else {
            checkConnection(mtRegistry, connection, input, triggers);
        }
    }

    /**
     * Validates connections between trigger's outputs and condition's inputs. It checks is there unconnected
     * required inputs and compatibility of data types of connected inputs and outputs. Throws exception if they are
     * incompatible.
     *
     * @param condition is a Condition module whose connections have to be validated
     * @param triggers  is a list with triggers of the rule on which the condition belongs
     * @throws IllegalArgumentException when validation fails.
     */
    private static void validateConditionConnections(ModuleTypeRegistry mtRegistry, @NonNull Condition condition,
            @NonNull List<? extends @NonNull Trigger> triggers) {

        ConditionType type = (ConditionType) mtRegistry.get(condition.getTypeUID()); // get module type of the condition
        if (type == null) {
            // if module type not exists in the system - throws exception
            throw new IllegalArgumentException("Condition Type \"" + condition.getTypeUID() + "\" does not exist!");
        }

        List<Input> inputs = type.getInputs(); // get inputs of the condition according to module type definition

        // gets connected inputs from the condition module and put them into map
        Set<Connection> cons = getConnections(condition.getInputs());
        Map<String, Connection> connectionsMap = new HashMap<>();
        Iterator<Connection> connectionsI = cons.iterator();
        while (connectionsI.hasNext()) {
            Connection connection = connectionsI.next();
            String inputName = connection.getInputName();
            connectionsMap.put(inputName, connection);
        }

        // checks is there unconnected required inputs
        if (inputs != null && !inputs.isEmpty()) {
            for (Input input : inputs) {
                String name = input.getName();
                Connection connection = connectionsMap.get(name);
                if (connection != null) {
                    checkConnection(mtRegistry, connection, input, triggers);
                } else if (input.isRequired()) {
                    throw new IllegalArgumentException("Required input \"" + name + "\" of the condition \""
                            + condition.getId() + "\" not connected");
                }
            }
        }
    }

    /**
     * Validates the connection between outputs of list of triggers to the action's or condition's input. It
     * checks if the input is unconnected and compatibility of data types of the input and connected output. Throws
     * exception if they are incompatible.
     *
     * @param connection that should be validated
     * @param input      that should be validated
     * @param triggers   is a list with triggers of the rule on which the action belongs
     * @throws IllegalArgumentException when validation fails.
     */
    private static void checkConnection(ModuleTypeRegistry mtRegistry, Connection connection, Input input,
            @NonNull List<? extends @NonNull Trigger> triggers) {

        Map<String, Trigger> triggersMap = new HashMap<>();
        for (Trigger trigger : triggers) {
            triggersMap.put(trigger.getId(), trigger);
        }
        String moduleId = connection.getOuputModuleId();
        String msg = " Invalid Connection \"" + connection.getInputName() + "\" : ";
        if (moduleId != null) {
            Trigger trigger = triggersMap.get(moduleId);
            if (trigger == null) {
                throw new IllegalArgumentException(msg + " Trigger with ID \"" + moduleId + "\" does not exist!");
            }
            String triggerTypeUID = trigger.getTypeUID();
            TriggerType triggerType = (TriggerType) mtRegistry.get(triggerTypeUID);
            if (triggerType == null) {
                throw new IllegalArgumentException(
                        msg + " Trigger Type with UID \"" + triggerTypeUID + "\" does not exist!");
            }
            checkCompatibility(msg, connection, input, triggerType.getOutputs());
        }
    }

    /**
     * Checks the compatibility of data types of the input and connected output. Throws
     * exception if they are incompatible.
     *
     * @param msg        message should be extended with an information and thrown as exception when validation fails.
     * @param connection that should be validated
     * @param input      that should be validated
     * @param outputs    list with outputs of the module connected to the given input
     * @throws IllegalArgumentException when validation fails.
     */
    private static void checkCompatibility(String msg, Connection connection, Input input, List<Output> outputs) {
        String outputName = connection.getOutputName();
        if (outputs != null && !outputs.isEmpty()) {
            for (Output output : outputs) {
                if (output.getName().equals(outputName)) {
                    if (input.getType().equals("*")) {
                        return;
                    } else {
                        try {
                            Class<?> outputType = Class.forName(output.getType());
                            Class<?> inputType = Class.forName(input.getType());
                            if (inputType.isAssignableFrom(outputType)) {
                                return;
                            } else {
                                throw new IllegalArgumentException(msg + " Incompatible types : \"" + output.getType()
                                        + "\" and \"" + input.getType() + "\".");
                            }
                        } catch (ClassNotFoundException e) {
                            if (output.getType().equals(input.getType())) {
                                return;
                            } else {
                                throw new IllegalArgumentException(msg + " Incompatible types : \"" + output.getType()
                                        + "\" and \"" + input.getType() + "\".");
                            }
                        }
                    }
                }
            }
            throw new IllegalArgumentException(msg + " Output with name \"" + outputName
                    + "\" not exists in the ModuleImpl with ID \"" + connection.getOuputModuleId() + "\"");
        }
    }

    /**
     * Collects the {@link Connection}s of {@link Module}s.
     *
     * @param type       specifies the type of the automation object - module type, rule or rule template.
     * @param UID        is the unique identifier of the automation object - module type, rule or rule template.
     * @param jsonModule is a JSONObject representing the module.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Module}s creation.
     * @param log        is used for logging of exceptions.
     * @return collected Connections
     */
    public static Set<Connection> getConnections(Map<String, String> inputs) {
        Set<Connection> connections = new HashSet<>();
        for (Entry<String, String> input : inputs.entrySet()) {
            String inputName = input.getKey();
            String output = input.getValue();
            String outputName = output;
            String outputId = null;
            if (!output.startsWith(REF_IDENTIFIER)) {
                int index = output.indexOf('.');
                if (index != -1) {
                    outputId = output.substring(0, index);
                    outputName = output.substring(index + 1);
                } else {
                    throw new IllegalArgumentException("Wrong format of Output : " + inputName + " : " + output);
                }
            }
            Connection connection = new Connection(inputName, outputId, outputName);
            connections.add(connection);
        }
        return connections;
    }
}
