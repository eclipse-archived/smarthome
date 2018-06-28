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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.ModuleHandlerCallback;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleManager;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusDetail;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.internal.TriggerHandlerCallbackImpl.TriggerData;
import org.eclipse.smarthome.automation.core.internal.composite.CompositeModuleHandlerFactory;
import org.eclipse.smarthome.automation.core.util.ReferenceResolver;
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible to initialize and execute {@link RuleImpl}s, when the {@link RuleImpl}s are added in rule
 * engine. Each {@link RuleImpl} has associated {@link RuleStatusInfo} object which shows its {@link RuleStatus} and
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
 *
 */
@SuppressWarnings("rawtypes")
@Component(immediate = true)
public class RuleEngineImpl implements RuleManager, RegistryChangeListener<ModuleType> {

    /**
     * Constant defining separator between module id and output name.
     */
    public static final char OUTPUT_SEPARATOR = '.';

    private static final String DISABLED_RULE_STORAGE = "automation_rules_disabled";

    /**
     * Delay between rule's re-initialization tries.
     */
    private long scheduleReinitializationDelay;

    /**
     * {@link Map} holding all created {@link TriggerHandlerCallback} instances, corresponding to each {@link RuleImpl}.
     * There is only one {@link TriggerHandlerCallback} instance per {@link RuleImpl}. The relation is
     * {@link RuleImpl}'s UID to {@link TriggerHandlerCallback} instance.
     */
    private final Map<String, TriggerHandlerCallbackImpl> thCallbacks = new HashMap<String, TriggerHandlerCallbackImpl>();

    /**
     * {@link Map} holding all {@link ModuleType} UIDs that are available in some rule's module definition. The relation
     * is {@link ModuleType}'s UID to {@link Set} of {@link RuleImpl} UIDs.
     */
    private final Map<String, Set<String>> mapModuleTypeToRules = new HashMap<String, Set<String>>();

    /**
     * {@link Map} holding all available {@link ModuleHandlerFactory}s linked with {@link ModuleType}s that they
     * supporting. The relation is {@link ModuleType}'s UID to {@link ModuleHandlerFactory} instance.
     */
    private final Map<String, ModuleHandlerFactory> moduleHandlerFactories;

    /**
     * {@link Set} holding all available {@link ModuleHandlerFactory}s.
     */
    private final Set<ModuleHandlerFactory> allModuleHandlerFactories = new CopyOnWriteArraySet<>();

    /**
     * The storage for the disable information
     */
    private Storage<Boolean> disabledRulesStorage;

    /**
     * Locker which does not permit rule initialization when the rule engine is stopping.
     */
    private boolean isDisposed = false;

    protected Logger logger = LoggerFactory.getLogger(RuleEngineImpl.class.getName());

    /**
     * A callback that is notified when the status of a {@link RuleImpl} changes.
     */
    private RuleRegistryImpl ruleRegistry;

    /**
     * {@link Map} holding all RuleImpl context maps. RuleImpl context maps contain dynamic parameters used by the
     * {@link RuleImpl}'s {@link ModuleImpl}s to communicate with each other during the {@link RuleImpl}'s execution.
     * The context map of a {@link RuleImpl} is cleaned when the execution is completed. The relation is
     * {@link RuleImpl}'s UID to RuleImpl context map.
     */
    private Map<String, Map<String, Object>> contextMap;

    /**
     * This field holds reference to {@link ModuleTypeRegistry}. The {@link RuleEngineImpl} needs it to auto-map
     * connection between rule's modules and to determine module handlers.
     */
    private ModuleTypeRegistry mtRegistry;

    /**
     * Provides all composite {@link ModuleHandler}s.
     */
    private CompositeModuleHandlerFactory compositeFactory;

    /**
     * {@link Map} holding all scheduled {@link RuleImpl} re-initialization tasks. The relation is {@link RuleImpl}'s
     * UID to
     * re-initialization task as a {@link Future} instance.
     */
    private Map<String, Future> scheduleTasks = new HashMap<String, Future>(31);

    /**
     * Performs the {@link Rule} re-initialization tasks.
     */
    private ScheduledExecutorService executor;

    /**
     * This field holds {@link RegistryChangeListener} that listen for changes in the rule registry.
     * We cannot implement the interface ourselves as we are already a RegistryChangeListener for module types.
     */
    private RegistryChangeListener<Rule> listener;

    /**
     * Posts an event through the event bus in an asynchronous way. {@link RuleEngineImpl} use it for posting the
     * {@link RuleStatusInfoEvent}.
     */
    private EventPublisher eventPublisher;
    private static final String SOURCE = RuleEngineImpl.class.getSimpleName();

    private final ModuleHandlerCallback moduleHandlerCallback = new ModuleHandlerCallback() {

        @Override
        public Boolean isEnabled(String ruleUID) {
            return RuleEngineImpl.this.isEnabled(ruleUID);
        }

        @Override
        public void setEnabled(String uid, boolean isEnabled) {
            RuleEngineImpl.this.setEnabled(uid, isEnabled);
        }

        @Override
        public RuleStatusInfo getStatusInfo(String ruleUID) {
            return RuleEngineImpl.this.getStatusInfo(ruleUID);
        }

        @Override
        public RuleStatus getStatus(String ruleUID) {
            return RuleEngineImpl.this.getStatus(ruleUID);
        }

        @Override
        public void runNow(String uid) {
            RuleEngineImpl.this.runNow(uid);
        }

        @Override
        public void runNow(String uid, boolean considerConditions, Map<String, Object> context) {
            RuleEngineImpl.this.runNow(uid, considerConditions, context);
        }

    };

    /**
     * Constructor of {@link RuleEngineImpl}. It initializes the logger and starts tracker for
     * {@link ModuleHandlerFactory} services.
     */
    public RuleEngineImpl() {
        this.contextMap = new HashMap<String, Map<String, Object>>();
        this.moduleHandlerFactories = new HashMap<String, ModuleHandlerFactory>(20);
    }

    /**
     * This method is used to create a {@link CompositeModuleHandlerFactory} that handles all composite
     * {@link ModuleType}s. Called from DS to activate the rule engine component.
     */
    @Activate
    protected void activate() {
        compositeFactory = new CompositeModuleHandlerFactory(mtRegistry, this);

        // enable the rules that are not persisted as Disabled;
        for (Rule rule : ruleRegistry.getAll()) {
            String uid = rule.getUID();
            if (disabledRulesStorage == null || disabledRulesStorage.get(uid) == null) {
                setEnabled(uid, true);
            }
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
            scheduleReinitializationDelay = this.ruleRegistry.getScheduleReinitializationDelay();
            listener = new RegistryChangeListener<Rule>() {
                @Override
                public void added(Rule rule) {
                    RuleEngineImpl.this.addRule((RuleImpl) rule);
                }

                @Override
                public void removed(Rule rule) {
                    RuleEngineImpl.this.removeRule(rule.getUID());
                }

                @Override
                public void updated(Rule oldRule, Rule rule) {
                    added(rule);
                }
            };
            ruleRegistry.addRegistryChangeListener(listener);
            for (Rule rule : ruleRegistry.getAll()) {
                addRule((RuleImpl) rule);
            }
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
                    unregister(getRuleImpl(rUID), RuleStatusDetail.HANDLER_MISSING_ERROR,
                            "Update Module Type " + moduleType.getUID());
                    setStatus(rUID, new RuleStatusInfo(RuleStatus.INITIALIZING));
                }
            }
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
        allModuleHandlerFactories.add(moduleHandlerFactory);
        Collection<String> moduleTypes = moduleHandlerFactory.getTypes();
        Set<String> notInitializedRules = null;
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            Set<String> rules = null;
            synchronized (this) {
                moduleHandlerFactories.put(moduleTypeName, moduleHandlerFactory);
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
                        notInitializedRules = notInitializedRules != null ? notInitializedRules
                                : new HashSet<String>(20);
                        notInitializedRules.add(rUID);
                    }
                }
            }
        }
        if (notInitializedRules != null) {
            for (final String rUID : notInitializedRules) {
                scheduleRuleInitialization(rUID);
            }
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
        allModuleHandlerFactories.remove(moduleHandlerFactory);
        Collection<String> moduleTypes = moduleHandlerFactory.getTypes();
        removeMissingModuleTypes(moduleTypes);
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            moduleHandlerFactories.remove(moduleTypeName);
        }
    }

    /**
     * This method add a new rule into rule engine. Scope identity of the Rule is the identity of the caller.
     *
     * @param rule a rule which has to be added.
     */
    protected void addRule(RuleImpl rule) {
        synchronized (this) {
            if (isDisposed) {
                throw new IllegalStateException("RuleEngineImpl is disposed!");
            }
        }
        String rUID = rule.getUID();
        RuleStatusInfo initStatusInfo = disabledRulesStorage == null || disabledRulesStorage.get(rUID) == null
                ? new RuleStatusInfo(RuleStatus.INITIALIZING)
                : new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.DISABLED);
        rule.setStatusInfo(initStatusInfo);

        RuleImpl oldRule = getRuleImpl(rUID);
        if (oldRule != null) {
            unregister(oldRule);
        }

        if (rule.isEnabled()) {
            setRule(rule);
        }
    }

    /**
     * This method tries to initialize the rule. It uses available {@link ModuleHandlerFactory}s to create
     * {@link ModuleHandler}s for all {@link ModuleImpl}s of the {@link RuleImpl} and to link them. When all the modules
     * have associated module handlers then the {@link RuleImpl} is initialized and it is ready to working. It goes into
     * idle state. Otherwise the RuleImpl stays into not initialized and continue to wait missing handlers, module types
     * or templates.
     *
     * @param rUID a UID of rule which tries to be initialized.
     */
    private void setRule(RuleImpl rule) {
        if (isDisposed) {
            return;
        }
        String rUID = rule.getUID();
        setStatus(rUID, new RuleStatusInfo(RuleStatus.INITIALIZING));
        List<Module> modules = rule.getModules();
        for (Module m : modules) {
            updateMapModuleTypeToRule(rUID, m.getTypeUID());
            if (m instanceof ConditionImpl) {
                ((ConditionImpl) m).setConnections(resolveConnections(((ConditionImpl) m).getInputs()));
            }
            if (m instanceof ActionImpl) {
                ((ActionImpl) m).setConnections(resolveConnections(((ActionImpl) m).getInputs()));
            }
        }
        String errMsgs;
        try {
            validateModuleIDs(rule);
        } catch (IllegalArgumentException e) {
            setStatus(rUID,
                    new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.INVALID_RULE, e.getMessage()));
            return;
        }
        try {
            autoMapConnections(rule);
            ConnectionValidator.validateConnections(mtRegistry, rule);
        } catch (IllegalArgumentException e) {
            errMsgs = "\n Validation of rule " + rUID + " has failed! " + e.getLocalizedMessage();
            // change status to UNINITIALIZED
            setStatus(rUID,
                    new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.INVALID_RULE, errMsgs.trim()));
            return;
        }
        errMsgs = setModuleHandlers(rUID, modules);
        if (errMsgs == null) {
            register(rule);
            // change status to IDLE
            setStatus(rUID, new RuleStatusInfo(RuleStatus.IDLE));
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
            // change status to UNINITIALIZED
            setStatus(rUID,
                    new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.HANDLER_INITIALIZING_ERROR, errMsgs));
            unregister(rule);
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
     * use case is to notify event subscribers about the {@link RuleImpl}'s status change.
     *
     * @param ruleUID    the UID of the {@link RuleImpl}, whose status is changed.
     * @param statusInfo the new {@link RuleImpl}s status.
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
    private <T extends Module> String setModuleHandlers(String rUID, List<T> modules) {
        StringBuffer sb = null;
        if (modules != null) {
            for (T m : modules) {
                try {
                    ModuleHandler moduleHandler = getModuleHandler(m, rUID);
                    if (moduleHandler != null) {
                        if (m instanceof ActionImpl) {
                            ((ActionImpl) m).setModuleHandler((ActionHandler) moduleHandler);
                        } else if (m instanceof ConditionImpl) {
                            ((ConditionImpl) m).setModuleHandler((ConditionHandler) moduleHandler);
                        } else if (m instanceof TriggerImpl) {
                            ((TriggerImpl) m).setModuleHandler((TriggerHandler) moduleHandler);
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
     * Gets {@link TriggerHandlerCallback} for passed {@link RuleImpl}. If it does not exists, a callback object is
     * created.
     *
     * @param rule rule object for which the callback is looking for.
     * @return a {@link TriggerHandlerCallback} corresponding to the passed {@link RuleImpl} object.
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
     */
    private <T extends Module> void removeModuleHandlers(List<T> modules, String ruleUID) {
        if (modules != null) {
            for (T m : modules) {
                ModuleHandler handler = null;
                if (m instanceof ActionImpl) {
                    handler = ((ActionImpl) m).getModuleHandler();
                } else if (m instanceof ConditionImpl) {
                    handler = ((ConditionImpl) m).getModuleHandler();
                } else if (m instanceof TriggerImpl) {
                    handler = ((TriggerImpl) m).getModuleHandler();
                }

                if (handler != null) {
                    ModuleHandlerFactory factory = getModuleHandlerFactory(m.getTypeUID());
                    if (factory != null) {
                        factory.ungetHandler(m, ruleUID, handler);
                    }
                    if (m instanceof ActionImpl) {
                        ((ActionImpl) m).setModuleHandler(null);
                    } else if (m instanceof ConditionImpl) {
                        ((ConditionImpl) m).setModuleHandler(null);
                    } else if (m instanceof TriggerImpl) {
                        ((TriggerImpl) m).setModuleHandler(null);
                    }
                }
            }
        }
    }

    /**
     * This method register the RuleImpl to start working. This is the final step of initialization process where
     * triggers received {@link TriggerHandlerCallback}s object and starts to notify the rule engine when they are
     * triggered. After activating all triggers the rule goes into IDLE state.
     *
     * @param rule an initialized rule which has to starts tracking the triggers.
     */
    private void register(RuleImpl rule) {
        TriggerHandlerCallback thCallback = getTriggerHandlerCallback(rule.getUID());
        rule.getTriggers().forEach(t -> {
            TriggerHandler triggerHandler = ((TriggerImpl) t).getModuleHandler();
            if (triggerHandler != null) {
                triggerHandler.setCallback(thCallback);
            }
        });
        rule.getConditions().forEach(c -> {
            ConditionHandler conditionHandler = ((ConditionImpl) c).getModuleHandler();
            if (conditionHandler != null) {
                conditionHandler.setCallback(moduleHandlerCallback);
            }
        });
        rule.getActions().forEach(a -> {
            ActionHandler actionHandler = ((ActionImpl) a).getModuleHandler();
            if (actionHandler != null) {
                actionHandler.setCallback(moduleHandlerCallback);
            }
        });
    }

    /**
     * This method unregister a {@link RuleImpl} and it stops working. It is called when some
     * {@link ModuleHandlerFactory} is disposed or some {@link ModuleType} is updated. The {@link RuleImpl} is
     * available but its state should become {@link RuleStatus#UNINITIALIZED}.
     *
     * @param r      rule that should be unregistered.
     * @param detail provides the {@link RuleStatusDetail}, corresponding to the new <b>uninitialized</b> status, should
     *               be {@code null} if the status will be skipped.
     * @param msg    provides the {@link RuleStatusInfo} description, corresponding to the new <b>uninitialized</b>
     *               status,
     *               should be {@code null} if the status will be skipped.
     */
    private void unregister(RuleImpl r, RuleStatusDetail detail, String msg) {
        if (r != null) {
            unregister(r);
            String rUID = r.getUID();
            setStatus(rUID, new RuleStatusInfo(RuleStatus.UNINITIALIZED, detail, msg));
        }
    }

    /**
     * This method unregister a {@link RuleImpl} and it stops working. It is called when the {@link RuleImpl} is
     * removed, updated or disabled. Also it is called when some {@link ModuleHandlerFactory} is disposed or some
     * {@link ModuleType} is updated.
     *
     * @param r rule that should be unregistered.
     */
    private void unregister(RuleImpl r) {
        String rUID = r.getUID();
        synchronized (this) {
            TriggerHandlerCallbackImpl callback = thCallbacks.remove(rUID);
            if (callback != null) {
                callback.dispose();
            }
        }
        removeModuleHandlers(r.getModules(), rUID);
    }

    /**
     * This method is used to obtain a {@link ModuleHandler} for the specified {@link ModuleImpl}.
     *
     * @param m       the {@link ModuleImpl} which is looking for a handler.
     * @param ruleUID UID of the {@link RuleImpl} that the specified {@link ModuleImpl} belongs to.
     * @return handler that processing this module. Could be {@code null} if the {@link ModuleHandlerFactory} is not
     *         available.
     */
    private ModuleHandler getModuleHandler(Module m, String ruleUID) {
        String moduleTypeId = m.getTypeUID();
        ModuleHandlerFactory mhf = getModuleHandlerFactory(moduleTypeId);
        if (mhf == null || mtRegistry.get(moduleTypeId) == null) {
            return null;
        }
        return mhf.getHandler(m, ruleUID);
    }

    /**
     * Gets the {@link ModuleHandlerFactory} for the {@link ModuleType} with the specified UID.
     *
     * @param moduleTypeId the UID of the {@link ModuleType}.
     * @return the {@link ModuleHandlerFactory} responsible for the {@link ModuleType}.
     */
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

    /**
     * Updates the {@link ModuleType} to {@link RuleImpl}s mapping. The method adds the {@link RuleImpl}'s UID to the
     * list of
     * {@link RuleImpl}s that use this {@link ModuleType}.
     *
     * @param rUID         the UID of the {@link RuleImpl}.
     * @param moduleTypeId the UID of the {@link ModuleType}.
     */
    public synchronized void updateMapModuleTypeToRule(String rUID, String moduleTypeId) {
        Set<String> rules = mapModuleTypeToRules.get(moduleTypeId);
        if (rules == null) {
            rules = new HashSet<String>(11);
        }
        rules.add(rUID);
        mapModuleTypeToRules.put(moduleTypeId, rules);
    }

    /**
     * This method removes RuleImpl from the rule engine.
     *
     * @param rUID id of removed {@link RuleImpl}
     * @return true when a rule is deleted, false when there is no rule with such id.
     */
    protected boolean removeRule(String rUID) {
        RuleImpl r = getRuleImpl(rUID);
        if (r != null) {
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
            }
            scheduleTasks.remove(r.getUID());
            return true;
        }
        return false;
    }

    /**
     * Gets {@link RuleImpl} corresponding to the passed id. This method is used internally and it does not create a
     * copy of the rule.
     *
     * @param rUID unieque id of the {@link RuleImpl}
     * @return internal {@link RuleImpl} object
     */
    protected RuleImpl getRuleImpl(String rUID) {
        return (RuleImpl) ruleRegistry.get(rUID);
    }

    @Override
    public synchronized void setEnabled(String uid, boolean enable) {
        RuleImpl rule = getRuleImpl(uid);
        if (rule == null) {
            throw new IllegalArgumentException(String.format("No rule with id=%s was found!", uid));
        }
        if (enable) {
            if (disabledRulesStorage != null) {
                disabledRulesStorage.remove(uid);
            }
            if (rule.getStatus() == RuleStatus.UNINITIALIZED) {
                register(rule);
                // change status to IDLE
                setStatus(rule.getUID(), new RuleStatusInfo(RuleStatus.IDLE));
            }
        } else {
            if (disabledRulesStorage != null) {
                disabledRulesStorage.put(uid, true);
            }
            unregister(rule, RuleStatusDetail.DISABLED, null);
        }
    }

    @Override
    public RuleStatusInfo getStatusInfo(String ruleUID) {
        if (ruleUID == null) {
            return null;
        }
        RuleImpl runtimeRule = getRuleImpl(ruleUID);
        if (runtimeRule == null) {
            return null;
        }
        return runtimeRule.getStatusInfo();
    }

    @Override
    public RuleStatus getStatus(String ruleUID) {
        RuleStatusInfo statusInfo = getStatusInfo(ruleUID);
        return statusInfo == null ? null : statusInfo.getStatus();
    }

    @Override
    public Boolean isEnabled(String ruleUID) {
        return getStatus(ruleUID) == null ? null
                : !getStatusInfo(ruleUID).getStatusDetail().equals(RuleStatusDetail.DISABLED);
    }

    /**
     * This method updates the status of the {@link RuleImpl}
     *
     * @param rUID          unique id of the rule
     * @param newStatusInfo the new status of the rule
     */
    private void setStatus(String rUID, RuleStatusInfo newStatusInfo) {
        RuleImpl runtimeRule = getRuleImpl(rUID);
        if (runtimeRule == null) {
            return;
        }
        runtimeRule.setStatusInfo(newStatusInfo);
        postRuleStatusInfoEvent(rUID, newStatusInfo);
    }

    /**
     * Creates and schedules a re-initialization task for the {@link RuleImpl} with the specified UID.
     *
     * @param rUID the UID of the {@link RuleImpl}.
     */
    protected void scheduleRuleInitialization(final String rUID) {
        Future f = scheduleTasks.get(rUID);
        if (f == null || f.isDone()) {
            ScheduledExecutorService ex = getScheduledExecutor();
            f = ex.schedule(new Runnable() {
                @Override
                public void run() {
                    setRule(getRuleImpl(rUID));
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
                    if (ruleStatus == null) {
                        continue;
                    }
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
                unregister(getRuleImpl(rUID), RuleStatusDetail.HANDLER_MISSING_ERROR, sb.substring(0, sb.length() - 2));
            }
        }
    }

    /**
     * This method runs a {@link Rule}. It is called by the {@link TriggerHandlerCallback}'s thread when a new
     * {@link TriggerData} is available. This method switches
     *
     * @param ruleUID the {@link Rule} which has to evaluate new {@link TriggerData}.
     * @param td      {@link TriggerData} object containing new values for {@link Trigger}'s {@link Output}s
     */
    protected void runRule(String ruleUID, TriggerHandlerCallbackImpl.TriggerData td) {
        if (thCallbacks.get(ruleUID) == null) {
            // the rule was unregistered
            return;
        }
        synchronized (this) {
            final RuleStatus ruleStatus = getRuleStatus(ruleUID);
            if (ruleStatus != RuleStatus.IDLE) {
                logger.error("Failed to execute rule ‘{}' with status '{}'", ruleUID, ruleStatus.name());
                return;
            }
            // change state to RUNNING
            setStatus(ruleUID, new RuleStatusInfo(RuleStatus.RUNNING));
        }
        try {
            clearContext(ruleUID);

            setTriggerOutputs(ruleUID, td);
            RuleImpl rule = getRuleImpl(ruleUID);
            boolean isSatisfied = calculateConditions(rule);
            if (isSatisfied) {
                executeActions(rule, true);
                logger.debug("The rule '{}' is executed.", ruleUID);
            } else {
                logger.debug("The rule '{}' is NOT executed, since it has unsatisfied conditions.", ruleUID);
            }
        } catch (Throwable t) {
            logger.error("Failed to execute rule '{}': {}", ruleUID, t.getMessage());
            logger.debug("", t);
        }
        // change state to IDLE only if the rule has not been DISABLED.
        synchronized (this) {
            if (getRuleStatus(ruleUID) == RuleStatus.RUNNING) {
                setStatus(ruleUID, new RuleStatusInfo(RuleStatus.IDLE));
            }
        }
    }

    @Override
    public void runNow(String ruleUID, boolean considerConditions, Map<String, Object> context) {
        RuleImpl rule = getRuleImpl(ruleUID);
        if (rule == null) {
            logger.warn("Failed to execute rule '{}': Invalid RuleImpl UID", ruleUID);
            return;
        }
        synchronized (this) {
            final RuleStatus ruleStatus = getRuleStatus(ruleUID);
            if (ruleStatus != RuleStatus.IDLE) {
                logger.error("Failed to execute rule ‘{}' with status '{}'", ruleUID, ruleStatus.name());
                return;
            }
            // change state to RUNNING
            setStatus(ruleUID, new RuleStatusInfo(RuleStatus.RUNNING));
        }
        try {
            clearContext(ruleUID);
            if (context != null && !context.isEmpty()) {
                getContext(ruleUID, null).putAll(context);
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
            logger.error("Failed to execute rule '{}': ", ruleUID, t);
        }
        // change state to IDLE only if the rule has not been DISABLED.
        synchronized (this) {
            if (getRuleStatus(ruleUID) == RuleStatus.RUNNING) {
                setStatus(ruleUID, new RuleStatusInfo(RuleStatus.IDLE));
            }
        }
    }

    @Override
    public void runNow(String ruleUID) {
        runNow(ruleUID, false, null);
    }

    /**
     * Clears all dynamic parameters from the {@link Rule}'s context.
     *
     * @param ruleUID the UID of the rule whose context must be cleared.
     */
    protected void clearContext(String ruleUID) {
        Map<String, Object> context = contextMap.get(ruleUID);
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
     * @param outputs   new output values.
     */
    private void updateContext(String ruleUID, String moduleUID, Map<String, ?> outputs) {
        Map<String, Object> context = getContext(ruleUID, null);
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
     * @param rule the checked rule
     * @return true when all conditions of the rule are satisfied, false otherwise.
     */
    private boolean calculateConditions(RuleImpl rule) {
        List<Condition> conditions = rule.getConditions();
        if (conditions.size() == 0) {
            return true;
        }
        RuleStatus ruleStatus = null;
        for (Iterator<Condition> it = conditions.iterator(); it.hasNext();) {
            ruleStatus = getRuleStatus(rule.getUID());
            if (ruleStatus != RuleStatus.RUNNING) {
                return false;
            }
            ConditionImpl c = (ConditionImpl) it.next();
            ConditionHandler tHandler = c.getModuleHandler();
            Map<String, Object> context = getContext(rule.getUID(), c.getConnections());
            if (tHandler != null && !tHandler.isSatisfied(Collections.unmodifiableMap(context))) {
                logger.debug("The condition '{}' of rule '{}' is unsatisfied.",
                        new Object[] { c.getId(), rule.getUID() });
                return false;
            }
        }
        return true;
    }

    /**
     * This method evaluates actions of the {@link RuleImpl} and set their {@link Output}s when they exists.
     *
     * @param rule executed rule.
     */
    private void executeActions(RuleImpl rule, boolean stopOnFirstFail) {
        List<Action> actions = rule.getActions();
        if (actions.size() == 0) {
            return;
        }
        RuleStatus ruleStatus = null;
        for (Iterator<Action> it = actions.iterator(); it.hasNext();) {
            ruleStatus = getRuleStatus(rule.getUID());
            if (ruleStatus != RuleStatus.RUNNING) {
                return;
            }
            ActionImpl action = (ActionImpl) it.next();
            ActionHandler aHandler = action.getModuleHandler();
            if (aHandler != null) {
                String rUID = rule.getUID();
                Map<String, Object> context = getContext(rUID, action.getConnections());
                try {
                    Map<String, ?> outputs = aHandler.execute(Collections.unmodifiableMap(context));
                    if (outputs != null) {
                        context = getContext(rUID, null);
                        updateContext(rUID, action.getId(), outputs);
                    }
                } catch (Throwable t) {
                    String errMessage = "Fail to execute action: " + action.getId();
                    if (stopOnFirstFail) {
                        RuntimeException re = new RuntimeException(errMessage, t);
                        throw re;
                    } else {
                        logger.warn(errMessage, t);
                    }
                }
            }
        }
    }

    /**
     * The method cleans used resources by rule engine when it is deactivated.
     */
    @Deactivate
    protected void deactivate() {
        synchronized (this) {
            if (isDisposed) {
                return;
            }
            isDisposed = true;
        }
        if (compositeFactory != null) {
            compositeFactory.deactivate();
            compositeFactory = null;
        }
        for (Future f : scheduleTasks.values()) {
            f.cancel(true);
        }
        if (scheduleTasks.isEmpty() && executor != null) {
            executor.shutdown();
            executor = null;
        }
        scheduleTasks = null;
        if (contextMap != null) {
            contextMap.clear();
            contextMap = null;
        }
        RuleRegistryImpl ruleRegistry = this.ruleRegistry;
        if (ruleRegistry != null) {
            ruleRegistry.removeRegistryChangeListener(listener);
        }
        listener = null;
        this.ruleRegistry = null;
    }

    /**
     * This method gets rule's status object.
     *
     * @param rUID rule's UID
     * @return status of the rule or null when such rule does not exists.
     */
    protected RuleStatus getRuleStatus(String rUID) {
        RuleStatusInfo info = getStatusInfo(rUID);
        if (info != null) {
            return info.getStatus();
        }
        return null;
    }

    private ScheduledExecutorService getScheduledExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        return executor;
    }

    /**
     * Validates IDs of modules. The module ids must be alphanumeric with only underscores and dashes.
     *
     * @param rule the rule to validate
     * @throws IllegalArgumentException when a module id contains illegal characters
     */
    private void validateModuleIDs(RuleImpl rule) {
        for (Module m : rule.getModules()) {
            String mId = m.getId();
            if (!mId.matches("[A-Za-z0-9_-]*")) {
                rule.setStatusInfo(new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.INVALID_RULE,
                        "It is null or not fit to the pattern: [A-Za-z0-9_-]*"));
                throw new IllegalArgumentException(
                        "Invalid module uid: " + mId + ". It is null or not fit to the pattern: [A-Za-z0-9_-]*");
            }
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
     * Auto mapping is always applied when the rule is added or updated. It changes initial value of inputs of
     * conditions and actions participating in the rule. If an "auto map" connection has to be removed, the tags of
     * corresponding input/output have to be changed.
     *
     * @param r updated rule
     */
    private void autoMapConnections(RuleImpl r) {
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
                    Set<Connection> connections = copyConnections(((ConditionImpl) c).getConnections());

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
                        ((ConditionImpl) c).setInputs(connectionMap);
                        ((ConditionImpl) c).setConnections(connections);
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
                    Set<Connection> connections = copyConnections(((ConditionImpl) a).getConnections());
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
                        ((ConditionImpl) a).setInputs(connectionMap);
                        ((ConditionImpl) a).setConnections(connections);
                    }
                }
            }
        }
    }

    /**
     * Try to connect a free input to available outputs.
     *
     * @param input              a free input which has to be connected
     * @param outputTagMap       a map of set of tags to outptu references
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
