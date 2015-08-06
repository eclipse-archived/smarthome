package org.eclipse.smarthome.automation.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.provider.util.PersistableAction;
import org.eclipse.smarthome.automation.provider.util.PersistableCondition;
import org.eclipse.smarthome.automation.provider.util.PersistableTrigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

public class PersistableRule {

    public String uid;
    public String name;
    public Set<String> tags;
    public String description;
    public List<PersistableTrigger> triggers;
    public List<PersistableCondition> conditions;
    public List<PersistableAction> actions;
    public Set<ConfigDescriptionParameter> configDescriptions;
    public Map<String, ?> configurations;
    public String scopeId;

    public PersistableRule() {
    }

    public PersistableRule(Rule element) {
        uid = element.getUID();
        name = element.getName();
        description = element.getDescription();
        tags = element.getTags();
        configDescriptions = element.getConfigurationDescriptions();
        configurations = element.getConfiguration();
        scopeId = element.getScopeIdentifier();
        List<Action> actions = element.getModules(Action.class);
        this.actions = new ArrayList<PersistableAction>();
        for (Action action : actions) {
            this.actions.add(new PersistableAction(action));
        }
        List<Condition> conds = element.getModules(Condition.class);
        if (conds != null) {
            conditions = new ArrayList<PersistableCondition>();
            for (Condition condition : conds) {
                conditions.add(new PersistableCondition(condition));
            }
        }
        List<Trigger> triggers = element.getModules(Trigger.class);
        this.triggers = new ArrayList<PersistableTrigger>();
        for (Trigger trigger : triggers) {
            this.triggers.add(new PersistableTrigger(trigger));
        }
    }
}
