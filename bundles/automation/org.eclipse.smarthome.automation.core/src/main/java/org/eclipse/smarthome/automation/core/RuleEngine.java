/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

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
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusDetail;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.RuleEngineCallbackImpl.TriggerData;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to initialized and execute {@link Rule}s added in rule engine.
 * Each Rule has associated RuleStatus object which can show current state of of the Rule.
 * The states are:
 * <LI>not initialized - these are rules added to the rule engine, but not working because they have modules without
 * associated module handler.
 * <LI>Initialized - all the modules of the rule have linked module handlers. They are registered and can be executed.
 * <LI>Enable/Disabled - the rule is temporary stopped by the user.
 * <LI>Running - the executed triggered data at moment of status check.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
@SuppressWarnings("rawtypes")
public class RuleEngine implements ServiceTrackerCustomizer/* <ModuleHandlerFactory, ModuleHandlerFactory> */ {

    /**
     * Constant defining separator between system and custom module types. For example: SampleTrigger:CustomTrigger is a
     * custom module type uid which defines custom trigger type base on the SampleTrigge module type.
     */
    public static final int MODULE_TYPE_SEPARATOR = ':';

    /**
     * Constant defining header of automation logs.
     */
    public static final String LOG_HEADER = "[Automation] ";

    /**
     * {@link Map} of rule's id to corresponding {@link RuleEngineCallback}s. For each {@link Rule} there is one and
     * only one rule callback.
     */
    private Map<String, RuleEngineCallbackImpl> reCallbacks = new HashMap<String, RuleEngineCallbackImpl>();

    /**
     * {@link Map} of system module type (handler's module types) to rules where this module types are used in.
     */
    private Map<String, Set<String>> mapHandlerTypeToRule = new HashMap<String, Set<String>>();

    /**
     * {@link Map} of created rules. It contains all rules added to rule engine independent if they are initialized or
     * not. The relation is rule's id to {@link Rule} object.
     */
    private Map<String, RuleImpl> rules;

    /**
     * Tracker of module handler factories. Each factory has a type which can evaluate. This type corresponds to the
     * system module type of the module.
     */
    private ServiceTracker/* <ModuleHandlerFactory, ModuleHandlerFactory> */ mhfTracker;

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

    /**
     * Constructor of {@link RuleEngine}. It initializes rules and handler factories maps and starts
     * tracker for {@link ModuleHandlerFactory} services.
     *
     * @param bc {@link BundleContext} used for tracker registration and rule engine logger creation.
     */
    @SuppressWarnings("unchecked")
    public RuleEngine(BundleContext bc) {
        this.bc = bc;
        logger = LoggerFactory.getLogger(getClass());
        if (rules == null) {
            rules = new HashMap<String, RuleImpl>(20);
        }
        moduleHandlerFactories = new HashMap<String, ModuleHandlerFactory>(20);
        mhfTracker = new ServiceTracker/* <ModuleHandlerFactory, ModuleHandlerFactory> */(bc,
                ModuleHandlerFactory.class.getName(), this);
        mhfTracker.open();

    }

    /**
     * This method revive the rule. It links all {@link Module}s participating in the rule to their
     * {@link ModuleHandler}s.
     * When all the modules are connected to their module handlers then the {@link Rule} goes into initialized state and
     * it starts working. Otherwise the Rule is marked as not initialized and it continues waiting missing module
     * handler to be appeared. The rule's states can be gotten by the {@link RuleStatus} object.
     *
     * @param r a new rule which has to be evaluated by the RuleEngine.
     * @return true when the rule is successfully added to the RuleEngine.
     */
    protected synchronized void setRule(RuleImpl r) {
        if (isDisposed) {
            return;
        }

        RuleStatusInfo ruleStatus = statusMap.get(r.getUID());
        if (ruleStatus != null && RuleStatus.DISABLED == ruleStatus.getStatus()) {
            return;
        }

        System.out.println("RuleEngine setRule " + r.getUID());
        String rUID = r.getUID();

        setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.NOT_INITIALIZED));

        // List<RuleError> errList = null;
        String errMsgs = null;
        String errMessage;
        if (r.conditions != null) {
            errMessage = setModuleHandler(rUID, r.conditions);
            if (errMessage != null) {
                errMsgs = errMessage;
            }
        }

        errMessage = setModuleHandler(rUID, r.actions);
        if (errMessage != null) {
            errMsgs = errMsgs + errMessage;
        }

        errMessage = setModuleHandler(rUID, r.triggers);
        if (errMessage != null) {
            errMsgs = errMsgs + errMessage;
        }

        rules.put(rUID, r);

        if (errMessage == null) {
            register(r);

            // change state to IDLE
            setRuleStatusInfo(r.getUID(), new RuleStatusInfo(RuleStatus.IDLE));

            logger.debug(LOG_HEADER, "Rule started: " + r.getUID());
        } else {
            unregister(r);

            // change state to NOTINITIALIZED
            setRuleStatusInfo(r.getUID(), new RuleStatusInfo(RuleStatus.NOT_INITIALIZED,
                    RuleStatusDetail.HANDLER_INITIALIZING_ERROR, errMessage));

            logger.debug(LOG_HEADER, "Rule stopped: " + r.getUID());

        }
    }

    /**
     * This method changes {@link RuleStatusInfo} of the Rule.
     *
     * @param rUID rule's id
     * @param status new rule status info
     */
    private void setRuleStatusInfo(String rUID, RuleStatusInfo status) {
        statusMap.put(rUID, status);
        // TODO fire status change event
    }

    /**
     * This method links modules to corresponding module handlers.
     *
     * @param rId id of rule containing these modules
     * @param modules list of modules
     * @return null when all modules are connected or list of RuleErrors for missing handlers.
     */
    @SuppressWarnings("unchecked")
    private <T extends Module> String setModuleHandler(String rId, List<T> modules) {
        StringBuffer sb = null;
        if (modules != null) {
            for (Iterator<T> it = modules.iterator(); it.hasNext();) {
                T t = it.next();
                Set<String> rules = mapHandlerTypeToRule.get(t.getTypeUID());
                if (rules == null) {
                    rules = new HashSet<String>(11);
                }
                rules.add(rId);
                mapHandlerTypeToRule.put(t.getTypeUID(), rules);
                ModuleHandler moduleHandler = getModuleHandler(t);
                if (moduleHandler != null) {
                    ((ModuleImpl) t).setModuleHandler(moduleHandler);
                } else {
                    if (sb == null) {
                        sb = new StringBuffer();
                    }
                    String message = "Missing handler: " + t.getTypeUID() + ", for module: " + t.getId();
                    sb.append(message).append("\n");
                    logger.warn(message);
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
    private RuleEngineCallbackImpl getRuleEngineCallback(RuleImpl rule) {
        RuleEngineCallbackImpl result = reCallbacks.get(rule.getUID());
        if (result == null) {
            result = new RuleEngineCallbackImpl(this, rule);
            reCallbacks.put(rule.getUID(), result);
        }
        return result;
    }

    /**
     * Unlink module handlers from their modules. The method is called when the rule containing these modules is
     * deinitialized (i.e. when some of module handler factories is disappeared or the rule is removed).
     *
     * @param modules list of module which are disconnected.
     */
    @SuppressWarnings("unchecked")
    private <T extends Module> void removeHandlers(List<T> modules) {
        if (modules != null) {
            for (Iterator<T> it = modules.iterator(); it.hasNext();) {
                ModuleImpl m = (ModuleImpl) it.next();
                ModuleHandler moduleHandler = m.getModuleHandler();
                if (moduleHandler != null) {
                    moduleHandler.dispose();
                    m.setModuleHandler(null);
                }
            }
        }
    }

    /**
     * This method register the Rule to start working. This is the last step of initialization process and it is done
     * when all the modules are connected to their module handlers. The registration process is passing
     * {@link RuleEngineCallback} of the rule to rule's {@link Trigger}s. When the {@link Trigger}s have callback object
     * they can start notify the rule about triggering events.
     *
     * @param r
     */
    private void register(RuleImpl r) {
        RuleEngineCallback reCallback = getRuleEngineCallback(r);
        for (Iterator<Trigger> it = r.triggers.iterator(); it.hasNext();) {
            TriggerImpl t = (TriggerImpl) it.next();
            TriggerHandler triggerHandler = t.getModuleHandler();
            triggerHandler.setRuleEngineCallback(reCallback);
        }
    }

    /**
     * This method unregister rule form rule engine and the rule stops working. This is happen when the {@link Rule} is
     * removed or some of module handlers factories used by the rule's modules is disappeared. In the second case the
     * rule stays available but its state is moved to not initialized.
     *
     * @param r the unregistered rule
     */
    private void unregister(RuleImpl r) {
        RuleEngineCallbackImpl reCallback = reCallbacks.remove(r.getUID());
        if (reCallback != null) {
            reCallback.dispose();
        }
        removeHandlers(r.triggers);
        removeHandlers(r.actions);
        removeHandlers(r.conditions);
    }

    /**
     * Gets handler of passed module.
     *
     * @param m a {@link Module} which is looking for handler
     * @return handler for this module or null when it is not available.
     */
    private ModuleHandler getModuleHandler(Module m) {
        String mtId = m.getTypeUID();
        mtId = getSystemModuleType(mtId);
        ModuleHandlerFactory mhf = moduleHandlerFactories.get(mtId);
        if (mhf == null) {
            // throw new IllegalArgumentException("Invalid module handler factpry: " + mtId);
            return null;
        }
        return mhf.create(m);
    }

    /**
     * This method extract system module type of passed module type.
     *
     * @param mtId module type id
     * @return system module type for this module type.
     */
    private String getSystemModuleType(String mtId) {
        if (mtId == null) {
            throw new IllegalArgumentException("Invalid module type id. It must not be null!");
        }
        int idx = mtId.indexOf(MODULE_TYPE_SEPARATOR);
        if (idx != -1) {
            mtId = mtId.substring(0, idx);
        }
        return mtId;
    }

    /**
     * This method removes Rule from rule engine. It is called by the {@link RuleManager}
     *
     * @param id id of removed {@link Rule}
     * @return removed {@link Rule} object.
     */
    protected synchronized RuleImpl removeRule(String id) {
        RuleImpl r = rules.remove(id);
        removeRuleEntry(r);
        return r;
    }

    /**
     * Utility method cleaning status and handler type Maps for removed {@link Rule}.
     *
     * @param r removed {@link Rule}
     * @return removed rule
     */
    private RuleImpl removeRuleEntry(RuleImpl r) {
        unregister(r);
        statusMap.remove(r.getUID());

        for (Iterator<Map.Entry<String, Set<String>>> it = mapHandlerTypeToRule.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Set<String>> e = it.next();
            Set<String> rules = e.getValue();
            if (rules != null && rules.contains(r.getUID())) {
                rules.remove(r.getUID());
                if (rules.size() < 1) {
                    it.remove();
                }
            }
        }

        return r;
    }

    /**
     * Gets {@link Rule} object corresponding to the passed id
     *
     * @param rId rule id
     * @return {@link Rule} object or null when rule with such id is not added to the {@link RuleManager}.
     */
    protected RuleImpl getRule(String rId) {
        return rules.get(rId);
    }

    protected void setRuleEnable(String rUID, boolean isEnabled) {
        RuleStatus status = getRuleStatus(rUID);
        if (status != null) {
            if (isEnabled) {
                if (status == RuleStatus.DISABLED) {
                    setRule(getRule(rUID));
                } else {
                    logger.info("The rule rId = " + rUID + " is already enabled");
                }
            } else {
                unregister(getRule(rUID));
                // change state to DISABLED
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.DISABLED));
            }
        } else {
            throw new IllegalStateException("The rule rId = " + rUID + " is not available");
        }

    }

    /**
     * Gets running status of the {@link Rule}
     *
     * @param rUID id of the {@link Rule}
     * @return true when the rule is executing at the moment and false when the rule is initialized but it is into idle
     *         state.
     */
    protected boolean isRunning(String rUID) {
        RuleImpl r = rules.get(rUID);
        if (r != null) {
            RuleEngineCallbackImpl callback = reCallbacks.get(r.getUID());
            if (callback != null) {
                return callback.isRunning();
            }
        }
        return false;
    }

    /**
     * Gets collection of {@link Rule}s filtered by tag.
     *
     * @param tag the tag of looking rules.
     * @return Collection of rules containing specified tag.
     */
    public synchronized Collection<Rule> getRulesByTag(String tag) {
        Collection<Rule> result = new ArrayList<Rule>(10);
        for (Iterator<RuleImpl> it = rules.values().iterator(); it.hasNext();) {
            RuleImpl r = it.next();
            if (tag != null) {
                Set<String> tags = r.getTags();
                if (tags != null && tags.contains(tag)) {
                    result.add(new RuleImpl(r));
                }
            } else {
                result.add(new RuleImpl(r));
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
        for (Iterator<RuleImpl> it = rules.values().iterator(); it.hasNext();) {
            RuleImpl r = it.next();
            if (tags != null) {
                Set<String> rTags = r.getTags();
                if (tags != null) {
                    for (Iterator<String> i = rTags.iterator(); i.hasNext();) {
                        String tag = i.next();
                        if (tags.contains(tag)) {
                            result.add(new RuleImpl(r));
                            break;
                        }
                    }
                }
            } else {
                result.add(new RuleImpl(r));
            }
        }
        return result;
    }

    /**
     * Gets rules filtered by scope id.
     *
     * @return return rules belonging to specified scope.
     */
    protected Collection<String> getScopeIds() {
        Set<String> result = new HashSet<String>(10);
        for (Iterator<RuleImpl> it = rules.values().iterator(); it.hasNext();) {
            RuleImpl r = it.next();
            String id = r.getScopeIdentifier();
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    /**
     * This method tracks for {@link ModuleHandlerFactory}s. When a new factory is appeared it is added to the
     * {@link #moduleHandlerFactories} map and rules depending from the system module type handled by this factory are
     * tried to be initialized.
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
            Set<String> rules = mapHandlerTypeToRule.get(moduleTypeName);
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
                setRule(rules.get(rUID));
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
        // TODO Auto-generated method stub

    }

    /**
     * This method tracks for disappearing of {@link ModuleHandlerFactory} service. It deinitialise rules containing
     * modules depending from the types handled by this factory.
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
            moduleHandlerFactories.remove(moduleTypeName);
            Set<String> rules = mapHandlerTypeToRule.get(moduleTypeName);
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
        }
        if (mapMissingHandlers != null) {
            for (Entry<String, List<String>> e : mapMissingHandlers.entrySet()) {
                String rUID = e.getKey();
                List<String> missingTypes = e.getValue();
                // List<RuleError> errList = new ArrayList<RuleError>();
                StringBuffer sb = new StringBuffer();
                for (String typeUID : missingTypes) {
                    sb.append("Missing handler: ").append(typeUID).append("\n");
                }
                unregister(getRule(rUID));
                setRuleStatusInfo(rUID, new RuleStatusInfo(RuleStatus.NOT_INITIALIZED,
                        RuleStatusDetail.HANDLER_MISSING_ERROR, sb.toString()));
            }
        }
    }

    /**
     * This method triggers the {@link Rule}. It is called by the {@link RuleEngineCallback}'s thread when a new
     * {@link TriggerData} is available.
     *
     * @param rule the {@link Rule} which has to evaluated passed {@link TriggerData}.
     * @param td {@link TriggerData} object containing new values for {@link Trigger}'s {@link Output}s
     */
    protected void runRule(RuleImpl rule, RuleEngineCallbackImpl.TriggerData td) {
        RuleStatus ruleStatus = getRuleStatus(rule.getUID());
        if (ruleStatus == RuleStatus.IDLE) {
            try {

                // change state to RUNNING
                setRuleStatusInfo(rule.getUID(), new RuleStatusInfo(RuleStatus.RUNNING));
                setTriggerOutputs(td);
                boolean isSatisfied = calculateConditions(rule);
                if (isSatisfied) {
                    executeActions(rule);
                    logger.debug("The rule: " + rule.getUID() + " is executed.");
                }
            } catch (Throwable t) {
                logger.error("Fail to execute rule: " + rule.getUID(), t);
            }

            // change state to IDLE
            setRuleStatusInfo(rule.getUID(), new RuleStatusInfo(RuleStatus.IDLE));
        } else {
            logger.error("Try to execute NOT IDLE rule rID = " + rule.getUID() + " status = " + ruleStatus.getValue());
        }

    }

    /**
     * The method updates {@link Output} of the {@link Trigger} with a new triggered data
     *
     * @param td new Triggered data.
     */
    private void setTriggerOutputs(TriggerData td) {
        Trigger t = td.getTrigger();
        if (!(t instanceof SourceModule)) {
            throw new IllegalArgumentException("Invalid Trigger implementation: " + t);
        }

        SourceModule ds = (SourceModule) t;
        ds.setOutputs(td.getOutputs());
    }

    /**
     * This method checks if all rule's condition are satisfied or not.
     *
     * @param rule the checked rule
     * @return true when all conditions of the rule are satisfied, false otherwise.
     */
    private boolean calculateConditions(Rule rule) {
        List<Condition> conditions = ((RuleImpl) rule).conditions;
        if (conditions == null || conditions.size() == 0) {
            return true;
        }
        for (Iterator<Condition> it = conditions.iterator(); it.hasNext();) {
            ConditionImpl c = (ConditionImpl) it.next();
            Map<String, OutputRef> connectionObjects = c.getConnectedOutputs();
            if (connectionObjects == null) {
                connectionObjects = initConnections(c, rule);
            }
            ConditionHandler tHandler = c.getModuleHandler();
            Map<String, ?> inputs = getInputValues(connectionObjects);
            if (!tHandler.isSatisfied(inputs)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Utility method for getting {@link Map} of {@link Input}'s name and values applied to these inputs
     *
     * @param connectionOutputs {@link Map} of input's names and associated Outputs to them.
     * @return
     */
    private Map<String, ?> getInputValues(Map<String, OutputRef> connectionOutputs) {
        Map<String, Object> inputs = new HashMap<String, Object>(11);
        for (Iterator<Map.Entry<String, OutputRef>> it = connectionOutputs.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, OutputRef> e = it.next();
            inputs.put(e.getKey(), e.getValue().getValue());
        }
        return inputs;
    }

    /**
     * This method initialize connection between modules. It associate {@link OutputRef} objects to the inputs of
     * {@link ConnectedModule}. The inputs uses {@link OutputRef}s objects to get current value set to these outputs.
     *
     * @param cm connected module. These are module which have inputs (Conditions and Actions).
     * @param r rule where the {@link ConnectedModule} belongs to.
     * @return {@link Map} of inputs and associated to them {@link OutputRef}s
     */
    private Map<String, OutputRef> initConnections(ConnectedModule cm, Rule r) {
        Set<Connection> connections = cm.getConnections();
        Map<String, OutputRef> connectedOutputs = new HashMap<String, OutputRef>(11);
        if (connections != null) {
            for (Iterator<Connection> it = connections.iterator(); it.hasNext();) {
                Connection conn = it.next();
                String uid = conn.getOuputModuleId();
                Module m = ((RuleImpl) r).getModule0(uid);
                if (m instanceof SourceModule) {
                    OutputRef outputRef = new OutputRef(conn.getOutputName(), (SourceModule) m);
                    connectedOutputs.put(conn.getInputName(), outputRef);
                } else {
                    logger.warn("Condition " + cm + "can not be connected to module: " + uid
                            + ". The module is not available or not a data source!");
                }
            }
        }
        cm.setConnectedOutputs(connectedOutputs);
        return connectedOutputs;
    }

    /**
     * This method evaluates actions of the {@link Rule} and set their {@link Output}s when they exists.
     *
     * @param rule executed rule.
     */
    private void executeActions(Rule rule) {
        List<Action> actions = ((RuleImpl) rule).actions;
        if (actions == null || actions.size() == 0) {
            return;
        }
        for (Iterator<Action> it = actions.iterator(); it.hasNext();) {
            ActionImpl a = (ActionImpl) it.next();
            Map<String, OutputRef> connectionObjects = a.getConnectedOutputs();
            if (connectionObjects == null) {
                connectionObjects = initConnections(a, rule);
            }
            ActionHandler aHandler = a.getModuleHandler();
            Map<String, ?> inputs = getInputValues(connectionObjects);
            Map<String, ?> outputs = aHandler.execute(inputs);
            if (outputs != null) {
                a.setOutputs(outputs);
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
            for (Iterator<RuleImpl> it = rules.values().iterator(); it.hasNext();) {
                RuleImpl r = it.next();
                removeRuleEntry(r);
                it.remove();
            }
        }
    }

    /**
     * This method gets rule's status object.
     *
     * @param rUID rule uid
     * @return status of the rule or null when such rule does not exists.
     */
    protected RuleStatus getRuleStatus(String rUID) {
        RuleStatus status = statusMap.get(rUID).getStatus();
        return status;
    }

}
