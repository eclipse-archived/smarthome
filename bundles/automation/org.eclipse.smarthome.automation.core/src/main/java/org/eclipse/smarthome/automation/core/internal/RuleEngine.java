/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusDetail;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.StatusInfoCallback;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.internal.RuleEngineCallbackImpl.TriggerData;
import org.eclipse.smarthome.automation.core.internal.composite.CompositeModuleHandlerFactory;
import org.eclipse.smarthome.automation.core.internal.template.TemplateManager;
import org.eclipse.smarthome.automation.core.internal.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.core.util.ConnectionValidator;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to initialized and execute {@link Rule}s added in rule engine.
 * Each Rule has associated {@link RuleStatusInfo} object which shows status and status details of of the Rule.
 * The states are self excluded and they are:
 * <LI>disabled - the rule is temporary not available. This status is set by the user.
 * <LI>not initialized - the rule is enabled, but it still is not working because some of the module handlers are not
 * available or its module types or template is not resolved. The initialization problem is described by the status
 * details
 * <LI>idle - the rule is enabled and initialized and it is waiting for triggering events.
 * <LI>running - the rule is enabled and initialized and it is executing at the moment. When the execution is finished,
 * it goes to the idle state.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider, registry implementation and customized modules
 * @author Benedikt Niehues - change behavior for unregistering ModuleHandler
 *
 */
@SuppressWarnings("rawtypes")
public class RuleEngine implements ServiceTrackerCustomizer/* <ModuleHandlerFactory, ModuleHandlerFactory> */ {

    /**
     * Constant defining separator between parent and custom module types. For example: SampleTrigger:CustomTrigger is a
     * custom module type uid which defines custom trigger type base on the SampleTrigge module type.
     */
    public static final char MODULE_TYPE_SEPARATOR = ':';

    /**
     * Constant defining separator between module uid and output name.
     */
    public static final char OUTPUT_SEPARATOR = '.';

    /**
     * Prefix of {@link Rule}'s UID created by the rule engine.
     */
    public static final String ID_PREFIX = "rule_"; //$NON-NLS-1$

    /**
     * {@link Map} of rule's id to corresponding {@link RuleEngineCallback}s. For each {@link Rule} there is one and
     * only one rule callback.
     */
    private Map<String, RuleEngineCallbackImpl> reCallbacks = new HashMap<String, RuleEngineCallbackImpl>();

    /**
     * {@link Map} of module type UIDs to rules where these module types participated.
     */
    private Map<String, Set<String>> mapModuleTypeToRules = new HashMap<String, Set<String>>();

    /**
     * {@link Map} of template UIDs to rules where these templates participated.
     */
    private Map<String, Set<String>> mapTemplateToRules = new HashMap<String, Set<String>>();

    /**
     * {@link Map} of created rules. It contains all rules added to rule engine independent if they are initialized or
     * not. The relation is rule's id to {@link Rule} object.
     */
    private Map<String, RuntimeRule> rules;

    /**
     * Tracker of module handler factories. Each factory has a type which can evaluate. This type corresponds to the
     * system module type of the module.
     */
    private ServiceTracker/* <ModuleHandlerFactory, ModuleHandlerFactory> */ mhfTracker;

    /**
     * Bundle context field.
     */
    private BundleContext bc;

    /**
     * {@link Map} system module type to corresponding module handler factories.
     */
    private Map<String, ModuleHandlerFactory> moduleHandlerFactories;

    /**
     * Locker which does not permit rule initialization when the rule engine is stopping.
     */
    private boolean isDisposed = false;

    /**
     * {@link Map} of {@link Rule}'s id to current {@link RuleStatus} object.
     */
    private Map<String, RuleStatusInfo> statusMap = new HashMap<String, RuleStatusInfo>();

    private Logger logger;

    private StatusInfoCallback statusInfoCallback;

    private Map<String, Map<String, Object>> contextMap;

    private ModuleTypeManager mtManager;

    private TemplateManager tManager;

    private CompositeModuleHandlerFactory compositeFactory;

    private int ruleMaxID = 0;

    /**
     * Constructor of {@link RuleEngine}. It initializes the logger and starts
     * tracker for {@link ModuleHandlerFactory} services.
     *
     * @param bc {@link BundleContext} used for tracker registration and rule engine logger creation.
     */
    @SuppressWarnings("unchecked")
    public RuleEngine(BundleContext bc) {
        this.bc = bc;
        logger = LoggerFactory.getLogger(getClass());
        contextMap = new HashMap<String, Map<String, Object>>();
        if (rules == null) {
            rules = new HashMap<String, RuntimeRule>(20);
        }
        moduleHandlerFactories = new HashMap<String, ModuleHandlerFactory>(20);
        mhfTracker = new ServiceTracker/* <ModuleHandlerFactory, ModuleHandlerFactory> */(bc,
                ModuleHandlerFactory.class.getName(), this);
        mhfTracker.open();

    }

    /**
     * This method add a new rule into rule engine. Scope identity of the Rule is the identity of the caller.
     *
     * @param rule a rule which has to be added.
     * @return UID of added rule.
     */
    public synchronized String addRule(Rule rule) {
        return addRule0(rule, getScopeIdentifier());
    }

    /**
     * This method add a new rule into rule engine to scope of rules defined by the scope's identity. The rule engine
     * must check permission of the caller if he can put rules into this scope.
     *
     * @param rule a rule which has to be added.
     * @return UID of added rule.
     */
    public synchronized String addRule(Rule rule, String identity) {
        // TODO check permissions
        return addRule0(rule, identity);
    }

    /**
     * Utility method that adds rule into rule engine. It creates internal RuleImpl object which is deep copy of the
     * passed {@link Rule} object and adds this copy into RuleEngine
     *
     * @param rule a rule which has to be added
     * @param identity identity of the scope where the rule belongs to.
     * @throws IllegalArgumentException when the rule with the same UID is already added.
     */
    private String addRule0(Rule rule, String identity) {
        RuntimeRule r1;
        String rUID = rule.getUID();
        r1 = new RuntimeRule(rule);

        rUID = getRuleUID(rUID);
        r1.setScopeIdentifier(identity);
        r1.setUID(rUID);

        rules.put(rUID, r1);
        logger.debug("Added rule '{}'", rUID);

        return rUID;
    }

    /**
     * Utility method which checks for existence of the rule with passed UID or create an unique id when the parameter
     * is not passed
     *
     * @param rUID unique id of the rule
     * @return a new unique id of the rule.
     * @throws IllegalArgumentException when the rule with the same UID already exists.
     */
    private String getRuleUID(String rUID) {
        if (rUID != null) {
            if (hasRule(rUID)) {
                throw new IllegalArgumentException("Rule '" + rUID + "' already exists.");
            }
        } else {
            rUID = getUniqueId();
        }
        return rUID;
    }

    /**
     * This method is used to update existing rule. It creates an internal {@link RuntimeRule} object which is deep copy
     * of
     * passed {@link Rule} object. If the rule exist in the rule engine it will be replaced by the new one.
     *
     * @param rule a rule which has to be updated.
     */
    public synchronized void updateRule(Rule rule) {
        String rUID = rule.getUID();
        RuntimeRule r1;
        if (rUID == null) {
            rUID = getUniqueId();
            r1 = new RuntimeRule(rule);
            r1.setUID(rUID);
        } else {
            r1 = new RuntimeRule(rule);
        }

        rules.put(rUID, r1);
        logger.debug("Updated rule '{}'.", rUID);

        setRule(rUID);
    }

    /**
     * This method tries to initialize the rule. It uses available {@link ModuleHandlerFactory}s to create
     * {@link ModuleHandler}s for all {@link Module}s of the {@link Rule} and to link them.
     * When all the modules have associated module handlers then the {@link Rule} is initialized and it is ready to
     * working. It goes into idle state. Otherwise the Rule stays into not initialized and continue to wait missing
     * handlers, module types or templates.
     *
     * @param rUID a UID of rule which tries to be initialized.
     */
    protected synchronized void setRule(String rUID) {
        if (isDisposed) {
            return;
        }

        RuleStatusInfo ruleStatus = statusMap.get(rUID);
        if (ruleStatus != null && RuleStatus.DISABLED == ruleStatus.getStatus()) {
            return;
        }

        if (ruleStatus != null && RuleStatus.NOT_INITIALIZED != ruleStatus.getStatus()) {
            setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.NOT_INITIALIZED));
        }

        RuntimeRule r = getRule0(rUID);
        String templateUID = r.getTemplateUID();
        if (templateUID != null) {
            Rule notInitializedRule = r;
            r = getRuleByTemplate(r);
            if (r == null) {
                Set<String> rules = mapTemplateToRules.get(templateUID);
                if (rules == null) {
                    rules = new HashSet<String>(10);
                }
                rules.add(notInitializedRule.getUID());
                mapTemplateToRules.put(templateUID, rules);
                logger.error(
                        "The rule: " + rUID + " is not created! The template: " + templateUID + " is not available!");
                setRuleStatusInfo(rUID,
                        new RuleStatusInfo(RuleStatus.NOT_INITIALIZED, RuleStatusDetail.TEMPLATE_MISSING_ERROR,
                                "The template: " + templateUID + " is not available!"));
                return;
            }
            r.setUID(rUID);
        }

        String errMsgs = null;
        String errMessage;
        List<Condition> conditions = r.getConditions();
        if (conditions != null) {
            errMessage = setModuleHandler(rUID, conditions);
            if (errMessage != null) {
                errMsgs = errMessage;
            }
        }

        errMessage = setModuleHandler(rUID, r.getActions());
        if (errMessage != null) {
            errMsgs = errMsgs + "\n" + errMessage;
        }

        errMessage = setModuleHandler(rUID, r.getTriggers());
        if (errMessage != null) {
            errMsgs = errMsgs + "\n" + errMessage;
        }

        if (errMsgs == null) {

            try {
                ConnectionValidator.validateConnections(r);
                resolveDefaultValues(r);
                register(r);
                // change state to IDLE
                setRuleStatusInfo(r.getUID(), new RuleStatusInfo(RuleStatus.IDLE));

            } catch (Exception e) {
                unregister(r);
                errMsgs = errMsgs + "\n Validation of rule" + r.getUID() + "has failed! " + e.getMessage();
                // change state to NOTINITIALIZED
                setRuleStatusInfo(r.getUID(), new RuleStatusInfo(RuleStatus.NOT_INITIALIZED,
                        RuleStatusDetail.HANDLER_INITIALIZING_ERROR, errMessage));
            }

        } else {
            unregister(r);

            // change state to NOTINITIALIZED
            setRuleStatusInfo(r.getUID(), new RuleStatusInfo(RuleStatus.NOT_INITIALIZED,
                    RuleStatusDetail.HANDLER_INITIALIZING_ERROR, errMessage));
        }
    }

    /**
     * An utility method which tries to resolve templates and initialize the rule with modules defined by this template.
     *
     * @param rule a rule defined by template.
     * @return a rule containing modules defined by the template or null.
     */
    private RuntimeRule getRuleByTemplate(RuntimeRule rule) {
        String ruleTemplateUID = rule.getTemplateUID();
        RuleTemplate template = (RuleTemplate) tManager.getTemplate(ruleTemplateUID);
        if (template == null) {
            logger.debug("Rule template '" + ruleTemplateUID + "' does not exist.");
            return null;
        } else {
            RuntimeRule r1 = new RuntimeRule(template, rule.getConfiguration());
            r1.handleModuleConfigReferences();
            return r1;
        }
    }

    /**
     * This method is used to update {@link RuleStatusInfo} of the rule. It also notifies the registry about the change.
     *
     * @param rUID UID of the rule which has changed status info.
     * @param status new rule status info
     */
    private void setRuleStatusInfo(String rUID, RuleStatusInfo status) {
        statusMap.put(rUID, status);
        if (statusInfoCallback != null) {
            statusInfoCallback.statusInfoChanged(rUID, status);
        }
    }

    /**
     * This method links modules to corresponding module handlers.
     *
     * @param rUID id of rule containing these modules
     * @param modules list of modules
     * @return null when all modules are connected or list of RuleErrors for missing handlers.
     */
    private <T extends Module> String setModuleHandler(String rUID, List<T> modules) {
        StringBuffer sb = null;
        if (modules != null) {
            for (T m : modules) {
                updateMapModuleTypeToRule(rUID, m.getTypeUID());
                ModuleHandler moduleHandler = getModuleHandler(m, rUID);
                if (moduleHandler != null) {
                    if (m instanceof RuntimeAction) {
                        ((RuntimeAction) m).setModuleHandler((ActionHandler) moduleHandler);
                    } else if (m instanceof RuntimeCondition) {
                        ((RuntimeCondition) m).setModuleHandler((ConditionHandler) moduleHandler);
                    } else if (m instanceof RuntimeTrigger) {
                        ((RuntimeTrigger) m).setModuleHandler((TriggerHandler) moduleHandler);
                    }
                } else {
                    if (sb == null) {
                        sb = new StringBuffer();
                    }
                    String message = "Missing handler: " + m.getTypeUID() + ", for module: " + m.getId();
                    sb.append(message).append("\n");
                    logger.debug(message);
                }
            }
        }
        return sb != null ? sb.toString() : null;
    }

    /**
     * Gets {@link RuleEngineCallback} for passed {@link Rule}. If it does not exists, a callback object is created
     *
     * @param rule rule object for which the callback is looking for.
     * @return a {@link RuleEngineCallback} corresponding to the passed {@link Rule} object.
     */
    private RuleEngineCallbackImpl getRuleEngineCallback(RuntimeRule rule) {
        RuleEngineCallbackImpl result = reCallbacks.get(rule.getUID());
        if (result == null) {
            result = new RuleEngineCallbackImpl(this, rule);
            reCallbacks.put(rule.getUID(), result);
        }
        return result;
    }

    /**
     * Unlink module handlers from their modules. The method is called when the rule containing these modules goes into
     * not initialized state .
     *
     * @param modules list of module which are disconnected.
     */
    private <T extends Module> void removeHandlers(List<T> modules, String ruleUID) {
        if (modules != null) {
            for (T m : modules) {
                ModuleHandler handler = null;
                if (m instanceof RuntimeAction) {
                    handler = ((RuntimeAction) m).getModuleHandler();
                } else if (m instanceof RuntimeCondition) {
                    handler = ((RuntimeCondition) m).getModuleHandler();
                } else if (m instanceof RuntimeTrigger) {
                    handler = ((RuntimeTrigger) m).getModuleHandler();
                }

                if (handler != null) {
                    ModuleHandlerFactory factory = getModuleHandlerFactory(m.getTypeUID(), ruleUID);
                    factory.ungetHandler(m, ruleUID, handler);

                    if (m instanceof RuntimeAction) {
                        ((RuntimeAction) m).setModuleHandler(null);
                    } else if (m instanceof RuntimeCondition) {
                        ((RuntimeCondition) m).setModuleHandler(null);
                    } else if (m instanceof RuntimeTrigger) {
                        ((RuntimeTrigger) m).setModuleHandler(null);
                    }
                }
            }
        }
    }

    /**
     * This method register the Rule to start working. This is the final step of initialization process where triggers
     * received {@link RuleEngineCallback}s object and starts to notify the rule engine when they are triggered.
     * After activating all triggers the rule goes into IDLE state
     *
     * @param rule an initialized rule which has to starts tracking the triggers.
     */
    private void register(RuntimeRule rule) {
        RuleEngineCallback reCallback = getRuleEngineCallback(rule);
        for (Iterator<Trigger> it = rule.getTriggers().iterator(); it.hasNext();) {
            RuntimeTrigger t = (RuntimeTrigger) it.next();
            TriggerHandler triggerHandler = t.getModuleHandler();
            triggerHandler.setRuleEngineCallback(reCallback);
        }
    }

    /**
     * This method unregister rule form rule engine and the rule stops working. This is happen when the {@link Rule} is
     * removed or some of module handlers are disappeared. In the second case the
     * rule stays available but its state is moved to not initialized.
     *
     * @param r the unregistered rule
     */
    private void unregister(RuntimeRule r) {
        if (r != null) {
            RuleEngineCallbackImpl reCallback = reCallbacks.remove(r.getUID());
            if (reCallback != null) {
                reCallback.dispose();
            }
            removeHandlers(r.getTriggers(), r.getUID());
            removeHandlers(r.getActions(), r.getUID());
            removeHandlers(r.getConditions(), r.getUID());
        }
    }

    /**
     * Gets handler of passed module.
     *
     * @param m a {@link Module} which is looking for handler
     * @return handler for this module or null when it is not available.
     */
    public ModuleHandler getModuleHandler(Module m, String ruleUID) {
        ModuleHandlerFactory mhf = getModuleHandlerFactory(m.getTypeUID(), ruleUID);
        if (mhf == null) {
            return null;
        }
        return mhf.getHandler(m, ruleUID);
    }

    public ModuleHandlerFactory getModuleHandlerFactory(String moduleTypeId, String rUID) {
        ModuleHandlerFactory mhf = moduleHandlerFactories.get(moduleTypeId);
        if (mhf == null) {
            ModuleType mt = mtManager.getType(moduleTypeId);
            if (mt instanceof CompositeTriggerType || //
                    mt instanceof CompositeConditionType || //
                    mt instanceof CompositeActionType) {
                mhf = compositeFactory;
            }

        }
        return mhf;
    }

    public void updateMapModuleTypeToRule(String rUID, String moduleTypeId) {
        Set<String> rules = mapModuleTypeToRules.get(moduleTypeId);
        if (rules == null) {
            rules = new HashSet<String>(11);
        }
        rules.add(rUID);
        mapModuleTypeToRules.put(moduleTypeId, rules);
    }

    /**
     * This method removes Rule from rule engine. It is called by the {@link RuleRegistry}
     *
     * @param id id of removed {@link Rule}
     * @return true when a rule is deleted, false when there is no rule with such id.
     */
    public synchronized boolean removeRule(String id) {
        RuntimeRule r = rules.remove(id);
        if (r != null) {
            removeRuleEntry(r);
            return true;
        }
        return false;
    }

    /**
     * Utility method cleaning status and handler type Maps of removing {@link Rule}.
     *
     * @param r removed {@link Rule}
     * @return removed rule
     */
    private RuntimeRule removeRuleEntry(RuntimeRule r) {
        unregister(r);
        statusMap.remove(r.getUID());

        for (Iterator<Map.Entry<String, Set<String>>> it = mapModuleTypeToRules.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Set<String>> e = it.next();
            Set<String> rules = e.getValue();
            if (rules != null && rules.contains(r.getUID())) {
                rules.remove(r.getUID());
                if (rules.size() < 1) {
                    it.remove();
                }
            }
        }

        if (r.getTemplateUID() != null) {
            for (Iterator<Map.Entry<String, Set<String>>> it = mapTemplateToRules.entrySet().iterator(); it
                    .hasNext();) {
                Map.Entry<String, Set<String>> e = it.next();
                Set<String> rules = e.getValue();
                if (rules != null && rules.contains(r.getUID())) {
                    rules.remove(r.getUID());
                    if (rules.size() < 1) {
                        it.remove();
                    }
                }
            }
        }

        return r;
    }

    /**
     * Gets copy of the {@link Rule} corresponding to the passed id
     *
     * @param rId rule id
     * @return {@link Rule} object or null when rule with such id is not added to the {@link RuleManager}.
     */
    public synchronized Rule getRule(String rId) {
        RuntimeRule rule = rules.get(rId);
        if (rule != null) {
            Rule r = new RuntimeRule(rule);
            return r;
        }
        return null;
    }

    /**
     * Gets {@link RuntimeRule} corresponding to the passed id. This method is used internally and it does not create a
     * copy of the rule.
     *
     * @param rUID unieque id of the {@link Rule}
     * @return internal {@link RuntimeRule} object
     */
    private synchronized RuntimeRule getRule0(String rUID) {
        return rules.get(rUID);
    }

    /**
     * Gets all rules available in the rule engine.
     *
     * @return collection of all added rules.
     */
    public synchronized Collection<Rule> getRules() {
        return getRulesByTag((String) null);
    }

    /**
     * Gets collection of {@link Rule}s filtered by tag. When the tag is not specified the method returns all rules
     * available in the rule engine.
     *
     * @param tag the tag of looking rules.
     * @return Collection of rules containing specified tag.
     */
    public synchronized Collection<Rule> getRulesByTag(String tag) {
        Collection<Rule> result = new ArrayList<Rule>(10);
        for (Iterator<RuntimeRule> it = rules.values().iterator(); it.hasNext();) {
            RuntimeRule r = it.next();
            if (tag != null) {
                Set<String> tags = r.getTags();
                if (tags != null && tags.contains(tag)) {
                    result.add(new RuntimeRule(r));
                }
            } else {
                result.add(new RuntimeRule(r));
            }
        }
        return result;
    }

    /**
     * Gets collection of {@link Rule}s filtered by tags.
     *
     * @param tags list of tags of looking rules
     * @return collection of rules which have specified tags.
     */
    public synchronized Collection<Rule> getRulesByTags(Set<String> tags) {
        Collection<Rule> result = new ArrayList<Rule>(10);
        for (Iterator<RuntimeRule> it = rules.values().iterator(); it.hasNext();) {
            RuntimeRule r = it.next();
            if (tags != null) {
                Set<String> rTags = r.getTags();
                if (tags != null) {
                    for (Iterator<String> i = rTags.iterator(); i.hasNext();) {
                        String tag = i.next();
                        if (tags.contains(tag)) {
                            result.add(new RuntimeRule(r));
                            break;
                        }
                    }
                }
            } else {
                result.add(new RuntimeRule(r));
            }
        }
        return result;
    }

    /**
     * This method can switch enabled/ disabled state of the {@link Rule}
     *
     * @param rUID unique id of the rule
     * @param isEnabled true to enable the rule, false to disable it
     */
    public synchronized void setRuleEnabled(String rUID, boolean isEnabled) {
        RuleStatus status = getRuleStatus(rUID);
        if (isEnabled) {
            if (status == RuleStatus.DISABLED) {
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.NOT_INITIALIZED));
                setRule(rUID);
            } else {
                logger.info("The rule rId = " + rUID + " is already enabled");
            }
        } else {
            unregister(getRule0(rUID));
            setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.DISABLED));
        }
    }

    /**
     * Utility method which check if the rule engine contains a rule with passed UID
     *
     * @param rUID unique id of the {@link Rule}
     * @return true when such rule exists, false otherwise.
     */
    protected boolean hasRule(String rUID) {
        return rules.get(rUID) != null;
    }

    /**
     * This method tracks for {@link ModuleHandlerFactory}s. When a new factory is appeared it is added to the
     * {@link #moduleHandlerFactories} map and all rules which are waiting for handlers handled by this factory
     * are tried to be initialized.
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public synchronized ModuleHandlerFactory addingService(ServiceReference/* <ModuleHandlerFactory> */ reference) {
        @SuppressWarnings("unchecked")
        ModuleHandlerFactory mhf = (ModuleHandlerFactory) bc.getService(reference);
        Collection<String> moduleTypes = mhf.getTypes();
        Set<String> notInitailizedRules = null;
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            moduleHandlerFactories.put(moduleTypeName, mhf);
            Set<String> rules = mapModuleTypeToRules.get(moduleTypeName);
            if (rules != null) {
                for (String rUID : rules) {
                    RuleStatus ruleStatus = getRuleStatus(rUID);
                    if (ruleStatus == RuleStatus.NOT_INITIALIZED) {
                        notInitailizedRules = notInitailizedRules != null ? notInitailizedRules
                                : new HashSet<String>(20);
                        notInitailizedRules.add(rUID);
                    }

                }
            }
        }
        if (notInitailizedRules != null) {
            for (String rUID : notInitailizedRules) {
                setRule(rUID);
            }
        }
        return mhf;
    }

    /**
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference/* <ModuleHandlerFactory> */ reference,
            /* ModuleHandlerFactory */Object service) {
    }

    /**
     * This method tracks for disappearing of {@link ModuleHandlerFactory} service. It unregister all rules using
     * module handlers handled by this factory.
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public synchronized void removedService(
            ServiceReference/* <ModuleHandlerFactory> */ reference, /* ModuleHandlerFactory */
            Object service) {
        Collection<String> moduleTypes = ((ModuleHandlerFactory) service).getTypes();
        Map<String, List<String>> mapMissingHandlers = null;
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            Set<String> rules = mapModuleTypeToRules.get(moduleTypeName);
            if (rules != null) {
                for (String rUID : rules) {
                    RuleStatus ruleStatus = getRuleStatus(rUID);
                    switch (ruleStatus) {
                        case RUNNING:
                        case IDLE:
                            mapMissingHandlers = mapMissingHandlers != null ? mapMissingHandlers
                                    : new HashMap<String, List<String>>(20);
                            List<String> list = mapMissingHandlers.get(rUID);
                            if (list == null) {
                                list = new ArrayList<String>(5);
                            }
                            list.add(moduleTypeName);
                            mapMissingHandlers.put(rUID, list);

                            break;
                        default:
                            break;
                    }
                }
            }
        } // for
        if (mapMissingHandlers != null) {
            for (Entry<String, List<String>> e : mapMissingHandlers.entrySet()) {
                String rUID = e.getKey();
                List<String> missingTypes = e.getValue();
                StringBuffer sb = new StringBuffer();
                for (String typeUID : missingTypes) {
                    sb.append("Missing handler: ").append(typeUID).append("\n");
                }
                unregister(getRule0(rUID));
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.NOT_INITIALIZED,
                        RuleStatusDetail.HANDLER_MISSING_ERROR, sb.toString()));
            }
        }
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            moduleHandlerFactories.remove(moduleTypeName);
        }
    }

    /**
     * This method runs a {@link Rule}. It is called by the {@link RuleEngineCallback}'s thread when a new
     * {@link TriggerData} is available. This method switches
     *
     * @param rule the {@link Rule} which has to evaluate new {@link TriggerData}.
     * @param td {@link TriggerData} object containing new values for {@link Trigger}'s {@link Output}s
     */
    protected void runRule(RuntimeRule rule, RuleEngineCallbackImpl.TriggerData td) {
        RuleStatus ruleStatus = getRuleStatus(rule.getUID());
        if (ruleStatus == RuleStatus.IDLE) {
            try {

                // change state to RUNNING
                setRuleStatusInfo(rule.getUID(), new RuleStatusInfo(RuleStatus.RUNNING));
                clearContext(rule);

                setTriggerOutputs(rule.getUID(), td);
                boolean isSatisfied = calculateConditions(rule);
                if (isSatisfied) {
                    executeActions(rule);
                    logger.debug("The rule '{}' is executed.", rule.getUID());
                } else {
                    logger.debug("The rule '{}' is NOT executed, since it has unsatisfied conditions.", rule.getUID());
                }
            } catch (Throwable t) {
                logger.error("Fail to execute rule '{}': {}", new Object[] { rule.getUID(), t.getMessage() }, t);
            }

            // change state to IDLE
            setRuleStatusInfo(rule.getUID(), new RuleStatusInfo(RuleStatus.IDLE));
        } else {
            logger.error("Trying to execute rule ‘{}' with status '{}'",
                    new Object[] { rule.getUID(), ruleStatus.getValue() });
        }

    }

    private void clearContext(RuntimeRule rule) {
        Map<String, Object> context = contextMap.get(rule.getUID());
        if (context != null) {
            context.clear();
        }
    }

    /**
     * The method updates {@link Output} of the {@link Trigger} with a new triggered data.
     *
     * @param td new Triggered data.
     */
    private void setTriggerOutputs(String ruleUID, TriggerData td) {
        Trigger t = td.getTrigger();
        updateContext(ruleUID, t.getId(), td.getOutputs());
    }

    /**
     * Updates current context of rule engine.
     *
     * @param moduleUID uid of updated module.
     *
     * @param outputs new output values.
     */
    private void updateContext(String ruleUID, String moduleUID, Map<String, ?> outputs) {
        Map<String, Object> context = getContext(ruleUID);
        for (Map.Entry<String, ?> entry : outputs.entrySet()) {
            String key = moduleUID + OUTPUT_SEPARATOR + entry.getKey();
            context.put(key, entry.getValue());
        }
    }

    /**
     * @return copy of current context in rule engine
     */
    private Map<String, Object> getContext(String ruleUID) {
        return getContext(ruleUID, null);
    }

    private Map<String, Object> getContext(String ruleUID, Set<Connection> connections) {
        Map<String, Object> context = contextMap.get(ruleUID);
        if (context == null) {
            context = new HashMap<String, Object>();
            contextMap.put(ruleUID, context);
        }
        if (connections != null) {
            StringBuffer sb = new StringBuffer();
            for (Connection c : connections) {
                String outputModuleId = c.getOuputModuleId();
                if (outputModuleId != null) {
                    sb.append(outputModuleId).append(OUTPUT_SEPARATOR).append(c.getOutputName());
                    context.put(c.getInputName(), context.get(sb.toString()));
                    sb.setLength(0);
                } else {
                    // get reference from context
                    String ref = c.getOutputName();
                    Object value = context.get(ref);
                    if (value != null) {
                        context.put(c.getInputName(), value);
                    }
                }

            }
        }
        return context;
    }

    /**
     * This method checks if all rule's condition are satisfied or not.
     *
     * @param rule the checked rule
     * @return true when all conditions of the rule are satisfied, false otherwise.
     */
    private boolean calculateConditions(Rule rule) {
        List<Condition> conditions = ((RuntimeRule) rule).getConditions();
        if (conditions == null || conditions.size() == 0) {
            return true;
        }
        for (Iterator<Condition> it = conditions.iterator(); it.hasNext();) {
            RuntimeCondition c = (RuntimeCondition) it.next();
            ConditionHandler tHandler = c.getModuleHandler();
            Map<String, Object> context = getContext(rule.getUID(), c.getConnections());
            if (!tHandler.isSatisfied(context)) {
                logger.debug("The condition '{}' of rule '{}' is unsatisfied.",
                        new Object[] { c.getId(), rule.getUID() });
                return false;
            }
        }
        return true;
    }

    /**
     * This method evaluates actions of the {@link Rule} and set their {@link Output}s when they exists.
     *
     * @param rule executed rule.
     */
    private void executeActions(Rule rule) {
        List<Action> actions = ((RuntimeRule) rule).getActions();
        if (actions == null || actions.size() == 0) {
            return;
        }
        for (Iterator<Action> it = actions.iterator(); it.hasNext();) {
            RuntimeAction a = (RuntimeAction) it.next();
            ActionHandler aHandler = a.getModuleHandler();
            try {
                String rUID = rule.getUID();
                Map<String, Object> context = getContext(rUID, a.getConnections());
                Map<String, ?> outputs = aHandler.execute(context);
                if (outputs != null) {
                    context = getContext(rUID);
                    updateContext(rUID, a.getId(), outputs);
                }
            } catch (Throwable t) {
                logger.error("Fail to execute the action: " + a.getId(), t);
            }

        }

    }

    /**
     * The method clean used resource by rule engine when it is stopped.
     */
    public synchronized void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            if (mhfTracker != null) {
                mhfTracker.close();
                mhfTracker = null;
            }
            for (Iterator<RuntimeRule> it = rules.values().iterator(); it.hasNext();) {
                RuntimeRule r = it.next();
                removeRuleEntry(r);
                it.remove();
            }
            if (compositeFactory != null) {
                compositeFactory.dispose();
                compositeFactory = null;
            }
        }
        if (contextMap != null) {
            contextMap.clear();
            contextMap = null;
        }
        statusInfoCallback = null;
    }

    /**
     * This method gets rule's status object.
     *
     * @param rUID rule uid
     * @return status of the rule or null when such rule does not exists.
     */
    public synchronized RuleStatus getRuleStatus(String rUID) {
        RuleStatusInfo info = statusMap.get(rUID);
        RuleStatus status = null;
        if (info != null)
            status = info.getStatus();
        return status;
    }

    protected String getScopeIdentifier() {
        // TODO get the caller scope id.
        return null;
    }

    /**
     * Get all scope indentities
     *
     * @return
     */
    public synchronized Collection<String> getScopeIdentifiers() {
        // TODO check permissions
        Set<String> result = new HashSet<String>(10);
        for (Iterator<RuntimeRule> it = rules.values().iterator(); it.hasNext();) {
            RuntimeRule r = it.next();
            String id = r.getScopeIdentifier();
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    protected String getUniqueId() {
        return ID_PREFIX + getMaxId();
    }

    protected int getMaxId() {
        int result = 0;
        if (rules == null) {
            return result;
        }
        Set<String> col = rules.keySet();
        if (col != null) {
            for (Iterator<String> it = col.iterator(); it.hasNext();) {
                String rUID = it.next();
                if (rUID.startsWith(ID_PREFIX)) {
                    String sNum = rUID.substring(ID_PREFIX.length());
                    int i;
                    try {
                        i = Integer.parseInt(sNum);
                        result = i > result ? i : result; // find bigger key
                    } catch (NumberFormatException e) {
                        // skip this key
                    }
                }
            }
        }
        if (result > ruleMaxID) {
            ruleMaxID = result + 1;
        } else {
            ++ruleMaxID;
        }
        return ruleMaxID;
    }

    public void moduleTypeUpdated(Collection<ModuleType> moduleTypes) {
        Set<String> notInitailizedRules = null;
        for (Iterator<ModuleType> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next().getUID();
            Set<String> rules = mapModuleTypeToRules.get(moduleTypeName);
            if (rules != null) {
                for (String rUID : rules) {
                    RuleStatus ruleStatus = getRuleStatus(rUID);
                    if (ruleStatus == RuleStatus.NOT_INITIALIZED) {
                        notInitailizedRules = notInitailizedRules != null ? notInitailizedRules
                                : new HashSet<String>(20);
                        notInitailizedRules.add(rUID);
                    }

                }
            }
        }
        if (notInitailizedRules != null) {
            for (String rUID : notInitailizedRules) {
                setRule(rUID);
            }
        }

    }

    public void templateUpdated(Collection<Template> templates) {
        Set<String> notInitailizedRules = null;
        for (Template template : templates) {
            String templateUID = template.getUID();
            Set<String> rules = mapTemplateToRules.get(templateUID);
            if (rules != null) {
                for (String rUID : rules) {
                    RuleStatus ruleStatus = getRuleStatus(rUID);
                    if (ruleStatus == RuleStatus.NOT_INITIALIZED) {
                        notInitailizedRules = notInitailizedRules != null ? notInitailizedRules
                                : new HashSet<String>(20);
                        notInitailizedRules.add(rUID);
                    }

                }
            }
        }
        if (notInitailizedRules != null) {
            for (String rUID : notInitailizedRules) {
                setRule(rUID);
            }
        }

    }

    protected void setStatusInfoCallback(StatusInfoCallback statusInfoCallback) {
        this.statusInfoCallback = statusInfoCallback;
    }

    protected void setModuleTypeManager(ModuleTypeManager mtManager) {
        this.mtManager = mtManager;
    }

    protected void setTemplateManager(TemplateManager tManager) {
        this.tManager = tManager;
    }

    protected void setCompositeModuleFactory(CompositeModuleHandlerFactory compositeFactory) {
        this.compositeFactory = compositeFactory;
    }

    private void resolveDefaultValues(RuntimeRule r) {
        setDefautlValues(r.getUID(), r.getTriggers());
        setDefautlValues(r.getUID(), r.getConditions());
        setDefautlValues(r.getUID(), r.getActions());
    }

    private <T extends Module> void setDefautlValues(String ruleUID, List<T> modules) {
        for (T module : modules) {
            Map<String, Object> moduleConfiguration = module.getConfiguration();
            String typeId = module.getTypeUID();
            ModuleType mt = mtManager.getType(typeId);
            Set<ConfigDescriptionParameter> configs = mt.getConfigurationDescription();
            if (configs != null) {
                for (ConfigDescriptionParameter config : configs) {
                    String defaultValue = config.getDefault();
                    if (defaultValue != null) {
                        String configName = config.getName();
                        if (moduleConfiguration.get(configName) == null) {
                            moduleConfiguration.put(configName, defaultValue);
                        }
                    }
                } // for
            }

            Set<Output> outputs = null;
            if (mt instanceof TriggerType) {
                outputs = ((TriggerType) mt).getOutputs();
            } else if (mt instanceof ActionType) {
                outputs = ((ActionType) mt).getOutputs();
            }

            if (outputs != null) {
                Map<String, Object> result = new HashMap<>(11);
                for (Output output : outputs) {
                    Object defaultValue = output.getDefaultValue();
                    if (defaultValue != null) {
                        result.put(output.getName(), defaultValue);
                    }
                }
                if (result.size() > 0) {
                    updateContext(ruleUID, module.getId(), result);
                }
            }
        }
    }

}
