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
import java.util.Iterator;
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
public class ModuleTypeRegistryImpl extends AbstractRegistry<ModuleType, String, Provider<ModuleType>>
        implements ModuleTypeRegistry {

    private Set<ModuleTypeProvider> providers = new HashSet<ModuleTypeProvider>();

    public ModuleTypeRegistryImpl() {
        super(null);
    }

    protected void addModuleTypeProvider(ModuleTypeProvider moduleTypeProvider) {
        providers.add(moduleTypeProvider);
    }

    protected void removeModuleTypeProvider(ModuleTypeProvider moduleTypeProvider) {
        providers.remove(moduleTypeProvider);
    }

    @Override
    public ModuleType get(String typeUID) {
        return get(typeUID, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> T get(String moduleTypeUID, Locale locale) {
        for (ModuleTypeProvider provider : providers) {
            ModuleType mType = provider.getModuleType(moduleTypeUID, locale);
            if (mType != null) {
                return (T) createCopy(mType);
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
        Collection<ModuleType> moduleTypes = null;
        for (ModuleTypeProvider provider : providers) {
            moduleTypes = provider.getModuleTypes(locale);
            if (moduleTypes != null) {
                for (Iterator<ModuleType> it = moduleTypes.iterator(); it.hasNext();) {
                    ModuleType mt = it.next();
                    if (moduleTypeTag != null) {
                        Collection<String> tags = mt.getTags();
                        if (tags != null && tags.contains(moduleTypeTag)) {
                            result.add((T) createCopy(mt));
                        }
                    } else {
                        result.add((T) createCopy(mt));
                    }
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
        Collection<ModuleType> moduleTypes = null;
        for (ModuleTypeProvider provider : providers) {
            moduleTypes = provider.getModuleTypes(locale);
            if (moduleTypes != null) {
                for (Iterator<ModuleType> it = moduleTypes.iterator(); it.hasNext();) {
                    ModuleType mt = it.next();
                    if (tagSet != null) {
                        Collection<String> mtTags = mt.getTags();
                        if (mtTags.containsAll(tagSet)) {
                            result.add((T) createCopy(mt));
                        }
                    } else {
                        result.add((T) createCopy(mt));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Collection<TriggerType> getTriggers(Locale locale) {
        return getAll(TriggerType.class, locale);
    }

    @Override
    public Collection<TriggerType> getTriggers() {
        return getAll(TriggerType.class);
    }

    @Override
    public Collection<ConditionType> getConditions() {
        return getAll(ConditionType.class);
    }

    @Override
    public Collection<ConditionType> getConditions(Locale locale) {
        return getAll(ConditionType.class, locale);
    }

    @Override
    public Collection<ActionType> getActions() {
        return getAll(ActionType.class);
    }

    @Override
    public Collection<ActionType> getActions(Locale locale) {
        return getAll(ActionType.class, locale);
    }

    private <T extends ModuleType> Collection<T> getAll(Class<T> classModuleType) {
        return getAll(classModuleType, null);
    }

    @SuppressWarnings("unchecked")
    private <T extends ModuleType> Collection<T> getAll(Class<T> moduleType, Locale locale) {
        Collection<T> result = new ArrayList<T>(20);
        Collection<ModuleType> moduleTypes = null;
        for (ModuleTypeProvider provider : providers) {
            moduleTypes = provider.getModuleTypes(locale);
            if (moduleTypes != null) {
                for (Iterator<ModuleType> it = moduleTypes.iterator(); it.hasNext();) {
                    ModuleType mt = it.next();
                    if (moduleType != null) {
                        if (moduleType.isInstance(mt)) {
                            result.add((T) createCopy(mt));
                        }
                    } else {
                        result.add((T) createCopy(mt));
                    }
                }
            }
        }
        return result;
    }

    private ModuleType createCopy(ModuleType mType) {
        if (mType == null) {
            return null;
        }
        if (mType instanceof CompositeTriggerType) {
            CompositeTriggerType m = (CompositeTriggerType) mType;
            return new CompositeTriggerType(m.getUID(), m.getConfigurationDescriptions(), m.getLabel(),
                    m.getDescription(), m.getTags(), m.getVisibility(), m.getOutputs(), copyTriggers(m.getChildren()));

        }
        if (mType instanceof TriggerType) {
            TriggerType m = (TriggerType) mType;
            return new TriggerType(m.getUID(), m.getConfigurationDescriptions(), m.getLabel(), m.getDescription(),
                    m.getTags(), m.getVisibility(), m.getOutputs());

        }
        if (mType instanceof CompositeConditionType) {
            CompositeConditionType m = (CompositeConditionType) mType;
            return new CompositeConditionType(m.getUID(), m.getConfigurationDescriptions(), m.getLabel(),
                    m.getDescription(), m.getTags(), m.getVisibility(), m.getInputs(), copyConditions(m.getChildren()));

        }
        if (mType instanceof ConditionType) {
            ConditionType m = (ConditionType) mType;
            return new ConditionType(m.getUID(), m.getConfigurationDescriptions(), m.getLabel(), m.getDescription(),
                    m.getTags(), m.getVisibility(), m.getInputs());

        }
        if (mType instanceof CompositeActionType) {
            CompositeActionType m = (CompositeActionType) mType;
            return new CompositeActionType(m.getUID(), m.getConfigurationDescriptions(), m.getLabel(),
                    m.getDescription(), m.getTags(), m.getVisibility(), m.getInputs(), m.getOutputs(),
                    copyActions(m.getChildren()));

        }
        if (mType instanceof ActionType) {
            ActionType m = (ActionType) mType;
            return new ActionType(m.getUID(), m.getConfigurationDescriptions(), m.getLabel(), m.getDescription(),
                    m.getTags(), m.getVisibility(), m.getInputs(), m.getOutputs());

        } else {
            throw new IllegalArgumentException("Invalid template type:" + mType);
        }
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
