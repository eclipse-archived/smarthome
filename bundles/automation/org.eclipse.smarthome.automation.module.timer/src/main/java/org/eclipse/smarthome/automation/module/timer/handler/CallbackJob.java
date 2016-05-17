/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * This is an Job implementation which encapsulates a {@code RuleEngineCallback} in order to trigger a {@code Trigger}.
 * {@see TimerTriggerHandler}
 *
 * @author Christoph Knauf - Initial Contribution
 *
 */
public class CallbackJob implements Job {

    private final Logger logger = LoggerFactory.getLogger(TimerTriggerHandler.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();// each Job has its own JobDetail
        if (dataMap == null) {
            logger.error("Can't execute CallbackJob. JobDataMap is null");
        } else {
            RuleEngineCallback callback = (RuleEngineCallback) dataMap.get(TimerTriggerHandler.CALLBACK_CONTEXT_NAME);
            Trigger module = (Trigger) dataMap.get(TimerTriggerHandler.MODULE_CONTEXT_NAME);

            if (callback == null || module == null) {
                logger.error("Can't execute CallbackJob. Callback or module is null");
            } else {
                Map<String, Object> values = Maps.newHashMap();
                callback.triggered(module, values);
            }
        }
    }

}
