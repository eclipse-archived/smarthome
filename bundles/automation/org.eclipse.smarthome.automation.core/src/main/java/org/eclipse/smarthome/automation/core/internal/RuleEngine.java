/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
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
import org.eclipse.smarthome.automation.core.util.ConnectionValidator;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigUtil;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This class is used to initialized and execute {@link Rule}s added in rule engine. Each Rule has associated
 * {@link RuleStatusInfo} object which shows status and status details of of the Rule. The states are self excluded and
 * they are:
 * <LI>disabled - the rule is temporary not available. This status is set by the user.
 * <LI>not initialized -
 * the rule is enabled, but it still is not working because some of the module handlers are not available or its module
 * types or template is not resolved. The initialization problem is described by the status details
 * <LI>idle - the rule
 * is enabled and initialized and it is waiting for triggering events.
 * <LI>running - the rule is enabled and initialized
 * and it is executing at the moment. When the execution is finished, it goes to the idle state.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider, registry implementation and customized modules
 * @author Benedikt Niehues - change behavior for unregistering ModuleHandler
 *
 */
@SuppressWarnings("rawtypes")
public class RuleEngine implements RegistryChangeListener<ModuleType> {

    /**
     * Constant defining separator between module uid and output name.
     */
    public static final char OUTPUT_SEPARATOR = '.';

    /**
     * Prefix of {@link Rule}'s UID created by the rule engine.
     */
    public static final String ID_PREFIX = "rule_"; //$NON-NLS-1$

    /**
     * Default value of delay between rule's re-initialization tries.
     */
    public static final long DEFAULT_REINITIALIZATION_DELAY = 500;

    /**
     * Delay between rule's re-initialization tries.
     */
    public static final String CONFIG_PROPERTY_REINITIALIZATION_DELAY = "rule.reinitialization.delay";

    /**
     * Delay between rule's re-initialization tries.
     */
    private long scheduleReinitializationDelay;

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
     * {@link Map} of created rules. It contains all rules added to rule engine independent if they are initialized or
     * not. The relation is rule's id to {@link Rule} object.
     */
    private Map<String, RuntimeRule> rules;

    /**
     * {@link Map} system module type to corresponding module handler factories.
     */
    private Map<String, ModuleHandlerFactory> moduleHandlerFactories;
    private Set<ModuleHandlerFactory> allModuleHandlerFactories = new CopyOnWriteArraySet<>();

    /**
     * Locker which does not permit rule initialization when the rule engine is stopping.
     */
    private boolean isDisposed = false;

    /**
     * {@link Map} of {@link Rule}'s id to current {@link RuleStatus} object.
     */
    private Map<String, RuleStatusInfo> statusMap = new HashMap<String, RuleStatusInfo>();

    protected Logger logger = LoggerFactory.getLogger(RuleEngine.class.getName());

    private StatusInfoCallback statusInfoCallback;

    private Map<String, Map<String, Object>> contextMap;

    private ModuleTypeRegistry mtRegistry;

    private CompositeModuleHandlerFactory compositeFactory;

    private int ruleMaxID = 0;

    private Map<String, Future> scheduleTasks = new HashMap<String, Future>(31);

    private ScheduledExecutorService executor;

    private Gson gson;

    /**
     * Constructor of {@link RuleEngine}. It initializes the logger and starts tracker for {@link ModuleHandlerFactory}
     * services.
     *
     * @param bc {@link BundleContext} used for tracker registration and rule engine logger creation.
     * @throws InvalidSyntaxException
     */
    public RuleEngine() {
        this.rules = new HashMap<String, RuntimeRule>(20);
        this.contextMap = new HashMap<String, Map<String, Object>>();
        this.moduleHandlerFactories = new HashMap<String, ModuleHandlerFactory>(20);
    }

    protected void setModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        if (moduleTypeRegistry == null) {
            mtRegistry.removeRegistryChangeListener(this);
            mtRegistry = null;
        } else {
            mtRegistry = moduleTypeRegistry;
            mtRegistry.addRegistryChangeListener(this);
        }
        ConnectionValidator.setRegistry(mtRegistry);
    }

    protected void setCompositeModuleHandlerFactory(CompositeModuleHandlerFactory compositeFactory) {
        this.compositeFactory = compositeFactory;
    }

    @Override
    public void added(ModuleType moduleType) {
        String moduleTypeName = moduleType.getUID();
        for (ModuleHandlerFactory moduleHandlerFactory : allModuleHandlerFactories) {
            Collection<String> moduleTypes = moduleHandlerFactory.getTypes();
            if (moduleTypes.contains(moduleTypeName)) {
                synchronized (this) {
                    this.moduleHandlerFactories.put(moduleTypeName, moduleHandlerFactory);
                }
                break;
            }
        }
        Set<String> rules = null;
        synchronized (this) {
            Set<String> rulesPerModule = mapModuleTypeToRules.get(moduleTypeName);
            if (rulesPerModule != null) {
                rules = new HashSet<String>();
                rules.addAll(rulesPerModule);
            }
        }
        if (rules != null) {
            for (String rUID : rules) {
                RuleStatus ruleStatus = getRuleStatus(rUID);
                if (ruleStatus == RuleStatus.UNINITIALIZED) {
                    scheduleRuleInitialization(rUID);
                }
            }
        }
    }

    @Override
    public void removed(ModuleType moduleType) {
        // removing module types does not effect the rule
    }

    @Override
    public void updated(ModuleType oldElement, ModuleType moduleType) {
        if (moduleType.equals(oldElement)) {
            return;
        }
        String moduleTypeName = moduleType.getUID();
        Set<String> rules = null;
        synchronized (this) {
            Set<String> rulesPerModule = mapModuleTypeToRules.get(moduleTypeName);
            if (rulesPerModule != null) {
                rules = new HashSet<String>();
                rules.addAll(rulesPerModule);
            }
        }
        if (rules != null) {
            for (String rUID : rules) {
                if (getRuleStatus(rUID).equals(RuleStatus.IDLE) || getRuleStatus(rUID).equals(RuleStatus.RUNNING)) {
                    setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.UNINITIALIZED), true);
                    unregister(getRuntimeRule(rUID));
                }
                if (!getRuleStatus(rUID).equals(RuleStatus.DISABLED)) {
                    scheduleRuleInitialization(rUID);
                }
            }
        }
    }

    protected void addModuleHandlerFactory(ModuleHandlerFactory moduleHandlerFactory) {
        logger.debug("ModuleHandlerFactory added.");
        allModuleHandlerFactories.add(moduleHandlerFactory);
        Collection<String> moduleTypes = moduleHandlerFactory.getTypes();
        addNewModuleTypes(moduleHandlerFactory, moduleTypes);
    }

    protected void removeModuleHandlerFactory(ModuleHandlerFactory moduleHandlerFactory) {
        if (moduleHandlerFactory instanceof CompositeModuleHandlerFactory) {
            compositeFactory.deactivate();
            compositeFactory = null;
        }
        allModuleHandlerFactories.remove(moduleHandlerFactory);
        Collection<String> moduleTypes = moduleHandlerFactory.getTypes();
        removeMissingModuleTypes(moduleTypes);
        updateModuleHandlerFactoryMap(moduleTypes);
    }

    private synchronized void updateModuleHandlerFactoryMap(Collection<String> removedTypes) {
        for (Iterator<String> it = removedTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            moduleHandlerFactories.remove(moduleTypeName);
        }
    }

    /**
     * This method add a new rule into rule engine. Scope identity of the Rule is the identity of the caller.
     *
     * @param rule a rule which has to be added.
     * @param isEnabled specifies the rule to be added as disabled or not.
     */
    protected void addRule(Rule rule, boolean isEnabled) {
        String rUID = rule.getUID();
        RuntimeRule runtimeRule = new RuntimeRule(rule);
        synchronized (this) {
            rules.put(rUID, runtimeRule);
            if (isEnabled) {
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.UNINITIALIZED), false);
                setRule(runtimeRule);
            } else {
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.DISABLED), true);
            }
        }
    }

    /**
     * Validates IDs of modules. The module id must not contain dot.
     *
     * @param modules list of trigger, condition and action modules
     * @throws IllegalArgumentException when a module id contains dot.
     */
    private void validateModuleIDs(List<Module> modules) {
        for (Module m : modules) {
            String mId = m.getId();
            if (mId == null || !mId.matches("[A-Za-z0-9_-]*")) {
                throw new IllegalArgumentException("Invalid module uid: " + (mId != null ? mId : "null")
                        + ". It must not be null or not fit to the pattern: [A-Za-z0-9_-]*");
            }
        }
    }

    /**
     * This method is used to update existing rule. It creates an internal {@link RuntimeRule} object which is deep copy
     * of passed {@link Rule} object. If the rule exist in the rule engine it will be replaced by the new one.
     *
     * @param rule a rule which has to be updated.
     * @param enabled specifies the rule to be updated as disabled or not.
     */
    protected void updateRule(Rule rule, boolean isEnabled) {
        String rUID = rule.getUID();
        if (getRuntimeRule(rUID) == null) {
            logger.debug("There is no rule with UID '{}' which could be updated", rUID);
            return;
        }
        RuntimeRule runtimeRule = new RuntimeRule(rule);
        synchronized (this) {
            RuntimeRule oldRule = rules.get(rUID);
            unregister(oldRule);
            rules.put(rUID, runtimeRule);
            if (isEnabled) {
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.UNINITIALIZED), false);
                setRule(runtimeRule);
            } else {
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.DISABLED), true);
            }
        }
        logger.debug("Rule with UID '{}' is updated.", rUID);
    }

    /**
     * This method tries to initialize the rule. It uses available {@link ModuleHandlerFactory}s to create
     * {@link ModuleHandler}s for all {@link Module}s of the {@link Rule} and to link them. When all the modules have
     * associated module handlers then the {@link Rule} is initialized and it is ready to working. It goes into idle
     * state. Otherwise the Rule stays into not initialized and continue to wait missing handlers, module types or
     * templates.
     *
     * @param rUID a UID of rule which tries to be initialized.
     */
    private void setRule(RuntimeRule runtimeRule) {
        if (isDisposed) {
            return;
        }
        String rUID = runtimeRule.getUID();
        setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.INITIALIZING), true);

        if (runtimeRule.getTemplateUID() != null) {
            setRuleStatusInfo(rUID,
                    new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.TEMPLATE_MISSING_ERROR), true);
            return; // Template is not available (when a template is resolved it removes tempalteUID configuration
                    // property). The rule must stay NOT_INITIALISED.
        }

        List<Module> modules = runtimeRule.getModules(null);
        if (modules != null) {
            for (Module m : modules) {
                updateMapModuleTypeToRule(rUID, m.getTypeUID());
            }
        }

        String errMsgs;
        try {
            validateModuleIDs(modules);
            resolveConfiguration(runtimeRule);
            autoMapConnections(runtimeRule);
            ConnectionValidator.validateConnections(runtimeRule);
        } catch (RuntimeException e) {
            errMsgs = "\n Validation of rule " + rUID + " has failed! " + e.getLocalizedMessage();
            // change state to NOTINITIALIZED
            setRuleStatusInfo(rUID,
                    new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.CONFIGURATION_ERROR, errMsgs.trim()),
                    true);
            return;
        }

        errMsgs = setModuleHandlers(rUID, modules);
        if (errMsgs == null) {
            register(runtimeRule);
            // change state to IDLE
            setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.IDLE), true);

            Future f = scheduleTasks.remove(rUID);
            if (f != null) {
                if (!f.isDone()) {
                    f.cancel(true);
                }
            }

            if (scheduleTasks.isEmpty()) {
                if (executor != null) {
                    executor.shutdown();
                    executor = null;
                }
            }

        } else {
            // change state to NOTINITIALIZED
            setRuleStatusInfo(rUID,
                    new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.HANDLER_INITIALIZING_ERROR, errMsgs),
                    true);
            unregister(runtimeRule);
        }
    }

    /**
     * This method is used to update {@link RuleStatusInfo} of the rule. It also notifies the registry about the change.
     *
     * @param rUID UID of the rule which has changed status info.
     * @param status new rule status info
     */
    private void setRuleStatusInfo(String rUID, RuleStatusInfo status, boolean isSendEvent) {
        synchronized (this) {
            statusMap.put(rUID, status);
        }
        if (isSendEvent) {
            notifyStatusInfoCallback(rUID, status);
        }
    }

    private void notifyStatusInfoCallback(String rUID, RuleStatusInfo statusInfo) {
        StatusInfoCallback statusInfoCallback = this.statusInfoCallback;
        if (statusInfoCallback != null) {
            try {
                statusInfoCallback.statusInfoChanged(rUID, statusInfo);
            } catch (Exception exc) {
                logger.error("Exception while notifying StatusInfoCallback '{}' for rule '{}'", statusInfoCallback,
                        rUID, exc);
            }
        }
    }

    /**
     * This method links modules to corresponding module handlers.
     *
     * @param rUID id of rule containing these modules
     * @param modules list of modules
     * @return null when all modules are connected or list of RuleErrors for missing handlers.
     */
    private <T extends Module> String setModuleHandlers(String rUID, List<T> modules) {
        StringBuffer sb = null;
        if (modules != null) {
            for (T m : modules) {
                try {
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
                        String message = "Missing handler '" + m.getTypeUID() + "' for module '" + m.getId() + "'";
                        sb.append(message).append("\n");
                        logger.trace(message);
                    }
                } catch (Throwable t) {
                    if (sb == null) {
                        sb = new StringBuffer();
                    }
                    String message = "Getting handler '" + m.getTypeUID() + "' for module '" + m.getId() + "' failed: "
                            + t.getMessage();
                    sb.append(message).append("\n");
                    logger.trace(message);
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
    private synchronized RuleEngineCallbackImpl getRuleEngineCallback(RuntimeRule rule) {
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
    private <T extends Module> void removeModuleHandlers(List<T> modules, String ruleUID) {
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
                    ModuleHandlerFactory factory = getModuleHandlerFactory(m.getTypeUID());
                    if (factory != null) {
                        factory.ungetHandler(m, ruleUID, handler);
                    }
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
     * received {@link RuleEngineCallback}s object and starts to notify the rule engine when they are triggered. After
     * activating all triggers the rule goes into IDLE state
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
     * removed or some of module handlers are disappeared. In the second case the rule stays available but its state is
     * moved to not initialized.
     *
     * @param r the unregistered rule
     */
    private void unregister(RuntimeRule r) {
        if (r != null) {
            synchronized (this) {
                RuleEngineCallbackImpl reCallback = reCallbacks.remove(r.getUID());
                if (reCallback != null) {
                    reCallback.dispose();
                }
            }
            removeModuleHandlers(r.getTriggers(), r.getUID());
            removeModuleHandlers(r.getActions(), r.getUID());
            removeModuleHandlers(r.getConditions(), r.getUID());
        }
    }

    /**
     * Gets handler of passed module.
     *
     * @param m a {@link Module} which is looking for handler
     * @return handler for this module or null when it is not available.
     */
    private ModuleHandler getModuleHandler(Module m, String ruleUID) {
        String moduleTypeId = m.getTypeUID();
        ModuleHandlerFactory mhf = getModuleHandlerFactory(moduleTypeId);
        if (mhf == null || mtRegistry.get(moduleTypeId) == null) {
            return null;
        }
        return mhf.getHandler(m, ruleUID);
    }

    public ModuleHandlerFactory getModuleHandlerFactory(String moduleTypeId) {
        ModuleHandlerFactory mhf = null;
        synchronized (this) {
            mhf = moduleHandlerFactories.get(moduleTypeId);
        }
        if (mhf == null) {
            ModuleType mt = mtRegistry.get(moduleTypeId);
            if (mt instanceof CompositeTriggerType || //
                    mt instanceof CompositeConditionType || //
                    mt instanceof CompositeActionType) {
                mhf = compositeFactory;
            }

        }
        return mhf;
    }

    public synchronized void updateMapModuleTypeToRule(String rUID, String moduleTypeId) {
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
     * @param rUID id of removed {@link Rule}
     * @return true when a rule is deleted, false when there is no rule with such id.
     */
    protected synchronized boolean removeRule(String rUID) {
        RuntimeRule r = rules.remove(rUID);
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
        synchronized (this) {
            for (Iterator<Map.Entry<String, Set<String>>> it = mapModuleTypeToRules.entrySet().iterator(); it
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

            statusMap.remove(r.getUID());
        }
        return r;
    }

    /**
     * Gets {@link RuntimeRule} corresponding to the passed id. This method is used internally and it does not create a
     * copy of the rule.
     *
     * @param rUID unieque id of the {@link Rule}
     * @return internal {@link RuntimeRule} object
     */
    protected synchronized RuntimeRule getRuntimeRule(String rUID) {
        return rules.get(rUID);
    }

    /**
     * Gets all rules available in the rule engine.
     *
     * @return collection of all added rules.
     */
    protected synchronized Collection<RuntimeRule> getRuntimeRules() {
        return Collections.unmodifiableCollection(rules.values());
    }

    /**
     * This method can switch enabled/ disabled state of the {@link Rule}
     *
     * @param rUID unique id of the rule
     * @param isEnabled true to enable the rule, false to disable it
     */
    protected void setRuleEnabled(Rule rule, boolean isEnabled) {
        String rUID = rule.getUID();
        RuleStatus status = getRuleStatus(rUID);
        String enabled = isEnabled ? "enabled" : "disabled";
        RuntimeRule runtimeRule = getRuntimeRule(rUID);
        if (runtimeRule == null) {
            logger.debug("There is no rule with UID '{}' which could be {}", rUID, enabled);
            return;
        }
        if (isEnabled) {
            if (status == RuleStatus.DISABLED) {
                setRule(runtimeRule);
            } else {
                logger.debug("The rule rId = {} is already enabled.", rUID);
            }
        } else {
            unregister(runtimeRule);
            setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.DISABLED), true);
        }
    }

    private void addNewModuleTypes(ModuleHandlerFactory mhf, Collection<String> moduleTypes) {
        Set<String> notInitailizedRules = null;
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            Set<String> rules = null;
            synchronized (this) {
                moduleHandlerFactories.put(moduleTypeName, mhf);
                Set<String> rulesPerModule = mapModuleTypeToRules.get(moduleTypeName);
                if (rulesPerModule != null) {
                    rules = new HashSet<String>();
                    rules.addAll(rulesPerModule);
                }
            }
            if (rules != null) {
                for (String rUID : rules) {
                    RuleStatus ruleStatus = getRuleStatus(rUID);
                    if (ruleStatus == RuleStatus.UNINITIALIZED) {
                        notInitailizedRules = notInitailizedRules != null ? notInitailizedRules
                                : new HashSet<String>(20);
                        notInitailizedRules.add(rUID);
                    }

                }
            }
        }
        if (notInitailizedRules != null) {
            for (final String rUID : notInitailizedRules) {
                scheduleRuleInitialization(rUID);
            }
        }
    }

    protected void scheduleRuleInitialization(final String rUID) {
        Future f = scheduleTasks.get(rUID);
        if (f == null) {
            ScheduledExecutorService ex = getScheduledExecutor();
            f = ex.schedule(new Runnable() {

                @Override
                public void run() {
                    setRule(getRuntimeRule(rUID));
                }
            }, scheduleReinitializationDelay, TimeUnit.MILLISECONDS);
            scheduleTasks.put(rUID, f);
        }
    }

    private void removeMissingModuleTypes(Collection<String> moduleTypes) {
        Map<String, List<String>> mapMissingHandlers = null;
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            Set<String> rules = null;
            synchronized (this) {
                rules = mapModuleTypeToRules.get(moduleTypeName);
            }
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
                sb.append("Missing handlers: ");
                for (String typeUID : missingTypes) {
                    sb.append(typeUID).append(", ");
                }
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.UNINITIALIZED,
                        RuleStatusDetail.HANDLER_MISSING_ERROR, sb.substring(0, sb.length() - 2)), true);
                unregister(getRuntimeRule(rUID));
            }
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
        String rUID = rule.getUID();
        if (reCallbacks.get(rUID) == null) {
            // the rule was unregistered
            return;
        }

        synchronized (this) {
            final RuleStatus ruleStatus = getRuleStatus(rUID);
            if (ruleStatus != RuleStatus.IDLE) {
                logger.error("Failed to execute rule ‘{}' with status '{}'", rUID, ruleStatus.name());
                return;
            }
            // change state to RUNNING
            setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.RUNNING), true);
        }

        try {
            clearContext(rule);

            setTriggerOutputs(rUID, td);
            boolean isSatisfied = calculateConditions(rule);
            if (isSatisfied) {
                executeActions(rule, true);
                logger.debug("The rule '{}' is executed.", rUID);
            } else {
                logger.debug("The rule '{}' is NOT executed, since it has unsatisfied conditions.", rUID);
            }
        } catch (Throwable t) {
            logger.error("Failed to execute rule '{}': {}", rUID, t.getMessage());
            logger.debug("", t);
        }
        // change state to IDLE only if the rule has not been DISABLED.
        synchronized (this) {
            if (getRuleStatus(rUID) == RuleStatus.RUNNING) {
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.IDLE), true);
            }
        }
    }

    protected void runNow(String ruleUID, boolean considerConditions, Map<String, Object> context) {
        RuntimeRule rule = getRuntimeRule(ruleUID);
        if (rule == null) {
            logger.warn("Failed to execute rule '{}': Invalid Rule UID", ruleUID);
            return;
        }

        synchronized (this) {
            final RuleStatus ruleStatus = getRuleStatus(ruleUID);
            if (ruleStatus != RuleStatus.IDLE) {
                logger.error("Failed to execute rule ‘{}' with status '{}'", ruleUID, ruleStatus.name());
                return;
            }
            // change state to RUNNING
            setRuleStatusInfo(ruleUID, new RuleStatusInfo(RuleStatus.RUNNING), true);
        }

        try {
            clearContext(rule);
            if (context != null && !context.isEmpty()) {
                getContext(ruleUID).putAll(context);
            }
            if (considerConditions) {
                if (calculateConditions(rule)) {
                    executeActions(rule, false);
                }
            } else {
                executeActions(rule, false);
            }
            logger.debug("The rule '{}' is executed.", ruleUID);
        } catch (Throwable t) {
            logger.error("Fail to execute rule '{}': {}", new Object[] { ruleUID, t.getMessage() }, t);
        }
        // change state to IDLE only if the rule has not been DISABLED.
        synchronized (this) {
            if (getRuleStatus(ruleUID) == RuleStatus.RUNNING) {
                setRuleStatusInfo(ruleUID, new RuleStatusInfo(RuleStatus.IDLE), true);
            }
        }
    }

    protected void runNow(String ruleUID) {
        runNow(ruleUID, false, null);
    }

    protected void clearContext(RuntimeRule rule) {
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
        if (outputs != null) {
            for (Map.Entry<String, ?> entry : outputs.entrySet()) {
                String key = moduleUID + OUTPUT_SEPARATOR + entry.getKey();
                context.put(key, entry.getValue());
            }
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
                    final Object value = ReferenceResolverUtil.resolveReference(ref, context);

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
        if (conditions.size() == 0) {
            return true;
        }
        RuleStatus ruleStatus = null;
        for (Iterator<Condition> it = conditions.iterator(); it.hasNext();) {
            ruleStatus = getRuleStatus(rule.getUID());
            if (ruleStatus != RuleStatus.RUNNING) {
                return false;
            }
            RuntimeCondition c = (RuntimeCondition) it.next();
            ConditionHandler tHandler = c.getModuleHandler();
            Map<String, Object> context = getContext(rule.getUID(), c.getConnections());
            if (!tHandler.isSatisfied(Collections.unmodifiableMap(context))) {
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
    private void executeActions(Rule rule, boolean stopOnFirstFail) {
        List<Action> actions = ((RuntimeRule) rule).getActions();
        if (actions == null || actions.size() == 0) {
            return;
        }
        RuleStatus ruleStatus = null;
        RuntimeAction action = null;
        for (Iterator<Action> it = actions.iterator(); it.hasNext();) {
            ruleStatus = getRuleStatus(rule.getUID());
            if (ruleStatus != RuleStatus.RUNNING) {
                return;
            }
            action = (RuntimeAction) it.next();
            ActionHandler aHandler = action.getModuleHandler();
            String rUID = rule.getUID();
            Map<String, Object> context = getContext(rUID, action.getConnections());
            try {

                Map<String, ?> outputs = aHandler.execute(Collections.unmodifiableMap(context));
                if (outputs != null) {
                    context = getContext(rUID);
                    updateContext(rUID, action.getId(), outputs);
                }
            } catch (Throwable t) {
                String errMessage = "Fail to execute action: " + action != null ? action.getId() : "<unknown>";
                if (stopOnFirstFail) {
                    RuntimeException re = new RuntimeException(errMessage, t);
                    throw re;
                } else {
                    logger.warn(errMessage, t);
                }
            }
        }

    }

    /**
     * The method clean used resource by rule engine when it is stopped.
     */
    public synchronized void dispose() {
        if (!isDisposed) {
            isDisposed = true;
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

        for (Future f : scheduleTasks.values()) {
            f.cancel(true);
        }
        if (scheduleTasks.isEmpty()) {
            if (executor != null) {
                executor.shutdown();
                executor = null;
            }
        }
        scheduleTasks = null;

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
    protected RuleStatus getRuleStatus(String rUID) {
        RuleStatusInfo info = getRuleStatusInfo(rUID);
        RuleStatus status = null;
        if (info != null) {
            status = info.getStatus();
        }
        return status;
    }

    /**
     * This method gets rule's status info object.
     *
     * @param rUID rule uid
     * @return status of the rule or null when such rule does not exists.
     */
    protected synchronized RuleStatusInfo getRuleStatusInfo(String rUID) {
        return statusMap.get(rUID);
    }

    protected synchronized @NonNull String getUniqueId() {
        int result = 0;
        if (rules != null) {
            Set<String> col = rules.keySet();
            if (col != null) {
                for (Iterator<String> it = col.iterator(); it.hasNext();) {
                    String rUID = it.next();
                    if (rUID != null && rUID.startsWith(ID_PREFIX)) {
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
        }
        if (result > ruleMaxID) {
            ruleMaxID = result + 1;
        } else {
            ++ruleMaxID;
        }
        return ID_PREFIX + ruleMaxID;
    }

    protected void setStatusInfoCallback(StatusInfoCallback statusInfoCallback) {
        this.statusInfoCallback = statusInfoCallback;
    }

    private ScheduledExecutorService getScheduledExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        return executor;
    }

    protected void scheduleRulesConfigurationUpdated(Map<String, Object> config) {
        if (config != null) {
            Object value = config.get(CONFIG_PROPERTY_REINITIALIZATION_DELAY);
            if (value != null) {
                if (value instanceof Number) {
                    scheduleReinitializationDelay = ((Number) value).longValue();
                } else {
                    logger.error("Invalid configuration value: {}. It MUST be Number.", value);
                }
            } else {
                scheduleReinitializationDelay = DEFAULT_REINITIALIZATION_DELAY;
            }
        } else {
            scheduleReinitializationDelay = DEFAULT_REINITIALIZATION_DELAY;
        }
    }

    /**
     * The auto mapping tries to link not connected module inputs to output of other modules. The auto mapping will link
     * input to output only when following criteria are done: 1) input must not be connected. The auto mapping will not
     * overwrite explicit connections done by the user. 2) input tags must be subset of the output tags. 3) condition
     * inputs can be connected only to triggers' outputs 4) action outputs can be connected to both conditions and
     * actions
     * outputs 5) There is only one output, based on previous criteria, where the input can connect to. If more then one
     * candidate outputs exists for connection, this is a conflict and the auto mapping leaves the input unconnected.
     * Auto
     * mapping is always applied when the rule is added or updated. It changes initial value of inputs of conditions and
     * actions participating in the rule. If an "auto map" connection has to be removed, the tags of corresponding
     * input/output have to be changed.
     *
     * @param r updated rule
     */
    private void autoMapConnections(RuntimeRule r) {
        Map<Set<String>, OutputRef> triggerOutputTags = new HashMap<Set<String>, OutputRef>(11);
        for (Trigger t : r.getTriggers()) {
            TriggerType tt = (TriggerType) mtRegistry.get(t.getTypeUID());
            if (tt != null) {
                initTagsMap(t.getId(), tt.getOutputs(), triggerOutputTags);
            }
        }
        Map<Set<String>, OutputRef> actionOutputTags = new HashMap<Set<String>, OutputRef>(11);
        for (Action a : r.getActions()) {
            ActionType at = (ActionType) mtRegistry.get(a.getTypeUID());
            if (at != null) {
                initTagsMap(a.getId(), at.getOutputs(), actionOutputTags);
            }
        }

        // auto mapping of conditions
        if (!triggerOutputTags.isEmpty()) {
            for (Condition c : r.getConditions()) {
                boolean isConnectionChanged = false;
                ConditionType ct = (ConditionType) mtRegistry.get(c.getTypeUID());
                if (ct != null) {
                    Set<Connection> connections = ((RuntimeCondition) c).getConnections();

                    for (Input input : ct.getInputs()) {
                        if (isConnected(input, connections)) {
                            continue; // the input is already connected. Skip it.
                        }
                        if (addAutoMapConnections(input, triggerOutputTags, connections)) {
                            isConnectionChanged = true;
                        }
                    }
                    if (isConnectionChanged) {
                        // update condition inputs
                        connections = ((RuntimeCondition) c).getConnections();
                        Map<String, String> connectionMap = getConnectionMap(connections);
                        c.setInputs(connectionMap);
                    }
                }
            }
        }

        // auto mapping of actions
        if (!triggerOutputTags.isEmpty() || !actionOutputTags.isEmpty()) {
            for (Action a : r.getActions()) {
                boolean isConnectionChanged = false;
                ActionType at = (ActionType) mtRegistry.get(a.getTypeUID());
                if (at != null) {
                    Set<Connection> connections = ((RuntimeAction) a).getConnections();
                    for (Input input : at.getInputs()) {
                        if (isConnected(input, connections)) {
                            continue; // the input is already connected. Skip it.
                        }
                        if (addAutoMapConnections(input, triggerOutputTags, connections)) {
                            isConnectionChanged = true;
                        }
                        if (addAutoMapConnections(input, actionOutputTags, connections)) {
                            isConnectionChanged = true;
                        }
                    }
                    if (isConnectionChanged) {
                        // update condition inputs
                        connections = ((RuntimeAction) a).getConnections();
                        Map<String, String> connectionMap = getConnectionMap(connections);
                        a.setInputs(connectionMap);
                    }
                }
            }
        }
    }

    /**
     * Try to connect a free input to available outputs.
     *
     * @param input a free input which has to be connected
     * @param outputTagMap a map of set of tags to outptu references
     * @param currentConnections current connections of this module
     * @return true when only one output which meets auto mapping ctiteria is found. False otherwise.
     */
    private boolean addAutoMapConnections(Input input, Map<Set<String>, OutputRef> outputTagMap,
            Set<Connection> currentConnections) {
        boolean result = false;
        Set<String> inputTags = input.getTags();
        OutputRef outputRef = null;
        boolean conflict = false;
        if (inputTags.size() > 0) {
            for (Set<String> outTags : outputTagMap.keySet()) {
                if (outTags.containsAll(inputTags)) { // input tags must be subset of the output ones
                    if (outputRef == null) {
                        outputRef = outputTagMap.get(outTags);
                    } else {
                        conflict = true; // already exist candidate for autoMap
                        break;
                    }
                }
            }
            if (!conflict && outputRef != null) {
                if (currentConnections == null) {
                    currentConnections = new HashSet<Connection>(11);
                }
                currentConnections
                        .add(new Connection(input.getName(), outputRef.getModuleId(), outputRef.getOutputName()));
                result = true;
            }
        }
        return result;
    }

    private void initTagsMap(String moduleId, List<Output> outputs, Map<Set<String>, OutputRef> tagMap) {
        for (Output output : outputs) {
            Set<String> tags = output.getTags();
            if (tags.size() > 0) {
                if (tagMap.get(tags) != null) {
                    // this set of output tags already exists. (conflict)
                    tagMap.remove(tags);
                } else {
                    tagMap.put(tags, new OutputRef(moduleId, output.getName()));
                }
            }
        }
    }

    private boolean isConnected(Input input, Set<Connection> connections) {
        if (connections != null) {
            for (Connection connection : connections) {
                if (connection.getInputName().equals(input.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, String> getConnectionMap(Set<Connection> connections) {
        Map<String, String> connectionMap = new HashMap<String, String>(11);
        for (Connection connection : connections) {
            connectionMap.put(connection.getInputName(),
                    connection.getOuputModuleId() + "." + connection.getOutputName());
        }
        return connectionMap;
    }

    class OutputRef {

        private String moduleId;
        private String outputName;

        public OutputRef(String moduleId, String outputName) {
            this.moduleId = moduleId;
            this.outputName = outputName;
        }

        public String getModuleId() {
            return moduleId;
        }

        public String getOutputName() {
            return outputName;
        }
    }

    protected void resolveConfiguration(Rule rule) {
        List<ConfigDescriptionParameter> configDescriptions = rule.getConfigurationDescriptions();
        Map<String, Object> configuration = rule.getConfiguration().getProperties();
        if (configuration != null) {
            handleModuleConfigReferences(rule.getTriggers(), configuration);
            handleModuleConfigReferences(rule.getConditions(), configuration);
            handleModuleConfigReferences(rule.getActions(), configuration);
        }
        normalizeRuleConfigurations(rule);
        validateConfiguration(rule.getUID(), configDescriptions, new HashMap<String, Object>(configuration));
    }

    private void validateConfiguration(String uid, List<ConfigDescriptionParameter> configDescriptions,
            Map<String, Object> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            if (isOptionalConfig(configDescriptions)) {
                return;
            } else {
                for (ConfigDescriptionParameter configParameter : configDescriptions) {
                    if (configParameter.isRequired()) {
                        logger.error("Missing required configuration property '{}' for rule with UID '{}'!",
                                configParameter.getName(), uid);
                    }
                }
                throw new IllegalArgumentException("Missing required configuration properties!");
            }
        } else {
            for (ConfigDescriptionParameter configParameter : configDescriptions) {
                String configParameterName = configParameter.getName();
                processValue(configurations.remove(configParameterName), configParameter);
            }
            for (String name : configurations.keySet()) {
                logger.error("Extra configuration property '{}' for rule with UID '{}'!", name, uid);
            }
            if (!configurations.isEmpty()) {
                throw new IllegalArgumentException("Extra configuration properties!");
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
        if (configParameter.isRequired()) {
            throw new IllegalArgumentException(
                    "Required configuration property missing: \"" + configParameter.getName() + "\"!");
        }
    }

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
                        || configValue instanceof Double && ((Double) configValue).intValue() == (Double) configValue;
            case DECIMAL:
                return configValue instanceof BigDecimal || configValue instanceof Double;
        }
        return false;
    }

    private void handleModuleConfigReferences(List<? extends Module> modules, Map<String, ?> ruleConfiguration) {
        if (modules != null) {
            for (Module module : modules) {
                ReferenceResolverUtil.updateModuleConfiguration(module, ruleConfiguration);
            }
        }
    }

    private void normalizeRuleConfigurations(Rule rule) {
        List<ConfigDescriptionParameter> configDescriptions = rule.getConfigurationDescriptions();
        Map<String, ConfigDescriptionParameter> mapConfigDescriptions;
        if (configDescriptions != null) {
            mapConfigDescriptions = getConfigDescriptionMap(configDescriptions);
            normalizeConfiguration(rule.getConfiguration(), mapConfigDescriptions);
        }
        normalizeModuleConfigurations(rule.getTriggers());
        normalizeModuleConfigurations(rule.getConditions());
        normalizeModuleConfigurations(rule.getActions());

    }

    private <T extends Module> void normalizeModuleConfigurations(List<@NonNull T> modules) {
        for (Module module : modules) {
            Configuration config = module.getConfiguration();
            if (config != null) {
                String type = module.getTypeUID();
                ModuleType mt = mtRegistry.get(type);
                if (mt != null) {
                    List<ConfigDescriptionParameter> configDescriptions = mt.getConfigurationDescriptions();
                    if (configDescriptions != null) {
                        Map<String, ConfigDescriptionParameter> mapConfigDescriptions = getConfigDescriptionMap(
                                configDescriptions);
                        normalizeConfiguration(config, mapConfigDescriptions);
                    }
                }
            }
        }
    }

    private Map<String, ConfigDescriptionParameter> getConfigDescriptionMap(
            List<ConfigDescriptionParameter> configDesc) {
        Map<String, ConfigDescriptionParameter> mapConfigDescs = null;
        if (configDesc != null) {
            for (ConfigDescriptionParameter configDescriptionParameter : configDesc) {
                if (mapConfigDescs == null) {
                    mapConfigDescs = new HashMap<String, ConfigDescriptionParameter>();
                }
                mapConfigDescs.put(configDescriptionParameter.getName(), configDescriptionParameter);
            }
        }
        return mapConfigDescs;
    }

    private void normalizeConfiguration(Configuration config, Map<String, ConfigDescriptionParameter> mapCD) {
        if (config != null && mapCD != null) {
            for (String propName : mapCD.keySet()) {
                ConfigDescriptionParameter cd = mapCD.get(propName);
                if (cd != null) {
                    Object tmp = config.get(propName);
                    Object defaultValue = cd.getDefault();
                    if (tmp == null && defaultValue != null) {
                        config.put(propName, defaultValue);
                    }

                    if (cd.isMultiple()) {
                        tmp = config.get(propName);
                        if (tmp != null && tmp instanceof String) {
                            String sValue = (String) tmp;
                            if (gson == null) {
                                gson = new Gson();
                            }
                            try {
                                Object value = gson.fromJson(sValue, List.class);
                                config.put(propName, value);
                            } catch (JsonSyntaxException e) {
                                logger.error("Can't parse {} to list value.", sValue, e);
                            }
                            continue;
                        }
                    }
                }
                Object value = ConfigUtil.normalizeType(config.get(propName), cd);
                if (value != null) {
                    config.put(propName, value);
                }
            }
        }
    }

}