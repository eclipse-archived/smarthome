/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.Provider;

/**
 * The implementation of {@link ModuleTypeRegistry} that is registered as a service.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 */
public class ModuleTypeRegistryImpl extends AbstractRegistry<ModuleType, String, ModuleTypeProvider>
        implements ModuleTypeRegistry {

    public ModuleTypeRegistryImpl() {
        super(ModuleTypeProvider.class);
    }

    @Override
    protected void addProvider(Provider<ModuleType> provider) {
        if (provider instanceof ModuleTypeProvider) {
            super.addProvider(provider);
        }
    }

    @Override
    public ModuleType get(String typeUID) {
        return get(typeUID, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ModuleType> T get(String moduleTypeUID, Locale locale) {
        for (Provider<ModuleType> provider : elementMap.keySet()) {
            for (ModuleType mType : elementMap.get(provider)) {
                if (mType.getUID().equals(moduleTypeUID)) {
                    ModuleType mt = locale == null ? mType
                            : ((ModuleTypeProvider) provider).getModuleType(mType.getUID(), locale);
                    return (T) createCopy(mt);
                }
            }
        }
        return null;
    }

    @Override
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag) {
        return getByTag(moduleTypeTag, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag, Locale locale) {
        Collection<T> result = new ArrayList<T>(20);
        for (Provider<ModuleType> provider : elementMap.keySet()) {
            for (ModuleType mType : elementMap.get(provider)) {
                ModuleType mt = locale == null ? mType
                        : ((ModuleTypeProvider) provider).getModuleType(mType.getUID(), locale);
                Collection<String> tags = mt.getTags();
                if (moduleTypeTag == null) {
                    result.add((T) createCopy(mt));
                } else if (tags != null && tags.contains(moduleTypeTag)) {
                    result.add((T) createCopy(mt));
                }
            }
        }
        return result;
    }

    @Override
    public <T extends ModuleType> Collection<T> getByTags(String... tags) {
        return getByTags(null, tags);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ModuleType> Collection<T> getByTags(Locale locale, String... tags) {
        Set<String> tagSet = tags != null ? new HashSet<String>(Arrays.asList(tags)) : null;
        Collection<T> result = new ArrayList<T>(20);
        for (Provider<ModuleType> provider : elementMap.keySet()) {
            for (ModuleType mType : elementMap.get(provider)) {
                ModuleType mt = locale == null ? mType
                        : ((ModuleTypeProvider) provider).getModuleType(mType.getUID(), locale);
                if (tagSet == null) {
                    result.add((T) createCopy(mt));
                } else if (mt.getTags().containsAll(tagSet)) {
                    result.add((T) createCopy(mt));
                }
            }
        }
        return result;
    }

    @Override
    public Collection<TriggerType> getTriggers(Locale locale, String... tags) {
        Collection<ModuleType> moduleTypes = getByTags(locale, tags);
        Collection<TriggerType> triggerTypes = new ArrayList<TriggerType>();
        for (ModuleType mt : moduleTypes) {
            if (mt instanceof TriggerType) {
                triggerTypes.add((TriggerType) mt);
            }
        }
        return triggerTypes;
    }

    @Override
    public Collection<TriggerType> getTriggers(String... tags) {
        Collection<ModuleType> moduleTypes = getByTags(tags);
        Collection<TriggerType> triggerTypes = new ArrayList<TriggerType>();
        for (ModuleType mt : moduleTypes) {
            if (mt instanceof TriggerType) {
                triggerTypes.add((TriggerType) mt);
            }
        }
        return triggerTypes;
    }

    @Override
    public Collection<ConditionType> getConditions(String... tags) {
        Collection<ModuleType> moduleTypes = getByTags(tags);
        Collection<ConditionType> conditionTypes = new ArrayList<ConditionType>();
        for (ModuleType mt : moduleTypes) {
            if (mt instanceof ConditionType) {
                conditionTypes.add((ConditionType) mt);
            }
        }
        return conditionTypes;
    }

    @Override
    public Collection<ConditionType> getConditions(Locale locale, String... tags) {
        Collection<ModuleType> moduleTypes = getByTags(locale, tags);
        Collection<ConditionType> conditionTypes = new ArrayList<ConditionType>();
        for (ModuleType mt : moduleTypes) {
            if (mt instanceof ConditionType) {
                conditionTypes.add((ConditionType) mt);
            }
        }
        return conditionTypes;
    }

    @Override
    public Collection<ActionType> getActions(String... tags) {
        Collection<ModuleType> moduleTypes = getByTags(tags);
        Collection<ActionType> actionTypes = new ArrayList<ActionType>();
        for (ModuleType mt : moduleTypes) {
            if (mt instanceof ActionType) {
                actionTypes.add((ActionType) mt);
            }
        }
        return actionTypes;
    }

    @Override
    public Collection<ActionType> getActions(Locale locale, String... tags) {
        Collection<ModuleType> moduleTypes = getByTags(locale, tags);
        Collection<ActionType> actionTypes = new ArrayList<ActionType>();
        for (ModuleType mt : moduleTypes) {
            if (mt instanceof ActionType) {
                actionTypes.add((ActionType) mt);
            }
        }
        return actionTypes;
    }

    private ModuleType createCopy(ModuleType mType) {
        if (mType == null) {
            return null;
        }

        ModuleType result;
        if (mType instanceof CompositeTriggerType) {
            CompositeTriggerType m = (CompositeTriggerType) mType;
            result = new CompositeTriggerType(mType.getUID(), mType.getConfigurationDescriptions(), mType.getLabel(),
                    mType.getDescription(), mType.getTags(), mType.getVisibility(), m.getOutputs(),
                    copyTriggers(m.getChildren()));

        } else if (mType instanceof TriggerType) {
            TriggerType m = (TriggerType) mType;
            result = new TriggerType(mType.getUID(), mType.getConfigurationDescriptions(), mType.getLabel(),
                    mType.getDescription(), mType.getTags(), mType.getVisibility(), m.getOutputs());

        } else if (mType instanceof CompositeConditionType) {
            CompositeConditionType m = (CompositeConditionType) mType;
            result = new CompositeConditionType(mType.getUID(), mType.getConfigurationDescriptions(), mType.getLabel(),
                    mType.getDescription(), mType.getTags(), mType.getVisibility(), m.getInputs(),
                    copyConditions(m.getChildren()));

        } else if (mType instanceof ConditionType) {
            ConditionType m = (ConditionType) mType;
            result = new ConditionType(mType.getUID(), mType.getConfigurationDescriptions(), mType.getLabel(),
                    mType.getDescription(), mType.getTags(), mType.getVisibility(), m.getInputs());

        } else if (mType instanceof CompositeActionType) {
            CompositeActionType m = (CompositeActionType) mType;
            result = new CompositeActionType(mType.getUID(), mType.getConfigurationDescriptions(), mType.getLabel(),
                    mType.getDescription(), mType.getTags(), mType.getVisibility(), m.getInputs(), m.getOutputs(),
                    copyActions(m.getChildren()));

        } else if (mType instanceof ActionType) {
            ActionType m = (ActionType) mType;
            result = new ActionType(mType.getUID(), mType.getConfigurationDescriptions(), mType.getLabel(),
                    mType.getDescription(), mType.getTags(), mType.getVisibility(), m.getInputs(), m.getOutputs());

        } else {
            throw new IllegalArgumentException("Invalid template type:" + mType);
        }
        return result;
    }

    private static List<Trigger> copyTriggers(List<Trigger> triggers) {
        List<Trigger> res = new ArrayList<Trigger>(11);
        if (triggers != null) {
            for (Trigger t : triggers) {
                Configuration c = new Configuration();
                c.setProperties(t.getConfiguration().getProperties());
                Trigger trigger = new Trigger(t.getId(), t.getTypeUID(), c);
                trigger.setLabel(trigger.getLabel());
                trigger.setDescription(trigger.getDescription());
                res.add(trigger);
            }
        }
        return res;
    }

    private static List<Condition> copyConditions(List<Condition> conditions) {
        List<Condition> res = new ArrayList<Condition>(11);
        if (conditions != null) {
            for (Condition c : conditions) {
                Configuration conf = new Configuration();
                conf.setProperties(c.getConfiguration().getProperties());
                Condition condition = new Condition(c.getId(), c.getTypeUID(), conf,
                        new HashMap<String, String>(c.getInputs()));
                condition.setLabel(condition.getLabel());
                condition.setDescription(condition.getDescription());
                res.add(condition);
            }
        }
        return res;
    }

    private static List<Action> copyActions(List<Action> actions) {
        List<Action> res = new ArrayList<Action>();
        if (actions != null) {
            for (Action a : actions) {
                Configuration c = new Configuration();
                c.setProperties(a.getConfiguration().getProperties());
                Action action = new Action(a.getId(), a.getTypeUID(), c, a.getInputs());
                action.setLabel(a.getLabel());
                action.setDescription(a.getDescription());
                res.add(action);
            }
        }
        return res;
    }

}
