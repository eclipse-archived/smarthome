/**
 * Copyright (c) 2017 Kai Kreuzer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.handler;

import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.core.scheduler2.Scheduler;
import org.eclipse.smarthome.core.scheduler2.Scheduler.RunnableWithException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * at a specific time (format 'hh:mm').
 *
 * @author Kai Kreuzer - Initial Contribution
 *
 */
public class TimeOfDayTriggerHandler extends BaseTriggerModuleHandler implements RunnableWithException {

    private final Logger logger = LoggerFactory.getLogger(TimeOfDayTriggerHandler.class);

    public static final String MODULE_TYPE_ID = "timer.TimeOfDayTrigger";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    private static final String CFG_TIME = "time";

    private final Scheduler scheduler;
    private final String expression;
    private Closeable schedule;

    public TimeOfDayTriggerHandler(Trigger module, Scheduler scheduler) {
        super(module);
        this.scheduler = scheduler;
        String time = module.getConfiguration().get(CFG_TIME).toString();
        try {
            String[] parts = time.split(":");
            expression = MessageFormat.format("* {1} {0} * * *", Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new IllegalArgumentException("'time' parameter is not in valid format 'hh:mm'.", e);
        }
    }

    @Override
    public synchronized void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        super.setRuleEngineCallback(ruleCallback);
        scheduleJob();
    }

    private void scheduleJob() {
        schedule = scheduler.schedule(this, expression);
        logger.debug("Scheduled job for trigger '{}' at '{}' each day.", module.getId(),
                module.getConfiguration().get(CFG_TIME));
    }

    @Override
    public void run() {
        ruleEngineCallback.triggered(module, null);
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if (schedule != null) {
            try {
                schedule.close();
                logger.debug("cancelled job for trigger '{}'.", module.getId());
            } catch (IOException e) {
                logger.debug("Failed cancelling job for trigger '{}' - maybe it was never scheduled?", module.getId());
            }
        }
    }
}
