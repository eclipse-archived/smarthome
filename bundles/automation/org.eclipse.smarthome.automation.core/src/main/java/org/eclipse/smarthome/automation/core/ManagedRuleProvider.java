package org.eclipse.smarthome.automation.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleProvider;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.dto.ActionDTO;
import org.eclipse.smarthome.automation.dto.ConditionDTO;
import org.eclipse.smarthome.automation.dto.RuleDTO;
import org.eclipse.smarthome.automation.dto.TriggerDTO;
import org.eclipse.smarthome.core.common.registry.AbstractManagedProvider;
import org.eclipse.smarthome.core.storage.StorageService;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 */
public class ManagedRuleProvider extends AbstractManagedProvider<Rule, String, RuleDTO>implements RuleProvider {

    private RuleManager ruleManager;

    public ManagedRuleProvider(RuleManager ruleManager, StorageService storage) {
        this.ruleManager = ruleManager;
        setStorageService(storage);
    }

    @Override
    public void add(Rule element) {
        RuleImpl ruleWithId = ruleManager.addRule0(element, ruleManager.getScopeIdentifier());
        super.add(ruleWithId);
    }

    @Override
    public Rule remove(String key) {
        Rule rule = super.remove(key);
        if (rule != null) {
            ruleManager.removeRule(key);
        }
        return rule;
    }

    @Override
    public Rule update(Rule element) {
        if (element != null) {
            ruleManager.updateRule(element);// update memory map
            element = super.update(element);// update storage with new rule and return old rule
        }
        return element;
    }

    @Override
    protected String getKey(Rule element) {
        return element.getUID();
    }

    @Override
    protected String getStorageName() {
        return "automation_rules";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Override
    protected Rule toElement(String key, RuleDTO ruleDTO) {
        AutomationFactory factory = Activator.automationFactory;

        List<Action> actions = new ArrayList<Action>();
        for (ActionDTO paction : ruleDTO.actions) {
            actions.add(paction.createAction(factory));
        }
        List<Trigger> triggers = new ArrayList<Trigger>();
        for (TriggerDTO ptrigger : ruleDTO.triggers) {
            triggers.add(ptrigger.createTrigger(factory));
        }
        List<Condition> conditions = new ArrayList<Condition>();
        for (ConditionDTO pcondition : ruleDTO.conditions) {
            conditions.add(pcondition.createCondition(factory));
        }

        RuleImpl ruleImpl = (RuleImpl) factory.createRule(triggers, conditions, actions, ruleDTO.configDescriptions,
                ruleDTO.configurations);
        ruleImpl.setUID(ruleDTO.uid);
        ruleImpl.setName(ruleDTO.name);
        ruleImpl.setDescription(ruleDTO.description);
        ruleImpl.setTags(ruleDTO.tags);
        ruleImpl.setScopeIdentifier(ruleDTO.scopeId);
        return ruleImpl;
    }

    @Override
    protected RuleDTO toPersistableElement(Rule element) {
        return new RuleDTO(element);
    }

}
