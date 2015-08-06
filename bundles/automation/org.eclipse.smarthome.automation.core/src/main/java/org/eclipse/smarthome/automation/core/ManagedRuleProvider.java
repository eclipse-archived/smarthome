package org.eclipse.smarthome.automation.core;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.eclipse.smarthome.core.storage.StorageService;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 */
public class ManagedRuleProvider extends DefaultAbstractManagedProvider<Rule, String> {

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

}
