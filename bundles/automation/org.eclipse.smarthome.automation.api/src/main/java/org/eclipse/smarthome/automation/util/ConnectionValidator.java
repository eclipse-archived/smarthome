/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * @author Ana Dimova
 *
 */
public class ConnectionValidator {

    /**
     * 
     * @param admin
     * @param triggers
     * @param conditions
     * @param actions
     * @throws IllegalArgumentException
     */
    public static void validateConnections(ModuleTypeRegistry admin, List<Trigger> triggers, List<Condition> conditions,
            List<Action> actions) {
        if (conditions != null && !conditions.isEmpty())
            for (Condition condition : conditions) {
                validateConditionConnections(admin, condition, triggers);
            }
        for (Action action : actions) {
            validateActionConnections(admin, action, triggers, actions);
        }
    }

    /**
     * 
     * @param admin
     * @param action
     * @param triggers
     * @param actions
     * @throws IllegalArgumentException
     */
    private static void validateActionConnections(ModuleTypeRegistry admin, Action action, List<Trigger> triggers,
            List<Action> actions) {
        Map<String, Connection> connectionsMap = new HashMap<String, Connection>();
        Iterator<Connection> connectionsI = action.getConnections().iterator();
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
        ActionType type = (ActionType) admin.get(action.getTypeUID());
        if (type == null) throw new IllegalArgumentException("Action Type with UID \"" + action.getTypeUID() + "\" not exists!");
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
                    TriggerType triggerType = admin.get(triggerTypeUID);
                    if (triggerType == null) {
                        throw new IllegalArgumentException(msg + " Trigger Type with UID \"" + triggerTypeUID
                                + "\" not exists!");
                    }
                    outputs = triggerType.getOutputs();
                } else {
                    Action processor = actionsMap.get(moduleId);
                    if (processor == null) {
                        throw new IllegalArgumentException(msg + " Action " + moduleId + " not exists!");
                    }
                    String processorTypeUID = processor.getTypeUID();
                    ActionType processorType = admin.get(processorTypeUID);
                    if (processorType == null) {
                        throw new IllegalArgumentException(msg + " Action Type with UID \"" + processorTypeUID
                                + "\" not exists!");
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
     * 
     * @param admin
     * @param condition
     * @param triggers
     * @throws IllegalArgumentException
     */
    private static void validateConditionConnections(ModuleTypeRegistry admin, Condition condition, List<Trigger> triggers) {
        Map<String, Connection> connectionsMap = new HashMap<String, Connection>();
        Iterator<Connection> connectionsI = condition.getConnections().iterator();
        while (connectionsI.hasNext()) {
            Connection connection = connectionsI.next();
            String inputName = connection.getInputName();
            connectionsMap.put(inputName, connection);
        }
        Map<String, Trigger> triggersMap = new HashMap<String, Trigger>();
        for (Trigger trigger : triggers) {
            triggersMap.put(trigger.getId(), trigger);
        }
        ConditionType type = (ConditionType) admin.get(condition.getTypeUID());
        if (type == null) throw new IllegalArgumentException("Condition Type with UID \"" + condition.getTypeUID() + "\" not exists!");
        Set<Input> inputs = type.getInputs();
        if (inputs != null && !inputs.isEmpty()) {
            for (Input input : inputs) {
                String inputName = input.getName();
                Connection connection = connectionsMap.get(inputName);
                if (connection == null) {
                    throw new IllegalArgumentException("Input \"" + inputName + "\" in the Condition with ID \""
                            + condition.getId() + "\" not connected!");
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
                TriggerType triggerType = admin.get(triggerTypeUID);
                if (triggerType == null) {
                    throw new IllegalArgumentException(msg + " Trigger Type with UID \"" + triggerTypeUID
                            + "\" not exists!");
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
