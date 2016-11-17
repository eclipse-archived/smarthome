/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule.runtime.internal.engine;

import static org.eclipse.smarthome.model.rule.runtime.internal.engine.RuleTriggerManager.TriggerTypes.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.thing.events.ChannelTriggeredEvent;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener;
import org.eclipse.smarthome.model.rule.RulesStandaloneSetup;
import org.eclipse.smarthome.model.rule.jvmmodel.RulesJvmModelInferrer;
import org.eclipse.smarthome.model.rule.rules.Rule;
import org.eclipse.smarthome.model.rule.rules.RuleModel;
import org.eclipse.smarthome.model.rule.runtime.RuleEngine;
import org.eclipse.smarthome.model.script.engine.Script;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.model.script.engine.ScriptExecutionException;
import org.eclipse.smarthome.model.script.engine.ScriptExecutionThread;
import org.eclipse.xtext.naming.QualifiedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Injector;

/**
 * This class is the core of the openHAB rule engine.
 * It listens to changes to the rules folder, evaluates the trigger conditions of the rules and
 * schedules them for execution dependent on their triggering conditions.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Oliver Libutzki - Bugfixing
 *
 */
@SuppressWarnings("restriction")
public class RuleEngineImpl implements ItemRegistryChangeListener, StateChangeListener, ModelRepositoryChangeListener,
        RuleEngine, EventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(RuleEngineImpl.class);

    protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ItemRegistry itemRegistry;
    private ModelRepository modelRepository;
    private ScriptEngine scriptEngine;

    private RuleTriggerManager triggerManager;

    private Injector injector;

    private ScheduledFuture<?> startupJob;

    // this flag is used to signal that items are still being added and that we hence do not consider the rule engine
    // ready to be operational
    private boolean starting = true;

    private Runnable startupRunnable = new Runnable() {
        @Override
        public void run() {
            runStartupRules();
        }
    };

    public void activate() {
        injector = RulesStandaloneSetup.getInjector();
        triggerManager = injector.getInstance(RuleTriggerManager.class);

        if (!isEnabled()) {
            logger.info("Rule engine is disabled.");
            return;
        }

        logger.debug("Started rule engine");

        // read all rule files
        Iterable<String> ruleModelNames = modelRepository.getAllModelNamesOfType("rules");
        ArrayList<String> clonedList = Lists.newArrayList(ruleModelNames);
        for (String ruleModelName : clonedList) {
            EObject model = modelRepository.getModel(ruleModelName);
            if (model instanceof RuleModel) {
                RuleModel ruleModel = (RuleModel) model;
                triggerManager.addRuleModel(ruleModel);
            }
        }

        // register us as listeners
        itemRegistry.addRegistryChangeListener(this);
        modelRepository.addModelRepositoryChangeListener(this);

        // register us on all items which are already available in the registry
        for (Item item : itemRegistry.getItems()) {
            internalItemAdded(item);
        }
        scheduleStartupRules();
    }

    public void deactivate() {
        // unregister listeners
        for (Item item : itemRegistry.getItems()) {
            internalItemRemoved(item);
        }
        modelRepository.removeModelRepositoryChangeListener(this);
        itemRegistry.removeRegistryChangeListener(this);

        // execute all scripts that were registered for system shutdown
        executeRules(triggerManager.getRules(SHUTDOWN));
        triggerManager.clearAll();
        triggerManager = null;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    public void setModelRepository(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public void unsetModelRepository(ModelRepository modelRepository) {
        this.modelRepository = null;
    }

    public void setScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public void unsetScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = null;
    }

    @Override
    public void allItemsChanged(Collection<String> oldItemNames) {
        // add the current items again
        Collection<Item> items = itemRegistry.getItems();
        for (Item item : items) {
            internalItemAdded(item);
        }
        scheduleStartupRules();
    }

    @Override
    public void added(Item item) {
        internalItemAdded(item);
        scheduleStartupRules();
    }

    @Override
    public void removed(Item item) {
        internalItemRemoved(item);
    }

    @Override
    public void stateChanged(Item item, State oldState, State newState) {
        if (!starting && triggerManager != null) {
            Iterable<Rule> rules = triggerManager.getRules(CHANGE, item, oldState, newState);

            executeRules(rules, oldState);
        }
    }

    @Override
    public void stateUpdated(Item item, State state) {
        if (!starting && triggerManager != null) {
            Iterable<Rule> rules = triggerManager.getRules(UPDATE, item, state);
            executeRules(rules);
        }
    }

    private void receiveCommand(ItemCommandEvent commandEvent) {
        if (!starting && triggerManager != null && itemRegistry != null) {
            String itemName = commandEvent.getItemName();
            Command command = commandEvent.getItemCommand();
            try {
                Item item = itemRegistry.getItem(itemName);
                Iterable<Rule> rules = triggerManager.getRules(COMMAND, item, command);

                executeRules(rules, command);
            } catch (ItemNotFoundException e) {
                // ignore commands for non-existent items
            }
        }
    }

    private void receiveThingTrigger(ChannelTriggeredEvent event) {
        String triggerEvent = event.getEvent();
        String channel = event.getChannel().getAsString();

        Iterable<Rule> rules = triggerManager.getRules(TRIGGER, channel, triggerEvent);
        executeRules(rules, event);
    }

    private void internalItemAdded(Item item) {
        if (item instanceof GenericItem) {
            GenericItem genericItem = (GenericItem) item;
            genericItem.addStateChangeListener(this);
        }
    }

    private void internalItemRemoved(Item item) {
        if (item instanceof GenericItem) {
            GenericItem genericItem = (GenericItem) item;
            genericItem.removeStateChangeListener(this);
        }
    }

    @Override
    public void modelChanged(String modelName, org.eclipse.smarthome.model.core.EventType type) {
        if (triggerManager != null) {
            if (isEnabled() && modelName.endsWith("rules")) {
                RuleModel model = (RuleModel) modelRepository.getModel(modelName);

                // remove the rules from the trigger sets
                if (type == org.eclipse.smarthome.model.core.EventType.REMOVED
                        || type == org.eclipse.smarthome.model.core.EventType.MODIFIED) {
                    triggerManager.removeRuleModel(model);
                }

                // add new and modified rules to the trigger sets
                if (model != null && (type == org.eclipse.smarthome.model.core.EventType.ADDED
                        || type == org.eclipse.smarthome.model.core.EventType.MODIFIED)) {
                    triggerManager.addRuleModel(model);
                    // now execute all rules that are meant to trigger at startup
                    scheduleStartupRules();
                }
            }
        }
    }

    private void scheduleStartupRules() {
        if (startupJob != null && !startupJob.isCancelled() && !startupJob.isDone()) {
            startupJob.cancel(true);
        }
        startupJob = scheduler.schedule(startupRunnable, 5, TimeUnit.SECONDS);
    }

    private void runStartupRules() {
        if (triggerManager != null) {
            Iterable<Rule> startupRules = triggerManager.getRules(STARTUP);
            List<Rule> executedRules = Lists.newArrayList();

            for (Rule rule : startupRules) {
                try {
                    Script script = scriptEngine.newScriptFromXExpression(rule.getScript());
                    logger.debug("Executing startup rule '{}'", rule.getName());
                    RuleEvaluationContext context = new RuleEvaluationContext();
                    context.setGlobalContext(RuleContextHelper.getContext(rule, injector));
                    script.execute(context);
                    executedRules.add(rule);
                } catch (ScriptExecutionException e) {
                    if (!e.getMessage().contains("cannot be resolved to an item or type")) {
                        logger.error("Error during the execution of startup rule '{}': {}",
                                new Object[] { rule.getName(), e.getCause().getMessage() });
                        executedRules.add(rule);
                    } else {
                        logger.debug("Execution of startup rule '{}' has been postponed as items are still missing: {}",
                                rule.getName(), e.getMessage());
                    }
                }
            }
            for (Rule rule : executedRules) {
                triggerManager.removeRule(STARTUP, rule);
            }
            // now that we have executed the startup rules, we are ready for others as well
            starting = false;
            triggerManager.startTimerRuleExecution();
        }
    }

    protected synchronized void executeRule(Rule rule, RuleEvaluationContext context) {
        Script script = scriptEngine.newScriptFromXExpression(rule.getScript());

        logger.debug("Executing rule '{}'", rule.getName());

        context.setGlobalContext(RuleContextHelper.getContext(rule, injector));

        ScriptExecutionThread thread = new ScriptExecutionThread(rule.getName(), script, context);
        thread.start();
    }

    protected synchronized void executeRules(Iterable<Rule> rules) {
        for (Rule rule : rules) {
            RuleEvaluationContext context = new RuleEvaluationContext();
            executeRule(rule, context);
        }
    }

    protected synchronized void executeRules(Iterable<Rule> rules, ChannelTriggeredEvent event) {
        for (Rule rule : rules) {
            RuleEvaluationContext context = new RuleEvaluationContext();
            context.newValue(QualifiedName.create(RulesJvmModelInferrer.VAR_RECEIVED_EVENT), event);
            executeRule(rule, context);
        }
    }

    protected synchronized void executeRules(Iterable<Rule> rules, Command command) {
        for (Rule rule : rules) {
            RuleEvaluationContext context = new RuleEvaluationContext();
            context.newValue(QualifiedName.create(RulesJvmModelInferrer.VAR_RECEIVED_COMMAND), command);
            executeRule(rule, context);
        }
    }

    protected synchronized void executeRules(Iterable<Rule> rules, State oldState) {
        for (Rule rule : rules) {
            RuleEvaluationContext context = new RuleEvaluationContext();
            context.newValue(QualifiedName.create(RulesJvmModelInferrer.VAR_PREVIOUS_STATE), oldState);
            executeRule(rule, context);
        }
    }

    /**
     * we need to be able to deactivate the rule execution, otherwise the openHAB designer
     * would also execute the rules.
     *
     * @return true, if rules should be executed, false otherwise
     */
    private boolean isEnabled() {
        return !"true".equalsIgnoreCase(System.getProperty("noRules"));
    }

    @Override
    public void updated(Item oldItem, Item item) {
        removed(oldItem);
        added(item);
    }

    private final Set<String> subscribedEventTypes = ImmutableSet.of(ItemStateEvent.TYPE, ItemCommandEvent.TYPE,
            ChannelTriggeredEvent.TYPE);

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        if (event instanceof ItemCommandEvent) {
            receiveCommand((ItemCommandEvent) event);
        } else if (event instanceof ChannelTriggeredEvent) {
            receiveThingTrigger((ChannelTriggeredEvent) event);
        }
    }
}
