/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.automation.core.RuleEngineCallbackImpl.TriggerData;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.Input;

/**
 * @author Yordan Mihaylov
 *
 */
public class RuleEngine implements ServiceTrackerCustomizer/* <ModuleHandlerFactory, ModuleHandlerFactory> */{

    public static final int MODULE_TYPE_SEPARATOR = ':';

    private static Map<String, RuleImpl> rules;
    private static Map<String, Thread> runningRules;
    private ServiceTracker/* <ModuleHandlerFactory, ModuleHandlerFactory> */msfTracker;
    private BundleContext bc;
    private Map<String, ModuleHandlerFactory> moduleHandlerFactories;
    private Logger log;

    public RuleEngine(BundleContext bc) {
        this.bc = bc;
        this.log = LoggerFactory.getLogger(getClass());
        if (rules == null) {
            rules = new HashMap<String, RuleImpl>(20);
        }
        if (runningRules == null) {
            runningRules = new HashMap<String, Thread>(20);
        }
        moduleHandlerFactories = new HashMap<String, ModuleHandlerFactory>(20);
        msfTracker = new ServiceTracker/* <ModuleHandlerFactory, ModuleHandlerFactory> */(bc, ModuleHandlerFactory.class.getName(),
                this);
        msfTracker.open();

    }

    protected void setRule(RuleImpl r) {
        if (register(r.conditions, true)) {
            if (register(r.actions, false)) {
                if (register(r.triggers, false)) {
                    startRule(r);
                    rules.put(r.getUID(), r);
                } else {
                    unregister(r.triggers);
                    unregister(r.actions);
                    unregister(r.conditions);
                }
            } else {
                unregister(r.actions);
                unregister(r.conditions);
            }
        } else {
            unregister(r.conditions);
        }
    }

    private <T extends Module> boolean register(List<T> modules, boolean all) {
        boolean result = false;
        if (modules != null) {
            for (Iterator<T> it = modules.iterator(); it.hasNext();) {
                T t = it.next();
                try {
                    ModuleHandler moduleHandler = getModuleHandler(t);
                    ((ModuleImpl) t).setModuleHandler(moduleHandler);
                    result = true;
                } catch (Throwable e) {
                    log.warn(
                            "Missing handler: " + t.getTypeUID() + ", for the module: " + t.getId() + ". " + e.getMessage(),
                            e);
                    if (all) {
                        return false;
                    }
                }
            }
        }
        return result;
    }

    private <T extends Module> void unregister(List<T> modules) {
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

    private void startRule(RuleImpl r) {
        RuleEngineCallback reCallback = RuleEngineCallbackImpl.getInstance(r, this);
        for (Iterator<Trigger> it = r.triggers.iterator(); it.hasNext();) {
            TriggerImpl t = (TriggerImpl) it.next();
            TriggerHandler triggerHandler = t.getModuleHandler();
            triggerHandler.setRuleEngineCallback(reCallback);
        }
    }

    /**
     * @param id
     */
    private ModuleHandler getModuleHandler(Module m) {
        String mtId = m.getTypeUID();
        if (mtId == null) {
            throw new IllegalArgumentException("Invalid module type id. It must not be null!");
        }
        int idx = mtId.indexOf(MODULE_TYPE_SEPARATOR);
        if (idx != -1) {
            mtId = mtId.substring(0, idx);
        }
        ModuleHandlerFactory msf = moduleHandlerFactories.get(mtId);
        if (msf == null) {
            throw new IllegalArgumentException("Invalid module handler factpry: " + mtId);
        }
        return msf.create(m);
    }

    protected RuleImpl removeRule(String id) {
        return rules.remove(id);
    }

    protected RuleImpl getRule(String rUID) {
        return rules.get(rUID);
    }

    protected void setRuleEnable(String rUID, boolean isEnabled) {
        RuleImpl r = rules.get(rUID);
        if (r != null) {
            r.setEnabled(isEnabled);
        }
    }

    protected boolean isRunning(String rUID) {
        return runningRules.get(rUID) != null;
    }

    public Collection<Rule> getRulesByTag(String tag) {
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

    public Collection<Rule> getRules(String filter) {
        Collection<Rule> result = new ArrayList<Rule>(10);
        for (Iterator<RuleImpl> it = rules.values().iterator(); it.hasNext();) {
            RuleImpl r = it.next();
            result.add(new RuleImpl(r));
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
    public ModuleHandlerFactory addingService(ServiceReference/* <ModuleHandlerFactory> */reference) {
        ModuleHandlerFactory msf = (ModuleHandlerFactory) bc.getService(reference);
        Collection<String> moduleTypes = msf.getTypes();
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            if (moduleHandlerFactories.get(moduleTypeName) != null) {
                // log.error
            } else {
                moduleHandlerFactories.put(moduleTypeName, msf);
            }

        }
        return msf;
    }

    /**
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    public void modifiedService(ServiceReference/* <ModuleHandlerFactory> */reference, /* ModuleHandlerFactory */Object service) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    public void removedService(ServiceReference/* <ModuleHandlerFactory> */reference, /* ModuleHandlerFactory */Object service) {
        Collection<String> moduleTypes = ((ModuleHandlerFactory) service).getTypes();
        for (Iterator<String> it = moduleTypes.iterator(); it.hasNext();) {
            String moduleTypeName = it.next();
            moduleHandlerFactories.remove(moduleTypeName);
        }
    }

    public void runRule(RuleEngineCallbackImpl reCallback) {
        synchronized (reCallback) {
            Rule r = reCallback.getRule();
            Thread t = runningRules.get(r.getUID());

            if (t == null) {
                RuleThread rt = new RuleThread(reCallback); // TODO use ThreadPool
                t = new Thread(rt);
                runningRules.put(r.getUID(), t);
                t.start();
            }
        }
    }

    public void dispose() {
        if (msfTracker != null) {
            msfTracker.close();
            msfTracker = null;
        }
    }

    class RuleThread implements Runnable {

        private RuleEngineCallbackImpl reCallback;

        public RuleThread(RuleEngineCallbackImpl reCallback) {
            this.reCallback = reCallback;
        }

        public void run() {
            TriggerData td = null;
            synchronized (reCallback) {
                td = reCallback.getTriggeredData();
            }
            while (td != null) {
                RuleImpl r = (RuleImpl) reCallback.getRule();
                if (!r.isEnabled()) {
                    log.debug("The rule: " + r.getUID() + " is not enabled.");
                    td = popTriggerData(r);
                    continue;
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
                } finally {
                    td = popTriggerData(r);
                }
            }
        }

        private TriggerData popTriggerData(RuleImpl r) {
            TriggerData td = null;
            synchronized (reCallback) {
                td = reCallback.getTriggeredData();
                if (td == null) {
                    runningRules.remove(r.getUID());
                }
            }
            return td;
        }

        private boolean calculateConditions(Rule r) {
            List<Condition> conditions = ((RuleImpl) r).conditions;
            if (conditions == null || conditions.size() == 0) {
                return true;
            }
            for (Iterator<Condition> it = conditions.iterator(); it.hasNext();) {
                ConditionImpl c = (ConditionImpl) it.next();
                Map<String, OutputValue> connectionObjects = c.getConnectedObjects();
                if (connectionObjects == null) {
                    connectionObjects = initConnectios(c, r);
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
        private Map<String, ?> getInputValues(Map<Input, List<Input>> inputMap,
                Map<String, OutputValue> connectionObjects) {
            Map<String, Object> inputs = new HashMap<String, Object>(11);
            for (Iterator<Map.Entry<String, OutputValue>> it = connectionObjects.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, OutputValue> e = it.next();
                inputs.put(e.getKey(), e.getValue().getValue());
            }
            Map<String, ?> resolvedInputs = ModuleUtil.resolveInputs(inputMap, inputs);
            return resolvedInputs;
        }

        /**
         * @param c condition implementation object
         * @return
         */
        private Map<String, OutputValue> initConnectios(ConnectedModule c, Rule r) {
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
                                + ". The module is not available or it is not data source!");
                    }
                }
            }
            c.setConnectedObjects(connectedObjects);
            c.setInputMap(ModuleUtil.resolveInputReferences(c.getTypeUID()));
            return connectedObjects;
        }

        private void setTriggerOutputs(TriggerData td) {
            Trigger t = td.getTrigger();
            if (!(t instanceof SourceModule)) {
                throw new IllegalArgumentException("Invalid Trigger implementation: " + t);
            }

            SourceModule ds = (SourceModule) t;
            Map<String, ?> resolvedOutputs = ModuleUtil.resolveOutputs(t.getTypeUID(), td.getOutputs());

            ds.setOuputs(resolvedOutputs);
        }

        private void executeActions(Rule r) {
            // List<Action> actions = r.getModules(Action.class);
            List<Action> actions = ((RuleImpl) r).actions;
            if (actions == null || actions.size() == 0) {
                return;
            }
            for (Iterator<Action> it = actions.iterator(); it.hasNext();) {
                ActionImpl a = (ActionImpl) it.next();
                Map<String, OutputValue> connectionObjects = a.getConnectedObjects();
                if (connectionObjects == null) {
                    connectionObjects = initConnectios(a, r);
                }
                ActionHandler aHandler = a.getModuleHandler();
                Map<String, ?> inputs = getInputValues(a.getInputMap(), connectionObjects);
                Map outputs = aHandler.execute(inputs);
                if (outputs != null) {
                    a.setOuputs(outputs);
                }

            }

        }

    } // End RuleThread

}
