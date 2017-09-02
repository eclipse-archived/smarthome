/**
* Copyright (c) 2015, 2017 by Bosch Software Innovations and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*/
package org.eclipse.smarthome.automation.core.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleProvider;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.StatusInfoCallback;
import org.eclipse.smarthome.automation.core.internal.composite.CompositeModuleHandlerFactory;
import org.eclipse.smarthome.automation.core.internal.template.RuleTemplateRegistry;
import org.eclipse.smarthome.automation.events.RuleEventFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main implementation of the {@link RuleRegistry}, which is registered as a service.
 * The {@link RuleRegistryImpl} provides basic functionality for managing {@link Rule}s.
 * It can be used to
 * <ul>
 * <li>Add Rules with the {@link #add(Rule)}, {@link #added(Provider, Rule)}, {@link #addProvider(RuleProvider)}
 * methods.</li>
 * <li>Get the existing rules with the {@link #get(String)}, {@link #getAll()}, {@link #getByTag(String)},
 * {@link #getByTags(String[])} methods.</li>
 * <li>Update the existing rules with the {@link #update(Rule)}, {@link #updated(Provider, Rule, Rule)} methods.</li>
 * <li>Remove Rules with the {@link #remove(String)} method.</li>
 * </ul>
 * <p>
 * This class also persists the rules into the {@link StorageService} service and restores
 * them when the system is restarted.
 * <p>
 * The {@link RuleRegistry} manages the state (<b>enabled</b> or <b>disabled</b>) of the Rules:
 * <ul>
 * <li>A newly added Rule is always <b>enabled</b>.</li>
 * <li>To check a Rule's state, use the {@link #isEnabled(String)} method.</li>
 * <li>To change a Rule's state, use the {@link #setEnabled(String, boolean)} method.</li>
 * </ul>
 * <p>
 * The {@link RuleRegistry} manages the status of the Rules:
 * <ul>
 * <li>To check a Rule's status info, use the {@link #getStatusInfo(String)} method.</li>
 * <li>The status of a newly added Rule, or a Rule enabled with {@link #setEnabled(String, boolean)}, or an updated
 * Rule, is first set to {@link RuleStatus#UNINITIALIZED}.</li>
 * <li>After a Rule is added or enabled, or updated, a verification procedure is initiated. If the verification of the
 * modules IDs, connections between modules and configuration values of the modules is successful, and the module
 * handlers are correctly set, the status is set to {@link RuleStatus#IDLE}.</li>
 * <li>If some of the module handlers disappear, the Rule will become {@link RuleStatus#UNINITIALIZED} again.</li>
 * <li>If one of the Rule's Triggers is triggered, the Rule becomes {@link RuleStatus#RUNNING}.
 * When the execution is complete, it will become {@link RuleStatus#IDLE} again.</li>
 * <li>If a Rule is disabled with {@link #setEnabled(String, boolean)}, it's status is set to
 * {@link RuleStatus#DISABLED}.</li>
 * </ul>
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation & updating rules from providers
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation and other fixes
 * @author Benedikt Niehues - added events for rules
 * @author Victor Toni - return only copies of {@link Rule}s
 */
public class RuleRegistryImpl extends AbstractRegistry<Rule, String, RuleProvider>
        implements RuleRegistry, StatusInfoCallback, RegistryChangeListener<RuleTemplate> {

    private static final String DISABLED_RULE_STORAGE = "automation_rules_disabled";
    private static final String SOURCE = RuleRegistryImpl.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(RuleRegistryImpl.class.getName());

    private RuleEngine ruleEngine = new RuleEngine();
    private Storage<Boolean> disabledRulesStorage;
    private ModuleTypeRegistry moduleTypeRegistry;
    private RuleTemplateRegistry templateRegistry;

    /**
     * {@link Map} of template UIDs to rules where these templates participated.
     */
    private Map<String, Set<String>> mapTemplateToRules = new HashMap<String, Set<String>>();

    public RuleRegistryImpl() {
        super(RuleProvider.class);
    }

    /**
     * Activates this component. Called from DS.
     *
     * @param componentContext this component context.
     */

    protected void activate(BundleContext bundleContext, Map<String, Object> properties) throws Exception {
        ruleEngine.setCompositeModuleHandlerFactory(
                new CompositeModuleHandlerFactory(bundleContext, moduleTypeRegistry, ruleEngine));
        ruleEngine.setStatusInfoCallback(this);
        modified(properties);
        super.activate(bundleContext);
    }

    protected void modified(Map<String, Object> config) {
        ruleEngine.scheduleRulesConfigurationUpdated(config);
    }

    /**
     * Deactivates this component. Called from DS.
     */
    @Override
    protected void deactivate() {
        super.deactivate();
        ruleEngine.dispose();
    }

    /**
     * Bind the {@link ModuleTypeRegistry} service - called from DS.
     *
     * @param moduleTypeRegistry moduleTypeRegistry service.
     */
    protected void setModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        this.moduleTypeRegistry = moduleTypeRegistry;
        ruleEngine.setModuleTypeRegistry(moduleTypeRegistry);
    }

    /**
     * Unbind the {@link ModuleTypeRegistry} service - called from DS.
     *
     * @param moduleTypeRegistry moduleTypeRegistry service.
     */
    protected void unsetModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        this.moduleTypeRegistry = null;
        ruleEngine.setModuleTypeRegistry(null);
    }

    /**
     * Bind the {@link RuleTemplateRegistry} service - called from DS.
     *
     * @param templateRegistry templateRegistry service.
     */
    protected void setTemplateRegistry(TemplateRegistry<RuleTemplate> templateRegistry) {
        if (templateRegistry instanceof RuleTemplateRegistry) {
            this.templateRegistry = (RuleTemplateRegistry) templateRegistry;
            templateRegistry.addRegistryChangeListener(this);
        }
    }

    /**
     * Unbind the {@link RuleTemplateRegistry} service - called from DS.
     *
     * @param templateRegistry templateRegistry service.
     */
    protected void unsetTemplateRegistry(TemplateRegistry<RuleTemplate> templateRegistry) {
        if (templateRegistry instanceof RuleTemplateRegistry) {
            this.templateRegistry = null;
            templateRegistry.removeRegistryChangeListener(this);
        }
    }

    /**
     * Bind the {@link StorageService} - called from DS.
     *
     * @param storageService
     */
    protected void setStorageService(StorageService storageService) {
        this.disabledRulesStorage = storageService.<Boolean> getStorage(DISABLED_RULE_STORAGE,
                this.getClass().getClassLoader());
        // enable the rules that are not persisted as Disabled;
        for (Rule rule : getAll()) {
            String uid = rule.getUID();
            if (disabledRulesStorage.get(uid) == null) {
                setEnabled(uid, Boolean.TRUE);
            }
        }
    }

    /**
     * Unbind the {@link StorageService} - called from DS.
     *
     * @param storageService
     */
    protected void unsetStorageService(StorageService storageService) {
        this.disabledRulesStorage = null;
        // disable all rules;
        for (Rule rule : getAll()) {
            ruleEngine.setRuleEnabled(rule, Boolean.FALSE);
        }
    }

    protected void addModuleHandlerFactory(ModuleHandlerFactory moduleHandlerFactory) {
        ruleEngine.addModuleHandlerFactory(moduleHandlerFactory);
    }

    protected void removeModuleHandlerFactory(ModuleHandlerFactory moduleHandlerFactory) {
        ruleEngine.removeModuleHandlerFactory(moduleHandlerFactory);
    }

    /**
     * This method is used to register a {@link Rule} into the {@link RuleEngine}. First the {@link Rule} become
     * {@link RuleStatus#UNINITIALIZED}.
     * Then verification procedure will be done and the Rule become {@link RuleStatus#IDLE}.
     * If the verification fails, the Rule will stay {@link RuleStatus#UNINITIALIZED}.
     *
     * @param rule a {@link Rule} instance which have to be added into the {@link RuleEngine}.
     * @return a copy of the added {@link Rule}
     * @throws RuntimeException
     *             when passed module has a required configuration property and it is not specified in rule definition
     *             nor
     *             in the module's module type definition.
     * @throws IllegalArgumentException
     *             when a module id contains dot or when the rule with the same UID already exists.
     */
    @Override
    public Rule add(Rule rule) {
        String rUID = rule.getUID();
        if (rUID == null) {
            rUID = ruleEngine.getUniqueId();
            super.add(initRuleId(rUID, rule));
        } else {
            super.add(rule);
        }
        Rule ruleCopy = get(rUID);
        if (ruleCopy != null) {
            return ruleCopy;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Sets a unique ID on the rule that should be added in the registry. If the rule already has an ID the method will
     * not be invoked.
     *
     * @param rUID the unique Rule ID that should be set to the rule
     * @param rule candidate for unique ID
     * @return a rule with UID
     */
    protected @NonNull Rule initRuleId(String rUID, Rule rule) {
        Rule ruleWithUID = new Rule(rUID, rule.getTriggers(), rule.getConditions(), rule.getActions(),
                rule.getConfigurationDescriptions(), rule.getConfiguration(), rule.getTemplateUID(),
                rule.getVisibility());
        ruleWithUID.setName(rule.getName());
        ruleWithUID.setTags(rule.getTags());
        ruleWithUID.setDescription(rule.getDescription());
        return ruleWithUID;
    }

    /**
     * This method is used to add {@link Rule} into the RuleEngine.
     * When the rule is resolved it becomes into {@link RuleStatus#IDLE} state.
     * If the verification fails, the rule goes into {@link RuleStatus#UNINITIALIZED} state.
     *
     * @param provider a provider of the {@link Rule}.
     * @param element a {@link Rule} instance which have to be added into the RuleEngine.
     * @throws RuntimeException
     *             when passed module has a required configuration property and it is not specified in rule definition
     *             nor
     *             in the module's module type definition.
     * @throws IllegalArgumentException
     *             when a module id contains dot or when the rule with the same UID already exists.
     */
    @Override
    protected void notifyListenersAboutAddedElement(Rule rule) {
        super.notifyListenersAboutAddedElement(rule);
        postRuleAddedEvent(rule);
        String uid = rule.getUID();
        ruleEngine.addRule(rule, (disabledRulesStorage != null && disabledRulesStorage.get(uid) == null));
        String templateUID = rule.getTemplateUID();
        if (templateUID != null) {
            synchronized (this) {
                Set<String> ruleUIDs = mapTemplateToRules.get(templateUID);
                if (ruleUIDs == null) {
                    ruleUIDs = new HashSet<String>(11);
                    mapTemplateToRules.put(templateUID, ruleUIDs);
                }
                ruleUIDs.add(uid);
            }
        }
    }

    protected void postRuleAddedEvent(Rule rule) {
        postEvent(RuleEventFactory.createRuleAddedEvent(rule, SOURCE));
    }

    protected void postRuleRemovedEvent(Rule rule) {
        postEvent(RuleEventFactory.createRuleRemovedEvent(rule, SOURCE));
    }

    protected void postRuleUpdatedEvent(Rule rule, Rule oldRule) {
        postEvent(RuleEventFactory.createRuleUpdatedEvent(rule, oldRule, SOURCE));
    }

    protected void postRuleStatusInfoEvent(RuleStatusInfo statusInfo, String ruleUID) {
        postEvent(RuleEventFactory.createRuleStatusInfoEvent(statusInfo, ruleUID, SOURCE));
    }

    @Override
    protected void onRemoveElement(Rule rule) {
        String uid = rule.getUID();
        ruleEngine.removeRule(uid);
        String templateUID = rule.getTemplateUID();
        if (templateUID != null) {
            synchronized (this) {
                Set<String> ruleUIDs = mapTemplateToRules.get(templateUID);
                if (ruleUIDs != null) {
                    ruleUIDs.remove(uid);
                }
            }
        }
    }

    /**
     * This method is used to update an existing {@link Rule} in the {@link RuleEngine}.
     * When the {@link Rule} is resolved it becomes into {@link RuleStatus#IDLE} state.
     * If the verifications are failed, the {@link Rule} goes into {@link RuleStatus#UNINITIALIZED} state.
     *
     * @param oldElement a {@link Rule} instance that have to be replaced with value of the parameter
     *            <code>element</code>.
     * @param rule a {@link Rule} instance which have to be used for update of the {@link Rule} into {@link RuleEngine}.
     * @return a copy of the old value of the updated rule
     * @throws RuntimeException
     *             when passed module has a required configuration property and it is not specified in rule definition
     *             nor
     *             in the module's module type definition.
     * @throws IllegalArgumentException
     *             when module id contains dot.
     */
    @Override
    protected void notifyListenersAboutUpdatedElement(Rule oldElement, Rule element) {
        super.notifyListenersAboutUpdatedElement(oldElement, element);
        postRuleUpdatedEvent(element, oldElement);
        String uid = element.getUID();
        ruleEngine.updateRule(element, (disabledRulesStorage != null && disabledRulesStorage.get(uid) == null));
        String templateUID = element.getTemplateUID();
        if (templateUID != null) {
            synchronized (this) {
                Set<String> ruleUIDs = mapTemplateToRules.get(templateUID);
                if (ruleUIDs != null) {
                    ruleUIDs.remove(uid);
                }
            }
        }
    }

    @Override
    protected void notifyListenersAboutRemovedElement(Rule element) {
        super.notifyListenersAboutRemovedElement(element);
        postRuleRemovedEvent(element);
    }

    @Override
    public Rule get(String key) {
        for (Collection<Rule> rules : elementMap.values()) {
            for (Rule rule : rules) {
                if (rule.getUID().equals(key)) {
                    return RuleUtils.getRuleCopy(rule);
                }
            }
        }
        return null;
    }

    @Override
    public Stream<Rule> stream() {
        // create copies for consumers
        return super.stream().map(r -> RuleUtils.getRuleCopy(r));
    }

    @Override
    public Collection<Rule> getByTag(String tag) {
        Collection<Rule> result = new LinkedList<Rule>();
        if (tag != null) {
            for (Collection<Rule> rules : elementMap.values()) {
                for (Rule rule : rules) {
                    Set<String> tags = rule.getTags();
                    if (tags != null && tags.contains(tag)) {
                        result.add(RuleUtils.getRuleCopy(rule));
                    }
                }
            }
        } else {
            for (Collection<Rule> rules : elementMap.values()) {
                for (Rule rule : rules) {
                    result.add(RuleUtils.getRuleCopy(rule));
                }
            }
        }
        return result;
    }

    @Override
    public Collection<Rule> getByTags(String... tags) {
        Set<String> tagSet = tags != null ? new HashSet<String>(Arrays.asList(tags)) : null;
        Collection<Rule> result = new LinkedList<Rule>();
        if (tagSet == null || tagSet.isEmpty()) {
            for (Collection<Rule> rules : elementMap.values()) {
                for (Rule rule : rules) {
                    result.add(RuleUtils.getRuleCopy(rule));
                }
            }
        } else {
            for (Collection<Rule> rules : elementMap.values()) {
                for (Rule rule : rules) {
                    Set<String> rTags = rule.getTags();
                    if (rTags != null && rTags.containsAll(tagSet)) {
                        result.add(RuleUtils.getRuleCopy(rule));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public synchronized void setEnabled(String uid, boolean isEnabled) {
        if (disabledRulesStorage == null) {
            throw new IllegalStateException("Persisting rule state failed. Storage service is not available!");
        }
        Rule rule = get(uid);
        if (rule == null) {
            throw new IllegalArgumentException(String.format("No rule with such id=%s was found!", uid));
        } else {
            ruleEngine.setRuleEnabled(rule, isEnabled);
        }
        if (isEnabled) {
            disabledRulesStorage.remove(uid);
        } else {
            disabledRulesStorage.put(uid, isEnabled);
        }
    }

    @Override
    public RuleStatusInfo getStatusInfo(String ruleUID) {
        return ruleEngine.getRuleStatusInfo(ruleUID);
    }

    @Override
    public RuleStatus getStatus(String ruleUID) {
        return ruleEngine.getRuleStatus(ruleUID);
    }

    @Override
    public void statusInfoChanged(String ruleUID, RuleStatusInfo statusInfo) {
        postRuleStatusInfoEvent(statusInfo, ruleUID);
    }

    @Override
    public Boolean isEnabled(String ruleUID) {
        if (disabledRulesStorage != null && disabledRulesStorage.get(ruleUID) != null) {
            return Boolean.FALSE;
        }
        return ruleEngine.getRuleStatus(ruleUID) == null ? null
                : !ruleEngine.getRuleStatus(ruleUID).equals(RuleStatus.DISABLED);
    }

    /**
     * The method checks if the rule has to be resolved by template or not. If the rule does not contain tempateUID it
     * returns same rule, otherwise it tries to resolve the rule created from template. If the template is available
     * the method creates a new rule based on triggers, conditions and actions from template. If the template is not
     * available returns the same rule.
     *
     * @param rule a rule defined by template.
     * @return the resolved rule(containing modules defined by the template) or not resolved rule, if the template is
     *         missing.
     */
    private Rule resolveRuleByTemplate(Rule rule) {
        String templateUID = rule.getTemplateUID();
        if (templateUID != null) {
            RuleTemplate template = templateRegistry.get(templateUID);
            if (template == null) {
                logger.debug("Rule template {} does not exist.", templateUID);
                return rule;
            } else {
                Rule resolvedRule = new Rule(rule.getUID(), RuleUtils.getTriggersCopy(template.getTriggers()),
                        RuleUtils.getConditionsCopy(template.getConditions()),
                        RuleUtils.getActionsCopy(template.getActions()), template.getConfigurationDescriptions(),
                        rule.getConfiguration(), null, rule.getVisibility());
                resolvedRule.setName(rule.getName());
                resolvedRule.setTags(rule.getTags());
                resolvedRule.setDescription(rule.getDescription());

                // TODO this provide config resolution twice - It must be done only in RuleEngine. Remove it.
                ruleEngine.resolveConfiguration(resolvedRule);

                return resolvedRule;
            }
        }
        return rule;
    }

    @Override
    protected void addProvider(Provider<Rule> provider) {
        super.addProvider(provider);
        Collection<Rule> rules = new LinkedList<Rule>(elementMap.get(provider));
        for (Rule rule : rules) {
            updateRuleByTemplate(provider, rule);
        }
    }

    @Override
    public void added(Provider<Rule> provider, Rule element) {
        Rule ruleWithUID = element;
        if (element.getUID() == null) {
            String rUID = ruleEngine.getUniqueId();
            ruleWithUID = initRuleId(rUID, element);
        }
        super.added(provider, ruleWithUID);
        updateRuleByTemplate(provider, ruleWithUID);
    }

    private void updateRuleByTemplate(Provider<Rule> provider, Rule rule) {
        Rule resolvedRule = resolveRuleByTemplate(rule);
        if (rule != resolvedRule) {
            if (update(resolvedRule) == null) {
                super.updated(provider, rule, resolvedRule);
            }
        }
    }

    @Override
    public void updated(Provider<Rule> provider, Rule oldElement, Rule element) {
        Rule ruleWithUID = element;
        String rUID = oldElement.getUID();
        if (element.getUID() == null) {
            ruleWithUID = initRuleId(rUID, element);
        }
        Rule resolvedRule = resolveRuleByTemplate(ruleWithUID);
        super.updated(provider, oldElement, resolvedRule);
    }

    @Override
    public void added(RuleTemplate element) {
        String templateUID = element.getUID();
        Set<String> rules = new HashSet<String>();
        synchronized (this) {
            Set<String> rulesForResolving = mapTemplateToRules.remove(templateUID);
            if (rulesForResolving != null) {
                rules.addAll(rulesForResolving);
            }
        }
        if (rules != null) {
            for (String rUID : rules) {
                Rule rule = get(rUID);
                updateRuleByTemplate(getProvider(rule), rule);
            }
        }
    }

    private Provider<Rule> getProvider(Rule rule) {
        for (Entry<Provider<Rule>, Collection<Rule>> entry : elementMap.entrySet()) {
            if (entry.getValue().contains(rule)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void removed(RuleTemplate element) {
        // Do nothing - resolved rules are independent from templates
    }

    @Override
    public void updated(RuleTemplate oldElement, RuleTemplate element) {
        // Do nothing - resolved rules are independent from templates
    }

    @Override
    public void runNow(String ruleUID) {
        ruleEngine.runNow(ruleUID);
    }

    @Override
    public void runNow(String ruleUID, boolean considerConditions, Map<String, Object> context) {
        ruleEngine.runNow(ruleUID, considerConditions, context);
    }

}