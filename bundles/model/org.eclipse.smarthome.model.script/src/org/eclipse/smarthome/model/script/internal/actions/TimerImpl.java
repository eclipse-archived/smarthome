/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.internal.actions;

import static org.quartz.TriggerBuilder.newTrigger;

import org.eclipse.smarthome.model.script.actions.Timer;
import org.joda.time.DateTime;
import org.joda.time.base.AbstractInstant;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the {@link Timer} interface using the Quartz
 * library for scheduling.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class TimerImpl implements Timer {

    private final Logger logger = LoggerFactory.getLogger(TimerImpl.class);

    // the scheduler used for timer events
    public static Scheduler scheduler;

    static {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            LoggerFactory.getLogger(TimerImpl.class).error("initializing scheduler throws exception", e);
        }
    }

    private JobKey jobKey;
    private TriggerKey triggerKey;
    private AbstractInstant startTime;

    private boolean cancelled = false;
    private boolean terminated = false;

    public TimerImpl(JobKey jobKey, TriggerKey triggerKey, AbstractInstant startTime) {
        this.jobKey = jobKey;
        this.triggerKey = triggerKey;
        this.startTime = startTime;
    }

    @Override
    public boolean cancel() {
        try {
            boolean result = scheduler.deleteJob(jobKey);
            if (result) {
                cancelled = true;
            }
        } catch (SchedulerException e) {
            logger.warn("An error occurred while cancelling the job '{}': {}", jobKey.toString(), e.getMessage());
        }
        return cancelled;
    }

    @Override
    public boolean reschedule(AbstractInstant newTime) {
        try {
            Trigger trigger = newTrigger().startAt(newTime.toDate()).build();
            scheduler.rescheduleJob(triggerKey, trigger);
            this.triggerKey = trigger.getKey();
            this.cancelled = false;
            this.terminated = false;
            return true;
        } catch (SchedulerException e) {
            logger.warn("An error occurred while rescheduling the job '{}': {}", jobKey.toString(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isRunning() {
        try {
            for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
                if (context.getJobDetail().getKey().equals(jobKey)) {
                    return true;
                }
            }
            return false;
        } catch (SchedulerException e) {
            // fallback implementation
            logger.debug("An error occurred getting currently running jobs: {}", e.getMessage());
            return DateTime.now().isAfter(startTime) && !terminated;
        }
    }

    @Override
    public boolean hasTerminated() {
        return terminated;
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }
}
