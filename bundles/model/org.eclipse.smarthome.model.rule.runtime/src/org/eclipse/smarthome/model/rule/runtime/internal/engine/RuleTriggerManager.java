/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule.runtime.internal.engine;

import static org.eclipse.smarthome.model.rule.runtime.internal.engine.RuleTriggerManager.TriggerTypes.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.model.rule.rules.ChangedEventTrigger;
import org.eclipse.smarthome.model.rule.rules.CommandEventTrigger;
import org.eclipse.smarthome.model.rule.rules.EventEmittedTrigger;
import org.eclipse.smarthome.model.rule.rules.EventTrigger;
import org.eclipse.smarthome.model.rule.rules.Rule;
import org.eclipse.smarthome.model.rule.rules.RuleModel;
import org.eclipse.smarthome.model.rule.rules.SystemOnShutdownTrigger;
import org.eclipse.smarthome.model.rule.rules.SystemOnStartupTrigger;
import org.eclipse.smarthome.model.rule.rules.ThingStateChangedEventTrigger;
import org.eclipse.smarthome.model.rule.rules.ThingStateUpdateEventTrigger;
import org.eclipse.smarthome.model.rule.rules.TimerTrigger;
import org.eclipse.smarthome.model.rule.rules.UpdateEventTrigger;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This is a helper class which deals with everything about rule triggers.
 * It keeps lists of which rule must be executed for which trigger and takes
 * over the evaluation of states and trigger conditions for the rule engine.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class RuleTriggerManager {

    private final Logger logger = LoggerFactory.getLogger(RuleTriggerManager.class);

    public enum TriggerTypes {
        UPDATE, // fires whenever a status update is received for an item
        CHANGE, // same as UPDATE, but only fires if the current item state is changed by the update
        COMMAND, // fires whenever a command is received for an item
        TRIGGER, // fires whenever a trigger is emitted on a channel
        STARTUP, // fires when the rule engine bundle starts and once as soon as all required items are available
        SHUTDOWN, // fires when the rule engine bundle is stopped
        TIMER, // fires at a given time
        THINGUPDATE, // fires whenever the thing state is updated.
        THINGCHANGE, // fires if the thing state is changed by the update
    }

    // lookup maps for different triggering conditions
    private Map<String, Set<Rule>> updateEventTriggeredRules = Maps.newHashMap();
    private Map<String, Set<Rule>> changedEventTriggeredRules = Maps.newHashMap();
    private Map<String, Set<Rule>> commandEventTriggeredRules = Maps.newHashMap();
    private Map<String, Set<Rule>> thingUpdateEventTriggeredRules = Maps.newHashMap();
    private Map<String, Set<Rule>> thingChangedEventTriggeredRules = Maps.newHashMap();
    // Maps from channelName -> Rules
    private Map<String, Set<Rule>> triggerEventTriggeredRules = Maps.newHashMap();
    private Set<Rule> systemStartupTriggeredRules = new CopyOnWriteArraySet<>();
    private Set<Rule> systemShutdownTriggeredRules = new CopyOnWriteArraySet<>();
    private Set<Rule> timerEventTriggeredRules = new CopyOnWriteArraySet<>();

    // the scheduler used for timer events
    private Scheduler scheduler;

    @Inject
    public RuleTriggerManager(Injector injector) {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.setJobFactory(injector.getInstance(GuiceAwareJobFactory.class));

            // we want to defer timer rule execution until after the startup rules have been executed.
            scheduler.standby();
        } catch (SchedulerException e) {
            logger.error("initializing scheduler throws exception", e);
        }
    }

    /**
     * Returns all rules which have a trigger of a given type
     *
     * @param type the trigger type of the rules to return
     * @return rules with triggers of the given type
     */
    public Iterable<Rule> getRules(TriggerTypes type) {
        Iterable<Rule> result;
        switch (type) {
            case STARTUP:
                result = systemStartupTriggeredRules;
                break;
            case SHUTDOWN:
                result = systemShutdownTriggeredRules;
                break;
            case TIMER:
                result = timerEventTriggeredRules;
                break;
            case UPDATE:
                result = Iterables.concat(updateEventTriggeredRules.values());
                break;
            case CHANGE:
                result = Iterables.concat(changedEventTriggeredRules.values());
                break;
            case COMMAND:
                result = Iterables.concat(commandEventTriggeredRules.values());
                break;
            case TRIGGER:
                result = Iterables.concat(triggerEventTriggeredRules.values());
                break;
            case THINGUPDATE:
                result = Iterables.concat(thingUpdateEventTriggeredRules.values());
                break;
            case THINGCHANGE:
                result = Iterables.concat(thingChangedEventTriggeredRules.values());
                break;
            default:
                result = Sets.newHashSet();
        }
        List<Rule> filteredList = new ArrayList<>();
        for (Rule rule : result) {
            // we really only want to return rules that are still loaded
            if (rule.eResource() != null && !rule.eIsProxy()) {
                filteredList.add(rule);
            }
        }

        return filteredList;
    }

    /**
     * Returns all rules for which the trigger condition is true for the given type, item and state.
     *
     * @param triggerType
     * @param item
     * @param state
     * @return all rules for which the trigger condition is true
     */
    public Iterable<Rule> getRules(TriggerTypes triggerType, Item item, State state) {
        return internalGetRules(triggerType, item, null, state);
    }

    /**
     * Returns all rules for which the trigger condition is true for the given type, item and states.
     *
     * @param triggerType
     * @param item
     * @param oldState
     * @param newState
     * @return all rules for which the trigger condition is true
     */
    public Iterable<Rule> getRules(TriggerTypes triggerType, Item item, State oldState, State newState) {
        return internalGetRules(triggerType, item, oldState, newState);
    }

    /**
     * Returns all rules for which the trigger condition is true for the given type, item and command.
     *
     * @param triggerType
     * @param item
     * @param command
     * @return all rules for which the trigger condition is true
     */
    public Iterable<Rule> getRules(TriggerTypes triggerType, Item item, Command command) {
        return internalGetRules(triggerType, item, null, command);
    }

    /**
     * Returns all rules for which the trigger condition is true for the given type and channel.
     *
     * @param triggerType
     * @param channel
     * @return all rules for which the trigger condition is true
     */
    public Iterable<Rule> getRules(TriggerTypes triggerType, String channel, String event) {
        List<Rule> result = Lists.newArrayList();

        switch (triggerType) {
            case TRIGGER:
                Set<Rule> rules = triggerEventTriggeredRules.get(channel);
                if (rules == null) {
                    return Collections.emptyList();
                }
                for (Rule rule : rules) {
                    for (EventTrigger t : rule.getEventtrigger()) {
                        if (t instanceof EventEmittedTrigger) {
                            EventEmittedTrigger et = (EventEmittedTrigger) t;

                            if (et.getChannel().equals(channel)
                                    && (et.getTrigger() == null || et.getTrigger().equals(event))) {
                                // if the rule does not have a specific event , execute it on any event
                                result.add(rule);
                            }
                        }
                    }
                }
                break;
            default:
                return Collections.emptyList();
        }

        return result;
    }

    public Iterable<Rule> getRules(TriggerTypes triggerType, String thingUid, ThingStatus state) {
        return internalGetThingRules(triggerType, thingUid, null, state);
    }

    public Iterable<Rule> getRules(TriggerTypes triggerType, String thingUid, ThingStatus oldState,
            ThingStatus newState) {
        return internalGetThingRules(triggerType, thingUid, oldState, newState);
    }

    private Iterable<Rule> getAllRules(TriggerTypes type, String name) {
        switch (type) {
            case STARTUP:
                return systemStartupTriggeredRules;
            case SHUTDOWN:
                return systemShutdownTriggeredRules;
            case UPDATE:
                return updateEventTriggeredRules.get(name);
            case CHANGE:
                return changedEventTriggeredRules.get(name);
            case COMMAND:
                return commandEventTriggeredRules.get(name);
            case THINGUPDATE:
                return thingUpdateEventTriggeredRules.get(name);
            case THINGCHANGE:
                return thingChangedEventTriggeredRules.get(name);
            default:
                return Sets.newHashSet();
        }
    }

    private Iterable<Rule> internalGetRules(TriggerTypes triggerType, Item item, Type oldType, Type newType) {
        List<Rule> result = Lists.newArrayList();
        Iterable<Rule> rules = getAllRules(triggerType, item.getName());
        if (rules == null) {
            rules = Lists.newArrayList();
        }
        switch (triggerType) {
            case STARTUP:
                return systemStartupTriggeredRules;
            case SHUTDOWN:
                return systemShutdownTriggeredRules;
            case TIMER:
                return timerEventTriggeredRules;
            case UPDATE:
                if (newType instanceof State) {
                    State state = (State) newType;
                    for (Rule rule : rules) {
                        for (EventTrigger t : rule.getEventtrigger()) {
                            if (t instanceof UpdateEventTrigger) {
                                UpdateEventTrigger ut = (UpdateEventTrigger) t;
                                if (ut.getItem().equals(item.getName())) {
                                    if (ut.getState() != null) {
                                        State triggerState = TypeParser.parseState(item.getAcceptedDataTypes(),
                                                ut.getState());
                                        if (!state.equals(triggerState)) {
                                            continue;
                                        }
                                    }
                                    result.add(rule);
                                }
                            }
                        }
                    }
                }
                break;
            case CHANGE:
                if (newType instanceof State && oldType instanceof State) {
                    State newState = (State) newType;
                    State oldState = (State) oldType;
                    for (Rule rule : rules) {
                        for (EventTrigger t : rule.getEventtrigger()) {
                            if (t instanceof ChangedEventTrigger) {
                                ChangedEventTrigger ct = (ChangedEventTrigger) t;
                                if (ct.getItem().equals(item.getName())) {
                                    if (ct.getOldState() != null) {
                                        State triggerOldState = TypeParser.parseState(item.getAcceptedDataTypes(),
                                                ct.getOldState());
                                        if (!oldState.equals(triggerOldState)) {
                                            continue;
                                        }
                                    }
                                    if (ct.getNewState() != null) {
                                        State triggerNewState = TypeParser.parseState(item.getAcceptedDataTypes(),
                                                ct.getNewState());
                                        if (!newState.equals(triggerNewState)) {
                                            continue;
                                        }
                                    }
                                    result.add(rule);
                                }
                            }
                        }
                    }
                }
                break;
            case COMMAND:
                if (newType instanceof Command) {
                    final Command command = (Command) newType;
                    for (Rule rule : rules) {
                        for (final EventTrigger t : rule.getEventtrigger()) {
                            if (t instanceof CommandEventTrigger) {
                                final CommandEventTrigger ct = (CommandEventTrigger) t;
                                if (ct.getItem().equals(item.getName())) {
                                    if (ct.getCommand() != null) {
                                        final Command triggerCommand = TypeParser
                                                .parseCommand(item.getAcceptedCommandTypes(), ct.getCommand());
                                        if (!command.equals(triggerCommand)) {
                                            continue;
                                        }
                                    }
                                    result.add(rule);
                                }
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
        return result;
    }

    private Iterable<Rule> internalGetThingRules(TriggerTypes triggerType, String thingUid, ThingStatus oldStatus,
            ThingStatus newStatus) {
        List<Rule> result = Lists.newArrayList();
        Iterable<Rule> rules = getAllRules(triggerType, thingUid);
        if (rules == null) {
            rules = Lists.newArrayList();
        }

        switch (triggerType) {
            case THINGUPDATE:
                for (Rule rule : rules) {
                    for (EventTrigger t : rule.getEventtrigger()) {
                        if (t instanceof ThingStateUpdateEventTrigger) {
                            ThingStateUpdateEventTrigger tt = (ThingStateUpdateEventTrigger) t;
                            if (tt.getThing().equals(thingUid)) {
                                String stateString = tt.getState();
                                if (stateString != null) {
                                    ThingStatus triggerState = ThingStatus.valueOf(stateString);
                                    if (!newStatus.equals(triggerState)) {
                                        continue;
                                    }
                                }
                                result.add(rule);
                            }
                        }
                    }
                }
                break;
            case THINGCHANGE:
                for (Rule rule : rules) {
                    for (EventTrigger t : rule.getEventtrigger()) {
                        if (t instanceof ThingStateChangedEventTrigger) {
                            ThingStateChangedEventTrigger ct = (ThingStateChangedEventTrigger) t;
                            if (ct.getThing().equals(thingUid)) {
                                String oldStatusString = ct.getOldState();
                                if (oldStatusString != null) {
                                    ThingStatus triggerOldState = ThingStatus.valueOf(oldStatusString);
                                    if (!oldStatus.equals(triggerOldState)) {
                                        continue;
                                    }
                                }

                                String newStatusString = ct.getNewState();
                                if (newStatusString != null) {
                                    ThingStatus triggerNewState = ThingStatus.valueOf(newStatusString);
                                    if (!newStatus.equals(triggerNewState)) {
                                        continue;
                                    }
                                }
                                result.add(rule);
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Removes all rules with a given trigger type from the mapping tables.
     *
     * @param type the trigger type
     */
    public void clear(TriggerTypes type) {
        switch (type) {
            case STARTUP:
                systemStartupTriggeredRules.clear();
                break;
            case SHUTDOWN:
                systemShutdownTriggeredRules.clear();
                break;
            case UPDATE:
                updateEventTriggeredRules.clear();
                break;
            case CHANGE:
                changedEventTriggeredRules.clear();
                break;
            case COMMAND:
                commandEventTriggeredRules.clear();
                break;
            case TRIGGER:
                triggerEventTriggeredRules.clear();
                break;
            case TIMER:
                for (Rule rule : timerEventTriggeredRules) {
                    removeTimerRule(rule);
                }
                timerEventTriggeredRules.clear();
                break;
            case THINGUPDATE:
                thingUpdateEventTriggeredRules.clear();
                break;
            case THINGCHANGE:
                thingChangedEventTriggeredRules.clear();
                break;
        }
    }

    /**
     * Removes all rules from all mapping tables.
     */
    public void clearAll() {
        clear(STARTUP);
        clear(SHUTDOWN);
        clear(UPDATE);
        clear(CHANGE);
        clear(COMMAND);
        clear(TIMER);
        clear(TRIGGER);
        clear(THINGUPDATE);
        clear(THINGCHANGE);
    }

    /**
     * Adds a given rule to the mapping tables
     *
     * @param rule the rule to add
     */
    public synchronized void addRule(Rule rule) {
        for (EventTrigger t : rule.getEventtrigger()) {
            // add the rule to the lookup map for the trigger kind
            if (t instanceof SystemOnStartupTrigger) {
                systemStartupTriggeredRules.add(rule);
            } else if (t instanceof SystemOnShutdownTrigger) {
                systemShutdownTriggeredRules.add(rule);
            } else if (t instanceof CommandEventTrigger) {
                CommandEventTrigger ceTrigger = (CommandEventTrigger) t;
                Set<Rule> rules = commandEventTriggeredRules.get(ceTrigger.getItem());
                if (rules == null) {
                    rules = new HashSet<Rule>();
                    commandEventTriggeredRules.put(ceTrigger.getItem(), rules);
                }
                rules.add(rule);
            } else if (t instanceof UpdateEventTrigger) {
                UpdateEventTrigger ueTrigger = (UpdateEventTrigger) t;
                Set<Rule> rules = updateEventTriggeredRules.get(ueTrigger.getItem());
                if (rules == null) {
                    rules = new HashSet<Rule>();
                    updateEventTriggeredRules.put(ueTrigger.getItem(), rules);
                }
                rules.add(rule);
            } else if (t instanceof ChangedEventTrigger) {
                ChangedEventTrigger ceTrigger = (ChangedEventTrigger) t;
                Set<Rule> rules = changedEventTriggeredRules.get(ceTrigger.getItem());
                if (rules == null) {
                    rules = new HashSet<Rule>();
                    changedEventTriggeredRules.put(ceTrigger.getItem(), rules);
                }
                rules.add(rule);
            } else if (t instanceof TimerTrigger) {
                try {
                    createTimer(rule, (TimerTrigger) t);
                    timerEventTriggeredRules.add(rule);
                } catch (SchedulerException e) {
                    logger.error("Cannot create timer for rule '{}': {}", rule.getName(), e.getMessage());
                }
            } else if (t instanceof EventEmittedTrigger) {
                EventEmittedTrigger eeTrigger = (EventEmittedTrigger) t;
                Set<Rule> rules = triggerEventTriggeredRules.get(eeTrigger.getChannel());
                if (rules == null) {
                    rules = new HashSet<Rule>();
                    triggerEventTriggeredRules.put(eeTrigger.getChannel(), rules);
                }
                rules.add(rule);
            } else if (t instanceof ThingStateUpdateEventTrigger) {
                ThingStateUpdateEventTrigger tsuTrigger = (ThingStateUpdateEventTrigger) t;
                Set<Rule> rules = thingUpdateEventTriggeredRules.get(tsuTrigger.getThing());
                if (rules == null) {
                    rules = new HashSet<Rule>();
                    thingUpdateEventTriggeredRules.put(tsuTrigger.getThing(), rules);
                }
                rules.add(rule);
            } else if (t instanceof ThingStateChangedEventTrigger) {
                ThingStateChangedEventTrigger tscTrigger = (ThingStateChangedEventTrigger) t;
                Set<Rule> rules = thingChangedEventTriggeredRules.get(tscTrigger.getThing());
                if (rules == null) {
                    rules = new HashSet<Rule>();
                    thingChangedEventTriggeredRules.put(tscTrigger.getThing(), rules);
                }
                rules.add(rule);
            }
        }
    }

    /**
     * Removes a given rule from the mapping tables of a certain trigger type
     *
     * @param type the trigger type for which the rule should be removed
     * @param rule the rule to add
     */
    public void removeRule(TriggerTypes type, Rule rule) {
        switch (type) {
            case STARTUP:
                systemStartupTriggeredRules.remove(rule);
                break;
            case SHUTDOWN:
                systemShutdownTriggeredRules.remove(rule);
                break;
            case UPDATE:
                for (Set<Rule> rules : updateEventTriggeredRules.values()) {
                    rules.remove(rule);
                }
                break;
            case CHANGE:
                for (Set<Rule> rules : changedEventTriggeredRules.values()) {
                    rules.remove(rule);
                }
                break;
            case COMMAND:
                for (Set<Rule> rules : commandEventTriggeredRules.values()) {
                    rules.remove(rule);
                }
                break;
            case TRIGGER:
                for (Set<Rule> rules : triggerEventTriggeredRules.values()) {
                    rules.remove(rule);
                }
                break;
            case TIMER:
                timerEventTriggeredRules.remove(rule);
                removeTimerRule(rule);
                break;
            case THINGUPDATE:
                for (Set<Rule> rules : thingUpdateEventTriggeredRules.values()) {
                    rules.remove(rule);
                }
                break;
            case THINGCHANGE:
                for (Set<Rule> rules : thingChangedEventTriggeredRules.values()) {
                    rules.remove(rule);
                }
                break;
        }
    }

    /**
     * Adds all rules of a model to the mapping tables
     *
     * @param model the rule model
     */
    public void addRuleModel(RuleModel model) {
        for (Rule rule : model.getRules()) {
            addRule(rule);
        }
    }

    /**
     * Removes all rules of a given model (file) from the mapping tables.
     *
     * @param ruleModel the rule model
     */
    public void removeRuleModel(RuleModel ruleModel) {
        removeRules(UPDATE, updateEventTriggeredRules.values(), ruleModel);
        removeRules(CHANGE, changedEventTriggeredRules.values(), ruleModel);
        removeRules(COMMAND, commandEventTriggeredRules.values(), ruleModel);
        removeRules(TRIGGER, triggerEventTriggeredRules.values(), ruleModel);
        removeRules(STARTUP, Collections.singletonList(systemStartupTriggeredRules), ruleModel);
        removeRules(SHUTDOWN, Collections.singletonList(systemShutdownTriggeredRules), ruleModel);
        removeRules(TIMER, Collections.singletonList(timerEventTriggeredRules), ruleModel);
        removeRules(THINGUPDATE, thingUpdateEventTriggeredRules.values(), ruleModel);
        removeRules(THINGCHANGE, thingChangedEventTriggeredRules.values(), ruleModel);
    }

    private void removeRules(TriggerTypes type, Collection<? extends Collection<Rule>> ruleSets, RuleModel model) {
        for (Collection<Rule> ruleSet : ruleSets) {
            Set<Rule> clonedSet = new HashSet<Rule>(ruleSet);
            // first remove all rules of the model, if not null (=non-existent)
            if (model != null) {
                for (Rule newRule : model.getRules()) {
                    for (Rule oldRule : clonedSet) {
                        if (newRule.getName().equals(oldRule.getName())) {
                            ruleSet.remove(oldRule);
                            if (type == TIMER) {
                                removeTimerRule(oldRule);
                            }
                        }
                    }
                }
            }

            // now also remove all proxified rules from the set
            clonedSet = new HashSet<Rule>(ruleSet);
            for (Rule rule : clonedSet) {
                if (rule.eResource() == null) {
                    ruleSet.remove(rule);
                    if (type == TIMER) {
                        removeTimerRule(rule);
                    }
                }
            }
        }
    }

    private void removeTimerRule(Rule rule) {
        try {
            removeTimer(rule);
        } catch (SchedulerException e) {
            logger.error("Cannot remove timer for rule '{}'", rule.getName(), e);
        }
    }

    /**
     * Creates and schedules a new quartz-job and trigger with model and rule name as jobData.
     *
     * @param rule the rule to schedule
     * @param trigger the defined trigger
     *
     * @throws SchedulerException if there is an internal Scheduler error.
     */
    private void createTimer(Rule rule, TimerTrigger trigger) throws SchedulerException {
        String cronExpression = trigger.getCron();
        if (trigger.getTime() != null) {
            if (trigger.getTime().equals("noon")) {
                cronExpression = "0 0 12 * * ?";
            } else if (trigger.getTime().equals("midnight")) {
                cronExpression = "0 0 0 * * ?";
            } else {
                logger.warn("Unrecognized time expression '{}' in rule '{}'", trigger.getTime(), rule.getName());
                return;
            }
        }

        String jobIdentity = getJobIdentityString(rule, trigger);

        try {
            JobDetail job = newJob(ExecuteRuleJob.class)
                    .usingJobData(ExecuteRuleJob.JOB_DATA_RULEMODEL, rule.eResource().getURI().path())
                    .usingJobData(ExecuteRuleJob.JOB_DATA_RULENAME, rule.getName()).withIdentity(jobIdentity).build();
            Trigger quartzTrigger = newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
            scheduler.scheduleJob(job, Collections.singleton(quartzTrigger), true);

            logger.debug("Scheduled rule '{}' with cron expression '{}'", rule.getName(), cronExpression);
        } catch (RuntimeException e) {
            throw new SchedulerException(e.getMessage());
        }
    }

    /**
     * Delete all {@link Job}s of the DEFAULT group whose name starts with <code>rule.getName()</code>.
     *
     * @throws SchedulerException if there is an internal Scheduler error.
     */
    private void removeTimer(Rule rule) throws SchedulerException {
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Scheduler.DEFAULT_GROUP));
        for (JobKey jobKey : jobKeys) {
            String jobIdentityString = getJobIdentityString(rule, null);
            if (jobKey.getName().startsWith(jobIdentityString)) {
                boolean success = scheduler.deleteJob(jobKey);
                if (!success) {
                    logger.warn("Failed to delete cron job '{}'", jobKey.getName());
                } else {
                    logger.debug("Removed scheduled cron job '{}'", jobKey.getName());
                }
            }
        }
    }

    private String getJobIdentityString(Rule rule, TimerTrigger trigger) {
        String jobIdentity = EcoreUtil.getURI(rule).trimFragment().appendFragment(rule.getName()).toString();
        if (trigger != null) {
            if (trigger.getTime() != null) {
                jobIdentity += "#" + trigger.getTime();
            } else if (trigger.getCron() != null) {
                jobIdentity += "#" + trigger.getCron();
            }
        }
        return jobIdentity;
    }

    public void startTimerRuleExecution() {
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error("Error while starting the scheduler service: {}", e.getMessage());
        }
    }
}
