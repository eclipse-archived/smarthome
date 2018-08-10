/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleManager;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusDetail;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.internal.TriggerHandlerCallbackImpl.TriggerData;
import org.eclipse.smarthome.automation.core.internal.composite.CompositeModuleHandlerFactory;
import org.eclipse.smarthome.automation.core.internal.ruleengine.WrappedAction;
import org.eclipse.smarthome.automation.core.internal.ruleengine.WrappedCondition;
import org.eclipse.smarthome.automation.core.internal.ruleengine.WrappedModule;
import org.eclipse.smarthome.automation.core.internal.ruleengine.WrappedRule;
import org.eclipse.smarthome.automation.core.internal.ruleengine.WrappedTrigger;
import org.eclipse.smarthome.automation.core.util.ConfigurationNormalizer;
import org.eclipse.smarthome.automation.core.util.ReferenceResolver;
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerCallback;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.handler.TriggerHandlerCallback;
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
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible to initialize and execute {@link Rule}s, when the {@link Rule}s are added in rule
 * engine. Each {@link Rule} has associated {@link RuleStatusInfo} object which shows its {@link RuleStatus} and
 * {@link RuleStatusDetail}. The states are self excluded and they are:
 * <LI><b>disabled</b> - the rule is temporary not available. This status is set by the user.
 * <LI><b>uninitialized</b> - the rule is enabled, but it is still not working, because some of the module handlers are
 * not available or its module types or template are not resolved. The initialization problem is described by the status
 * details.
 * <LI><b>idle</b> - the rule is enabled and initialized and it is waiting for triggering events.
 * <LI><b>running</b> - the rule is enabled and initialized and it is executing at the moment. When the execution is
 * finished, it goes to the <b>idle</b> state.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider, registry implementation and customized modules
 * @author Benedikt Niehues - change behavior for unregistering ModuleHandler
 * @author Markus Rathgeb - use a managed rule
 *
 */
@SuppressWarnings("rawtypes")
@Component(immediate = true, service = RuleManager.class)
@NonNullByDefault
public class RuleEngine implements RuleManager, RegistryChangeListener<ModuleType> {

    /**
     * Defines separator between module id and output name.
     */
    public static final char OUTPUT_SEPARATOR = '.';

    static final String DISABLED_RULE_STORAGE = "automation_rules_disabled";

    /**
     * Defines the delay between rule's re-initialization tries.
     */
    private volatile long scheduleReinitializationDelay;

    private final Map<String, WrappedRule> managedRules = new ConcurrentHashMap<>();

    /**
     * {@link Map} holding all created {@link TriggerHandlerCallback} instances, corresponding to each {@link Rule}.
     * There is only one {@link TriggerHandlerCallback} instance per {@link Rule}. The relation is {@link Rule}'s UID to
     * {@link TriggerHandlerCallback} instance.
     */
    private final Map<String, TriggerHandlerCallbackImpl> thCallbacks = new ConcurrentHashMap<>();

    /**
     * {@link Map} holding all {@link ModuleType} UIDs that are available in some rule's module definition. The relation
     * is {@link ModuleType}'s UID to {@link Set} of {@link Rule} UIDs.
     */
    private final Map<String, Set<String>> mapModuleTypeToRules = new ConcurrentHashMap<>();

    /**
     * {@link Map} holding all available {@link ModuleHandlerFactory}s linked with {@link ModuleType}s that they
     * supporting. The relation is {@link ModuleType}'s UID to {@link ModuleHandlerFactory} instance.
     */
    private final Map<String, ModuleHandlerFactory> mapModuleTypeToHandlerFactory = new ConcurrentHashMap<>();

    /**
     * {@link Set} holding all available {@link ModuleHandlerFactory}s.
     */
    private final Set<ModuleHandlerFactory> moduleHandlerFactories = new CopyOnWriteArraySet<>();

    /**
     * The storage for the disable information.
     */
    private @NonNullByDefault({}) Storage<Boolean> disabledRulesStorage;

    /**
     * Locker which does not permit rule initialization when the rule engine is stopping.
     */
    private volatile boolean isDisposed;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock rLock = rwLock.readLock();
    private final Lock wLock = rwLock.writeLock();

    protected Logger logger = LoggerFactory.getLogger(RuleEngine.class.getName());

    /**
     * Holds reference to {@link RuleRegistry}. The {@link RuleEngine} needs it to be notified when a {@link Rule} is
     * added, removed or updated and to has permanent access to the available rules.
     */
    private @NonNullByDefault({}) RuleRegistryImpl ruleRegistry;

    /**
     * Holds reference to {@link ModuleTypeRegistry}. The {@link RuleEngine} needs it to auto-map connections
     * between rule's modules and to determine module handlers.
     */
    private @NonNullByDefault({}) ModuleTypeRegistry mtRegistry;

    /**
     * Provides all composite {@link ModuleHandler}s.
     */
    private @NonNullByDefault({}) CompositeModuleHandlerFactory compositeFactory;

    /**
     * {@link Map} holding all scheduled {@link Rule} re-initialization tasks. The relation is {@link Rule}'s UID to
     * re-initialization task as a {@link Future} instance.
     */
    private final Map<String, Future<?>> scheduleTasks = new ConcurrentHashMap<>();

    /**
     * Performs the {@link Rule} re-initialization tasks.
     */
    private @Nullable ScheduledExecutorService executor;

    /**
     * This field holds {@link RegistryChangeListener} that listen for a single {@link Rule} has been added, updated,
     * enabled, disabled or removed from the rule registry and involves {@link RuleEngine}, which is already a
     * RegistryChangeListener for module types.
     */
    private @NonNullByDefault({}) RegistryChangeListener<Rule> listener;

    /**
     * Posts an event through the event bus in an asynchronous way. {@link RuleEngine} use it for posting the
     * {@link RuleStatusInfoEvent}.
     */
    private @Nullable EventPublisher eventPublisher;
    private static final String SOURCE = RuleEngine.class.getSimpleName();

    private final ModuleHandlerCallback moduleHandlerCallback = new ModuleHandlerCallback() {

        @Override
        public @Nullable Boolean isEnabled(String ruleUID) {
            return RuleEngine.this.isEnabled(ruleUID);
        }

        @Override
        public void setEnabled(String uid, boolean isEnabled) {
            RuleEngine.this.setEnabled(uid, isEnabled);
        }

        @Override
        public @Nullable RuleStatusInfo getStatusInfo(String ruleUID) {
            return RuleEngine.this.getStatusInfo(ruleUID);
        }

        @Override
        public @Nullable RuleStatus getStatus(String ruleUID) {
            return RuleEngine.this.getStatus(ruleUID);
        }

        @Override
        public void runNow(String uid) {
            RuleEngine.this.runNow(uid);
        }

        @Override
        public void runNow(String uid, boolean considerConditions, @Nullable Map<String, Object> context) {
            RuleEngine.this.runNow(uid, considerConditions, context);
        }

    };

    /**
     * This method is used to create a {@link CompositeModuleHandlerFactory} that handles all composite
     * {@link ModuleType}s. Called from DS to activate the rule engine component.
     *
     * @param context a {@link BundleContext} used for {@link CompositeModuleHandlerFactory} creation.
     */
    @Activate
    protected void activate(BundleContext context) {
        wLock.lock();
        try {
            isDisposed = false;
            compositeFactory = new CompositeModuleHandlerFactory(mtRegistry, this);
            scheduleReinitializationDelay = this.ruleRegistry.getScheduleReinitializationDelay();
            listener = new RegistryChangeListener<Rule>() {
                @Override
                public void added(Rule rule) {
                    RuleEngine.this.addRule(null, rule);
                }

                @Override
                public void removed(Rule rule) {
                    RuleEngine.this.removeRule(rule);
                }

                @Override
                public void updated(Rule oldRule, Rule rule) {
                    RuleEngine.this.addRule(oldRule, rule);
                }
            };
            ruleRegistry.addRegistryChangeListener(listener);
            for (Rule rule : ruleRegistry.getAll()) {
                addRule(null, rule);
            }
        } finally {
            wLock.unlock();
        }
    }

    /**
     * Bind the {@link ModuleTypeRegistry} service - called from DS.
     *
     * @param moduleTypeRegistry a {@link ModuleTypeRegistry} service.
     */
    @Reference
    protected void setModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        mtRegistry = moduleTypeRegistry;
        mtRegistry.addRegistryChangeListener(this);
    }

    /**
     * Unbind the {@link ModuleTypeRegistry} service - called from DS.
     *
     * @param moduleTypeRegistry a {@link ModuleTypeRegistry} service.
     */
    protected void unsetModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        mtRegistry.removeRegistryChangeListener(this);
        mtRegistry = null;
    }

    /**
     * Bind the {@link RuleRegistry} service - called from DS.
     *
     * @param ruleRegistry a {@link RuleRegistry} service.
     */
    @Reference
    protected void setRuleRegistry(RuleRegistry ruleRegistry) {
        if (ruleRegistry instanceof RuleRegistryImpl) {
            this.ruleRegistry = (RuleRegistryImpl) ruleRegistry;
        } else {
            logger.error("Unexpected RuleRegistry service: {}", ruleRegistry);
        }
    }

    /**
     * Unbind the {@link RuleRegistry} service - called from DS.
     *
     * @param ruleRegistry a {@link RuleRegistry} service.
     */
    protected void unsetRuleRegistry(RuleRegistry ruleRegistry) {
        if (this.ruleRegistry == ruleRegistry) {
            ruleRegistry.removeRegistryChangeListener(listener);
            listener = null;
            this.ruleRegistry = null;
        }
    }

    /**
     * Bind the {@link StorageService} - called from DS.
     *
     * @param storageService the {@link StorageService} instance.
     */
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setStorageService(StorageService storageService) {
        this.disabledRulesStorage = storageService.<Boolean> getStorage(DISABLED_RULE_STORAGE,
                this.getClass().getClassLoader());
    }

    /**
     * Unbind the {@link StorageService} - called from DS.
     *
     * @param storageService the {@link StorageService} instance.
     */
    protected void unsetStorageService(StorageService storageService) {
        this.disabledRulesStorage = null;
    }

    @Override
    public void added(ModuleType moduleType) {
        rLock.lock();
        try {
            if (isDisposed) {
                return;
            }
            String moduleTypeUID = moduleType.getUID();
            for (ModuleHandlerFactory moduleHandlerFactory : moduleHandlerFactories) {
                Collection<String> moduleTypes = moduleHandlerFactory.getTypes();
                if (moduleTypes.contains(moduleTypeUID)) {
                    mapModuleTypeToHandlerFactory.put(moduleTypeUID, moduleHandlerFactory);
                    break;
                }
            }
            Set<String> rules = mapModuleTypeToRules.get(moduleTypeUID);
            if (rules != null) {
                for (String rUID : new HashSet<>(rules)) {
                    RuleStatus ruleStatus = getManagedRule(rUID).getStatus();
                    if (ruleStatus == RuleStatus.UNINITIALIZED) {
                        scheduleRuleInitialization(rUID);
                    }
                }
            }
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public void removed(ModuleType moduleType) {
        // removing module types does not effect the rule
    }

    @Override
    public void updated(ModuleType oldElement, ModuleType moduleType) {
        rLock.lock();
        try {
            if (isDisposed || moduleType.equals(oldElement)) {
                return;
            }
            String moduleTypeUID = moduleType.getUID();
            Set<String> rules = mapModuleTypeToRules.get(moduleTypeUID);
            if (rules != null) {
                for (String rUID : new HashSet<>(rules)) {
                    WrappedRule rule = getManagedRule(rUID);
                    switch (rule.getStatus()) {
                        case IDLE:
                        case RUNNING:
                            unregister(rule, RuleStatusDetail.HANDLER_MISSING_ERROR,
                                    "Update Module Type " + moduleTypeUID);
                            // fall through
                        case UNINITIALIZED:
                            scheduleRuleInitialization(rUID);
                        default:
                            break;
                    }
                }
            }
        } finally {
            rLock.unlock();
        }
    }

    /**
     * Bind the {@link ModuleHandlerFactory} service - called from DS.
     *
     * @param moduleHandlerFactory a {@link ModuleHandlerFactory} service.
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addModuleHandlerFactory(ModuleHandlerFactory moduleHandlerFactory) {
        logger.debug("ModuleHandlerFactory added.");
        moduleHandlerFactories.add(moduleHandlerFactory);
        Set<String> uninitailizedRules = new HashSet<>(20);
        for (String moduleTypeUID : moduleHandlerFactory.getTypes()) {
            Set<String> rulesCopy = new HashSet<>();
            Set<String> rules = mapModuleTypeToRules.get(moduleTypeUID);
            if (rules != null) {
                rulesCopy.addAll(rules);
            }
            mapModuleTypeToHandlerFactory.put(moduleTypeUID, moduleHandlerFactory);
            for (String rUID : rulesCopy) {
                if (getManagedRule(rUID).getStatus() == RuleStatus.UNINITIALIZED) {
                    uninitailizedRules.add(rUID);
                }
            }
        }
        for (final String rUID : uninitailizedRules) {
            scheduleRuleInitialization(rUID);
        }
    }

    /**
     * Unbind the {@link ModuleHandlerFactory} service - called from DS.
     *
     * @param moduleHandlerFactory a {@link ModuleHandlerFactory} service.
     */
    protected void removeModuleHandlerFactory(ModuleHandlerFactory moduleHandlerFactory) {
        if (moduleHandlerFactory instanceof CompositeModuleHandlerFactory) {
            compositeFactory.deactivate();
            compositeFactory = null;
        }
        moduleHandlerFactories.remove(moduleHandlerFactory);
        Collection<String> moduleTypes = moduleHandlerFactory.getTypes();
        removeMissingModuleTypes(moduleTypes);
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            mapModuleTypeToHandlerFactory.remove(moduleTypeName);
        }
    }

    /**
     * This method add a new rule into rule engine. Scope identity of the Rule is the identity of the caller.
     *
     * @param oldRule the rule that should be replaced with the new one.
     * @param rule    a rule which has to be added.
     */
    protected void addRule(@Nullable Rule oldRule, Rule rule) {
        rLock.lock();
        try {
            if (isDisposed) {
                throw new IllegalStateException("Rule Engine is disposed!");
            }
            String rUID = rule.getUID();
            WrappedRule wRule = new WrappedRule(rule);
            RuleStatusInfo initStatus = (disabledRulesStorage == null || disabledRulesStorage.get(rUID) == null)
                    ? wRule.getStatusInfo()
                    : new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.DISABLED);
            setStatus(wRule, new RuleStatusInfo(RuleStatus.INITIALIZING));
            WrappedRule wOldRule = getManagedRule(rUID);
            managedRules.put(rUID, wRule);
            if (oldRule != null && wOldRule != null) {
                unregister(wOldRule);
            }
            if (isEnabled(rUID) && RuleStatusDetail.NONE.equals(initStatus.getStatusDetail())) {
                setRule(wRule);
            } else {
                setStatus(wRule, initStatus);
            }
        } finally {
            rLock.unlock();
        }
    }

    /**
     * This method tries to initialize the rule. It uses available {@link ModuleHandlerFactory}s to create
     * {@link ModuleHandler}s for all {@link Module}s of the {@link Rule} and to link them. When all the modules have
     * associated module handlers then the {@link Rule} is initialized and it is ready to working. It goes into idle
     * state. Otherwise the Rule stays into not initialized and continue to wait missing handlers, module types or
     * templates.
     *
     * @param rule a rule which tries to be initialized.
     */
    private void setRule(WrappedRule rule) {
        rLock.lock();
        try {
            if (isDisposed) {
                return;
            }
            String rUID = rule.getUID();
            for (final WrappedAction action : rule.getActions()) {
                updateMapModuleTypeToRule(rUID, action.unwrap().getTypeUID());
                action.setConnections(resolveConnections(action.getInputs()));
            }
            for (final WrappedCondition condition : rule.getConditions()) {
                updateMapModuleTypeToRule(rUID, condition.unwrap().getTypeUID());
                condition.setConnections(resolveConnections(condition.getInputs()));
            }
            for (final WrappedTrigger trigger : rule.getTriggers()) {
                updateMapModuleTypeToRule(rUID, trigger.unwrap().getTypeUID());
            }
            try {
                ConfigurationNormalizer.normalizeModuleConfigurations(rule.unwrap().getModules(), mtRegistry);
            } catch (RuntimeException e) {
                // change status to UNINITIALIZED
                setStatus(rule, new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.CONFIGURATION_ERROR,
                        e.getLocalizedMessage()));
                return;
            }
            try {
                autoMapConnections(rule);
                ConnectionValidator.validateConnections(mtRegistry, rule.unwrap());
            } catch (RuntimeException e) {
                // change status to UNINITIALIZED
                setStatus(rule, new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.INVALID_RULE,
                        "Validation of rule " + rUID + " has failed! " + e.getLocalizedMessage()));
                return;
            }
            final String errMsgs = setModuleHandlers(rUID, rule.getModules());
            if (errMsgs == null) {
                removeScheduledTask(rUID);
                try {
                    register(rule);
                    // change status to IDLE
                    setStatus(rule, new RuleStatusInfo(RuleStatus.IDLE));
                } catch (Exception e) {
                    setStatus(rule, new RuleStatusInfo(RuleStatus.UNINITIALIZED,
                            RuleStatusDetail.HANDLER_INITIALIZING_ERROR, e.getLocalizedMessage()));
                }
            } else {
                // change status to UNINITIALIZED
                unregister(rule, RuleStatusDetail.HANDLER_INITIALIZING_ERROR, errMsgs);
            }
        } finally {
            rLock.unlock();
        }
    }

    private void removeScheduledTask(String rUID) {
        rLock.lock();
        try {
            if (isDisposed) {
                return;
            }
            cancelFuture(scheduleTasks.remove(rUID));
        } finally {
            rLock.unlock();
        }
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    /**
     * This method can be used in order to post events through the Eclipse SmartHome events bus. A common
     * use case is to notify event subscribers about the {@link Rule}'s status change.
     *
     * @param ruleUID    the UID of the {@link Rule}, whose status is changed.
     * @param statusInfo the new {@link Rule}s status.
     */
    protected void postRuleStatusInfoEvent(String ruleUID, RuleStatusInfo statusInfo) {
        if (eventPublisher != null) {
            Event event = RuleEventFactory.createRuleStatusInfoEvent(statusInfo, ruleUID, SOURCE);
            try {
                eventPublisher.post(event);
            } catch (Exception ex) {
                logger.error("Could not post event of type '{}'.", event.getType(), ex);
            }
        }
    }

    /**
     * This method links modules to corresponding module handlers.
     *
     * @param rUID    id of rule containing these modules
     * @param modules list of modules
     * @return null when all modules are connected or list of RuleErrors for missing handlers.
     */
    private <T extends WrappedModule<?, ?>> @Nullable String setModuleHandlers(String rUID, List<T> modules) {
        StringBuffer sb = null;
        if (modules != null) {
            for (T mm : modules) {
                final Module m = mm.unwrap();
                try {
                    ModuleHandler moduleHandler = getModuleHandler(m, rUID);
                    if (moduleHandler != null) {
                        if (mm instanceof WrappedAction) {
                            ((WrappedAction) mm).setModuleHandler((ActionHandler) moduleHandler);
                        } else if (mm instanceof WrappedCondition) {
                            ((WrappedCondition) mm).setModuleHandler((ConditionHandler) moduleHandler);
                        } else if (mm instanceof WrappedTrigger) {
                            ((WrappedTrigger) mm).setModuleHandler((TriggerHandler) moduleHandler);
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
     * Gets {@link TriggerHandlerCallback} for passed {@link Rule}. If it does not exists, a callback object is
     * created.
     *
     * @param ruleUID specifies the rule object for which the callback is looking for.
     * @return a {@link TriggerHandlerCallback} corresponding to the passed {@link Rule}s UID.
     */
    private synchronized TriggerHandlerCallbackImpl getTriggerHandlerCallback(String ruleUID) {
        TriggerHandlerCallbackImpl result = thCallbacks.get(ruleUID);
        if (result == null) {
            result = new TriggerHandlerCallbackImpl(this, ruleUID);
            thCallbacks.put(ruleUID, result);
        }
        return result;
    }

    /**
     * Unlink module handlers from their modules. The method is called when the rule containing these modules goes into
     * {@link RuleStatus#UNINITIALIZED} state.
     *
     * @param modules list of modules which should be disconnected.
     * @param ruleUID specifies the rule object for which the ModuleHandlers are removed.
     */
    private <T extends WrappedModule<?, ?>> void removeModuleHandlers(List<T> modules, String ruleUID) {
        if (modules != null) {
            for (T mm : modules) {
                final Module m = mm.unwrap();
                ModuleHandler handler = mm.getModuleHandler();

                if (handler != null) {
                    ModuleHandlerFactory factory = getModuleHandlerFactory(m.getTypeUID());
                    if (factory != null) {
                        factory.ungetHandler(m, ruleUID, handler);
                    }
                    mm.setModuleHandler(null);
                }
            }
        }
    }

    /**
     * Registers a Rule to start working. This is the final step of initialization process where triggers received
     * {@link TriggerHandlerCallback}s object and starts to notify the rule engine when they are triggered. After
     * activating all triggers the rule goes into IDLE state.
     *
     * @param rule an initialized rule which has to starts tracking the triggers.
     */
    private void register(WrappedRule rule) {
        final String ruleUID = rule.getUID();

        TriggerHandlerCallback thCallback = getTriggerHandlerCallback(ruleUID);
        rule.getTriggers().forEach(trigger -> {
            TriggerHandler triggerHandler = trigger.getModuleHandler();
            if (triggerHandler != null) {
                triggerHandler.setCallback(thCallback);
            }
        });
        rule.getConditions().forEach(condition -> {
            ConditionHandler conditionHandler = condition.getModuleHandler();
            if (conditionHandler != null) {
                conditionHandler.setCallback(moduleHandlerCallback);
            }
        });
        rule.getActions().forEach(action -> {
            ActionHandler actionHandler = action.getModuleHandler();
            if (actionHandler != null) {
                actionHandler.setCallback(moduleHandlerCallback);
            }
        });
    }

    /**
     * Unregisters a Rule and it stops working. It is called when some {@link ModuleHandlerFactory} is
     * disposed or some {@link ModuleType} is updated. The Rule is available but its state should become
     * {@link RuleStatus#UNINITIALIZED}.
     *
     * @param rule   rule that should be unregistered.
     * @param detail provides the {@link RuleStatusDetail}, corresponding to the new <b>uninitialized</b> status, should
     *               be {@code null} if the status will be skipped.
     * @param msg    provides the {@link RuleStatusInfo} description, corresponding to the new <b>uninitialized</b>
     *               status, should be {@code null} if the status will be skipped.
     */
    private void unregister(WrappedRule rule, RuleStatusDetail detail, @Nullable String msg) {
        setStatus(rule, new RuleStatusInfo(RuleStatus.UNINITIALIZED, detail, msg));
        unregister(rule);
    }

    /**
     * This method unregister a {@link Rule} and it stops working. It is called when the {@link Rule} is removed,
     * updated or disabled. Also it is called when some {@link ModuleHandlerFactory} is disposed or some
     * {@link ModuleType} is updated.
     *
     * @param r rule that should be unregistered.
     */
    private void unregister(WrappedRule r) {
        String rUID = r.getUID();
        TriggerHandlerCallbackImpl reCallback = null;
        synchronized (thCallbacks) {
            reCallback = thCallbacks.remove(rUID);
        }
        if (reCallback != null) {
            reCallback.dispose();
        }
        removeModuleHandlers(r.getModules(), rUID);
    }

    /**
     * Obtains a {@link ModuleHandler} for a specified {@link Module}.
     *
     * @param m       the {@link Module} which is looking for a handler.
     * @param ruleUID UID of the {@link Rule} that the specified {@link Module} belongs to.
     * @return handler that processing this module. Could be {@code null} if the {@link ModuleHandlerFactory} is not
     *         available.
     */
    private @Nullable ModuleHandler getModuleHandler(Module m, String ruleUID) {
        String moduleTypeId = m.getTypeUID();
        ModuleHandlerFactory mhf = getModuleHandlerFactory(moduleTypeId);
        if (mhf == null || mtRegistry.get(moduleTypeId) == null) {
            return null;
        }
        return mhf.getHandler(m, ruleUID);
    }

    /**
     * Gets the {@link ModuleHandlerFactory} for a {@link ModuleType} with the specified UID.
     *
     * @param moduleTypeId the UID of the {@link ModuleType}.
     * @return the {@link ModuleHandlerFactory} responsible for the {@link ModuleType}.
     */
    public ModuleHandlerFactory getModuleHandlerFactory(String moduleTypeId) {
        ModuleHandlerFactory mhf = mapModuleTypeToHandlerFactory.get(moduleTypeId);
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

    /**
     * Updates the {@link ModuleType} to {@link Rule}s mapping. The method adds the {@link Rule}'s UID to the list of
     * {@link Rule}s that use this {@link ModuleType}.
     *
     * @param rUID         the UID of the {@link Rule}.
     * @param moduleTypeId the UID of the {@link ModuleType}.
     */
    public synchronized void updateMapModuleTypeToRule(String rUID, String moduleTypeId) {
        Set<String> rules = mapModuleTypeToRules.get(moduleTypeId) == null ? new HashSet<String>()
                : mapModuleTypeToRules.get(moduleTypeId);
        rules.add(rUID);
        mapModuleTypeToRules.put(moduleTypeId, rules);
    }

    /**
     * This method removes Rule from rule engine. It is called by the {@link RuleRegistry}.
     *
     * @param r the {@link Rule} that should be removed.
     * @return true when a rule is deleted, false when there is no rule with such id.
     */
    protected void removeRule(Rule r) {
        String rUID = r.getUID();
        unregister(managedRules.remove(rUID));
        List<String> toRemove = new LinkedList<>();
        for (Iterator<Map.Entry<String, Set<String>>> it = mapModuleTypeToRules.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Set<String>> e = it.next();
            Set<String> rules = e.getValue();
            if (rules.contains(rUID)) {
                rules.remove(rUID);
                if (rules.size() < 1) {
                    toRemove.add(e.getKey());
                }
            }
        }
        for (String key : toRemove) {
            mapModuleTypeToRules.remove(key, Collections.EMPTY_SET);
        }
        removeScheduledTask(rUID);
    }

    /**
     * Gets {@link WrappedRule} corresponding to the passed id. This method is used internally and it does not create a
     * copy of the rule.
     *
     * @param rUID the unique identifier of the desired rule.
     * @return internal {@link WrappedRule} object.
     */
    WrappedRule getManagedRule(String rUID) {
        return managedRules.get(rUID);
    }

    /**
     * Gets {@link Rule} corresponding to the passed id. This method is used internally and it does not create a copy of
     * the rule.
     *
     * @param rUID the unique identifier of the desired rule.
     * @return internal wrapped {@link Rule} object.
     */
    protected Rule getRule(String rUID) {
        return getManagedRule(rUID).unwrap();
    }

    @Override
    public void setEnabled(String uid, boolean enable) {
        final WrappedRule rule = managedRules.get(uid);
        if (rule == null) {
            throw new IllegalArgumentException(String.format("No rule with id=%s was found!", uid));
        }
        if (enable) {
            if (disabledRulesStorage != null) {
                disabledRulesStorage.remove(uid);
            }
            if (rule.getStatus() == RuleStatus.UNINITIALIZED) {
                setStatus(rule, new RuleStatusInfo(RuleStatus.INITIALIZING));
                setRule(rule);
            }
        } else {
            if (disabledRulesStorage != null) {
                disabledRulesStorage.put(uid, true);
            }
            if (rule.getStatus() == RuleStatus.IDLE || rule.getStatus() == RuleStatus.RUNNING) {
                unregister(rule, RuleStatusDetail.DISABLED, null);
            } else {
                setStatus(rule, new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.DISABLED));
            }
        }
    }

    @Override
    public @Nullable RuleStatusInfo getStatusInfo(String ruleUID) {
        if (ruleUID == null) {
            return null;
        }
        final WrappedRule rule = managedRules.get(ruleUID);
        if (rule == null) {
            return null;
        }
        return rule.getStatusInfo();
    }

    @Override
    public @Nullable RuleStatus getStatus(String ruleUID) {
        RuleStatusInfo statusInfo = getStatusInfo(ruleUID);
        return statusInfo == null ? null : statusInfo.getStatus();
    }

    @Override
    public @Nullable Boolean isEnabled(String ruleUID) {
        return getStatus(ruleUID) == null ? null
                : !getStatusInfo(ruleUID).getStatusDetail().equals(RuleStatusDetail.DISABLED);
    }

    /**
     * This method updates the status of the {@link Rule}
     *
     * @param ruleUID       unique id of the rule
     * @param newStatusInfo the new status of the rule
     */
    private void setStatus(WrappedRule rule, RuleStatusInfo newStatusInfo) {
        rule.setStatusInfo(newStatusInfo);
        postRuleStatusInfoEvent(rule.getUID(), newStatusInfo);
    }

    /**
     * Creates and schedules a re-initialization task for the {@link Rule} with the specified UID.
     *
     * @param rUID the UID of the {@link Rule}.
     */
    @SuppressWarnings("null")
    protected void scheduleRuleInitialization(final String rUID) {
        rLock.lock();
        try {
            if (isDisposed) {
                return;
            }
            if (executor == null) {
                executor = Executors.newSingleThreadScheduledExecutor();
            }
            scheduleTasks.compute(rUID, (key, f) -> {
                if (shouldReschedule(f)) {
                    f = executor.schedule(new Callable<RuleStatus>() {
                        @Override
                        public RuleStatus call() throws Exception {
                            WrappedRule rule = getManagedRule(rUID);
                            setRule(rule);
                            return rule.getStatusInfo().getStatus();
                        }
                    }, scheduleReinitializationDelay, TimeUnit.MILLISECONDS);
                }
                return f;
            });
        } finally {
            rLock.unlock();
        }
    }

    private boolean shouldReschedule(Future f) {
        try {
            return f == null || (f.isDone() && RuleStatus.UNINITIALIZED.equals(f.get()));
        } catch (InterruptedException | ExecutionException e) {
            return true;
        }
    }

    private void removeMissingModuleTypes(Collection<String> moduleTypes) {
        Map<String, List<String>> mapMissingHandlers = null;
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            Set<String> rulesCopy = new HashSet<>();
            Set<String> rules = mapModuleTypeToRules.get(moduleTypeName);
            if (rules != null) {
                rulesCopy.addAll(rules);
            }
            for (String rUID : rulesCopy) {
                RuleStatus ruleStatus = getManagedRule(rUID).getStatus();
                switch (ruleStatus) {
                    case RUNNING:
                    case IDLE:
                        mapMissingHandlers = mapMissingHandlers != null ? mapMissingHandlers : new HashMap<>(20);
                        @SuppressWarnings("null")
                        List<String> list = mapMissingHandlers.get(rUID) == null ? new ArrayList<>()
                                : mapMissingHandlers.get(rUID);
                        list.add(moduleTypeName);
                        mapMissingHandlers.put(rUID, list);
                        break;
                    default:
                        break;
                }
            } // for
        }
        if (mapMissingHandlers != null) {
            for (Entry<String, List<String>> e : mapMissingHandlers.entrySet()) {
                String rUID = e.getKey();
                List<String> missingTypes = e.getValue();
                StringBuffer sb = new StringBuffer();
                sb.append("Missing handlers: ");
                for (String typeUID : missingTypes) {
                    sb.append(typeUID).append(", ");
                }
                unregister(getManagedRule(rUID), RuleStatusDetail.HANDLER_MISSING_ERROR,
                        sb.substring(0, sb.length() - 2));
            }
        }
    }

    /**
     * This method runs a {@link Rule}. It is called by the {@link TriggerHandlerCallback}'s thread when a new
     * {@link TriggerData} is available.
     *
     * @param ruleUID a {@link Rule}s UID, which specifies the rule that should be executed.
     * @param td      {@link TriggerData} object containing new values from {@link Trigger}'s {@link Output}s.
     */
    protected void runRule(String ruleUID, TriggerHandlerCallbackImpl.TriggerData td) {
        try {
            runNow(ruleUID, true, getContext(td));
        } catch (IllegalArgumentException e) {
            logger.debug("Failed to execute rule '{}'.", ruleUID, e);
        }
    }

    @Override
    public void runNow(String ruleUID, boolean considerConditions, @Nullable Map<String, Object> context) {
        final WrappedRule rule = getManagedRule(ruleUID);
        if (rule == null) {
            throw new IllegalStateException("Failed to execute rule '" + ruleUID + "': Invalid Rule UID");
        }
        if (rule.getStatus() != RuleStatus.IDLE) {
            throw new IllegalStateException("The rule is not in an appropriate state for the requested operation.");
        }
        Map<String, Object> contextCopy = new HashMap<>(context);
        try {
            boolean isSatisfied = true;
            if (considerConditions) {
                isSatisfied = calculateConditions(rule, contextCopy);
            }
            if (isSatisfied && rule.getStatus() == RuleStatus.IDLE) {
                // change state to RUNNING
                setStatus(rule, new RuleStatusInfo(RuleStatus.RUNNING));
                executeActions(rule, contextCopy, true);
                logger.debug("The rule '{}' is executed.", rule.getUID());
            }
        } catch (Throwable t) {
            logger.error("Fail to execute rule '{}': {}", new Object[] { ruleUID, t.getMessage() }, t);
        }
        // change state to IDLE only if it still has been RUNNING.
        if (rule.getStatus() == RuleStatus.RUNNING) {
            setStatus(rule, new RuleStatusInfo(RuleStatus.IDLE));
        }
    }

    @Override
    public void runNow(String ruleUID) {
        runNow(ruleUID, false, new HashMap<>());
    }

    /**
     * The method updates {@link Output} of the {@link Trigger} with a new triggered data.
     *
     * @param td new Triggered data.
     */
    private Map<String, Object> getContext(TriggerData td) {
        Trigger t = td.getTrigger();
        Map<String, Object> context = new HashMap<>();
        updateContext(context, t.getId(), td.getOutputs());
        return context;
    }

    private void updateContext(Map<String, Object> context, String moduleUID, Map<String, ?> outputs) {
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
    private Map<String, Object> updateContext(Map<String, Object> context, Set<Connection> connections) {
        if (connections != null) {
            StringBuilder sb = new StringBuilder();
            for (Connection c : connections) {
                String outputModuleId = c.getOuputModuleId();
                if (outputModuleId != null) {
                    sb.append(outputModuleId).append(OUTPUT_SEPARATOR).append(c.getOutputName());
                    Object outputValue = context.get(sb.toString());
                    sb.setLength(0);
                    if (c.getReferenceTokens() == null && outputValue != null) {
                        context.put(c.getInputName(), outputValue);
                    } else if (outputValue != null) {
                        context.put(c.getInputName(),
                                ReferenceResolver.resolveComplexDataReference(outputValue, c.getReferenceTokens()));
                    }
                } else {
                    // get reference from context
                    String ref = c.getOutputName();
                    final Object value = ReferenceResolver.resolveReference(ref, context);
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
     * @param rule the checked rule.
     * @return true when all conditions of the rule are satisfied, false otherwise.
     */
    @SuppressWarnings("null")
    boolean calculateConditions(WrappedRule rule, Map<String, Object> context) {
        for (WrappedCondition c : rule.getConditions()) {
            RuleStatus ruleStatus = rule.getStatus();
            if (ruleStatus != RuleStatus.IDLE && ruleStatus != RuleStatus.RUNNING) {
                return false;
            }
            ConditionHandler tHandler = c.getModuleHandler();
            Condition unwrapped = c.unwrap();
            try {
                if (!tHandler.isSatisfied(Collections.unmodifiableMap(updateContext(context, c.getConnections())))) {
                    logger.debug("The condition '{}' of rule '{}' is unsatisfied.",
                            new Object[] { unwrapped.getId(), rule.getUID() });
                    context.putAll(unwrapped.getConfiguration().getProperties());
                    return false;
                }
            } catch (Throwable t) {
                String errMessage = "Fail to check condition: " + unwrapped.getId();
                logger.warn(errMessage, t);
                return false;
            }
        }
        RuleStatus ruleStatus = rule.getStatus();
        return ruleStatus == RuleStatus.IDLE || ruleStatus == RuleStatus.RUNNING;
    }

    /**
     * This method evaluates actions of the {@link Rule} and set their {@link Output}s when they exists.
     *
     * @param rule executed rule.
     */
    void executeActions(WrappedRule rule, Map<String, Object> context, boolean stopOnFirstFail) {
        List<WrappedAction> actions = rule.getActions();
        for (WrappedAction action : actions) {
            if (rule.getStatus() != RuleStatus.RUNNING) {
                return;
            }
            ActionHandler aHandler = action.getModuleHandler();
            Action unwrapped = action.unwrap();
            context.putAll(unwrapped.getConfiguration().getProperties());
            try {
                @SuppressWarnings("null")
                Map<String, ?> outputs = aHandler
                        .execute(Collections.unmodifiableMap(updateContext(context, action.getConnections())));
                updateContext(context, unwrapped.getId(), outputs);
            } catch (Throwable t) {
                String errMessage = "Fail to execute action: " + unwrapped.getId();
                if (stopOnFirstFail) {
                    RuntimeException re = new RuntimeException(errMessage, t);
                    throw re;
                }
                logger.warn(errMessage, t);
            }
        }
    }

    /**
     * The method cleans used resources by rule engine when it is deactivated.
     */
    @Deactivate
    protected void deactivate() {
        wLock.lock();
        try {
            if (isDisposed) {
                return;
            }
            isDisposed = true;
            if (compositeFactory != null) {
                compositeFactory.deactivate();
                compositeFactory = null;
            }
            if (executor != null) {
                executor.shutdown();
            }
            RuleRegistryImpl ruleRegistry = this.ruleRegistry;
            if (ruleRegistry != null) {
                ruleRegistry.removeRegistryChangeListener(listener);
                ruleRegistry.stream().forEach(r -> removeRule(r));
            }
            listener = null;
            this.ruleRegistry = null;
        } finally {
            wLock.unlock();
        }
    }

    private void cancelFuture(Future f) {
        if (f != null && !f.isDone()) {
            f.cancel(true);
        }
    }

    /**
     * The auto-mapping tries to link not connected module inputs to output of other modules. The auto-mapping will link
     * input to output only when following criteria are done: 1) input must not be connected. The auto-mapping will not
     * overwrite explicit connections done by the user. 2) input tags must be subset of the output tags. 3) condition
     * inputs can be connected only to triggers' outputs 4) action outputs can be connected to both conditions and
     * actions outputs 5) There is only one output, based on previous criteria, where the input can connect to. If more
     * then one candidate outputs exists for connection, this is a conflict and the auto mapping leaves the input
     * unconnected. Auto-mapping is always applied when the rule is added or updated. It changes initial value of inputs
     * of conditions and actions participating in the rule. If an "auto-map" connection has to be removed, the tags of
     * corresponding input/output have to be changed.
     *
     * rule updated rule
     */
    private void autoMapConnections(WrappedRule rule) {
        Map<Set<String>, OutputRef> triggerOutputTags = new HashMap<Set<String>, OutputRef>(11);
        for (WrappedTrigger mt : rule.getTriggers()) {
            final Trigger t = mt.unwrap();
            TriggerType tt = (TriggerType) mtRegistry.get(t.getTypeUID());
            if (tt != null) {
                initTagsMap(t.getId(), tt.getOutputs(), triggerOutputTags);
            }
        }
        Map<Set<String>, OutputRef> actionOutputTags = new HashMap<Set<String>, OutputRef>(11);
        for (WrappedAction ma : rule.getActions()) {
            final Action a = ma.unwrap();
            ActionType at = (ActionType) mtRegistry.get(a.getTypeUID());
            if (at != null) {
                initTagsMap(a.getId(), at.getOutputs(), actionOutputTags);
            }
        }
        // auto mapping of conditions
        if (!triggerOutputTags.isEmpty()) {
            for (WrappedCondition mc : rule.getConditions()) {
                final Condition c = mc.unwrap();
                boolean isConnectionChanged = false;
                ConditionType ct = (ConditionType) mtRegistry.get(c.getTypeUID());
                if (ct != null) {
                    Set<Connection> connections = copyConnections(mc.getConnections());

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
                        Map<String, String> connectionMap = getConnectionMap(connections);
                        mc.setInputs(connectionMap);
                        mc.setConnections(connections);
                    }
                }
            }
        }
        // auto mapping of actions
        if (!triggerOutputTags.isEmpty() || !actionOutputTags.isEmpty()) {
            for (final WrappedAction ma : rule.getActions()) {
                final Action a = ma.unwrap();
                boolean isConnectionChanged = false;
                ActionType at = (ActionType) mtRegistry.get(a.getTypeUID());
                if (at != null) {
                    Set<Connection> connections = copyConnections(ma.getConnections());
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
                        Map<String, String> connectionMap = getConnectionMap(connections);
                        ma.setInputs(connectionMap);
                        ma.setConnections(connections);
                    }
                }
            }
        }
    }

    /**
     * Try to connect a free input to available outputs.
     *
     * @param input              a free input which has to be connected
     * @param outputTagMap       a map of set of tags to output references
     * @param currentConnections current connections of this module
     * @return true when only one output which meets auto mapping criteria is found. False otherwise.
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
        for (Connection connection : connections) {
            if (connection.getInputName().equals(input.getName())) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> getConnectionMap(Set<Connection> connections) {
        Map<String, String> connectionMap = new HashMap<>();
        for (Connection connection : connections) {
            connectionMap.put(connection.getInputName(),
                    connection.getOuputModuleId() + "." + connection.getOutputName());
        }
        return connectionMap;
    }

    /**
     * Utility method creating deep copy of passed connection set.
     *
     * @param connections connections used by this module.
     * @return copy of passed connections.
     */
    private Set<Connection> copyConnections(Set<Connection> connections) {
        Set<Connection> result = new HashSet<>(connections.size());
        for (Iterator<Connection> it = connections.iterator(); it.hasNext();) {
            Connection c = it.next();
            result.add(new Connection(c.getInputName(), c.getOuputModuleId(), c.getOutputName()));
        }
        return result;
    }

    /**
     * This method is used for collecting connections of {@link Module}s.
     *
     * @param inputs The map of inputs of the module
     * @return set of connections
     */
    public Set<Connection> resolveConnections(Map<String, String> inputs) {
        final String REF_IDENTIFIER = "$";
        Set<Connection> connections = new HashSet<>();
        if (inputs != null) {
            for (Entry<String, String> input : inputs.entrySet()) {
                String inputName = input.getKey();
                String outputName = null;

                String output = input.getValue();
                if (output.startsWith(REF_IDENTIFIER)) {
                    outputName = output;
                    Connection connection = new Connection(inputName, null, outputName);
                    connections.add(connection);
                } else {
                    int index = output.indexOf('.');
                    if (index != -1) {
                        String outputId = output.substring(0, index);
                        outputName = output.substring(index + 1);
                        Connection connection = new Connection(inputName, outputId, outputName);
                        connections.add(connection);
                    } else {
                        logger.error("Wrong format of Output : {}: {}", inputName, output);
                        continue;
                    }
                }
            }
        }
        return connections;
    }

    class OutputRef {

        private final String moduleId;
        private final String outputName;

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
}
