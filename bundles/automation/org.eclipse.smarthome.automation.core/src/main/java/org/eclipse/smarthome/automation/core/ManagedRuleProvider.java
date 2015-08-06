package org.eclipse.smarthome.automation.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.provider.util.PersistableAction;
import org.eclipse.smarthome.automation.provider.util.PersistableCondition;
import org.eclipse.smarthome.automation.provider.util.PersistableTrigger;
import org.eclipse.smarthome.core.common.registry.AbstractManagedProvider;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation
 */
public class ManagedRuleProvider extends AbstractManagedProvider<Rule, String, PersistableRule> {

    private RuleManager ruleManager;
    private RuleRegistryImpl registry;
    @SuppressWarnings("rawtypes")
    private ServiceTracker storageTracker;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ManagedRuleProvider(RuleManager ruleManager, final BundleContext bc) {
        this.ruleManager = ruleManager;
        storageTracker = new ServiceTracker(bc, StorageService.class.getName(), new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference reference) {
                StorageService storage = (StorageService) bc.getService(reference);
                if (storage != null) {
                    System.out.println(" *** loading all stored rules");
                    setStorageService(storage);
                    registry.providerInitCallback();
                    Collection<Rule> all = getAll();
                    for (Rule r : all) {
                        System.out.println(" *** loaded rule: " + r);
                        RuleEngine re = RuleManager.re;
                        re.setRule((RuleImpl) r);
                    }
                }
                return storage;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                unsetStorageService((StorageService) service);
            }
        });
    }

    public void open(RuleRegistryImpl registry) {
        this.registry = registry;
        storageTracker.open();
    }

    public void dispose() {
        storageTracker.close();
        storageTracker = null;
    }

    @Override
    public void add(Rule element) {
        System.out.println(" *** ManagedRuleProvider: addRule " + element);
        RuleImpl ruleWithId = ruleManager.addRule0(element, ruleManager.getScopeIdentifier());
        super.add(ruleWithId);
    }

    @Override
    public Rule remove(String key) {
        System.out.println(" *** removeRule " + key);
        Rule rule = super.remove(key);
        if (rule != null) {
            ruleManager.removeRule(key);
        }
        return rule;
    }

    @Override
    public Rule update(Rule element) {
        System.out.println(" *** updateRule " + element);
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
    protected Rule toElement(String key, PersistableRule persistableElement) {
        System.out.println(" *** toElement PersistableRule" + key);
        AutomationFactory factory = Activator.automationFactory;

        List<Action> actions = new ArrayList<Action>();
        for (PersistableAction paction : persistableElement.actions) {
            actions.add(paction.createAction(factory));
        }
        List<Trigger> triggers = new ArrayList<Trigger>();
        for (PersistableTrigger ptrigger : persistableElement.triggers) {
            triggers.add(ptrigger.createTrigger(factory));
        }
        List<Condition> conditions = new ArrayList<Condition>();
        for (PersistableCondition pcondition : persistableElement.conditions) {
            conditions.add(pcondition.createCondition(factory));
        }

        RuleImpl ruleImpl = (RuleImpl) factory.createRule(triggers, conditions, actions,
                persistableElement.configDescriptions, persistableElement.configurations);
        ruleImpl.setUID(persistableElement.uid);
        ruleImpl.setName(persistableElement.name);
        ruleImpl.setDescription(persistableElement.description);
        ruleImpl.setTags(persistableElement.tags);
        ruleImpl.setScopeIdentifier(persistableElement.scopeId);
        return ruleImpl;
    }

    @Override
    protected PersistableRule toPersistableElement(Rule element) {
        System.out.println(" *** toPersistableRule " + element);
        return new PersistableRule(element);
    }
}
