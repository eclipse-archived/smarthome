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
import org.eclipse.smarthome.automation.RuleError;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.RuleEngineCallbackImpl.TriggerData;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.Input;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yordan Mihaylov - Initial Contribution
 */
public class RuleEngine implements ServiceTrackerCustomizer/* <ModuleHandlerFactory, ModuleHandlerFactory> */ {

    public static final int MODULE_TYPE_SEPARATOR = ':';
    public static final String LOG_HEADER = "[Automation] ";

    private static Map<String, RuleEngineCallbackImpl> reCallbacks = new HashMap<String, RuleEngineCallbackImpl>();
    private static Map<String, Set<String>> mapHandlerTypeToRule = new HashMap<String, Set<String>>();

    private Map<String, RuleImpl> rules;
    private ServiceTracker/* <ModuleHandlerFactory, ModuleHandlerFactory> */ mhfTracker;
    private BundleContext bc;
    private Map<String, ModuleHandlerFactory> moduleHandlerFactories;
    private Map<String, Set<String>> mapTypeToRules;
    private Map<String, Set<String>> mapRuleToTypes;
    private boolean isDisposed = false;
    private static Map<String, RuleStatus> statusMap = new HashMap<String, RuleStatus>();
    private static Logger log;

    public RuleEngine(BundleContext bc) {
        this.bc = bc;
        RuleEngine.log = LoggerFactory.getLogger(getClass());
        if (rules == null) {
            rules = new HashMap<String, RuleImpl>(20);
        }
        mapTypeToRules = new HashMap<String, Set<String>>(20);
        mapTypeToRules = new HashMap<String, Set<String>>(20);
        moduleHandlerFactories = new HashMap<String, ModuleHandlerFactory>(20);
        mhfTracker = new ServiceTracker/* <ModuleHandlerFactory, ModuleHandlerFactory> */(bc,
                ModuleHandlerFactory.class.getName(), this);
        mhfTracker.open();

    }

    /**
     * This method revive the rule. It connects create and connect module handler objects for each
     * module participating in the rule.
     *
     * @param r a new rule which has to be evaluated by the RuleEngine.
     * @return true when the rule is successfully added to the RuleEngine.
     */
    protected synchronized void setRule(RuleImpl r) {
        if (isDisposed) {
            return;
        }
        System.out.println("RuleEngine setRule " + r.getUID());
        String rUID = r.getUID();
        List<RuleError> errList = null;
        if (r.conditions != null) {
            errList = setModuleHandler(rUID, r.conditions);
        }

        List<RuleError> errors = setModuleHandler(rUID, r.actions);
        if (errors != null) {
            if (errList == null) {
                errList = errors;
            } else {
                errList.addAll(errors);
            }
        }

        errors = setModuleHandler(rUID, r.triggers);
        if (errors != null) {
            if (errList == null) {
                errList = errors;
            } else {
                errList.addAll(errors);
            }
        }

        rules.put(r.getUID(), r);

        if (errList == null) {
            register(r);
        } else {
            unregister(r, errList);
        }
    }

    private void setRuleStatus(String rUID, RuleStatus status) {
        RuleStatus oldStatus = statusMap.get(rUID);
        if (!status.equals(oldStatus)) {
            statusMap.put(rUID, status);
            // TODO fire status change event
        }
    }

    /**
     * This method connects modules to corresponding module handlers.
     *
     * @param modules list of modules
     * @param all flag determinating result of the method. When it is true the registration
     *            will be successful only when all the modules are connected to their module handlers. When it is false
     *            the registration is successful even when one module is connected to its module handler.
     * @return null when all modules are connected or list of RuleErrors of missing hendlers.
     */
    private <T extends Module> List<RuleError> setModuleHandler(String rUID, List<T> modules) {
        List<RuleError> result = null;

        if (modules != null) {
            for (Iterator<T> it = modules.iterator(); it.hasNext();) {
                T t = it.next();
                Set rules = mapHandlerTypeToRule.get(t.getTypeUID());
                if (rules == null) {
                    rules = new HashSet<String>(11);
                }
                rules.add(rUID);
                mapHandlerTypeToRule.put(t.getTypeUID(), rules);
                ModuleHandler moduleHandler = getModuleHandler(t);
                if (moduleHandler != null) {
                    ((ModuleImpl) t).setModuleHandler(moduleHandler);
                } else {
                    String message = "Missing handler: " + t.getTypeUID() + ", for module: " + t.getId();
                    RuleError err = new RuleErrorImpl(RuleError.ERROR_CODE_MISSING_HANDLER, message);
                    if (result == null) {
                        result = new ArrayList<RuleError>();
                        result.add(err);
                    }
                    log.warn(err.getMessage());
                }
            }
        }
        return result;
    }

    private static RuleEngineCallbackImpl getRuleEngineCallback(RuleImpl r) {
        RuleEngineCallbackImpl result = reCallbacks.get(r.getUID());
        if (result == null) {
            result = new RuleEngineCallbackImpl(r);
            reCallbacks.put(r.getUID(), result);
        }
        return result;
    }

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

    private void register(RuleImpl r) {
        RuleEngineCallback reCallback = getRuleEngineCallback(r);
        for (Iterator<Trigger> it = r.triggers.iterator(); it.hasNext();) {
            TriggerImpl t = (TriggerImpl) it.next();
            TriggerHandler triggerHandler = t.getModuleHandler();
            triggerHandler.setRuleEngineCallback(reCallback);
        }
        RuleStatus status = statusMap.get(r.getUID());
        boolean isEnabled = status != null ? status.isEnabled() : r.initialEnabled;
        status = new RuleStatusImpl(isEnabled, false);
        setRuleStatus(r.getUID(), status);

        log.debug(LOG_HEADER, "Rule started: " + r.getUID());
    }

    private void unregister(RuleImpl r, List<RuleError> errList) {
        RuleEngineCallbackImpl reCallback = reCallbacks.remove(r.getUID());
        if (reCallback != null) {
            reCallback.dispose();
        }
        removeHandlers(r.triggers);
        removeHandlers(r.actions);
        removeHandlers(r.conditions);

        RuleStatus status = statusMap.get(r.getUID());
        boolean isEnabled = status != null ? status.isEnabled() : r.initialEnabled;
        status = new RuleStatusImpl(isEnabled, false, false, errList);
        setRuleStatus(r.getUID(), status);

        log.debug(LOG_HEADER, "Rule stopped: " + r.getUID());

    }

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

    protected synchronized RuleImpl removeRule(String id) {
        RuleImpl r = rules.remove(id);
        removeRuleEntry(r);
        return r;
    }

    private RuleImpl removeRuleEntry(RuleImpl r) {
        unregister(r, null);
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

    protected RuleImpl getRule(String rUID) {
        return rules.get(rUID);
    }

    protected void setRuleEnable(String rUID, boolean isEnabled) {
        RuleStatus status = getRuleStatus(rUID);
        if (status != null) {
            status = new RuleStatusImpl(isEnabled, status.isRunning(), status.isInitialize(), status.getErrors());
        } else {
            status = new RuleStatusImpl(isEnabled, false);
        }
        setRuleStatus(rUID, status);
    }

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
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public synchronized ModuleHandlerFactory addingService(ServiceReference/* <ModuleHandlerFactory> */ reference) {
        ModuleHandlerFactory mhf = (ModuleHandlerFactory) bc.getService(reference);
        Collection<String> moduleTypes = mhf.getTypes();
        Set<String> notInitailizedRules = null;
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            // if (moduleHandlerFactories.get(moduleTypeName) != null) {
            // log.error("Module handler factory for the type: " + moduleTypeName + " is already degined!");
            // } else {
            moduleHandlerFactories.put(moduleTypeName, mhf);
            Set<String> rules = mapHandlerTypeToRule.get(moduleTypeName);
            if (rules != null) {
                for (String rUID : rules) {
                    RuleStatus status = getRuleStatus(rUID);
                    if (status == null || !status.isInitialize()) {
                        notInitailizedRules = notInitailizedRules != null ? notInitailizedRules
                                : new HashSet<String>(20);
                        notInitailizedRules.add(rUID);
                    }

                }
            }
            // }
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
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public synchronized void removedService(
            ServiceReference/* <ModuleHandlerFactory> */ reference, /* ModuleHandlerFactory */
            Object service) {
        Collection<String> moduleTypes = ((ModuleHandlerFactory) service).getTypes();
        Map<String, List<String>> mapMissingHanlers = null;
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            moduleHandlerFactories.remove(moduleTypeName);
            Set<String> rules = mapHandlerTypeToRule.get(moduleTypeName);
            if (rules != null) {
                for (String rUID : rules) {
                    RuleStatus status = getRuleStatus(rUID);
                    if (status != null && status.isInitialize()) {
                        mapMissingHanlers = mapMissingHanlers != null ? mapMissingHanlers
                                : new HashMap<String, List<String>>(20);
                        List<String> list = mapMissingHanlers.get(rUID);
                        if (list == null) {
                            list = new ArrayList<String>(5);
                        }
                        list.add(moduleTypeName);
                        mapMissingHanlers.put(rUID, list);
                    }
                }
            }
        }
        if (mapMissingHanlers != null) {
            for (Entry<String, List<String>> e : mapMissingHanlers.entrySet()) {
                String rUID = e.getKey();
                List<String> missingTypes = e.getValue();
                List<RuleError> errList = new ArrayList<RuleError>();
                for (String typeUID : missingTypes) {
                    String message = "Missing handler: " + typeUID;
                    RuleError err = new RuleErrorImpl(RuleError.ERROR_CODE_MISSING_HANDLER, message);
                    errList.add(err);
                }
                unregister(getRule(rUID), errList);
            }
        }
    }

    protected static void runRule(RuleImpl r, RuleEngineCallbackImpl.TriggerData td) {
        RuleStatus status = getRuleStatus(r.getUID());
        if (status == null || !status.isEnabled()) {
            log.debug("The rule: " + r.getUID() + " is not exists or not enabled.");
            return;
        }
        try {
            setTriggerOutputs(td);
            boolean isSatisfied = calculateConditions(r);
            if (isSatisfied) {
                executeActions(r);
                log.debug("The rule: " + r.getUID() + " is executed.");
            }
        } catch (Throwable t) {
            log.error("Fail to execute rule: " + r.getUID(), t);
        }

    }

    private static void setTriggerOutputs(TriggerData td) {
        Trigger t = td.getTrigger();
        if (!(t instanceof SourceModule)) {
            throw new IllegalArgumentException("Invalid Trigger implementation: " + t);
        }

        SourceModule ds = (SourceModule) t;
        ds.setOutputs(td.getOutputs());
    }

    private static boolean calculateConditions(Rule r) {
        List<Condition> conditions = ((RuleImpl) r).conditions;
        if (conditions == null || conditions.size() == 0) {
            return true;
        }
        for (Iterator<Condition> it = conditions.iterator(); it.hasNext();) {
            ConditionImpl c = (ConditionImpl) it.next();
            Map<String, OutputValue> connectionObjects = c.getConnectedObjects();
            if (connectionObjects == null) {
                connectionObjects = initConnections(c, r);
            }
            ConditionHandler tHandler = c.getModuleHandler();
            Map<String, ?> inputs = getInputValues(c.getInputMap(), connectionObjects);
            if (!tHandler.isSatisfied(inputs)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param connectionObjects
     * @return
     */
    private static Map<String, ?> getInputValues(Map<Input, List<Input>> inputMap,
            Map<String, OutputValue> connectionObjects) {
        Map<String, Object> inputs = new HashMap<String, Object>(11);
        for (Iterator<Map.Entry<String, OutputValue>> it = connectionObjects.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, OutputValue> e = it.next();
            inputs.put(e.getKey(), e.getValue().getValue());
        }
        return inputs;
    }

    /**
     * @param c condition implementation object
     * @return
     */
    private static Map<String, OutputValue> initConnections(ConnectedModule c, Rule r) {
        Set<Connection> connections = c.getConnections();
        Map<String, OutputValue> connectedObjects = new HashMap<String, OutputValue>(11);
        if (connections != null) {
            for (Iterator<Connection> it = connections.iterator(); it.hasNext();) {
                Connection conn = it.next();
                String uid = conn.getOuputModuleId();
                Module m = ((RuleImpl) r).getModule0(uid);
                if (m instanceof SourceModule) {
                    OutputValue outputValue = new OutputValue(conn.getOutputName(), (SourceModule) m);
                    connectedObjects.put(conn.getInputName(), outputValue);
                } else {
                    log.warn("Condition " + c + "can not be connected to module: " + uid
                            + ". The module is not available or not a data source!");
                }
            }
        }
        c.setConnectedObjects(connectedObjects);
        return connectedObjects;
    }

    private static void executeActions(Rule r) {
        List<Action> actions = ((RuleImpl) r).actions;
        if (actions == null || actions.size() == 0) {
            return;
        }
        for (Iterator<Action> it = actions.iterator(); it.hasNext();) {
            ActionImpl a = (ActionImpl) it.next();
            Map<String, OutputValue> connectionObjects = a.getConnectedObjects();
            if (connectionObjects == null) {
                connectionObjects = initConnections(a, r);
            }
            ActionHandler aHandler = a.getModuleHandler();
            Map<String, ?> inputs = getInputValues(a.getInputMap(), connectionObjects);
            Map outputs = aHandler.execute(inputs);
            if (outputs != null) {
                a.setOutputs(outputs);
            }

        }

    }

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
    protected static RuleStatus getRuleStatus(String rUID) {
        RuleStatus status = statusMap.get(rUID);
        return status;
    }

}
