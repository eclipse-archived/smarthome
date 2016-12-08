/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.handler;

import java.util.UUID;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * based on a cron expression. The cron expression can be set with the
 * configuration.
 *
 * @author Christoph Knauf - Initial Contribution
 *
 */
public class TimerTriggerHandler extends BaseTriggerModuleHandler {

    private final Logger logger = LoggerFactory.getLogger(TimerTriggerHandler.class);

    private JobDetail job;
    private CronTrigger trigger;
    private Scheduler scheduler;

    public static final String MODULE_TYPE_ID = "TimerTrigger";
    public static final String CALLBACK_CONTEXT_NAME = "CALLBACK";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    private static final String CFG_CRON_EXPRESSION = "cronExpression";

    public TimerTriggerHandler(Trigger module) {
        super(module);
        String cronExpression = (String) module.getConfiguration().get(CFG_CRON_EXPRESSION);
        this.trigger = TriggerBuilder.newTrigger().withIdentity(MODULE_TYPE_ID + UUID.randomUUID().toString())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
    }

    @Override
    public synchronized void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        super.setRuleEngineCallback(ruleCallback);
        initalizeTimer();
    }

    private void initalizeTimer() {
        this.job = JobBuilder.newJob(CallbackJob.class).withIdentity(MODULE_TYPE_ID + UUID.randomUUID().toString())
                .build();
        this.job.getJobDataMap().put(CALLBACK_CONTEXT_NAME, this.ruleEngineCallback);
        this.job.getJobDataMap().put(MODULE_CONTEXT_NAME, this.module);
        try {
            this.scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            logger.error("Error while scheduling Job: {}", e.getMessage());
        }
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        try {
            if (scheduler != null) {
                scheduler.clear();
            }
            scheduler = null;
            trigger = null;
            job = null;
        } catch (SchedulerException e) {
            logger.error("Error while disposing Job: {}", e.getMessage());
        }
    }
}
