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
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.internal.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * This class contains utility methods for comparison of data types between connected inputs and outputs of modules
 * participating in a rule.
 *
 * @author Ana Dimova
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
public class ConnectionValidator {

    private static ModuleTypeManager mtManager;

    public static void setManager(ModuleTypeManager mtManager) {
        ConnectionValidator.mtManager = mtManager;
    }

    /**
     * The method validates connections between inputs and outputs of modules participated in rule. It compares data
     * types of connected inputs and outputs and throws exception when there is a lack of coincidence.
     *
     * @param r rule which must be checked
     * @throws IllegalArgumentException when validation fails.
     */
    public static void validateConnections(Rule r) {
        if (r == null) {
            throw new IllegalArgumentException("Validation of rule  is failed! Rule must not be null!");
        }

        ConnectionValidator.validateConnections(r.getTriggers(), r.getConditions(), r.getActions());
    }

    /**
     * The method validates connections between inputs and outputs of modules participated in rule. It compares data
     * types of connected inputs and outputs and throws exception when there is a lack of coincidence.
     *
     * @param triggers triggers of the rule
     * @param conditions condition of the rule
     * @param actions actions of the rule.
     * @throws IllegalArgumentException when validation fails.
     */
    public static void validateConnections(List<Trigger> triggers, List<Condition> conditions, List<Action> actions) {
        if (!conditions.isEmpty())
            for (Condition condition : conditions) {
                validateConditionConnections(condition, triggers);
            }
        if (!actions.isEmpty()) {
            for (Action action : actions) {
                validateActionConnections(action, triggers, actions);
            }
        }
    }

    /**
     * The method validates connections between outputs of triggers and actions and action's inputs. It compares data
     * types of connected inputs and outputs and throws exception when there is a lack of coincidence.
     *
     * @param action validated action.
     * @param triggers list of rule's triggers
     * @param actions list rule's actions.
     * @throws IllegalArgumentException when validation fails.
     */
    private static void validateActionConnections(Action action, List<Trigger> triggers, List<Action> actions) {
        Map<String, Connection> connectionsMap = new HashMap<String, Connection>();
        Set<Connection> cons = action.getConnections();
        if (cons == null) {
            return;
        }
        Iterator<Connection> connectionsI = cons.iterator();
        while (connectionsI.hasNext()) {
            Connection connection = connectionsI.next();
            String inputName = connection.getInputName();
            connectionsMap.put(inputName, connection);
        }
        Map<String, Trigger> triggersMap = new HashMap<String, Trigger>();
        for (Trigger trigger : triggers) {
            triggersMap.put(trigger.getId(), trigger);
        }
        Map<String, Action> actionsMap = new HashMap<String, Action>();
        for (Action a : actions) {
            actionsMap.put(a.getId(), a);
        }
        ActionType type = (ActionType) mtManager.getType(action.getTypeUID());
        if (type == null)
            throw new IllegalArgumentException("Action Type with UID \"" + action.getTypeUID() + "\" not exists!");
        Set<Input> inputs = type.getInputs();
        if (inputs != null && !inputs.isEmpty()) {
            for (Input input : inputs) {
                String inputName = input.getName();
                Connection connection = connectionsMap.get(inputName);
                if (connection == null) {
                    throw new IllegalArgumentException("Input \"" + inputName + "\" in the Action with ID \""
                            + action.getId() + "\" not connected!");
                }
                String moduleId = connection.getOuputModuleId();
                String outputName = connection.getOutputName();
                String msg = "Connection \"" + inputName + ":" + moduleId + "." + outputName
                        + "\" in the Action with ID \"" + action.getId() + "\" is invalid!";
                Trigger trigger = triggersMap.get(moduleId);
                Set<Output> outputs;
                boolean notFound = true;
                if (trigger != null) {
                    String triggerTypeUID = trigger.getTypeUID();
                    TriggerType triggerType = mtManager.getType(triggerTypeUID);
                    if (triggerType == null) {
                        throw new IllegalArgumentException(
                                msg + " Trigger Type with UID \"" + triggerTypeUID + "\" not exists!");
                    }
                    outputs = triggerType.getOutputs();
                } else {
                    Action processor = actionsMap.get(moduleId);
                    if (processor == null) {
                        throw new IllegalArgumentException(msg + " Action " + moduleId + " not exists!");
                    }
                    String processorTypeUID = processor.getTypeUID();
                    ActionType processorType = mtManager.getType(processorTypeUID);
                    if (processorType == null) {
                        throw new IllegalArgumentException(
                                msg + " Action Type with UID \"" + processorTypeUID + "\" not exists!");
                    }
                    outputs = processorType.getOutputs();
                }
                if (outputs != null && !outputs.isEmpty()) {
                    for (Output output : outputs) {
                        if (output.getName().equals(outputName)) {
                            notFound = false;
                            if (output.getType().equals(input.getType())) {
                                break;
                            } else {
                                throw new IllegalArgumentException(msg + " Incompatible types : \"" + output.getType()
                                        + "\" and \"" + input.getType() + "\".");
                            }
                        }
                    }
                }
                if (notFound)
                    throw new IllegalArgumentException(msg + " Output with name \"" + outputName
                            + "\" not exists in the Module with ID \"" + moduleId + "\"");
            }
        }
    }

    /**
     * The method validates connections between trigger's outputs and condition's inputs. It compares data types of
     * connected inputs and outputs and throws exception when there is a lack of coincidence.
     *
     * @param condition validated condition
     * @param triggers list of triggers
     * @throws IllegalArgumentException when validation is failed.
     */
    private static void validateConditionConnections(Condition condition, List<Trigger> triggers) {
        Map<String, Connection> connectionsMap = new HashMap<String, Connection>();
        Set<Connection> cons = condition.getConnections();
        if (cons == null) {
            return;
        }
        Iterator<Connection> connectionsI = cons.iterator();
        while (connectionsI.hasNext()) {
            Connection connection = connectionsI.next();
            String inputName = connection.getInputName();
            connectionsMap.put(inputName, connection);
        }
        Map<String, Trigger> triggersMap = new HashMap<String, Trigger>();
        for (Trigger trigger : triggers) {
            triggersMap.put(trigger.getId(), trigger);
        }
        ConditionType type = (ConditionType) mtManager.getType(condition.getTypeUID());
        if (type == null)
            throw new IllegalArgumentException("Condition Type \"" + condition.getTypeUID() + "\" does not exist!");
        Set<Input> inputs = type.getInputs();
        if (inputs != null && !inputs.isEmpty()) {
            for (Input input : inputs) {
                String inputName = input.getName();
                Connection connection = connectionsMap.get(inputName);
                if (connection == null) {
                    continue;
                    // throw new IllegalArgumentException("Input \"" + inputName + "\" in the Condition with ID \""
                    // + condition.getId() + "\" not connected!");
                }
                String moduleId = connection.getOuputModuleId();
                String outputName = connection.getOutputName();
                String msg = "Connection \"" + inputName + ":" + moduleId + "." + outputName
                        + "\" in the Condition with ID \"" + condition.getId() + "\" is invalid!";
                Trigger trigger = triggersMap.get(moduleId);
                if (trigger == null) {
                    throw new IllegalArgumentException(msg + " Trigger with ID \"" + moduleId + "\" not exists!");
                }
                String triggerTypeUID = trigger.getTypeUID();
                TriggerType triggerType = mtManager.getType(triggerTypeUID);
                if (triggerType == null) {
                    throw new IllegalArgumentException(
                            msg + " Trigger Type with UID \"" + triggerTypeUID + "\" not exists!");
                }
                Set<Output> outputs = triggerType.getOutputs();
                boolean notFound = true;
                if (outputs != null && !outputs.isEmpty()) {
                    for (Output output : outputs) {
                        if (output.getName().equals(outputName)) {
                            notFound = false;
                            if (output.getType().equals(input.getType())) {
                                break;
                            } else {
                                throw new IllegalArgumentException(msg + " Incompatible types : \"" + output.getType()
                                        + "\" and \"" + input.getType() + "\".");
                            }
                        }
                    }
                }
                if (notFound)
                    throw new IllegalArgumentException(msg + " Output with name \"" + outputName
                            + "\" not exists in the Trigger with ID \"" + moduleId + "\"");
            }
        }
    }

}
