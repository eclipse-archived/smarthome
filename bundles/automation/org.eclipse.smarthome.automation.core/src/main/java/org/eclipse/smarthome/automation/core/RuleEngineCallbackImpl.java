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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;

/**
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public class RuleEngineCallbackImpl implements RuleEngineCallback {

    private RuleImpl r;

    private ExecutorService executor;

    private Future<?> feature;

    private static final Logger log = LoggerFactory.getLogger(RuleEngineCallback.class);

    protected RuleEngineCallbackImpl(RuleImpl r) {
        this.r = r;
        executor = Executors.newSingleThreadExecutor();
    }

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

        public void run() {
            RuleEngine.runRule(r, this);
        }
    }

    public void dispose() {
        executor.shutdownNow();
        executor = null;
        r = null;
    }

}
