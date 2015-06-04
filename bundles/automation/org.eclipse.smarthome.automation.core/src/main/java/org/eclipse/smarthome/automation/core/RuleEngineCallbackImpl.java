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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;

/**
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public class RuleEngineCallbackImpl implements RuleEngineCallback {

    private static Map<String, RuleEngineCallback> reCallbacks = new HashMap<String, RuleEngineCallback>();

    LinkedList<TriggerData> queue;

    private RuleEngine re;

    private Rule r;

    private RuleEngineCallbackImpl(Rule r, RuleEngine re) {
        this.r = r;
        this.re = re;
        queue = new LinkedList<TriggerData>();
    }

    public static RuleEngineCallback getInstance(Rule r, RuleEngine re) {
        RuleEngineCallback result = reCallbacks.get(r.getUID());
        if (result == null) {
            result = new RuleEngineCallbackImpl(r, re);
            reCallbacks.put(r.getUID(), result);
        }
        return result;
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.RuleEngineCallback#triggered(org.eclipse.smarthome.automation.Trigger,
     *      java.util.Map)
     */
    @Override
    public void triggered(Trigger trigger, Map<String, ?> outputs) {
        if (trigger instanceof SourceModule) {
            queue.add(new TriggerData(trigger, outputs));
            re.runRule(this);
        } else {
            // log error
        }

    }

    public Rule getRule() {
        return r;
    }

    public TriggerData getTriggeredData() {
        return queue.size() > 0 ? queue.removeFirst() : null;
    }

    class TriggerData {

        private Trigger trigger;

        public Trigger getTrigger() {
            return trigger;
        }

        public Map getOutputs() {
            return outputs;
        }

        private Map outputs;

        public TriggerData(Trigger t, Map outputs) {
            this.trigger = t;
            this.outputs = outputs;
        }
    }

}
