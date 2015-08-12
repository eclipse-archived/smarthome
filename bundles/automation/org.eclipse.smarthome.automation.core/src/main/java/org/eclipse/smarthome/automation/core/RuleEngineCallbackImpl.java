/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is implementation of {@link RuleEngineCallback} used by the {@link Trigger}s to notify rule engine about
 * appearing of new triggered data. There is one and only one {@link RuleEngineCallback} per Rule and it is used by all
 * rule's {@link Trigger}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class RuleEngineCallbackImpl implements RuleEngineCallback {

    private RuleImpl r;

    private ExecutorService executor;

    private Future<?> feature;

    private final Logger log = LoggerFactory.getLogger(RuleEngineCallback.class);

    private RuleEngine re;

    protected RuleEngineCallbackImpl(RuleEngine re, RuleImpl r) {
        this.re = re;
        this.r = r;
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void triggered(Trigger trigger, Map<String, ?> outputs) {
        if (trigger instanceof SourceModule) {
            feature = executor.submit(new TriggerData(trigger, outputs));
        } else {
            log.error("The trigger " + trigger.getId() + " is not data source!");
        }

    }

    public Rule getRule() {
        return r;
    }

    public boolean isRunning() {
        return feature == null || !feature.isDone();
    }

    class TriggerData implements Runnable {

        private Trigger trigger;

        public Trigger getTrigger() {
            return trigger;
        }

        public Map<String, ?> getOutputs() {
            return outputs;
        }

        private Map<String, ?> outputs;

        public TriggerData(Trigger t, Map<String, ?> outputs) {
            this.trigger = t;
            this.outputs = outputs;
        }

        @Override
        public void run() {
            re.runRule(r, this);
        }
    }

    public void dispose() {
        executor.shutdownNow();
        executor = null;
        r = null;
    }

}
