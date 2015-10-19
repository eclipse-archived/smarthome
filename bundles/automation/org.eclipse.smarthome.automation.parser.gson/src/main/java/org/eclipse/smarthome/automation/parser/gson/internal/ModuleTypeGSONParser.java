/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * This class can parse and serialize sets of {@link ModuleType}.
 *
 * @author Kai Kreuzer - Initial Contribution
 *
 */
public class ModuleTypeGSONParser extends AbstractGSONParser<ModuleType> {

    public ModuleTypeGSONParser() {
    }

    @Override
    public Set<ModuleType> parse(InputStreamReader reader) throws ParsingException {
        try {
            ModuleTypeParsingContainer mtContainer = gson.fromJson(reader, ModuleTypeParsingContainer.class);
            Set<ModuleType> result = new HashSet<ModuleType>();
            addAll(result, mtContainer.triggers);
            addAll(result, mtContainer.conditions);
            addAll(result, mtContainer.actions);
            return result;
        } catch (Exception e) {
            throw new ParsingException(new ParsingNestedException(ParsingNestedException.MODULE_TYPE, null, e));
        }
    }

    @Override
    public void serialize(Set<ModuleType> dataObjects, OutputStreamWriter writer) throws Exception {
        Map<String, Map<String, ? extends ModuleType>> map = createMapByType(dataObjects);
        gson.toJson(map, writer);
    }

    private void addAll(Set<ModuleType> result, Map<String, ? extends ModuleType> moduleTypes) {
        if (moduleTypes != null) {
            for (Entry<String, ? extends ModuleType> entry : moduleTypes.entrySet()) {
                if (entry.getValue() instanceof CompositeTriggerType) {
                    CompositeTriggerType triggerType = (CompositeTriggerType) entry.getValue();
                    result.add(new CompositeTriggerType(entry.getKey(), triggerType.getConfigurationDescription(),
                            triggerType.getLabel(), triggerType.getDescription(), triggerType.getTags(),
                            triggerType.getVisibility(), triggerType.getOutputs(), triggerType.getModules()));
                } else if (entry.getValue() instanceof TriggerType) {
                    TriggerType triggerType = (TriggerType) entry.getValue();
                    result.add(new TriggerType(entry.getKey(), triggerType.getConfigurationDescription(),
                            triggerType.getLabel(), triggerType.getDescription(), triggerType.getTags(),
                            triggerType.getVisibility(), triggerType.getOutputs()));
                } else if (entry.getValue() instanceof CompositeConditionType) {
                    CompositeConditionType conditionType = (CompositeConditionType) entry.getValue();
                    result.add(new CompositeConditionType(entry.getKey(), conditionType.getConfigurationDescription(),
                            conditionType.getLabel(), conditionType.getDescription(), conditionType.getTags(),
                            conditionType.getVisibility(), conditionType.getInputs(), conditionType.getModules()));
                } else if (entry.getValue() instanceof ConditionType) {
                    ConditionType conditionType = (ConditionType) entry.getValue();
                    result.add(new ConditionType(entry.getKey(), conditionType.getConfigurationDescription(),
                            conditionType.getLabel(), conditionType.getDescription(), conditionType.getTags(),
                            conditionType.getVisibility(), conditionType.getInputs()));
                } else if (entry.getValue() instanceof CompositeActionType) {
                    CompositeActionType actionType = (CompositeActionType) entry.getValue();
                    result.add(new CompositeActionType(entry.getKey(), actionType.getConfigurationDescription(),
                            actionType.getLabel(), actionType.getDescription(), actionType.getTags(),
                            actionType.getVisibility(), actionType.getInputs(), actionType.getOutputs(),
                            actionType.getModules()));
                } else if (entry.getValue() instanceof ActionType) {
                    ActionType actionType = (ActionType) entry.getValue();
                    result.add(new ActionType(entry.getKey(), actionType.getConfigurationDescription(),
                            actionType.getLabel(), actionType.getDescription(), actionType.getTags(),
                            actionType.getVisibility(), actionType.getInputs(), actionType.getOutputs()));
                }
            }
        }

    }

    private Map<String, Map<String, ? extends ModuleType>> createMapByType(Set<ModuleType> dataObjects) {
        Map<String, Map<String, ? extends ModuleType>> map = new HashMap<String, Map<String, ? extends ModuleType>>();

        HashMap<String, TriggerType> triggers = new HashMap<String, TriggerType>();
        HashMap<String, ConditionType> conditions = new HashMap<String, ConditionType>();
        HashMap<String, ActionType> actions = new HashMap<String, ActionType>();
        for (ModuleType moduleType : dataObjects) {
            if (moduleType instanceof TriggerType) {
                triggers.put(moduleType.getUID(), (TriggerType) moduleType);
            } else if (moduleType instanceof ConditionType) {
                conditions.put(moduleType.getUID(), (ConditionType) moduleType);
            } else if (moduleType instanceof ActionType) {
                actions.put(moduleType.getUID(), (ActionType) moduleType);
            }
        }
        map.put("triggers", triggers);
        map.put("conditions", triggers);
        map.put("actions", triggers);
        return map;
    }
}
