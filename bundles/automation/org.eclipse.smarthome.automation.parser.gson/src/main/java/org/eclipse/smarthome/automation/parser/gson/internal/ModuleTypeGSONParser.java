/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
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
        Map<String, List<? extends ModuleType>> map = createMapByType(dataObjects);
        gson.toJson(map, writer);
    }

    private void addAll(Set<ModuleType> result, List<? extends ModuleType> moduleTypes) {
        if (moduleTypes != null) {
            for (ModuleType mt : moduleTypes) {
                if (mt instanceof CompositeTriggerType) {
                    List<Trigger> children = ((CompositeTriggerType) mt).getChildren();
                    if (children != null && !children.isEmpty()) {
                        result.add(mt);
                    } else {
                        result.add(new TriggerType(mt.getUID(), mt.getConfigurationDescriptions(), mt.getLabel(),
                                mt.getDescription(), mt.getTags(), mt.getVisibility(),
                                ((TriggerType) mt).getOutputs()));
                    }

                } else if (mt instanceof CompositeConditionType) {
                    List<Condition> children = ((CompositeConditionType) mt).getChildren();
                    if (children != null && !children.isEmpty()) {
                        result.add(mt);
                    } else {
                        result.add(new ConditionType(mt.getUID(), mt.getConfigurationDescriptions(), mt.getLabel(),
                                mt.getDescription(), mt.getTags(), mt.getVisibility(),
                                ((ConditionType) mt).getInputs()));
                    }

                } else if (mt instanceof CompositeActionType) {
                    List<Action> children = ((CompositeActionType) mt).getChildren();
                    if (children != null && !children.isEmpty()) {
                        result.add(mt);
                    } else {
                        result.add(new ActionType(mt.getUID(), mt.getConfigurationDescriptions(), mt.getLabel(),
                                mt.getDescription(), mt.getTags(), mt.getVisibility(), ((ActionType) mt).getInputs(),
                                ((ActionType) mt).getOutputs()));
                    }
                }
            }
        }
    }

    private Map<String, List<? extends ModuleType>> createMapByType(Set<ModuleType> dataObjects) {
        Map<String, List<? extends ModuleType>> map = new HashMap<String, List<? extends ModuleType>>();

        List<TriggerType> triggers = new ArrayList<TriggerType>();
        List<ConditionType> conditions = new ArrayList<ConditionType>();
        List<ActionType> actions = new ArrayList<ActionType>();
        for (ModuleType moduleType : dataObjects) {
            if (moduleType instanceof TriggerType) {
                triggers.add((TriggerType) moduleType);
            } else if (moduleType instanceof ConditionType) {
                conditions.add((ConditionType) moduleType);
            } else if (moduleType instanceof ActionType) {
                actions.add((ActionType) moduleType);
            }
        }
        map.put("triggers", triggers);
        map.put("conditions", conditions);
        map.put("actions", actions);
        return map;
    }

}
