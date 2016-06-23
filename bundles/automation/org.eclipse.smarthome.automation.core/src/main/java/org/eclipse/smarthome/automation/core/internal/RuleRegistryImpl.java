/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleProvider;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.StatusInfoCallback;
import org.eclipse.smarthome.automation.core.internal.template.TemplateManager;
import org.eclipse.smarthome.automation.events.RuleEventFactory;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.ManagedProvider;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
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
 * <li>To check a Rule's status info, use the {@link #getStatus(String)} method.</li>
 * <li>The status of a newly added Rule, or a Rule enabled with {@link #setEnabled(String, boolean)}, or an updated
 * Rule, is first set to {@link RuleStatus#NOT_INITIALIZED}.</li>
 * <li>After a Rule is added or enabled, or updated, a verification procedure is initiated. If the verification of the
 * modules IDs, connections between modules and configuration values of the modules is successful, and the module
 * handlers are correctly set, the status is set to {@link RuleStatus#IDLE}.</li>
 * <li>If some of the module handlers disappear, the Rule will become {@link RuleStatus#NOT_INITIALIZED} again.</li>
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
 */
public class RuleRegistryImpl extends AbstractRegistry<Rule, String>implements RuleRegistry, StatusInfoCallback {

    private RuleEngine ruleEngine;
    private Logger logger;
    private Storage<Boolean> disabledRulesStorage;
    private TemplateManager templateManager;

    /**
     * {@link Map} of template UIDs to rules where these templates participated.
     */
    private Map<String, Set<String>> mapTemplateToRules = new HashMap<String, Set<String>>();
    @SuppressWarnings("rawtypes")
    private ServiceTracker templateProviderTracker;

    private static final String SOURCE = RuleRegistryImpl.class.getSimpleName();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public RuleRegistryImpl(RuleEngine ruleEngine, TemplateManager tManager, final BundleContext bc) {
        logger = LoggerFactory.getLogger(getClass());
        this.ruleEngine = ruleEngine;
        this.templateManager = tManager;
        ruleEngine.setStatusInfoCallback(this);
        templateProviderTracker = new ServiceTracker(bc, TemplateProvider.class.getName(),
                new ServiceTrackerCustomizer() {

                    @Override
                    public Object addingService(ServiceReference reference) {
                        TemplateProvider provider = bc.getService(reference);
                        templateUpdated(provider.getTemplates(null));
                        return provider;
                    }

                    @Override
                    public void modifiedService(ServiceReference reference, Object service) {
                        TemplateProvider provider = bc.getService(reference);
                        templateUpdated(provider.getTemplates(null));

                    }

                    @Override
                    public void removedService(ServiceReference reference, Object service) {
                    }

                });
        templateProviderTracker.open();

    }

    /**
     * This method is used to register all {@link Rule}s provided via the {@link RuleProvider}, into the
     * {@link RuleEngine}.
     *
     * @param provider a provider of {@link Rule}s.
     * @throws RuntimeException
     *             when passed module has a required configuration property and it is not specified in rule definition
     *             nor
     *             in the module's module type definition.
     * @throws IllegalArgumentException
     *             when a module id contains dot or when the rule with the same UID already exists.
     */
    @Override
    protected void addProvider(Provider<Rule> provider) {
        logger.info("Rule provider: {} is added.", provider);
        super.addProvider(provider);
    }

    @Override
    protected void setManagedProvider(ManagedProvider<Rule, String> provider) {
        super.setManagedProvider(provider);
        logger.info("Rule Managed Provider: {} is added.", provider);
    }

    @Override
    protected void removeProvider(Provider<Rule> provider) {
        logger.info("Rule provider: {} is removed.", provider);
        super.removeProvider(provider);
    }

    @Override
    protected void removeManagedProvider(ManagedProvider<Rule, String> provider) {
        super.removeManagedProvider(provider);
        logger.info("Rule Managed provider: {} is removed.", provider);
    }

    /**
     * This method is used to register a {@link Rule} into the {@link RuleEngine}. First the {@link Rule} become
     * {@link RuleStatus#NOT_INITIALIZED}.
     * Then verification procedure will be done and the Rule become {@link RuleStatus#IDLE}.
     * If the verification fails, the Rule will stay {@link RuleStatus#NOT_INITIALIZED}.
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
        if (rule == null) {
            throw new IllegalArgumentException("The added rule must not be null!");
        }
        String rUID = rule.getUID();
        Rule ruleWithUID = (rUID == null) ? ruleEngine.initRuleId(rule) : rule;

        Rule r1 = resolveTemplate(ruleWithUID); // the resolved rule must be store in managed provider
        if (r1 != null) {
            super.add(r1);
        } else {
            // template is not available
            super.add(ruleWithUID);
        }
        return ruleWithUID;
    }

    /**
     * This method is used to register a {@link Rule} into the RuleEngine. The {@link Rule} comes from Rule provider.
     * First the Rule become {@link RuleStatus#NOT_INITIALIZED}.
     * Then verification procedure will be done and the Rule become {@link RuleStatus#IDLE}.
     * If the verification fails, the Rule will stay {@link RuleStatus#NOT_INITIALIZED}.
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
    protected void onAddElement(Rule r) throws IllegalArgumentException {
        Rule rule = resolveTemplate(r); // can be called from any provider.
        try {
            postEvent(RuleEventFactory.createRuleAddedEvent(rule, SOURCE));
            String rUID = rule.getUID();
            if (rUID != null && disabledRulesStorage != null && disabledRulesStorage.get(rUID) != null) {
                ruleEngine.addRule(rule, false);
            } else {
                ruleEngine.addRule(rule, true);
            }
            super.onAddElement(rule);

        } catch (Exception e) {
            logger.error("Can't add rule: {}", rule.getUID(), e);
        }
    }

    @Override
    protected void onRemoveElement(Rule rule) {
        String uid = rule.getUID();
        if (ruleEngine.removeRule(uid)) {
            postEvent(RuleEventFactory.createRuleRemovedEvent(rule, SOURCE));
        }
        if (disabledRulesStorage != null) {
            disabledRulesStorage.remove(uid);
        }

        if (rule.getTemplateUID() != null) {
            for (Iterator<Map.Entry<String, Set<String>>> it = mapTemplateToRules.entrySet().iterator(); it
                    .hasNext();) {
                Map.Entry<String, Set<String>> e = it.next();
                Set<String> rules = e.getValue();
                if (rules != null && rules.contains(rule.getUID())) {
                    rules.remove(rule.getUID());
                    if (rules.size() < 1) {
                        it.remove();
                    }
                }
            }
        }

        super.onRemoveElement(rule);
    }

    /**
     * This method is used to update an existing {@link Rule} in the {@link RuleEngine}. First the {@link Rule} become
     * {@link RuleStatus#NOT_INITIALIZED}.
     * Then verification procedure will be done and the {@link Rule} become {@link RuleStatus#IDLE}.
     * If the verification fails, the {@link Rule} will stay {@link RuleStatus#NOT_INITIALIZED}.
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
    protected void onUpdateElement(Rule oldElement, Rule element) throws IllegalArgumentException {
        postEvent(RuleEventFactory.createRuleUpdatedEvent(element, oldElement, SOURCE));
        String rUID = element.getUID();
        if (disabledRulesStorage != null && disabledRulesStorage.get(rUID) != null) {
            ruleEngine.setRuleEnabled(rUID, false);
        }
        ruleEngine.updateRule(element);
    }

    @Override
    public Rule get(String key) {
        return ruleEngine.getRule(key);
    }

    @Override
    public Collection<Rule> getByTag(String tag) {
        return ruleEngine.getRulesByTag(tag);
    }

    @Override
    public Collection<Rule> getByTags(Set<String> tags) {
        return ruleEngine.getRulesByTags(tags);
    }

    @Override
    public synchronized void setEnabled(String uid, boolean isEnabled) {
        ruleEngine.setRuleEnabled(uid, isEnabled);
        if (disabledRulesStorage != null) {
            if (isEnabled) {
                disabledRulesStorage.remove(uid);
            } else {
                disabledRulesStorage.put(uid, isEnabled);
            }
        }
    }

    @Override
    public RuleStatusInfo getStatus(String ruleUID) {
        return ruleEngine.getRuleStatusInfo(ruleUID);
    }

    protected void setDisabledRuleStorage(Storage<Boolean> disabledRulesStorage) {
        this.disabledRulesStorage = disabledRulesStorage;
        for (Rule rule : ruleEngine.getRules()) {
            String uid = rule.getUID();
            if (ruleEngine.getRuleStatus(uid).equals(RuleStatus.DISABLED)) {
                disabledRulesStorage.put(uid, false);
            } else {
                disabledRulesStorage.remove(uid);
            }
        }
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        super.setEventPublisher(eventPublisher);
    }

    @Override
    public void unsetEventPublisher(EventPublisher eventPublisher) {
        super.unsetEventPublisher(eventPublisher);
    }

    @Override
    public void statusInfoChanged(String ruleUID, RuleStatusInfo statusInfo) {
        postEvent(RuleEventFactory.createRuleStatusInfoEvent(statusInfo, ruleUID, SOURCE));
    }

    @Override
    public Boolean isEnabled(String ruleUID) {
        if (disabledRulesStorage != null && disabledRulesStorage.get(ruleUID) != null) {
            return Boolean.FALSE;
        }
        return ruleEngine.hasRule(ruleUID) ? !ruleEngine.getRuleStatus(ruleUID).equals(RuleStatus.DISABLED) : null;
    }

    public void dispose() {
        if (templateProviderTracker != null) {
            templateProviderTracker.close();
            templateProviderTracker = null;
        }
    }

    /**
     * The method checks if the rule has to be resolved by tempalte or not. If it does not contain tempateUID the
     * returns same rule, otherwise it tried to resolves the rule created from template. If the template is available
     * the method create a new rule
     * based on trigger conditions and actions from template. If the template is not available returns null.
     *
     * @param r checked rule object.
     * @return rule object or null
     */
    protected Rule resolveTemplate(Rule r) {
        String templateUID = r.getTemplateUID();
        if (templateUID != null) {
            Rule rr = getRuleByTemplate(r);
            if (rr != null) {
                return rr;
            } else {
                Set<String> ruleUIDs = mapTemplateToRules.get(templateUID);
                if (ruleUIDs == null) {
                    ruleUIDs = new HashSet<String>(11);
                }
                ruleUIDs.add(r.getUID());
                mapTemplateToRules.put(templateUID, ruleUIDs);
                return r;
            }
        } else {
            return r;
        }
    }

    /**
     * An utility method which tries to resolve templates and initialize the rule with modules defined by this template.
     *
     * @param rule a rule defined by template.
     * @return a rule containing modules defined by the template or null.
     */
    private Rule getRuleByTemplate(Rule rule) {
        String ruleTemplateUID = rule.getTemplateUID();
        RuleTemplate template = (RuleTemplate) templateManager.get(ruleTemplateUID);
        if (template == null) {
            logger.debug("Rule template {} does not exist.", ruleTemplateUID);
            return null;
        } else {
            Rule r1 = new Rule(rule.getUID(), RuleUtils.getTriggersCopy(template.getTriggers()),
                    RuleUtils.getConditionsCopy(template.getConditions()),
                    RuleUtils.getActionsCopy(template.getActions()), template.getConfigurationDescription(),
                    rule.getConfiguration(), template.getUID(), template.getVisibility());
            validateConfiguration(r1);
            r1.setName(rule.getName());
            r1.setTags(template.getTags());
            r1.setDescription(template.getDescription());

            return r1;
        }
    }

    public void templateUpdated(Collection<Template> templates) {
        for (Template template : templates) {
            String templateUID = template.getUID();
            Set<String> rules = null;
            synchronized (this) {
                rules = mapTemplateToRules.get(templateUID);
            }
            if (rules != null) {
                for (String rUID : rules) {
                    RuleStatus ruleStatus = getStatus(rUID).getStatus();
                    if (ruleStatus == RuleStatus.NOT_INITIALIZED) {
                        Rule oldRule, newRule;
                        if ((oldRule = managedProvider.get(rUID)) != null) {
                            // The rule belongs to managed provider
                            newRule = resolveTemplate(oldRule);
                            update(newRule);
                        } else {
                            // the rule is coming from read only provider and must not be stored
                            oldRule = get(rUID);
                            newRule = resolveTemplate(oldRule);
                            onUpdateElement(oldRule, newRule);
                        }
                    }
                }
            }
        }
    }

    private void validateConfiguration(Rule r) {
        List<ConfigDescriptionParameter> configDescriptions = r.getConfigurationDescriptions();
        Configuration moduleConfiguration = r.getConfiguration();
        Map<String, Object> configuration = moduleConfiguration.getProperties();
        if (configuration != null) {
            validateConfiguration(configDescriptions, new HashMap<String, Object>(configuration));
            handleModuleConfigReferences(r.getTriggers(), configuration);
            handleModuleConfigReferences(r.getConditions(), configuration);
            handleModuleConfigReferences(r.getActions(), configuration);
        }
    }

    private void handleModuleConfigReferences(List<? extends Module> modules, Map<String, ?> ruleConfiguration) {
        if (modules != null) {
            for (Module module : modules) {
                ReferenceResolverUtil.updateModuleConfiguration(module, ruleConfiguration);
            }
        }
    }

    private void validateConfiguration(List<ConfigDescriptionParameter> configDescriptions,
            Map<String, Object> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            if (isOptionalConfig(configDescriptions)) {
                return;
            } else {
                throw new IllegalArgumentException("Missing required configuration properties!");
            }
        } else {
            for (ConfigDescriptionParameter configParameter : configDescriptions) {
                String configParameterName = configParameter.getName();
                processValue(configurations.remove(configParameterName), configParameter);
            }
            if (!configurations.isEmpty()) {
                String msg = "\"";
                Iterator<ConfigDescriptionParameter> i = configDescriptions.iterator();
                while (i.hasNext()) {
                    ConfigDescriptionParameter configParameter = i.next();
                    if (i.hasNext()) {
                        msg = msg + configParameter.getName() + "\", ";
                    } else {
                        msg = msg + configParameter.getName();
                    }
                }
                throw new IllegalArgumentException("Extra configuration properties : " + msg + "\"!");
            }
        }

    }

    private boolean isOptionalConfig(List<ConfigDescriptionParameter> configDescriptions) {
        if (configDescriptions != null && !configDescriptions.isEmpty()) {
            boolean required = false;
            Iterator<ConfigDescriptionParameter> i = configDescriptions.iterator();
            while (i.hasNext()) {
                ConfigDescriptionParameter param = i.next();
                required = required || param.isRequired();
            }
            return !required;
        }
        return true;
    }

    private void processValue(Object configValue, ConfigDescriptionParameter configParameter) {
        if (configValue != null) {
            checkType(configValue, configParameter);
            return;
        }
        if (configParameter.getDefault() != null) {
            return;
        }
        if (configParameter.isRequired()) {
            throw new IllegalArgumentException(
                    "Required configuration property missing: \"" + configParameter.getName() + "\"!");
        }
    }

    @SuppressWarnings("rawtypes")
    private void checkType(Object configValue, ConfigDescriptionParameter configParameter) {
        Type type = configParameter.getType();
        if (configParameter.isMultiple()) {
            if (configValue instanceof List) {
                List lConfigValues = (List) configValue;
                for (Object value : lConfigValues) {
                    if (!checkType(type, value)) {
                        throw new IllegalArgumentException("Unexpected value for configuration property \""
                                + configParameter.getName() + "\". Expected type: " + type);
                    }
                }
            }
            throw new IllegalArgumentException(
                    "Unexpected value for configuration property \"" + configParameter.getName()
                            + "\". Expected is Array with type for elements : " + type.toString() + "!");
        } else {
            if (!checkType(type, configValue)) {
                throw new IllegalArgumentException("Unexpected value for configuration property \""
                        + configParameter.getName() + "\". Expected is " + type.toString() + "!");
            }
        }
    }

    private boolean checkType(Type type, Object configValue) {
        switch (type) {
            case TEXT:
                return configValue instanceof String;
            case BOOLEAN:
                return configValue instanceof Boolean;
            case INTEGER:
                return configValue instanceof BigDecimal || configValue instanceof Integer
                        || configValue instanceof Double && ((Double) configValue).intValue() == (double) configValue;
            case DECIMAL:
                return configValue instanceof BigDecimal || configValue instanceof Double;
        }
        return false;
    }

}
