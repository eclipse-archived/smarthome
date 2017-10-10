/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.handler;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.core.scheduler2.Scheduler;
import org.eclipse.smarthome.core.scheduler2.Scheduler.RunnableWithException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * based on a cron expression. The cron expression can be set with the
 * configuration.
 *
 * @author Christoph Knauf - Initial Contribution
 * @author Yordan Mihaylov - Remove Quarz lib dependency
 *
 */
public class GenericCronTriggerHandler extends BaseTriggerModuleHandler implements RunnableWithException {

    private final Logger logger = LoggerFactory.getLogger(GenericCronTriggerHandler.class);

    public static final String MODULE_TYPE_ID = "timer.GenericCronTrigger";
    public static final String CALLBACK_CONTEXT_NAME = "CALLBACK";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    private static final String CFG_CRON_EXPRESSION = "cronExpression";
    private final Scheduler scheduler;
    private final String expression;
    private Closeable schedule;

    public GenericCronTriggerHandler(Trigger module, Scheduler scheduler) {
        super(module);
        this.scheduler = scheduler;
        this.expression = (String) module.getConfiguration().get(CFG_CRON_EXPRESSION);
    }

    @Override
    public synchronized void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        super.setRuleEngineCallback(ruleCallback);
        scheduleJob();
    }

    private void scheduleJob() {
        schedule = scheduler.schedule(this, expression);
        logger.debug("Scheduled cron job '{}' for trigger '{}'.", module.getConfiguration().get(CFG_CRON_EXPRESSION),
                module.getId());
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

    @Override
    public void run() {
        ruleEngineCallback.triggered(module, null);
    }
}
