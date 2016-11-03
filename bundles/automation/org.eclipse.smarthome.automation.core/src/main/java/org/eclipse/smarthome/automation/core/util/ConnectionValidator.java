/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.internal.Connection;
import org.eclipse.smarthome.automation.core.internal.RuntimeAction;
import org.eclipse.smarthome.automation.core.internal.RuntimeCondition;
import org.eclipse.smarthome.automation.core.internal.RuntimeRule;
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

    private static ModuleTypeRegistry mtRegistry;

    public static void setRegistry(ModuleTypeRegistry mtRegistry) {
        ConnectionValidator.mtRegistry = mtRegistry;
    }

    /**
     * The method validates connections between inputs and outputs of modules participated in rule. It compares data
     * types of connected inputs and outputs and throws exception when there is a lack of coincidence.
     *
     * @param r rule which must be checked
     * @throws IllegalArgumentException when validation fails.
     */
    public static void validateConnections(RuntimeRule r) {
        if (r == null) {
            throw new IllegalArgumentException("Validation of rule  is failed! Rule must not be null!");
        }

        ConnectionValidator.validateConnections(r.getTriggers(), r.getConditions(), r.getActions());
    }

    /**
     * The method validates connections between inputs and outputs of the modules participated in a rule. It checks is
     * there unconnected required inputs and compatibility of data types of connected inputs and outputs. Throws
     * exception if they are incompatible.
     *
     * @param triggers is a list with triggers of the rule whose connections have to be validated
     * @param conditions is a list with conditions of the rule whose connections have to be validated
     * @param actions is a list with actions of the rule whose connections have to be validated
     * @throws IllegalArgumentException when validation fails.
     */
    public static void validateConnections(List<Trigger> triggers, List<Condition> conditions, List<Action> actions) {
        if (!conditions.isEmpty()) {
            for (Condition condition : conditions) {
                validateConditionConnections((RuntimeCondition) condition, triggers);
            }
        }
        if (!actions.isEmpty()) {
            for (Action action : actions) {
                validateActionConnections((RuntimeAction) action, triggers, actions);
            }
        }
    }

    /**
     * The method validates connections between outputs of triggers and actions and action's inputs. It checks is there
     * unconnected required inputs and compatibility of data types of connected inputs and outputs. Throws exception if
     * they are incompatible.
     *
     * @param action is an Action module whose connections have to be validated
     * @param triggers is a list with triggers of the rule on which the action belongs
     * @param actions is a list with actions of the rule on which the action belongs
     * @throws IllegalArgumentException when validation fails.
     */
    private static void validateActionConnections(RuntimeAction action, List<Trigger> triggers, List<Action> actions) {

        ActionType type = (ActionType) mtRegistry.get(action.getTypeUID()); // get module type of the condition
        if (type == null) {
            // if module type not exists in the system - throws exception
            throw new IllegalArgumentException("Condition Type \"" + action.getTypeUID() + "\" does not exist!");
        }

        List<Input> inputs = type.getInputs(); // get inputs of the condition according to module type definition

        // gets connected inputs from the condition module and put them into map
        Set<Connection> cons = action.getConnections();
        Map<String, Connection> connectionsMap = new HashMap<String, Connection>();
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
                    checkConnection(connection, input, triggers, actions);
                }
            }
        }
    }

    /**
     * The method validates the connection between outputs of list of triggers and actions to the action's input. It
     * checks if the input is unconnected and compatibility of data types of the input and connected output. Throws
     * exception if they are incompatible.
     *
     * @param connection that should be validated
     * @param input that should be validated
     * @param triggers is a list with triggers of the rule on which the action belongs
     * @param actions is a list with actions of the rule on which the action belongs
     * @throws IllegalArgumentException when validation fails.
     */
    private static void checkConnection(Connection connection, Input input, List<Trigger> triggers,
            List<Action> actions) {
        Map<String, Action> actionsMap = new HashMap<String, Action>();
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
            checkConnection(connection, input, triggers);
        }
    }

    /**
     * The method validates connections between trigger's outputs and condition's inputs. It checks is there unconnected
     * required inputs and compatibility of data types of connected inputs and outputs. Throws exception if they are
     * incompatible.
     *
     * @param condition is a Condition module whose connections have to be validated
     * @param triggers is a list with triggers of the rule on which the condition belongs
     * @throws IllegalArgumentException when validation fails.
     */
    private static void validateConditionConnections(RuntimeCondition condition, List<Trigger> triggers) {

        ConditionType type = (ConditionType) mtRegistry.get(condition.getTypeUID()); // get module type of the condition
        if (type == null) {
            // if module type not exists in the system - throws exception
            throw new IllegalArgumentException("Condition Type \"" + condition.getTypeUID() + "\" does not exist!");
        }

        List<Input> inputs = type.getInputs(); // get inputs of the condition according to module type definition

        // gets connected inputs from the condition module and put them into map
        Set<Connection> cons = condition.getConnections();
        Map<String, Connection> connectionsMap = new HashMap<String, Connection>();
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
                    checkConnection(connection, input, triggers);
                } else if (input.isRequired()) {
                    throw new IllegalArgumentException("Required input \"" + name + "\" of the condition \""
                            + condition.getId() + "\" not connected");
                }
            }
        }
    }

    /**
     * The method validates the connection between outputs of list of triggers to the action's or condition's input. It
     * checks if the input is unconnected and compatibility of data types of the input and connected output. Throws
     * exception if they are incompatible.
     *
     * @param connection that should be validated
     * @param input that should be validated
     * @param triggers is a list with triggers of the rule on which the action belongs
     * @throws IllegalArgumentException when validation fails.
     */
    private static void checkConnection(Connection connection, Input input, List<Trigger> triggers) {

        Map<String, Trigger> triggersMap = new HashMap<String, Trigger>();
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
     * This method checks the compatibility of data types of the input and connected output. Throws
     * exception if they are incompatible.
     *
     * @param msg message should be extended with an information and thrown as exception when validation fails.
     * @param connection that should be validated
     * @param input that should be validated
     * @param outputs list with outputs of the module connected to the given input
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
                    + "\" not exists in the Module with ID \"" + connection.getOuputModuleId() + "\"");
        }
    }
}
